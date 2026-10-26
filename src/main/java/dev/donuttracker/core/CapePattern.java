package dev.donuttracker.core;

/** Deterministic 4x-resolution vanilla cape atlas and separate emissive mask. */
public final class CapePattern {
    public static final int WIDTH=256,HEIGHT=128;
    public enum Design { CLASSIC, ORBIT, CROWN }
    private CapePattern(){}
    public static int[] pixels(Design design,int background,int frosting,int glow,double strength,boolean emissive){
        int[] out=new int[WIDTH*HEIGHT];
        if(!emissive)for(int y=0;y<68;y++)for(int x=0;x<92;x++)out[y*WIDTH+x]=0xFF000000|background;
        for(int face=0;face<2;face++)for(int y=0;y<64;y++)for(int x=0;x<40;x++){
            double radius=Math.hypot((x-19.5),(y-27.5)*.91);
            int color=background,shine=0;
            // Deliberately quiet cloth weave, with an inset luminous stitched edge.
            if(!emissive)color=mix(background,0xFFFFFF,(x%4==0?.018:0)+(y/63d)*.025);
            boolean trim=(x==2||x==37)&&y>4&&y<59||y==60&&x>3&&x<36;
            if(trim){color=mix(background,glow,.8);shine=glow;}
            if(radius<14&&radius>5.6){
                color=mix(0xB5753E,0xE3A65B,(64-y)/64d);
                double edge=12.2+.7*Math.sin(x*.7)+.5*Math.sin(y*.85);
                if(radius<edge&&radius>6.4){color=mix(frosting,0xFFFFFF,Math.max(0,(27-y)/40d));if(radius>edge-1.2)shine=mix(frosting,glow,.4);}
                if(radius>13)shine=mix(glow,frosting,.5);
                if(radius>8&&radius<11&&Math.floorMod(x*7+y*13,29)<2)color=((x+y)%2==0?0xFFF2CD:glow);
            }
            boolean star=false;
            int[][] stars={{8,10},{31,14},{8,47},{29,50}};
            for(int[] s:stars)if((x==s[0]&&Math.abs(y-s[1])<=2)||(y==s[1]&&Math.abs(x-s[0])<=1))star=true;
            if(design==Design.ORBIT){double ring=Math.hypot((x-20)/1.6,(y-29)*1.6);if(Math.abs(ring-14)<.7){star=true;}}
            if(design==Design.CROWN&&y>=4&&y<=9&&x>=13&&x<=26&&(y>=8||x==13||x==26||x==19||x==20)){star=true;}
            if(star){color=mix(glow,0xFFFFFF,.4);shine=color;}
            int target=(y+4)*WIDTH+(face==0?4:48)+x;
            if(emissive){if(shine!=0&&strength>0)out[target]=0xFF000000|mix(0,shine,Math.clamp(strength,0,1));}
            else out[target]=0xFF000000|color;
        }
        return out;
    }
    private static int mix(int a,int b,double n){n=Math.clamp(n,0,1);return ((int)((a>>16&255)*(1-n)+(b>>16&255)*n)<<16)|((int)((a>>8&255)*(1-n)+(b>>8&255)*n)<<8)|(int)((a&255)*(1-n)+(b&255)*n);}
}
