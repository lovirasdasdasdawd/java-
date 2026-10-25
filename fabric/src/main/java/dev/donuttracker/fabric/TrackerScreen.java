package dev.donuttracker.fabric;

import dev.donuttracker.core.Money;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

/** One small ledger, a module rail, and native pixel typography. */
final class TrackerScreen extends Screen {
    private final MenuMotion transition=new MenuMotion(true);
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int x,y,w,h,rail,cx,cw;
    TrackerScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Donut Tracker"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(490,width-24);h=Math.min(238,height-20);x=(width-w)/2;y=(height-h)/2;rail=90;cx=x+rail+14;cw=w-rail-28;
        nav("Tracker",y+62,()->minecraft.setScreen(new ModuleSettingsScreen(mod,this,false)));
        nav("Payments",y+86,()->minecraft.setScreen(new ModuleSettingsScreen(mod,this,true)));
        nav("Auctions",y+110,()->mod.auctions.openScreen(this));
        nav("Move HUDs",y+142,()->minecraft.setScreen(new HudEditorScreen(mod,this)));
        nav("Atmosphere",y+166,()->minecraft.setScreen(new AtmosphereScreen(mod,this)));
        addRenderableWidget(new MenuWidgets.Action(x+8,y+h-28,rail-16,20,"Close",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        addRenderableWidget(new MenuWidgets.Action(cx+cw-87,y+44,87,21,mod.tracker.running()?"End session":"Start session",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{mod.sessionCommand(!mod.tracker.running());rebuildWidgets();}));
        addRenderableWidget(new MenuWidgets.Action(cx+cw-168,y+h-19,96,16,"Background",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new AtmosphereScreen(mod,this))));
        addRenderableWidget(new MenuWidgets.Action(cx+cw-66,y+h-19,66,16,mod.settings.menuGlass?"Glass: On":"Glass: Off",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->{mod.settings.menuGlass=!mod.settings.menuGlass;CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();}));
    }
    private void nav(String name,int yy,Runnable run){addRenderableWidget(new MenuWidgets.Action(x+7,yy,rail-14,20,name,CoastalStyle::accent,MenuWidgets.Kind.NAV,run));}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);g.fill(0,0,width,height,0x30000000);
        transition.begin(g,x,y,w,h);
        CoastalStyle.panel(g,x,y,w,h);
        g.fill(x+1,y+1,x+rail,y+h-1,CoastalStyle.menuSurface());g.fill(x+rail,y+1,x+rail+1,y+h-1,CoastalStyle.line());
        TrackerHud.text(g,"DONUT",x+12,y+15,CoastalStyle.ink());TrackerHud.text(g,"TRACKER",x+12,y+27,CoastalStyle.accent());
        TrackerHud.scaled(g,"MODULES",x+12,y+52,.7f,CoastalStyle.muted());
        sidebarDivider(g);
        TrackerHud.text(g,"Session",cx,y+15,CoastalStyle.ink());
        String state=mod.tracker.running()?"RECORDING":"STANDBY";right(g,state,cx+cw,y+16,.7f,mod.tracker.running()?CoastalStyle.accent():CoastalStyle.muted());
        line(g,y+33);
        TrackerHud.scaled(g,"NET PROFIT",cx,y+48,.75f,CoastalStyle.muted());
        int colour=CoastalStyle.value(mod.tracker.profit().signum()<0,mod.tracker.profit().signum()<0?mod.settings.profit.negative:mod.settings.profit.positive);
        String amount=Money.signed(mod.tracker.profit());float scale=Math.min(2.4f,cw/(float)Math.max(1,font.width(amount)));
        TrackerHud.scaled(g,amount,cx,y+72,scale,colour);
        int third=cw/3;
        stat(g,"Elapsed",mod.tracker.duration(),cx,y+106,third);
        stat(g,"Per hour",Money.signed(mod.tracker.perHour()),cx+third,y+106,third);
        stat(g,"Balance",mod.tracker.hasBalance()?Money.format(mod.tracker.balance()):"Not read",cx+third*2,y+106,cw-third*2);
        line(g,y+140);
        TrackerHud.scaled(g,"PAYMENT QUEUE",cx,y+151,.75f,CoastalStyle.muted());right(g,mod.queue.waiting()+" waiting",cx+cw,y+151,.75f,CoastalStyle.muted());
        var a=mod.queue.active();
        if(a==null){TrackerHud.text(g,"Queue empty",cx,y+168,CoastalStyle.ink());TrackerHud.scaled(g,"The next payment will appear here.",cx,y+185,.7f,CoastalStyle.muted());}
        else {TrackerHud.text(g,a.player(),cx,y+168,CoastalStyle.ink());right(g,"+"+Money.format(a.amount()),cx+cw,y+168,1f,CoastalStyle.value(false,mod.settings.queue.positive));String next=mod.queue.waiting()>0?"Next: "+mod.queue.all().get(1).player():"No players waiting";TrackerHud.scaled(g,next,cx,y+185,.75f,CoastalStyle.muted());}
        line(g,y+h-23);TrackerHud.scaled(g,CoastalStyle.sceneName(),cx,y+h-14,.65f,CoastalStyle.muted());
        super.render(g,transition.pointerX(mx),transition.pointerY(my),delta);
        transition.end(g);
        if(transition.closed())minecraft.setScreen(parent);
    }
    private void sidebarDivider(GuiGraphics g){
        int yy=y+136,mid=x+rail/2;
        g.fill(x+10,yy,mid-6,yy+1,CoastalStyle.line());g.fill(mid+7,yy,x+rail-9,yy+1,CoastalStyle.line());
        g.fill(x+10,yy,x+19,yy+1,CoastalStyle.accent());
        g.fill(mid-3,yy-2,mid+4,yy+3,CoastalStyle.line());g.fill(mid-1,yy-1,mid+2,yy+2,CoastalStyle.accent());
    }
    private void stat(GuiGraphics g,String name,String value,int xx,int yy,int width){TrackerHud.scaled(g,name,xx,yy,.75f,CoastalStyle.muted());TrackerHud.scaled(g,value,xx,yy+14,Math.min(.9f,(width-8f)/Math.max(1,font.width(value))),CoastalStyle.ink());}
    private void line(GuiGraphics g,int yy){g.fill(cx,yy,cx+cw,yy+1,CoastalStyle.line());}
    private void right(GuiGraphics g,String s,int xx,int yy,float scale,int c){TrackerHud.scaled(g,s,xx-Math.round(font.width(s)*scale),yy,scale,c);}
    @Override public boolean keyPressed(KeyEvent event){if(event.key()==GLFW.GLFW_KEY_RIGHT_SHIFT){onClose();return true;}return super.keyPressed(event);}
    @Override public void onClose(){mod.save();transition.close();if(!MenuMotion.enabled())minecraft.setScreen(parent);}
}
