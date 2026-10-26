package dev.donuttracker.core;

/** High-resolution cape assembly and a real spatial emissive halo; no frame-time effects. */
public final class CapeStudio {
    public static final int SCALE=4,WIDTH=1024,HEIGHT=512,FACE_W=160,FACE_H=256;
    public record Textures(int[] cloth,int[] glow,int[] preview){}
    private CapeStudio(){}
    public static Textures create(CapePattern.Design design,int cloth,int art,int accent,double strength,double aura,boolean enabled,int[] wolf,int wolfW,int wolfH){
        var low=CapePattern.pixels(design,cloth,art,accent,1,false);
        var lowGlow=CapePattern.pixels(design,cloth,art,accent,1,true);
        int[] base=new int[WIDTH*HEIGHT],face=new int[FACE_W*FACE_H],mask=new int[face.length];
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++)base[y*WIDTH+x]=low[(y/SCALE)*CapePattern.WIDTH+x/SCALE];
        for(int y=0;y<FACE_H;y++)for(int x=0;x<FACE_W;x++){
            int i=y*FACE_W+x;face[i]=low[(y/SCALE+4)*CapePattern.WIDTH+x/SCALE+4];mask[i]=lowGlow[(y/SCALE+4)*CapePattern.WIDTH+x/SCALE+4]&0xFFFFFF;
            int pixel=0;
            if(design==CapePattern.Design.WOLF&&wolf!=null){
                int wx=x-(FACE_W-wolfW)/2,wy=y-38;
                if(wx>=0&&wx<wolfW&&wy>=0&&wy<wolfH)pixel=wolf[wy*wolfW+wx];
            }else if(design.ordinal()>=4)pixel=emblem(design,x/4d,y/4d,art,accent);
            int alpha=pixel>>>24;
            if(alpha>0){
                int tinted=tint(pixel&0xFFFFFF,art,design==CapePattern.Design.WOLF?.12:0);
                face[i]=0xFF000000|mix(face[i],tinted,alpha/255d);
                // The complete bright artwork emits, not just the stitched border.
                double luminance=((tinted>>16&255)*.22+(tinted>>8&255)*.65+(tinted&255)*.13)/255d;
                mask[i]=scale(tinted,alpha/255d*Math.min(1,luminance*1.8));
            }
        }
        int[] halo=halo(mask,FACE_W,FACE_H,2+(int)(Math.clamp(aura,0,1)*12),enabled?strength:0),glow=new int[base.length],preview=base.clone();
        for(int side=0;side<2;side++)for(int y=0;y<FACE_H;y++)for(int x=0;x<FACE_W;x++){
            int i=y*FACE_W+x,target=(y+16)*WIDTH+(side==0?16:192)+x;
            base[target]=face[i];int c=halo[i];glow[target]=(c&0xFFFFFF)==0?0:0xFF000000|c;
            preview[target]=0xFF000000|add(face[i],c,.8);
        }
        return new Textures(base,glow,preview);
    }
    /** Three separable box passes approximate a Gaussian without a quadratic kernel. */
    public static int[] halo(int[] seed,int w,int h,int radius,double strength){
        if(w<1||h<1||seed.length!=w*h)throw new IllegalArgumentException("Invalid halo dimensions");
        strength=Double.isFinite(strength)?Math.clamp(strength,0,1):0;radius=Math.clamp(radius,1,32);
        int[] result=new int[seed.length];if(strength==0)return result;
        float[][] channels=new float[3][seed.length];
        for(int ch=0;ch<3;ch++){
            float[] a=new float[seed.length],b=new float[seed.length];int shift=(2-ch)*8;
            for(int i=0;i<a.length;i++)a[i]=(seed[i]>>shift)&255;
            for(int pass=0;pass<3;pass++){
                for(int y=0;y<h;y++){double sum=0;for(int x=-radius;x<=radius;x++)if(x>=0&&x<w)sum+=a[y*w+x];for(int x=0;x<w;x++){b[y*w+x]=(float)(sum/(radius*2+1));int prev=x-radius,next=x+radius+1;if(prev>=0)sum-=a[y*w+prev];if(next<w)sum+=a[y*w+next];}}
                for(int x=0;x<w;x++){double sum=0;for(int y=-radius;y<=radius;y++)if(y>=0&&y<h)sum+=b[y*w+x];for(int y=0;y<h;y++){a[y*w+x]=(float)(sum/(radius*2+1));int prev=y-radius,next=y+radius+1;if(prev>=0)sum-=b[prev*w+x];if(next<h)sum+=b[next*w+x];}}
            }
            channels[ch]=a;
        }
        for(int i=0;i<seed.length;i++){
            int color=0;for(int ch=0;ch<3;ch++){int raw=(seed[i]>>((2-ch)*8))&255;int v=(int)Math.min(255,(raw*.95+channels[ch][i]*1.8)*strength);color|=v<<((2-ch)*8);}result[i]=color;
        }
        return result;
    }
    private static int emblem(CapePattern.Design d,double x,double y,int art,int accent){
        double u=x-19.5,v=y-29;
        if(d==CapePattern.Design.PHOENIX){
            double wing=Math.abs(u),curve=25-wing*.65+2*Math.sin(wing*.45);
            boolean feather=wing>2&&wing<15&&y>curve&&y<curve+7+2*Math.sin(wing*2);
            boolean body=Math.abs(u)<2.5&&y>22&&y<43||Math.abs(u)<(48-y)*.25&&y>38&&y<48;
            boolean head=Math.hypot(u-1,v+9)<3||x>20&&x<25&&y>18&&y<20;
            if(feather||body||head)return 0xFF000000|mix(accent,art,Math.clamp((y-15)/35,0,1));
        }else if(d==CapePattern.Design.VOID_SKULL){
            boolean skull=(u*u/100+(v+3)*(v+3)/90<1)||(Math.abs(u)<7&&y>30&&y<39);
            if(skull){boolean eye=Math.pow((Math.abs(u)-4)/2.5,2)+Math.pow((y-27)/2.6,2)<1,nose=Math.abs(u)<1.4&&y>30&&y<33,teeth=y>34&&y<38&&Math.abs(u)%3<1;
                return 0xFF000000|(eye?accent:nose||teeth?0x0D0821:mix(art,0xFFFFFF,.25));}
            if(Math.abs(Math.hypot(u/1.2,v)-15)<.5)return 0xFF000000|accent;
        }else if(d==CapePattern.Design.NOVA){
            double r=Math.hypot(u,v*.8),a=Math.atan2(v,u);
            boolean rays=r<14&&r>3&&Math.abs(Math.sin(a*4))<.15+(14-r)*.015;
            if(r<4||rays||Math.abs(r-11)<.4)return 0xFF000000|mix(accent,0xFFFFFF,r<4?.7:.1);
            if(Math.abs(Math.hypot(u/1.4,v*1.8)-13)<.5)return 0xFF000000|art;
        }else if(d==CapePattern.Design.LOTUS){
            for(int k=-2;k<=2;k++){
                double angle=k*.4,xx=u*Math.cos(angle)+(y-38)*Math.sin(angle),yy=-u*Math.sin(angle)+(y-38)*Math.cos(angle);
                if(Math.pow(xx/(3.2+Math.abs(k)*.4),2)+Math.pow((yy+7)/11,2)<1&&yy<0)return 0xFF000000|mix(accent,art,Math.abs(k)/3d);
            }
            if(Math.abs(u)<13&&y>40&&y<41)return 0xFF000000|accent;
        }
        return 0;
    }
    private static int tint(int a,int b,double amount){return mix(a,b,amount);}
    private static int mix(int a,int b,double n){int c=0;for(int shift:new int[]{16,8,0})c|=(int)(((a>>shift&255)*(1-n)+(b>>shift&255)*n))<<shift;return c;}
    private static int scale(int a,double n){return mix(0,a,n);}
    private static int add(int a,int b,double n){int c=0;for(int shift:new int[]{16,8,0})c|=Math.min(255,(a>>shift&255)+(int)((b>>shift&255)*n))<<shift;return c;}
}
