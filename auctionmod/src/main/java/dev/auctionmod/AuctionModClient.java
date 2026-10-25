package dev.auctionmod;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuctionModClient implements ClientModInitializer {
    static final Logger LOG=LoggerFactory.getLogger("auctionmod");
    final AuctionState auction=new AuctionState();
    final Path configPath=FabricLoader.getInstance().getConfigDir().resolve("auctionmod.json");
    AuctionConfig config;
    public interface HudPlacement { int y(int screenWidth,int screenHeight,int x,int y,int width,int height); }
    HudPlacement hudPlacement;
    public void setHudPlacement(HudPlacement placement){hudPlacement=placement;}
    private PaymentParser parser;
    private java.util.function.Function<net.minecraft.client.gui.screens.Screen,net.minecraft.client.gui.screens.Screen> screenFactory;
    ItemStack auctionItem=ItemStack.EMPTY;
    boolean configWritable=true;
    String configError="";
    @Override public void onInitializeClient(){
        try{config=AuctionConfig.load(configPath);}catch(IOException ex){config=new AuctionConfig();configWritable=false;configError="Invalid config preserved. Fix config/auctionmod.json and restart.";LOG.error(configError,ex);}
        parser=new PaymentParser(config.paymentRegex);
        var category=KeyMapping.Category.register(Identifier.fromNamespaceAndPath("auctionmod","auction"));
        var key=KeyBindingHelper.registerKeyBinding(new KeyMapping("key.auctionmod.setup",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_O,category));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(key.consumeClick())if(client.player!=null){if(client.screen instanceof AuctionScreen screen)screen.onClose();else openScreen(client.screen);}
            finishLocally();
        });
        // Observe the original notice before another mod appends clickable return-payment buttons.
        // The later GAME observation is harmless: an equal/duplicate bid never replaces the top bid.
        ClientReceiveMessageEvents.ALLOW_GAME.register((message,overlay)->{receive(message);return true;});
        ClientReceiveMessageEvents.GAME.register((message,overlay)->receive(message));
        // Use the displayed/decorated text so ordinary signed player chat cannot impersonate a bare server payment line.
        ClientReceiveMessageEvents.CHAT.register((message,signed,sender,params,time)->receive(params.decorate(message)));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("auctionmod","auction_hud"),(g,delta)->AuctionHud.render(g,this));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{auction.reset();auctionItem=ItemStack.EMPTY;});
        LOG.info("Auction Mod ready; O opens setup. Mojang mappings / Minecraft 1.21.11.");
    }
    public void setScreenFactory(java.util.function.Function<net.minecraft.client.gui.screens.Screen,net.minecraft.client.gui.screens.Screen> factory){this.screenFactory=java.util.Objects.requireNonNull(factory);}
    public void openScreen(net.minecraft.client.gui.screens.Screen parent){Minecraft.getInstance().setScreen(screenFactory==null?new AuctionScreen(this,parent):screenFactory.apply(parent));}
    private void receive(Component message){
        var client=Minecraft.getInstance();
        if(!client.isSameThread()){client.execute(()->receive(message));return;}
        if(client.player==null)return;
        if(auction.phase()==AuctionState.Phase.RUNNING)parser.parse(message.getString()).ifPresent(auction::offer);
        finishLocally();
    }
    private void finishLocally(){
        if(auction.consumeEndNotification()&&config.notifyWinner&&Minecraft.getInstance().player!=null){
            var top=auction.top();String result=top==null?"No bids":top.player()+" - $"+Amounts.format(top.amount());
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Auction ended: "+result).withStyle(style->style.withColor(AuctionUi.YELLOW&0xFFFFFF)));
        }
    }
    void start(ItemStack item,String minimum,String time)throws IOException{
        if(item.isEmpty())throw new IllegalArgumentException("Choose an item image first.");
        if(auction.phase()==AuctionState.Phase.RUNNING)throw new IllegalStateException("Stop the current auction first.");
        var amount=Amounts.parse(minimum);if(amount.signum()<=0)throw new IllegalArgumentException("Minimum must be greater than zero.");int seconds=Amounts.duration(time);
        config.minimum=minimum.strip();config.durationSeconds=seconds;config.selectedItem=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
        saveConfig();auctionItem=item.copy();auction.start(amount,seconds);
    }
    void saveConfig()throws IOException{if(!configWritable)throw new IOException(configError);config.save(configPath);}
}
