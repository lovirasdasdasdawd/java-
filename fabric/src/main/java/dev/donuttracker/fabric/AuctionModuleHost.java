package dev.donuttracker.fabric;

import dev.auctionmod.AuctionScreen;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

/** Auctions live inside the same branded module shell, not a second mod or independent popup. */
final class AuctionModuleHost implements AuctionScreen.Host {
    private final DonutTrackerClient mod;
    private int x,y,w,h;
    private static final int RAIL=100;
    AuctionModuleHost(DonutTrackerClient mod){this.mod=mod;}
    @Override public AuctionScreen.Area area(int width,int height){
        w=Math.min(600,width-24);h=Math.min(280,height-20);x=(width-w)/2;y=(height-h)/2;
        return new AuctionScreen.Area(x+RAIL+4,y+2,w-RAIL-8,h-4);
    }
    @Override public void navigation(Consumer<AbstractWidget> add,Screen current){
        nav(add,"Tracker",62,false,()->Minecraft.getInstance().setScreen(new ModuleSettingsScreen(mod,new TrackerScreen(mod,null),false)));
        nav(add,"Payments",86,false,()->Minecraft.getInstance().setScreen(new ModuleSettingsScreen(mod,new TrackerScreen(mod,null),true)));
        nav(add,"Auctions",110,true,()->{});
        nav(add,"Move HUDs",142,false,()->Minecraft.getInstance().setScreen(new HudEditorScreen(mod,current)));
        nav(add,"Atmosphere",166,false,()->Minecraft.getInstance().setScreen(new AtmosphereScreen(mod,current)));
        add.accept(new MenuWidgets.Action(x+8,y+h-28,RAIL-16,20,"Close",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->Minecraft.getInstance().setScreen(null)));
    }
    private void nav(Consumer<AbstractWidget> add,String label,int yy,boolean selected,Runnable action){
        add.accept(new MenuWidgets.Action(x+7,y+yy,RAIL-14,20,label,CoastalStyle::accent,MenuWidgets.Kind.NAV,()->selected,-1,action));
    }
    @Override public void render(GuiGraphics g,int width,int height){
        CoastalStyle.background(g,width,height);g.fill(0,0,width,height,0x30000000);
        CoastalStyle.panel(g,x,y,w,h);g.fill(x+1,y+1,x+RAIL,y+h-1,CoastalStyle.menuSurface());g.fill(x+RAIL,y+1,x+RAIL+1,y+h-1,CoastalStyle.line());
        TrackerHud.text(g,"DONUT",x+12,y+15,CoastalStyle.ink());TrackerHud.text(g,"TRACKER",x+12,y+27,CoastalStyle.accent());TrackerHud.scaled(g,"MODULES",x+12,y+52,.7f,CoastalStyle.muted());
        int yy=y+136,mid=x+RAIL/2;g.fill(x+10,yy,mid-6,yy+1,CoastalStyle.line());g.fill(mid+7,yy,x+RAIL-9,yy+1,CoastalStyle.line());g.fill(mid-2,yy-1,mid+3,yy+2,CoastalStyle.accent());
    }
}
