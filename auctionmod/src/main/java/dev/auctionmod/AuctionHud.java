package dev.auctionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class AuctionHud {
    public record Bounds(int x,int y,int width,int height,int baseWidth,float scale){}
    private AuctionHud(){}
    static Bounds bounds(int sw,int sh,AuctionModClient mod){
        float scale=(float)Math.clamp(mod.config.hudScale,.4,1.6);
        int base=Math.min(286,Math.max(100,(int)((sw-8)/scale))),w=(int)Math.ceil(base*scale),h=(int)Math.ceil(86*scale);
        int x=Math.clamp(mod.config.hudX,0,Math.max(0,sw-w)),y=Math.clamp(mod.config.hudY,0,Math.max(0,sh-h));
        if(!mod.config.hudPinned&&mod.hudPlacement!=null)y=Math.clamp(mod.hudPlacement.y(sw,sh,x,y,w,h),0,Math.max(0,sh-h));
        return new Bounds(x,y,w,h,base,scale);
    }
    public static void render(GuiGraphics g,AuctionModClient mod){if(!(Minecraft.getInstance().screen instanceof AuctionHudEditor))draw(g,mod,false);}
    static void draw(GuiGraphics g,AuctionModClient mod,boolean editing){
        var client=Minecraft.getInstance();var view=mod.auction.snapshot();var phase=view.phase();
        if(client.player==null||!editing&&(client.options.hideGui||!mod.config.hudEnabled||phase==AuctionState.Phase.IDLE))return;
        var b=bounds(g.guiWidth(),g.guiHeight(),mod);int w=b.baseWidth(),h=86;
        g.pose().pushMatrix();g.pose().translate((float)b.x(),(float)b.y());g.pose().scale(b.scale(),b.scale());
        AuctionUi.panel(g,0,0,w,h,mod.config.glassPanels);
        AuctionUi.text(g,"Auction Mod by claude",10,9,AuctionUi.CYAN);g.fill(10,23,w-10,24,0xFF344455);
        ItemStack item=mod.auctionItem;
        if(editing&&item.isEmpty()){var id=Identifier.tryParse(mod.config.selectedItem);if(id!=null)item=BuiltInRegistries.ITEM.getOptional(id).map(i->i.getDefaultInstance()).orElse(ItemStack.EMPTY);}
        if(!item.isEmpty()){g.renderItem(item,9,29);AuctionUi.text(g,client.font.plainSubstrByWidth(item.getHoverName().getString(),w-44),31,33,AuctionUi.INK);}
        else if(editing)AuctionUi.text(g,"Select an item in setup",10,33,AuctionUi.MUTED);
        var top=view.top();String bid=top==null?"Top bid: Waiting for a bid -":"Top bid: "+top.player()+" - $"+Amounts.format(top.amount());
        if(phase==AuctionState.Phase.ENDED)bid=top==null?"No bids":"Winner: "+top.player()+" - $"+Amounts.format(top.amount());
        if(phase==AuctionState.Phase.CANCELLED)bid="Auction cancelled";
        AuctionUi.text(g,client.font.plainSubstrByWidth(bid,w-20),10,53,AuctionUi.YELLOW);
        AuctionUi.text(g,"Time: "+Amounts.time(view.remainingSeconds()),10,69,AuctionUi.INK);
        String status=phase==AuctionState.Phase.IDLE?"PREVIEW":phase==AuctionState.Phase.RUNNING?"LIVE":phase==AuctionState.Phase.ENDED?"ENDED":"STOPPED";
        AuctionUi.text(g,status,w-10-client.font.width(status),69,AuctionUi.MUTED);g.pose().popMatrix();
    }
}
