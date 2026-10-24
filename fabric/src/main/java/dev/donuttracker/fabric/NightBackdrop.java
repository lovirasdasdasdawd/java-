package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Three restrained night scenes, using stable seeded details and slow periodic motion. */
final class NightBackdrop {
    private static int seed(int i,int n){return Math.floorMod(i*1103515245+12345,Math.max(1,n));}
    static void draw(GuiGraphics g,int x,int y,int w,int h,Settings.BackgroundScene scene,double t,Settings.SceneDesign d,int tint){
        switch(scene){case VOLCANO->volcano(g,x,y,w,h,t,d,tint);case MARSH->marsh(g,x,y,w,h,t,d,tint);case RAILWAY->railway(g,x,y,w,h,t,d,tint);default->{}}
    }
    private static void volcano(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF191824,CoastalStyle.mix(0xFF5A2E31,c,(float)(.15*d.intensity)));
        int peak=x+w*3/4,py=y+h/3;
        for(int dx=0;dx<w;dx+=2){double slope=Math.abs(dx-w*.75)/Math.max(1,w*.7);int top=Math.min(y+h,py+(int)(slope*h*.8));g.fill(x+dx,top,x+Math.min(w,dx+2),y+h,0xFF231E29);}
        int crater=Math.max(2,w/45);g.fill(peak-crater,py,peak+crater,py+2,0xFF000000|d.tint);
        for(int yy=0;yy<h*2/3;yy+=2){int xx=peak+(int)(Math.sin(yy*.075)*w*.02);g.fill(xx,y+h/3+yy,xx+Math.max(1,w/150),y+h/3+yy+2,CoastalStyle.mix(0xFF793830,c,(float)d.intensity));}
        for(int i=0;i<4+(int)(d.density*55);i++){double q=(seed(i+77,1000)/1000d+t*.02)%1;int xx=x+seed(i+2,w),yy=y+h-(int)(q*h);int a=(int)((30+110*d.intensity)*(1-q));g.fill(xx,yy,xx+1,yy+2,a<<24|d.tint);}
    }
    private static void marsh(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF102B2C,0xFF263B39);int water=y+h*2/3;g.fillGradient(x,water,x+w,y+h,0xFF142E31,0xFF091C25);
        for(int i=0;i<6;i++)CoastalStyle.wave(g,x,water+i*h/20,w,1,0x303F6A63,t*.08+i);
        for(int i=0;i<25;i++){int xx=x+seed(i+17,w),th=h/15+seed(i+29,Math.max(1,h/6)),base=y+h;g.fill(xx,base-th,xx+1,base,0xFF102B2C);g.fill(xx-1,base-th-3,xx+2,base-th+3,0xFF3D5140);}
        for(int i=0;i<4+(int)(d.density*55);i++){int xx=x+Math.floorMod(seed(i+81,w)+(int)(Math.sin(t*.19+i)*7),w),yy=y+h/5+seed(i+21,Math.max(1,h*3/4));int a=(int)((35+155*d.intensity)*(.55+.45*Math.sin(t*.35+i)));g.fill(xx,yy,xx+2,yy+1,a<<24|d.tint);}
    }
    private static void railway(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF0B142A,0xFF293B50);int horizon=y+h*3/5;
        for(int i=0;i<8;i++){int xx=x+seed(i+15,w),bh=h/12+seed(i+22,Math.max(1,h/5));g.fill(xx,horizon-bh,Math.min(x+w,xx+Math.max(4,w/20)),horizon,0xFF142238);}
        g.fill(x,horizon,x+w,y+h,0xFF101B2C);
        for(int i=0;i<7;i++){int yy=horizon+(i*i)*Math.max(1,h/130);g.fill(x,yy,x+w,yy+1,0xFF354254);}
        int lx=x+w/12;g.fill(lx,y+h/3,lx+2,y+h*4/5,0xFF142032);g.fill(lx-3,y+h/3,lx+15,y+h/3+2,0xFF4E5C6A);g.fillGradient(lx+7,y+h/3+2,lx+14,y+h*4/5,0x40000000|d.tint,0x00000000|d.tint);
        int trainW=Math.max(25,w/3);double phase=(t*.018)%1;int trainX=x+(int)(phase*(w+trainW))-trainW,trainY=horizon-h/9;
        g.fill(trainX,trainY,trainX+trainW,horizon-3,0xFF354357);g.fill(trainX,trainY,trainX+trainW,trainY+2,0xFF718191);
        for(int xx=trainX+5;xx<trainX+trainW-5;xx+=Math.max(7,w/45))g.fill(xx,trainY+4,xx+4,Math.max(trainY+5,horizon-8),0xD0000000|d.tint);
        for(int i=0;i<4+(int)(d.density*40);i++){double q=(seed(i+42,1000)/1000d+t*.12)%1;int xx=x+seed(i+31,w),yy=y+(int)(q*h);g.fill(xx,yy,xx+1,yy+2,((int)(35+65*d.intensity)<<24)|d.tint);}
    }
}
