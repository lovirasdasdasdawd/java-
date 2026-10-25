package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Only the top-level menu entrance and exit move. Module pages and controls stay still. */
final class MenuMotion {
    private static Settings.UiMotion mode=Settings.UiMotion.FULL;
    private final boolean centerPop;
    private long started,closeStarted;
    private boolean transformed;
    private float renderScale=1,offsetY;
    private int x,y,w,h;
    MenuMotion(){this(false);}
    MenuMotion(boolean centerPop){this.centerPop=centerPop;}
    static void configure(Settings.UiMotion value){mode=value;}
    static boolean full(){return mode==Settings.UiMotion.FULL;}
    static boolean enabled(){return mode!=Settings.UiMotion.OFF;}
    static void clicked(int x,int y){} // Compatibility for existing controls; no click effects.
    void page(){} // Module and tab changes are deliberately instantaneous.
    void close(){if(closeStarted==0)closeStarted=System.nanoTime();}
    boolean closing(){return closeStarted!=0;}
    boolean closed(){return closing()&&(!enabled()||System.nanoTime()-closeStarted>=(mode==Settings.UiMotion.REDUCED?90:190)*1_000_000L);}
    int pointerX(int value){float c=x+w/2f;return Math.round(c+(value-c)/renderScale);}
    int pointerY(int value){float c=y+h/2f;return Math.round(c+(value-c-offsetY)/renderScale);}
    void begin(GuiGraphics g,int x,int y,int w,int h){
        if(!centerPop)return;
        this.x=x;this.y=y;this.w=w;this.h=h;
        long now=System.nanoTime();if(started==0)started=now;
        double p=Math.clamp((now-(closing()?closeStarted:started))/((closing()?190d:290d)*1_000_000d),0,1);
        double size=closing()?1-p*p*p:1-Math.pow(1-p,3);
        transformed=full()&&(closing()||p<1);
        renderScale=transformed?(float)(.12+.88*size):1;offsetY=transformed?(float)(12*(1-size)):0;
        if(transformed){float cx=x+w/2f,cy=y+h/2f;g.pose().pushMatrix();g.pose().translate(cx,cy+offsetY);g.pose().scale(renderScale,renderScale);g.pose().translate(-cx,-cy);}
    }
    boolean tooltipsReady(){return true;}
    void end(GuiGraphics g){
        if(transformed){g.pose().popMatrix();transformed=false;}
        // Reduced motion uses a short stationary fade instead of zooming.
        if(centerPop&&mode==Settings.UiMotion.REDUCED){
            double p=Math.clamp((System.nanoTime()-(closing()?closeStarted:started))/((closing()?90d:110d)*1_000_000d),0,1);
            int alpha=(int)(95*(closing()?p:1-p));if(alpha>0)g.fill(x,y,x+w,y+h,alpha<<24|(CoastalStyle.panel()&0xFFFFFF));
        }
    }
}
