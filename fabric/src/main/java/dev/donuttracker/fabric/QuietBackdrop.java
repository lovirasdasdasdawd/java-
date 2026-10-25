package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Five small landscapes built from restrained silhouettes and slow natural motion. */
final class QuietBackdrop {
    static void draw(GuiGraphics g,int x,int y,int w,int h,Settings.BackgroundScene scene,double t,Settings.SceneDesign d,int tint){
        g.enableScissor(x,y,x+w,y+h);
        try{switch(scene){
            case LIGHTHOUSE->harbor(g,x,y,w,h,t,d,tint);
            case RIVER->river(g,x,y,w,h,t,d,tint);
            case CLOUDS->clouds(g,x,y,w,h,t,d,tint);
            case MOON->lake(g,x,y,w,h,t,d,tint);
            case BAMBOO->bamboo(g,x,y,w,h,t,d,tint);
            default->{}
        }}finally{g.disableScissor();}
    }
    private static int mix(int a,int b,double p){return CoastalStyle.mix(a,b,(float)p);}
    private static int seed(int i,int n){return Math.floorMod(i*1103515245+12345,Math.max(1,n));}
    private static void disc(GuiGraphics g,int cx,int cy,int r,int c){for(int yy=-r;yy<=r;yy++){int dx=(int)Math.sqrt(r*r-yy*yy);g.fill(cx-dx,cy+yy,cx+dx+1,cy+yy+1,c);}}
    private static void hills(GuiGraphics g,int x,int y,int w,int h,int c,double level,double amplitude,int phase){
        for(int dx=0;dx<w;dx+=2){double u=dx/(double)w;int yy=y+(int)(h*(level+amplitude*Math.sin(u*9+phase)+amplitude*.4*Math.sin(u*21+phase)));g.fill(x+dx,yy,x+Math.min(w,dx+2),y+h,c);}
    }
    private static void harbor(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF102035,mix(0xFF8A6A68,c,.18*d.intensity));
        int horizon=y+h*3/5;g.fillGradient(x,horizon,x+w,y+h,0xFF263C4B,0xFF0C1B29);
        for(int i=0;i<3+(int)(d.density*8);i++)CoastalStyle.wave(g,x,horizon+3+i*Math.max(2,h/23),w,1+i/4,((int)(18+40*d.intensity)<<24)|d.tint,t*.22+i);
        hills(g,x,y,w/3,h,0xFF101D29,.78,.07,2);
        int bx=x+w/7,base=y+h*4/5,tower=Math.max(8,h/3),tw=Math.max(3,w/75),top=base-tower;
        g.fill(bx-tw,top,bx+tw,base,0xFFB3B4AA);g.fill(bx-tw,top+tower/2,bx+tw,top+tower/2+Math.max(2,tower/8),0xFF6E5556);
        g.fill(bx-tw-2,top-4,bx+tw+2,top,0xFFE4CE9C);g.fill(bx-tw-3,top-6,bx+tw+3,top-4,0xFF1B2530);
        // A slow low-contrast beam, never a flashing lighthouse pulse.
        double angle=Math.sin(t*.09)*.08;
        for(int dx=0;dx<w/2;dx+=3){int yy=top-2+(int)(dx*angle),spread=1+dx/30;g.fill(bx+tw+dx,yy-spread,bx+tw+dx+3,yy+spread,((int)((1-dx/(double)(w/2))*12*d.intensity)<<24)|d.tint);}
    }
    private static void river(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,mix(0xFF1B3437,c,.12*d.intensity),0xFF536861);
        hills(g,x,y,w,h,0xFF344F4B,.48,.08,1);hills(g,x,y,w,h,0xFF203E37,.67,.07,3);
        for(int row=h/2;row<h;row+=2){double v=(row-h*.5)/(h*.5);int centre=x+(int)(w*(.5+.12*Math.sin(v*4))),span=(int)(w*(.025+.24*v));g.fill(centre-span,y+row,centre+span,y+row+2,mix(0xFF365B60,c,.22*d.intensity));
            if(row%6==0){int offset=(int)(Math.sin(t*.45+v*20)*span*.3);g.fill(centre-span/2+offset,y+row,centre+span/2+offset,y+row+1,0x3586B9B4);}}
        for(int i=0;i<6+(int)(d.density*20);i++){int xx=x+seed(i,w),yy=y+h*3/4+seed(i+30,Math.max(1,h/4));int trunk=Math.max(4,h/10+seed(i+8,Math.max(1,h/6)));if(Math.abs(xx-(x+w/2))<w/4)continue;g.fill(xx,yy-trunk,xx+2,yy,0xFF122C29);for(int k=0;k<trunk*3/4;k+=2){int span=k/3+1;g.fill(xx-span,yy-trunk+k,xx+span+2,yy-trunk+k+2,0xFF18372E);}}
    }
    private static void clouds(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF202C49,mix(0xFF998A9F,c,.3*d.intensity));
        disc(g,x+w*4/5,y+h/5,Math.max(3,h/14),mix(0xFFC8B8AD,c,.45*d.intensity));
        for(int layer=0;layer<4;layer++){
            int base=y+h*(5+layer)/9,col=mix(layer%2==0?0xFF817A95:0xFF5D647E,c,(.08+layer*.04)*d.intensity);
            for(int dx=0;dx<w;dx+=3){double u=dx/(double)w;int yy=base+(int)(h*.055*(Math.sin(u*12+t*.025*(layer+1)+layer)+.4*Math.sin(u*27+layer)));g.fill(x+dx,yy,x+Math.min(w,dx+3),y+h,col);}
        }
        for(int i=0;i<(int)(d.density*22);i++){int xx=x+seed(i+31,w),yy=y+seed(i+67,Math.max(1,h/3));g.fill(xx,yy,xx+1,yy+1,0x50DBD7EB);}
    }
    private static void lake(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF0A172B,mix(0xFF345065,c,.12*d.intensity));
        int mx=x+w*3/4,my=y+h/5,r=Math.max(3,h/14);disc(g,mx,my,r,mix(0xFFBBC9D6,c,d.intensity));disc(g,mx+r/3,my-r/5,r*4/5,0xFF14253A);
        for(int i=0;i<12+(int)(d.density*60);i++){int xx=x+seed(i,w),yy=y+seed(i+44,Math.max(1,h/2));g.fill(xx,yy,xx+1,yy+1,0x809FBCCC);}
        hills(g,x,y,w,h,0xFF192D3B,.59,.07,0);
        int horizon=y+h*2/3;g.fillGradient(x,horizon,x+w,y+h,0xFF1A3444,0xFF0D202F);
        for(int i=0;i<6+(int)(d.density*12);i++){int yy=horizon+2+i*Math.max(1,h/55),span=2+i*2,xx=mx+(int)(Math.sin(t*.22+i)*span*.5);g.fill(xx-span,yy,xx+span,yy+1,((int)(15+38*d.intensity)<<24)|d.tint);}
        hills(g,x,y,w,h,0xFF0A1B26,.94,.025,2);
    }
    private static void bamboo(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,mix(0xFF1A342E,c,.1*d.intensity),0xFF53634A);
        for(int layer=0;layer<3;layer++){int count=5+layer*2;for(int i=0;i<count;i++){
            int xx=x+seed(i+layer*51,w),col=layer==0?0xFF405941:layer==1?0xFF2A4636:0xFF163529,thick=1+layer;
            for(int yy=0;yy<h;yy+=2){int sway=(int)(Math.sin(t*.16+i)*2*(1-yy/(double)h));g.fill(xx+sway,y+yy,xx+sway+thick,y+yy+2,col);
                if(yy%Math.max(6,h/9)<2){g.fill(xx-1+sway,y+yy,xx+thick+1+sway,y+yy+1,mix(col,c,.12*d.intensity));int dir=(i+yy)%2==0?1:-1;for(int k=0;k<8;k++){int lx=xx+sway+dir*k;g.fill(lx,y+yy-k/2,lx+2,y+yy-k/2+1,col);}}}
        }}
        g.fillGradient(x,y+h*2/3,x+w,y+h,0x003D5644,0x503D5644);
        for(int i=0;i<(int)(d.density*30);i++){double q=(seed(i+19,1000)/1000d+t*.012)%1;int xx=x+Math.floorMod(seed(i+7,w)+(int)(Math.sin(t*.15+i)*5),w),yy=y+(int)(q*h);g.fill(xx,yy,xx+1,yy+1,0x60BEC8A1);}
    }
}
