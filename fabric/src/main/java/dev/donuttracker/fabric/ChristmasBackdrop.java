package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Calm winter scenery and static garlands; no flashing lights or extra UI transitions. */
final class ChristmasBackdrop {
    private ChristmasBackdrop(){}
    private static int seed(int i,int n){return Math.floorMod(i*1103515245+12345,Math.max(1,n));}
    static void draw(GuiGraphics g,int w,int h,boolean motion,Settings.SceneDesign d){
        g.fillGradient(0,0,w,h,0xFF0B1824,0xFF24483D);
        int moonX=w*7/8,moonY=h/6,r=Math.max(4,h/22);
        for(int yy=-r;yy<=r;yy++){int span=(int)Math.sqrt(r*r-yy*yy);g.fill(moonX-span,moonY+yy,moonX+span+1,moonY+yy+1,0xFFDADDC5);}
        for(int layer=0;layer<3;layer++){
            int base=h*(78+layer*8)/100,trees=8+layer*4,col=layer==0?0xFF25463D:layer==1?0xFF19392F:0xFF102B25;
            for(int i=0;i<trees;i++){int x=i*w/trees+seed(i+layer*31,Math.max(1,w/trees)),height=h/9+seed(i+layer*53,Math.max(1,h/5));g.fill(x,base-height,x+2,base+4,col);
                for(int row=0;row<height;row+=2){int span=Math.max(1,row/4);g.fill(Math.max(0,x-span),base-height+row,Math.min(w,x+span+2),base-height+row+2,col);}
            }
        }
        for(int x=0;x<w;x+=3){int y=h*9/10+(int)(Math.sin(x/(double)Math.max(1,w)*9)*h*.025);g.fill(x,y,Math.min(w,x+3),h,0xFF72958E);}
        int tx=w/16,base=h*94/100,height=h*42/100;
        g.fill(tx-1,base-height,tx+2,base,0xFF544336);
        for(int row=0;row<height;row+=2){int span=Math.max(1,(int)(row*.23*(.72+.28*((row%Math.max(1,height/4))/(double)Math.max(1,height/4)))));g.fill(Math.max(0,tx-span),base-height+row,Math.min(w,tx+span+2),base-height+row+2,0xFF1D5A3A);}
        for(int i=1;i<14;i++){int row=i*height/15,xx=tx+(int)(Math.sin(i*2.4)*row*.16),yy=base-height+row,c=i%2==0?0xFFF0D993:0xFFDB6970;g.fill(xx-1,yy-1,xx+2,yy+2,c);}
        g.fill(tx,base-height-6,tx+1,base-height+1,0xFFFFE6A4);g.fill(tx-2,base-height-3,tx+3,base-height-2,0xFFFFE6A4);
        for(int i=0;i<3;i++){int xx=tx-10+i*8,yy=base-2;g.fill(xx,yy,xx+7,yy+6,i%2==0?0xFF9B3E4A:0xFF31654D);g.fill(xx+3,yy,xx+4,yy+6,0xFFEBCF94);}
        double time=motion?System.nanoTime()/1e9*(.04+d.speed*.14):0;
        int snow=18+(int)(d.density*90);
        for(int i=0;i<snow;i++){double phase=(seed(i+17,1000)/1000d+time*.17)%1;int yy=(int)(phase*h),xx=Math.floorMod(seed(i+71,w)+(int)(Math.sin(phase*5+i)*5),Math.max(1,w));int size=i%9==0?2:1;
            int alpha=(int)(70+90*d.intensity);g.fill(xx,yy,xx+size,yy+size,alpha<<24|0xE1EFE9);}
    }
    static void garland(GuiGraphics g,int x,int y,int w){
        if(w<80)return;
        int span=w-28;
        for(int dx=0;dx<span;dx+=2){int yy=y+4+(int)(2*Math.sin(dx*.085));g.fill(x+14+dx,yy,x+16+dx,yy+1,0xFF386447);}
        for(int dx=8;dx<span-4;dx+=23){int xx=x+14+dx,yy=y+6+(int)(2*Math.sin(dx*.085));
            int c=dx%2==0?0xFFEACB81:0xFFE4797E;
            g.fill(xx-1,yy-1,xx+2,yy+2,0x33000000|(c&0xFFFFFF));g.fill(xx,yy,xx+1,yy+2,c);
        }
        g.fill(x+15,y+1,x+33,y+2,0xFFD2E3D5);g.fill(x+w-33,y+1,x+w-15,y+2,0xFFD2E3D5);
    }
}
