package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Coastal stationery colours and deliberately sparse pixel wave contours. */
final class CoastalStyle {
    private static Settings.MenuPalette palette=Settings.MenuPalette.SEA_GLASS;
    private static boolean motion=true;
    static void configure(Settings settings){palette=settings.menuPalette;motion=settings.menuMotion;}
    static int panel(){return switch(palette){case SEA_GLASS->0xFF102F3B;case DUSK->0xFF292D46;case PAPER->0xFFE5D9B8;};}
    static int surface(){return switch(palette){case SEA_GLASS->0xFF193D46;case DUSK->0xFF35364F;case PAPER->0xFFF1E7CB;};}
    static int line(){return switch(palette){case SEA_GLASS->0xFF426D70;case DUSK->0xFF6D657F;case PAPER->0xFFA7B4A1;};}
    static int ink(){return palette==Settings.MenuPalette.PAPER?0xFF243F48:0xFFF3E8CB;}
    static int muted(){return switch(palette){case SEA_GLASS->0xFFA5BAB0;case DUSK->0xFFC1B4BD;case PAPER->0xFF617776;};}
    static int accent(){return switch(palette){case SEA_GLASS->0xFF90D7C2;case DUSK->0xFFE9AEAA;case PAPER->0xFF327E77;};}
    static int controlAccent(int custom){return palette==Settings.MenuPalette.PAPER?accent():0xFF000000|custom;}
    static int value(boolean loss,int custom){return palette==Settings.MenuPalette.PAPER?(loss?0xFF9B4951:0xFF286B55):0xFF000000|custom;}
    static int primaryInk(){return palette==Settings.MenuPalette.PAPER?0xFFFFF4D8:0xFF183A3E;}
    static int hover(){return mix(surface(),accent(),.12f);}
    static int mix(int a,int b,float amount){int r=Math.round((a>>16&255)*(1-amount)+(b>>16&255)*amount),g=Math.round((a>>8&255)*(1-amount)+(b>>8&255)*amount),v=Math.round((a&255)*(1-amount)+(b&255)*amount);return 0xFF000000|r<<16|g<<8|v;}
    static void background(GuiGraphics g,int width,int height){
        int top=palette==Settings.MenuPalette.PAPER?0xEA344F54:palette==Settings.MenuPalette.DUSK?0xEF171E36:0xEF071E2D;
        int bottom=palette==Settings.MenuPalette.PAPER?0xE9A8BDAB:palette==Settings.MenuPalette.DUSK?0xEE63516A:0xEF23545B;
        g.fillGradient(0,0,width,height,top,bottom);
        double phase=motion?(System.nanoTime()/1_000_000_000d)*.22:0;
        for(int row=0;row<3;row++)wave(g,0,height-23-row*14,width,4+row,0x28000000|(accent()&0xFFFFFF),phase+row*1.8);
    }
    static void panel(GuiGraphics g,int x,int y,int w,int h){
        TrackerHud.rounded(g,x-1,y+4,x+w+1,y+h+5,0x50000000);
        TrackerHud.rounded(g,x,y,x+w,y+h,panel());
        TrackerHud.border(g,x,y,x+w,y+h,line());
        g.fill(x+10,y+1,x+w-10,y+2,mix(panel(),ink(),.16f));
    }
    static void wave(GuiGraphics g,int x,int y,int width,int amplitude,int colour,double phase){
        int step=3;for(int dx=0;dx<width;dx+=step){double u=dx/(double)Math.max(1,width);
            int yy=y+(int)Math.round(amplitude*(.65*Math.sin(u*Math.PI*4+phase)+.35*Math.sin(u*Math.PI*7-phase*.6)));
            g.fill(x+dx,yy,x+Math.min(width,dx+step),yy+1,colour);
        }
    }
    static void headerWave(GuiGraphics g,int x,int y,int width){wave(g,x,y,width,2,0x80000000|(accent()&0xFFFFFF),motion?System.nanoTime()/1_000_000_000d*.18:0);}
    static void matchHud(Settings settings){
        for(var h:new Settings.Hud[]{settings.profit,settings.queue}){
            h.accent=accent()&0xFFFFFF;h.text=ink()&0xFFFFFF;h.background=panel()&0xFFFFFF;
            h.positive=palette==Settings.MenuPalette.PAPER?0x286B55:0xA5E3B7;
            h.negative=palette==Settings.MenuPalette.PAPER?0xA4434B:0xF4A296;
        }
        settings.queue.accent=palette==Settings.MenuPalette.SEA_GLASS?0xB6C6E9:accent()&0xFFFFFF;
    }
}
