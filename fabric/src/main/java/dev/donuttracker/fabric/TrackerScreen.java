package dev.donuttracker.fabric;

import dev.donuttracker.core.Money;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Two independent modules, with session controls available without opening settings. */
final class TrackerScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left, top, panelWidth, card;
    TrackerScreen(DonutTrackerClient mod, Screen parent) { super(Component.literal("Donut Tracker")); this.mod=mod;this.parent=parent; }
    @Override public boolean isPauseScreen() { return false; }
    @Override protected void init() {
        panelWidth=Math.min(584,width-24);left=(width-panelWidth)/2;top=Math.max(6,(height-216)/2);card=(panelWidth-12)/2;
        action("Tracker settings",left+12,top+145,card-24,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new ModuleSettingsScreen(mod,this,false)));
        action("Queue settings",left+card+24,top+145,card-24,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new ModuleSettingsScreen(mod,this,true)));
        action(mod.tracker.running()?"End session":"Start session",left,top+185,panelWidth/3-5,MenuWidgets.Kind.PRIMARY,()->{mod.sessionCommand(!mod.tracker.running());rebuildWidgets();});
        action("Arrange HUDs",left+panelWidth/3,top+185,panelWidth/3-5,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new HudEditorScreen(mod,this)));
        action("Back to game",left+panelWidth*2/3,top+185,panelWidth-panelWidth*2/3,MenuWidgets.Kind.SECONDARY,this::onClose);
    }
    private void action(String label,int x,int y,int w,MenuWidgets.Kind kind,Runnable run){addRenderableWidget(new MenuWidgets.Action(x,y,w,22,label,()->mod.settings.profit.accent,kind,run));}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xD0090C10);
        g.drawString(font,"Donut Tracker",left,top+3,0xFFF1F3F5,false);
        g.drawString(font,"Session controls & HUD settings",left,top+19,0xFF89929F,false);
        String state=mod.tracker.running()?"TRACKING":"READY";
        int sx=left+panelWidth-font.width(state);g.fill(sx-10,top+6,sx-6,top+10,mod.tracker.running()?0xFF8AE8C3:0xFF727C88);
        g.drawString(font,state,sx,top+4,0xFFB0BAC6,false);
        module(g,left,"TRACKER",mod.settings.profit.accent);
        module(g,left+card+12,"PAYMENT QUEUE",mod.settings.queue.accent);
        int profitColour=0xFF000000|(mod.tracker.profit().signum()<0?mod.settings.profit.negative:mod.settings.profit.positive);
        TrackerHud.scaled(g,Money.signed(mod.tracker.profit()),left+13,top+79,1.6f,profitColour);
        g.drawString(font,"Net profit",left+13,top+65,0xFF89929F,false);
        String time=mod.tracker.duration();g.drawString(font,time,left+13,top+113,0xFFD8DDE4,false);
        String rate=Money.signed(mod.tracker.perHour())+" / hr";
        g.drawString(font,font.plainSubstrByWidth(rate,card-94),left+92,top+113,0xFF89929F,false);
        int qx=left+card+25;var active=mod.queue.active();
        g.drawString(font,active==null?"Queue is clear":font.plainSubstrByWidth(active.player(),card-26),qx,top+69,0xFFE9EDF2,false);
        TrackerHud.scaled(g,active==null?"—":"+"+Money.format(active.amount()),qx,top+86,1.4f,active==null?0xFF58616C:0xFF000000|mod.settings.queue.positive);
        g.drawString(font,mod.queue.waiting()+" waiting",qx,top+113,0xFF89929F,false);
        super.render(g,mx,my,delta);
    }
    private void module(GuiGraphics g,int x,String title,int accent){
        TrackerHud.rounded(g,x,top+44,x+card,top+174,0xF2181D24);
        TrackerHud.border(g,x,top+44,x+card,top+174,0xFF303740);
        g.fill(x+13,top+44,x+35,top+46,0xFF000000|accent);
        g.drawString(font,title,x+13,top+52,0xFFAFB7C2,false);
    }
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
