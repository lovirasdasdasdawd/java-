package dev.donuttracker.fabric;

import net.minecraft.client.gui.GuiGraphics;

/** Bounded, shader-free aurora curtains with a deterministic starfield. */
final class AuroraBackdrop {
    private AuroraBackdrop(){}
    static void draw(GuiGraphics g,int x,int y,int w,int h,boolean motion,float strength){
        g.fillGradient(x,y,x+w,y+h,0xFF050B20,0xFF142840);
        double time=motion?System.nanoTime()/1_000_000_000d*.10:0;
        int stars=Math.min(76,Math.max(12,w/6));
        for(int i=0;i<stars;i++){
            int seed=i*1103515245+12345;
            int sx=x+Math.floorMod(seed,w),sy=y+Math.floorMod(seed>>>7,Math.max(1,h*3/4));
            int alpha=90+Math.floorMod(seed>>>13,120);
            g.fill(sx,sy,sx+1,sy+1,alpha<<24|0xDCE9FA);
            if(i%19==0&&w>200){g.fill(sx-1,sy,sx+2,sy+1,0x70DCE9FA);g.fill(sx,sy-1,sx+1,sy+2,0x70DCE9FA);}
        }
        int[] colours={0x68EFBE,0x9292F3,0x61BDE6};
        int step=Math.max(3,w/150);
        for(int ribbon=2;ribbon>=0;ribbon--){
            double offset=ribbon*1.7;
            for(int dx=0;dx<w;dx+=step){
                double u=dx/(double)Math.max(1,w);
                double drift=Math.sin(u*4.2+time+offset)*h*.09+Math.sin(u*12-time*.6+offset)*h*.018;
                int top=y+(int)(h*(.11+ribbon*.065)+drift);
                int length=(int)(h*(.25+.045*Math.cos(u*7+time*.5+offset)));
                int middle=top+length*2/3,bottom=top+length;
                int alpha=(int)((50+70*(.5+.5*Math.sin(u*24+offset+time*.8)))*strength);
                int colour=colours[ribbon],end=x+Math.min(w,dx+step);
                g.fillGradient(x+dx,top,end,middle,colour,alpha<<24|colour);
                g.fillGradient(x+dx,middle,end,bottom,alpha<<24|colour,colour);
            }
        }
        // Two still ridgelines anchor the moving sky.
        int terrainStep=Math.max(3,w/180);
        for(int dx=0;dx<w;dx+=terrainStep){
            double u=dx/(double)Math.max(1,w);
            int far=y+(int)(h*(.91-.13*Math.abs(Math.sin(u*7.4+.8))-.035*Math.abs(Math.sin(u*25))));
            int near=y+(int)(h*(.98-.10*Math.abs(Math.sin(u*9.2+2))));
            g.fill(x+dx,far,x+Math.min(w,dx+terrainStep),y+h,0xFF101E36);
            g.fill(x+dx,near,x+Math.min(w,dx+terrainStep),y+h,0xFF080F21);
        }
        if(w>200)for(int i=0;i<7;i++){
            int tx=x+(int)(w*(i<4?.04+i*.038:.83+(i-4)*.04));int th=10+(i%3)*5,base=y+h-2;
            g.fill(tx,base-th,tx+1,base,0xFF050C18);
            for(int row=0;row<4;row++){int yy=base-th+3+row*th/5,half=2+row*2;g.fill(tx-half,yy,tx+half+1,yy+2,0xFF050C18);}
        }
    }
}
