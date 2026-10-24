package dev.donuttracker.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.donuttracker.core.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;

public final class DonutTrackerClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("donuttracker");
    final Path config = FabricLoader.getInstance().getConfigDir().resolve("donut-tracker.properties");
    final Path state = FabricLoader.getInstance().getConfigDir().resolve("donut-tracker-sessions.properties");
    Settings settings;
    Tracker tracker = new Tracker();
    final PaymentQueue queue = new PaymentQueue();
    String status = "Run /bal to read your balance before starting.";
    private long lastRequest;
    private boolean pendingStart, pendingStop;
    private long pendingAt;
    private boolean settingsWritable = true, stateWritable = true;
    private final Map<String, PaymentQueue.Payment> chatReceipts = new HashMap<>();

    @Override public void onInitializeClient() {
        try { settings = Settings.load(config); } catch (IOException ex) { settings = new Settings(); settingsWritable = false; LOG.error("Settings could not be read; original file preserved", ex); status = "Invalid settings file: " + config; }
        try { StateStore.load(state, tracker, queue); } catch (IOException ex) { stateWritable = false; LOG.error("History could not be read; original file preserved", ex); status = "Invalid history file: " + state; }
        KeyMapping menu = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.donuttracker.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menu.consumeClick()) if (client.player != null) client.setScreen(new TrackerScreen(this, client.screen));
            long now = System.currentTimeMillis();
            if ((pendingStart || pendingStop) && now - pendingAt > 10_000) {
                pendingStart = pendingStop = false; status = "No matching /bal response. Check Detection or enter a manual balance.";
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var multiplier = ClientCommandManager.argument("multiplier", IntegerArgumentType.integer(2,3))
                .executes(context -> returnFromChat(StringArgumentType.getString(context,"receipt"), IntegerArgumentType.getInteger(context,"multiplier")));
            var receipt = ClientCommandManager.argument("receipt", StringArgumentType.word()).then(multiplier);
            dispatcher.register(ClientCommandManager.literal("donuttracker_return").then(receipt));
        });
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::receiveServerMessage);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("donuttracker", "hud"), (g, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && !client.options.hideGui) TrackerHud.render(g, this, false);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (tracker.running()) { tracker.stop(); status = "Disconnected: session ended at last observed balance."; }
            save(); var history = tracker.history(); tracker = new Tracker(); tracker.restoreHistory(history);
            pendingStart = pendingStop = false;
            chatReceipts.clear();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
        LOG.info("Donut Tracker initialized; press Right Shift for settings.");
    }
    private Component receiveServerMessage(Component message, boolean overlay) {
        if (overlay) return message;
        try {
            ChatParser.Result result = new ChatParser(settings).accept(message.getString(), tracker, queue);
            if (result == ChatParser.Result.BALANCE) {
                status = "Balance: " + Money.format(tracker.balance());
                if (pendingStart) { pendingStart = false; tracker.start(); status = "Session started with a fresh balance."; }
                if (pendingStop) { pendingStop = false; tracker.stop(); status = "Session ended: " + Money.signed(tracker.profit()); save(); }
            } else if (result == ChatParser.Result.PAYMENT) {
                status = "Payment queued. Balance changes only when you refresh it manually.";
                save();
                chatReceipts.entrySet().removeIf(entry -> queue.all().stream().noneMatch(payment -> payment == entry.getValue()));
                String token = UUID.randomUUID().toString();
                PaymentQueue.Payment receipt = queue.all().getLast();
                chatReceipts.put(token,receipt);
                return message.copy().append(chatReturnButton(token,receipt,2)).append(chatReturnButton(token,receipt,3));
            } else if (result == ChatParser.Result.BELOW_MINIMUM && tracker.running()) status = "Payment below queue minimum. No automatic balance command sent.";
        } catch (RuntimeException ex) { status = "Message not recorded: " + ex.getMessage(); LOG.warn(status); }
        return message;
    }
    private Component chatReturnButton(String token, PaymentQueue.Payment receipt, int multiplier) {
        return Component.literal(" [" + multiplier + "×]").setStyle(Style.EMPTY
            .withColor(multiplier == 2 ? 0x9BCEDB : 0xB8A5F4).withBold(true)
            .withClickEvent(new ClickEvent.RunCommand("/donuttracker_return " + token + " " + multiplier))
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Verstuur /" + ReturnPayments.command(receipt,multiplier)))));
    }
    private int returnFromChat(String token, int multiplier) {
        var client = Minecraft.getInstance();
        try {
            PaymentQueue.Payment receipt = chatReceipts.get(token);
            if (receipt == null) throw new IllegalStateException("Deze betaling is niet meer beschikbaar. Gebruik een nieuw betaalbericht uit deze sessie.");
            returnPayment(receipt,multiplier);
            return 1;
        } catch (RuntimeException ex) {
            status = ex.getMessage();
            client.gui.getChat().addMessage(Component.literal(status).withStyle(style -> style.withColor(0xF08091)));
            return 0;
        }
    }
    void requestBalance() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) throw new IllegalStateException("Join a server first.");
        if (System.currentTimeMillis() - lastRequest < 2500) throw new IllegalStateException("Wait a moment before requesting /bal again.");
        connection.sendCommand("bal"); lastRequest = System.currentTimeMillis(); status = "Waiting for /bal response…";
    }
    void startFresh() {
        if (tracker.running()) throw new IllegalStateException("End the current session first.");
        requestBalance(); pendingStart = true; pendingStop = false; pendingAt = System.currentTimeMillis();
    }
    void stopFresh() {
        if (!tracker.running()) throw new IllegalStateException("No active session.");
        requestBalance(); pendingStop = true; pendingStart = false; pendingAt = System.currentTimeMillis();
    }
    void cancelPending() { pendingStart = pendingStop = false; }
    void returnPayment(PaymentQueue.Payment expected, int multiplier) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || Minecraft.getInstance().player == null) throw new IllegalStateException("Join a server first.");
        if (!stateWritable) throw new IllegalStateException("Fix the history file before sending return payments.");
        try {
            String command = ReturnPayments.send(queue, expected, multiplier, () -> StateStore.save(state, tracker, queue), connection::sendCommand);
            status = "Sent /" + command + ". Check the server response in chat.";
        } catch (IOException ex) { throw new java.io.UncheckedIOException("Could not save the return request; no command sent.", ex); }
    }
    void save() {
        try {
            if (settingsWritable) settings.save(config);
            if (stateWritable) StateStore.save(state, tracker, queue);
            if (!settingsWritable || !stateWritable) status = "Fix invalid config/history file and restart to save changes.";
        }
        catch (IOException ex) { status = "Could not save settings: " + ex.getMessage(); LOG.error(status, ex); }
    }
    void act(Runnable action) {
        try { action.run(); save(); }
        catch (RuntimeException ex) { status = ex.getMessage(); }
    }
}
