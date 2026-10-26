package dev.donuttracker.core;

/** High-resolution cape assembly and a real spatial emissive halo; no frame-time effects. */
public final class CapeStudio {
    public static final int SCALE=4,WIDTH=1024,HEIGHT=512,FACE_W=160,FACE_H=256;
    public record Textures(int[] cloth,int[] glow,int[] preview){}
    private CapeStudio(){}
    public static Textures create(CapePattern.Design design,int cloth,int art,int accent,double strength,double aura,boolean enabled,int[] wolf,int wolfW,int wolfH){
        return create(design,cloth,art,accent,strength,aura,enabled,wolf,wolfW,wolfH,CapeTuning.DEFAULT);
    }
    public static Textures create(CapePattern.Design design,int cloth,int art,int accent,double strength,double aura,boolean enabled,int[] wolf,int wolfW,int wolfH,CapeTuning tuning){
        var low=CapePattern.pixels(design,cloth,art,accent,1,false);
        var lowGlow=CapePattern.pixels(design,cloth,art,accent,1,true);
        int[] base=new int[WIDTH*HEIGHT],face=new int[FACE_W*FACE_H],mask=new int[face.length];
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++)base[y*WIDTH+x]=low[(y/SCALE)*CapePattern.WIDTH+x/SCALE];
        for(int y=0;y<FACE_H;y++)for(int x=0;x<FACE_W;x++){
            int i=y*FACE_W+x;double fx=x/4d,fy=y/4d;
            if(design.ordinal()<3){
                int sx=(int)Math.clamp((fx-20)/(.7+tuning.primary()*.6)+20,0,39),sy=(int)Math.clamp((fy-32)/(.7+tuning.secondary()*.6)+32,0,63);
                face[i]=low[(sy+4)*CapePattern.WIDTH+sx+4];mask[i]=scale(lowGlow[(sy+4)*CapePattern.WIDTH+sx+4]&0xFFFFFF,.4+tuning.density()*1.2);
            }else{face[i]=CapeComposition.cloth(design,fx,fy,cloth,accent);mask[i]=0;}
            int pixel=design.ordinal()>=3?CapeComposition.sample(design,fx,fy,art,accent,tuning):0;
            if(design==CapePattern.Design.WOLF&&wolf!=null){
                if(pixel!=0){face[i]=pixel;mask[i]=pixel&0xFFFFFF;}
                pixel=0;double size=112+tuning.primary()*48,startX=(FACE_W-size)/2+8,startY=18+tuning.secondary()*76;
                int wx=(int)((x-startX)*wolfW/size),wy=(int)((y-startY)*wolfH/size);
                if(x>=startX&&y>=startY&&wx>=0&&wx<wolfW&&wy>=0&&wy<wolfH)pixel=wolf[wy*wolfW+wx];
            }
            int alpha=pixel>>>24;
            if(alpha>0){
                int tinted=tint(pixel&0xFFFFFF,art,design==CapePattern.Design.WOLF?.12:0);
                face[i]=0xFF000000|mix(face[i],tinted,alpha/255d);
                // The complete bright artwork emits, not just the stitched border.
                double luminance=((tinted>>16&255)*.22+(tinted>>8&255)*.65+(tinted&255)*.13)/255d;
                mask[i]=scale(tinted,alpha/255d*Math.min(1,luminance*1.8)*CapeComposition.emissionWeight(design,tinted,fx,fy,tuning));
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
    private static int tint(int a,int b,double amount){return mix(a,b,amount);}
    private static int mix(int a,int b,double n){int c=0;for(int shift:new int[]{16,8,0})c|=(int)(((a>>shift&255)*(1-n)+(b>>shift&255)*n))<<shift;return c;}
    private static int scale(int a,double n){int c=0;for(int k:new int[]{16,8,0})c|=Math.clamp((int)((a>>k&255)*n),0,255)<<k;return c;}
    private static int add(int a,int b,double n){int c=0;for(int shift:new int[]{16,8,0})c|=Math.min(255,(a>>shift&255)+(int)((b>>shift&255)*n))<<shift;return c;}
}
