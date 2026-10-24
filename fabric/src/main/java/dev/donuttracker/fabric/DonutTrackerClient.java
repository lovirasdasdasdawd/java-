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
    private final PaymentBalanceSync paymentBalanceSync = new PaymentBalanceSync();

    @Override public void onInitializeClient() {
        try { settings = Settings.load(config); } catch (IOException ex) { settings = new Settings(); settingsWritable = false; LOG.error("Settings could not be read; original file preserved", ex); status = "Invalid settings file: " + config; }
        try { StateStore.load(state, tracker, queue); } catch (IOException ex) { stateWritable = false; LOG.error("History could not be read; original file preserved", ex); status = "Invalid history file: " + state; }
        KeyMapping menu = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.donuttracker.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));
        KeyMapping payments = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.donuttracker.payments", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menu.consumeClick()) if (client.player != null) client.setScreen(new TrackerScreen(this, client.screen));
            while (payments.consumeClick()) if (client.player != null) client.setScreen(new PaymentActionScreen(this, client.screen));
            long now = System.currentTimeMillis();
            if ((pendingStart || pendingStop) && now - pendingAt > 10_000) {
                pendingStart = pendingStop = false; status = "No matching /bal response. Check Detection or enter a manual balance.";
            }
            boolean connected = client.getConnection() != null;
            if (paymentBalanceSync.shouldRequest(connected, tracker.running(), pendingStart || pendingStop, now, lastRequest)
                    || (settings.pollBalance && tracker.running() && now - lastRequest >= 15_000 && connected)) requestBalance();
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            // Server-generated chat only. Player chat cannot inject queue entries.
            if (overlay) return;
            try {
                ChatParser.Result result = new ChatParser(settings).accept(message.getString(), tracker, queue);
                paymentBalanceSync.received(result, tracker.running());
                if (result == ChatParser.Result.BALANCE) {
                    status = "Balance: " + Money.format(tracker.balance());
                    if (pendingStart) { pendingStart = false; tracker.start(); status = "Session started with a fresh balance."; }
                    if (pendingStop) { pendingStop = false; tracker.stop(); status = "Session ended: " + Money.signed(tracker.profit()); save(); }
                } else if (result == ChatParser.Result.PAYMENT) { status = tracker.running() ? "Payment queued; updating session balance…" : "Payment queued. Start Tracker to track session profit."; save(); }
                else if (result == ChatParser.Result.BELOW_MINIMUM && tracker.running()) status = "Payment below queue minimum; updating session balance…";
            } catch (RuntimeException ex) { status = "Message not recorded: " + ex.getMessage(); LOG.warn(status); }
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("donuttracker", "hud"), (g, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && !client.options.hideGui && !(client.screen instanceof PaymentActionScreen)) TrackerHud.render(g, this, false);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (tracker.running()) { tracker.stop(); status = "Disconnected: session ended at last observed balance."; }
            save(); var history = tracker.history(); tracker = new Tracker(); tracker.restoreHistory(history);
            pendingStart = pendingStop = false;
            paymentBalanceSync.reset();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
        LOG.info("Donut Tracker initialized; press Right Shift for settings.");
    }
    void requestBalance() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) throw new IllegalStateException("Join a server first.");
        if (System.currentTimeMillis() - lastRequest < 2500) throw new IllegalStateException("Wait a moment before requesting /bal again.");
        connection.sendCommand("bal"); lastRequest = System.currentTimeMillis(); paymentBalanceSync.requested(); status = "Waiting for /bal response…";
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
            paymentBalanceSync.received(ChatParser.Result.PAYMENT, tracker.running());
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
