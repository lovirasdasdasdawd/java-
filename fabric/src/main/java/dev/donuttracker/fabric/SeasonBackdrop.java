package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;

/** Five quiet pixel landscapes. Density affects particles; all geometry is clipped to its preview. */
final class SeasonBackdrop {
    static void draw(GuiGraphics g,int x,int y,int w,int h,Settings.BackgroundScene scene,double t,Settings.SceneDesign d,int tint){
        g.enableScissor(x,y,x+w,y+h);
        try{switch(scene){case DESERT->desert(g,x,y,w,h,t,d,tint);case SNOW->snow(g,x,y,w,h,t,d,tint);case ABYSS->abyss(g,x,y,w,h,t,d,tint);case SAKURA->sakura(g,x,y,w,h,t,d,tint);case STORM->storm(g,x,y,w,h,t,d,tint);default->{}}}finally{g.disableScissor();}
    }
    private static int mix(int a,int b,double n){return CoastalStyle.mix(a,b,(float)n);}
    private static int seed(int i,int n){return Math.floorMod(i*1103515245+12345,Math.max(1,n));}
    private static void disc(GuiGraphics g,int cx,int cy,int r,int colour){for(int yy=-r;yy<=r;yy++){int dx=(int)Math.sqrt(r*r-yy*yy);g.fill(cx-dx,cy+yy,cx+dx+1,cy+yy+1,colour);}}
    private static void desert(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF292139,mix(0xFFAC705C,c,.35*d.intensity));
        disc(g,x+w*4/5,y+h/5,Math.max(2,h/12),mix(0xFFCC8F65,c,d.intensity));
        for(int layer=0;layer<3;layer++)for(int dx=0;dx<w;dx+=3){double u=dx/(double)w;int top=y+(int)(h*(.65+layer*.12+.055*Math.sin(u*7+layer*2)+.04*Math.sin(u*3+layer)));
            int col=layer==0?mix(0xFF84635D,c,.2*d.intensity):layer==1?0xFF62484A:0xFF3F303A;g.fill(x+dx,top,x+Math.min(w,dx+3),y+h,col);}
        for(int i=0;i<4+(int)(d.density*40);i++){int xx=x+(int)((seed(i+7,1000)/1000d+t*.018)%1*w),yy=y+h*3/4+seed(i+20,Math.max(1,h/4));g.fill(xx,yy,xx+2,yy+1,((int)(35+45*d.intensity)<<24)|d.tint);}
        int tx=x+w/12,base=y+h*9/10,th=Math.max(5,h/7);g.fill(tx,base-th,tx+2,base,0xFF302C32);g.fill(tx-5,base-th*2/3,tx+7,base-th*2/3+2,0xFF302C32);g.fill(tx-5,base-th,tx-3,base-th*2/3+2,0xFF302C32);
    }
    private static void snow(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF142536,mix(0xFF617989,c,.18*d.intensity));
        disc(g,x+w/6,y+h/6,Math.max(2,h/16),mix(0xFF859BAF,c,.6*d.intensity));
        for(int layer=0;layer<2;layer++)for(int dx=0;dx<w;dx+=3){double u=dx/(double)w;int top=y+(int)(h*(.72+layer*.17-.24*Math.abs(Math.sin(u*9+layer))));int col=layer==0?0xFF526A7C:0xFF233D50;g.fill(x+dx,top,x+Math.min(w,dx+3),y+h,col);if(layer==0)g.fill(x+dx,top,x+Math.min(w,dx+3),top+Math.max(1,h/50),mix(0xFFA4BCC9,c,.3*d.intensity));}
        for(int i=0;i<8+(int)(d.density*100);i++){double q=(seed(i+80,1000)/1000d+t*.035)%1;int xx=x+Math.floorMod(seed(i+1,w)+(int)(Math.sin(q*7+i)*8),w),yy=y+(int)(q*h);int size=i%5==0?2:1;g.fill(xx,yy,xx+size,yy+size,((int)(80+120*d.intensity)<<24)|d.tint);}
    }
    private static void abyss(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,mix(0xFF103A50,c,.15*d.intensity),0xFF030F23);
        for(int i=0;i<5;i++){int xx=x+(int)(w*(.08+i*.23)+Math.sin(t*.08+i)*w*.02),spread=Math.max(2,w/22);g.fillGradient(xx,y,xx+spread,y+h,((int)(25*d.intensity)<<24)|d.tint,0x00000000|d.tint);}
        for(int i=0;i<5+(int)(d.density*60);i++){double q=(seed(i+19,1000)/1000d+t*.018)%1;int xx=x+seed(i+61,w),yy=y+h-(int)(q*h);int a=(int)(40+90*d.intensity);if(i%4==0)TrackerHud.border(g,xx,yy,xx+3,yy+4,a<<24|d.tint);else g.fill(xx,yy,xx+1,yy+1,a<<24|d.tint);}
        // Seafloor ridges and kelp sway independently from the ascending particles.
        for(int dx=0;dx<w;dx+=3){int top=y+(int)(h*(.93+.025*Math.sin(dx*.04)));g.fill(x+dx,top,x+Math.min(w,dx+3),y+h,0xFF061928);}
        for(int i=0;i<9;i++){int root=x+seed(i+5,w),th=h/8+seed(i+8,Math.max(1,h/4));for(int k=0;k<th;k+=2){int xx=root+(int)(Math.sin(k*.08+t*.25+i)*3);g.fill(xx,y+h-k-2,xx+2,y+h-k,mix(0xFF133A44,c,.1*d.intensity));}}
    }
    private static void sakura(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        g.fillGradient(x,y,x+w,y+h,0xFF24233B,mix(0xFF79576A,c,.2*d.intensity));
        disc(g,x+w*4/5,y+h/7,Math.max(2,h/18),0xFFD6B8BB);
        g.fillGradient(x,y+h*4/5,x+w,y+h,0xFF493A48,0xFF211E30);
        for(int side=0;side<2;side++){int tx=x+(side==0?w/12:w*11/12),base=y+h,th=h*3/4;int trunk=Math.max(2,w/120);g.fill(tx,base-th,tx+trunk,base,0xFF332A39);
            for(int i=0;i<9;i++){int cx=tx+(i-4)*w/70,cy=base-th+Math.abs(i-4)*h/24;int radius=Math.max(2,h/12);g.fill(Math.min(tx,cx),cy,Math.max(tx,cx)+trunk,cy+2,0xFF332A39);disc(g,cx,cy,radius,mix(i%2==0?0xFF8D607B:0xFFB17C94,c,.45*d.intensity));}}
        for(int side=0;side<2;side++){int lx=x+(side==0?w/5:w*4/5),ly=y+h*4/5;g.fill(lx,ly-10,lx+2,ly+5,0xFF302938);g.fill(lx-2,ly-12,lx+4,ly-6,0xFFCAA47F);g.fill(lx-3,ly-13,lx+5,ly-12,0xFF483745);}
        for(int i=0;i<5+(int)(d.density*50);i++){double q=(seed(i+42,1000)/1000d+t*.025)%1;int xx=x+Math.floorMod(seed(i+21,w)+(int)(Math.sin(q*9+i)*12+t*2),w),yy=y+(int)(q*h);g.fill(xx,yy,xx+2,yy+1,mix(0xFFAE7B93,c,d.intensity));}
    }
    private static void storm(GuiGraphics g,int x,int y,int w,int h,double t,Settings.SceneDesign d,int c){
        // No flashing lightning: distant thunder belongs to the soundtrack only.
        g.fillGradient(x,y,x+w,y+h,0xFF171E30,mix(0xFF405366,c,.15*d.intensity));
        int horizon=y+h*3/5;g.fillGradient(x,horizon,x+w,y+h,0xFF263D4D,0xFF0C1C2B);
        for(int i=0;i<4;i++){int cy=y+h/12+i*h/20;CoastalStyle.wave(g,x,cy,w,Math.max(1,h/35),0x350A1424,t*.04+i);}
        for(int i=0;i<6;i++)CoastalStyle.wave(g,x,horizon+i*h/13,w,Math.max(1,1+i/2),((int)(40+45*d.intensity)<<24)|d.tint,t*.35-i);
        for(int dx=0;dx<w/7;dx+=2){int rock=y+h*3/4-(int)(Math.sin(dx*.08)*h*.03);g.fill(x+dx,rock,x+dx+2,y+h,0xFF0B1723);g.fill(x+w-dx-2,rock-h/12,x+w-dx,y+h,0xFF0B1723);}
        for(int i=0;i<10+(int)(d.density*100);i++){double q=(seed(i+71,1000)/1000d+t*.19)%1;int xx=x+Math.floorMod(seed(i+13,w)+(int)(q*w*.07),w),yy=y+(int)(q*h);g.fill(xx,yy,xx+1,yy+Math.max(2,h/70),((int)(45+65*d.intensity)<<24)|d.tint);}
    }
}
