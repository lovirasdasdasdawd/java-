package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Visual clock only. Root zoom animates the panel; module wipes keep hit boxes fixed. */
final class MenuMotion {
    private static Settings.UiMotion mode=Settings.UiMotion.FULL;
    private static MenuMotion previous;
    private static long lastFrame,clickTime;
    private static int clickX,clickY;
    private static final Map<Screen,Map<String,Tween>> toggles=new WeakHashMap<>();
    private final boolean centerPop;
    private boolean transformed;
    private float renderScale=1,offsetY;
    private long started;
    private int x,y,w,h;
    private boolean switching,clipped;
    private double progress=1;
    MenuMotion(){this(false);}
    MenuMotion(boolean centerPop){this.centerPop=centerPop;}
    int pointerX(int value){float center=x+w/2f;return Math.round(center+(value-center)/renderScale);}
    int pointerY(int value){float center=y+h/2f;return Math.round(center+(value-center-offsetY)/renderScale);}
    static void configure(Settings.UiMotion value){mode=value;}
    static boolean full(){return mode==Settings.UiMotion.FULL;}
    static boolean enabled(){return mode!=Settings.UiMotion.OFF;}
    static void clicked(int x,int y){if(enabled()){clickX=x;clickY=y;clickTime=System.nanoTime();}}
    void page(){started=System.nanoTime();switching=true;}
    void begin(GuiGraphics g,int x,int y,int w,int h){
        long now=System.nanoTime();
        if(previous!=this){switching=!centerPop&&previous!=null&&now-lastFrame<350_000_000L;started=now;previous=this;}
        lastFrame=now;this.x=x;this.y=y;this.w=w;this.h=h;
        double duration=mode==Settings.UiMotion.REDUCED?110:switching?210:290;
        progress=enabled()?Math.clamp((now-started)/(duration*1_000_000d),0,1):1;
        double ease=1-Math.pow(1-progress,3);
        transformed=centerPop&&full()&&progress<1;
        renderScale=transformed?(float)(.12+.88*ease):1;offsetY=transformed?(float)(12*(1-ease)):0;
        if(transformed){float cx=x+w/2f,cy=y+h/2f;g.pose().pushMatrix();g.pose().translate(cx,cy+offsetY);g.pose().scale(renderScale,renderScale);g.pose().translate(-cx,-cy);}
        clipped=!centerPop&&full()&&progress<1;
        if(clipped){
            // Module content wipes in from the rail; initial opening unfolds down from its top edge.
            if(switching)g.enableScissor(x-3,y-3,x+(int)Math.ceil((w+6)*ease),y+h+7);
            else {int half=(int)Math.ceil((w+6)*(.5+.5*ease)/2);g.enableScissor(x+w/2-half,y-3,x+w/2+half,y+(int)Math.ceil((h+7)*ease));}
        }
    }
    boolean tooltipsReady(){return !full()||progress>=.97;}
    void end(GuiGraphics g){
        if(clipped){g.disableScissor();clipped=false;}
        try{
        if(!enabled())return;
        double ease=1-Math.pow(1-progress,3);
        if(progress<1){
            if(mode==Settings.UiMotion.REDUCED)g.fill(x,y,x+w,y+h,((int)(65*(1-progress))<<24)|(CoastalStyle.panel()&0xFFFFFF));
            else {
                int alpha=(int)(140*(1-progress));int colour=alpha<<24|(CoastalStyle.accent()&0xFFFFFF);
                int end=x+Math.max(2,(int)((w-12)*ease));
                g.fill(x+6,y,end,y+1,colour);g.fill(end,y-1,end+2,y+2,colour);
                if(switching){int edge=x+(int)(w*ease);g.fill(edge,y+8,edge+1,y+h-8,((int)(45*(1-progress))<<24)|(CoastalStyle.accent()&0xFFFFFF));}
            }
        }
        long elapsed=System.nanoTime()-clickTime;
        if(full()&&elapsed>=0&&elapsed<220_000_000L&&clickX>=x&&clickX<=x+w&&clickY>=y&&clickY<=y+h){
            double p=elapsed/220_000_000d;int radius=2+(int)(p*8),a=(int)(105*(1-p)),c=a<<24|(CoastalStyle.accent()&0xFFFFFF);
            g.fill(clickX-radius,clickY,clickX-radius+2,clickY+1,c);g.fill(clickX+radius-1,clickY,clickX+radius+1,clickY+1,c);
            g.fill(clickX,clickY-radius,clickX+1,clickY-radius+2,c);g.fill(clickX,clickY+radius-1,clickX+1,clickY+radius+1,c);
        }
        }finally{if(transformed){g.pose().popMatrix();transformed=false;}}
    }
    static Tween toggle(String key,boolean initial){
        Screen screen=Minecraft.getInstance().screen;
        return toggles.computeIfAbsent(screen,s->new HashMap<>()).computeIfAbsent(key,k->new Tween(initial?1:0));
    }
    static final class Tween {
        private double value;
        private long time=System.nanoTime();
        Tween(){this(0);}
        Tween(double initial){value=initial;}
        double to(double target){long now=System.nanoTime();double dt=Math.clamp((now-time)/1e9,0,.1);time=now;value=enabled()?value+(target-value)*(1-Math.exp(-dt/0.055)):target;if(Math.abs(value-target)<.003)value=target;return value;}
    }
}
