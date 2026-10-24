package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Small deterministic landscapes; no shaders, textures, remote assets or random frame jitter. */
final class WorldBackdrop {
    static void draw(GuiGraphics g,int x,int y,int w,int h,Settings.BackgroundScene scene,boolean motion,Settings.SceneDesign d){
        if(w<1||h<1)return;
        double t=motion?System.nanoTime()/1e9*(.15+d.speed*1.3):0;
        int tint=0xFF000000|d.tint;
        switch(scene){case SPACE->space(g,x,y,w,h,t,d,tint);case CITY->city(g,x,y,w,h,t,d,tint);case FOREST->forest(g,x,y,w,h,t,d,tint);default->{}}
    }
    private static int blend(int a,int b,double n){return CoastalStyle.mix(a,b,(float)n);}
    private static int seed(int i,int n){return Math.floorMod(i*1103515245+12345,n);}
    private static void space(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF050713,blend(0xFF0E1428,c,.12*d.intensity));
        // A distant spiral band stays behind the dashboard rather than obscuring its text.
        for(int i=0;i<70;i++){double u=i/69d;int xx=x+(int)(u*w),yy=y+(int)(h*(.18+.12*Math.sin(u*4+t*.06)));int r=Math.max(1,h/12);int a=(int)(d.intensity*10);g.fillGradient(xx,yy-r,Math.min(x+w,xx+Math.max(1,w/70+1)),yy,a<<24|d.tint,0);}
        int count=20+(int)(d.density*130);
        for(int i=0;i<count;i++){int xx=x+seed(i,w),yy=y+seed(i+301,h);int a=60+(int)(150*d.intensity*(.75+.25*Math.sin(t*.3+i)));g.fill(xx,yy,xx+1,yy+1,a<<24|0xDFE7FF);}
        int cx=x+w*4/5,cy=y+h/4,r=Math.max(4,Math.min(w,h)/7);
        // Scan-line sphere with a quiet crescent and two thin orbital rings.
        for(int dy=-r;dy<=r;dy++){int span=(int)Math.sqrt(r*r-dy*dy);g.fill(cx-span,cy+dy,cx+span+1,cy+dy+1,blend(0xFF10152C,c,(dy+r)/(double)(r*2)*.32*d.intensity));}
        for(int i=0;i<100;i++){double a=i*Math.PI*2/100;int xx=cx+(int)(Math.cos(a)*r*1.65),yy=cy+(int)(Math.sin(a)*r*.32+Math.cos(a)*r*.25);g.fill(xx,yy,xx+2,yy+1,0x60000000|d.tint);}
        if(t>0){double phase=(t*.04)%1;int xx=x+(int)(phase*w),yy=y+(int)(h*(.07+phase*.18));if(phase<.3)for(int j=0;j<12;j++)g.fill(xx-j*2,yy-j,xx-j*2+2,yy-j+1,((12-j)*12)<<24|0xDAE5FF);}
    }
    private static void city(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF101724,blend(0xFF263544,c,.16*d.intensity));
        int horizon=y+h*4/5;
        for(int layer=0;layer<2;layer++){int step=Math.max(9,w/23);for(int i=0,xx=x;xx<x+w;i++,xx+=step){int bh=h/8+seed(i+layer*97,Math.max(1,h/3));int roof=horizon-bh+layer*h/12;int bw=Math.min(step-2,x+w-xx);g.fill(xx,roof,xx+bw,horizon,layer==0?0xFF1D2936:0xFF101923);if(layer==1)for(int wy=roof+5;wy<horizon-3;wy+=7)for(int wx=xx+3;wx<xx+bw-2;wx+=5)if(seed(wx+wy,10)<d.density*8)g.fill(wx,wy,wx+2,wy+2,blend(0xFF313441,c,.6*d.intensity));}}
        g.fillGradient(x,horizon,x+w,y+h,0xFF1E2B37,0xFF0C1720);
        for(int i=0;i<25;i++){int xx=x+seed(i+90,w),yy=horizon+seed(i+99,Math.max(1,h/5));g.fill(xx,yy,Math.min(x+w,xx+2+seed(i,12)),yy+1,((int)(35*d.intensity)<<24)|d.tint);}
        int count=10+(int)(d.density*100);for(int i=0;i<count;i++){double q=(seed(i+13,1000)/1000d+t*.16)%1;int yy=y+(int)(q*h),xx=x+Math.floorMod(seed(i+39,w)+(int)(q*14),w);g.fill(xx,yy,xx+1,Math.min(y+h,yy+3+seed(i,5)),((int)(40+70*d.intensity)<<24)|0xB4CEDD);}
        // A lamplit shelter at the edge gives the skyline a human scale.
        int lx=x+w/12;g.fill(lx,horizon-h/3,lx+2,horizon+4,0xFF080F18);g.fill(lx-3,horizon-h/3,lx+12,horizon-h/3+2,0xFF111B25);g.fillGradient(lx+5,horizon-h/3+2,lx+12,horizon,0x35000000|d.tint,0x00000000|d.tint);
    }
    private static void forest(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,blend(0xFF162C2A,c,.12*d.intensity),0xFF514A35);
        int sunx=x+w*3/4,suny=y+h/5,r=Math.max(3,h/14);for(int dy=-r;dy<=r;dy++){int span=(int)Math.sqrt(r*r-dy*dy);g.fill(sunx-span,suny+dy,sunx+span+1,suny+dy+1,blend(0xFF8D8761,c,.65*d.intensity));}
        for(int layer=0;layer<3;layer++){int n=10+layer*4;for(int i=0;i<n;i++){int xx=x+i*w/n+seed(i+layer*31,Math.max(1,w/n)),base=y+h,th=h/3+seed(i+layer*41,Math.max(1,h/2));int trunk=layer+1;int col=layer==0?0xFF3C5145:layer==1?0xFF273B32:0xFF132A25;g.fill(xx,base-th,xx+trunk,base,col);for(int row=0;row<th*4/5;row+=2){int yy=base-th+row,span=Math.max(1,(int)(row*.24*(.8+.2*Math.sin(row*.35+i))));g.fill(Math.max(x,xx-span),yy,Math.min(x+w,xx+span+trunk),Math.min(base,yy+2),col);}}}
        // Soft low mist, with amber leaves following slow periodic paths.
        g.fillGradient(x,y+h*3/4,x+w,y+h,0x003B5B4F,0x483B5B4F);
        for(int i=0;i<6+(int)(d.density*38);i++){double q=(seed(i+41,1000)/1000d+t*.025)%1;int xx=x+Math.floorMod(seed(i+17,w)+(int)(Math.sin(q*9+i)*10+t*2),w),yy=y+(int)(q*h);int col=blend(0xFF776445,c,d.intensity);g.fill(xx,yy,Math.min(x+w,xx+2),Math.min(y+h,yy+1+seed(i,2)),col);}
    }
}
