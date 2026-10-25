package dev.auctionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class AuctionHud {
    private AuctionHud(){}
    public static void render(GuiGraphics g,AuctionModClient mod){
        var client=Minecraft.getInstance();var view=mod.auction.snapshot();var phase=view.phase();
        if(client.player==null||client.options.hideGui||phase==AuctionState.Phase.IDLE)return;
        int w=Math.min(286,g.guiWidth()-8),h=86,x=Math.clamp(mod.config.hudX,0,Math.max(0,g.guiWidth()-w)),y=Math.clamp(mod.config.hudY,0,Math.max(0,g.guiHeight()-h));
        if(mod.hudPlacement!=null)y=Math.clamp(mod.hudPlacement.y(g.guiWidth(),g.guiHeight(),x,y,w,h),0,Math.max(0,g.guiHeight()-h));
        AuctionUi.panel(g,x,y,w,h,mod.config.glassPanels);
        AuctionUi.text(g,"Auction Mod by claude",x+10,y+9,AuctionUi.CYAN);
        g.fill(x+10,y+23,x+w-10,y+24,0xFF344455);
        if(!mod.auctionItem.isEmpty()){g.renderItem(mod.auctionItem,x+9,y+29);AuctionUi.text(g,client.font.plainSubstrByWidth(mod.auctionItem.getHoverName().getString(),w-44),x+31,y+33,AuctionUi.INK);}
        var top=view.top();
        String bid=top==null?"Top bid: Waiting for a bid -":"Top bid: "+top.player()+" - $"+Amounts.format(top.amount());
        if(phase==AuctionState.Phase.ENDED)bid=top==null?"No bids":"Winner: "+top.player()+" - $"+Amounts.format(top.amount());
        if(phase==AuctionState.Phase.CANCELLED)bid="Auction cancelled";
        AuctionUi.text(g,client.font.plainSubstrByWidth(bid,w-20),x+10,y+53,AuctionUi.YELLOW);
        AuctionUi.text(g,"Time: "+Amounts.time(view.remainingSeconds()),x+10,y+69,AuctionUi.INK);
        String status=phase==AuctionState.Phase.RUNNING?"LIVE":phase==AuctionState.Phase.ENDED?"ENDED":"STOPPED";
        AuctionUi.text(g,status,x+w-10-client.font.width(status),y+69,AuctionUi.MUTED);
    }
}
