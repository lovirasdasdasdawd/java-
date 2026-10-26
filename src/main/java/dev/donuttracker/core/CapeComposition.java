package dev.donuttracker.core;

/** Composition-specific geometry. The all-over designs do not share a frame or icon slot. */
public final class CapeComposition {
    private CapeComposition(){}
    private static int mix(int a,int b,double t){t=Math.clamp(t,0,1);int c=0;for(int k:new int[]{16,8,0})c|=(int)((a>>k&255)*(1-t)+(b>>k&255)*t)<<k;return c;}
    private static int hash(int a,int b){return Math.floorMod(a*1103515245+b*12345+32531,997);}
    public static int cloth(CapePattern.Design d,double x,double y,int background,int accent){
        double amount=switch(d){case PHOENIX->Math.pow(y/64,4)*.16;case TIDAL->.08+y/64*.16;case LOTUS->y>37?.08:.015;case WOLF->Math.abs(y-(x*.75+7))<4?.08:.015;case NOVA->Math.exp(-Math.pow((y-x*.5-14)/8,2))*.12;case RIFT->.02;default->.01;};
        return 0xFF000000|mix(background,accent,amount);
    }
    public static int sample(CapePattern.Design d,double x,double y,int art,int accent,CapeTuning t){
        double u=x-20,v=y-29,p=t.primary(),s=t.secondary(),n=t.density();int c=0;
        if(d==CapePattern.Design.WOLF){
            // Offset lower claw marks replace the repeated border.
            for(int k=0;k<3;k++)if(y>49+k&&y<58+k&&Math.abs(x-(8+k*4+(y-49)*.4))<.35)c=accent;
            int gx=(int)(x/3),gy=(int)(y/3);if(y<22&&hash(gx,gy)<n*160&&Math.hypot(x-gx*3-1.5,y-gy*3-1.5)<.22)c=mix(accent,0xFFFFFF,.3);
        }else if(d==CapePattern.Design.PHOENIX){
            double wing=Math.abs(u)/(.65+p*.75),curve=24-wing*.58+2*Math.sin(wing*.45);
            boolean feather=wing>2&&wing<18&&y>curve&&y<curve+6+2*Math.sin(wing*2);
            boolean tail=y>29&&y<39+s*20&&Math.abs(u)<(40+s*20-y)*.16*(.6+.4*Math.cos(y*.5));
            if(feather||tail||Math.hypot(u-1,y-20)<3||Math.abs(u)<2&&y>20&&y<35)c=mix(accent,art,(y-12)/48);
            if(y>54+3*Math.sin(x*.43)&&y<63&&Math.abs(Math.sin(x*.5))>.45)c=mix(accent,art,.2);
            int gx=(int)(x/2.5),gy=(int)(y/3);if(y>35&&hash(gx,gy)<n*250&&Math.hypot(x-gx*2.5-1,y-gy*3-1)<.3)c=art;
        }else if(d==CapePattern.Design.VOID_SKULL){
            double yy=(y-25)/(.7+p*.6)+25;
            if(u*u/90+(yy-24)*(yy-24)/85<1||Math.abs(u)<6.5&&yy>25&&yy<36){
                boolean eye=Math.pow((Math.abs(u)-4)/(1.4+s*2),2)+Math.pow((yy-24)/(1.5+s*2),2)<1;
                c=eye?accent:Math.abs(u)<1&&yy>27&&yy<30||yy>32&&Math.abs(u)%3<.7?0x070811:mix(0x161323,art,.6);
            }
            for(int k=0;k<(int)(n*10);k++)if(Math.abs(Math.pow((Math.abs(u)-13)/1.4,2)+Math.pow((y-13-k*4)/1.8,2)-1)<.35)c=mix(accent,art,.4);
            if(y<13&&Math.abs(Math.abs(u)-(13-y)*.7)<.4)c=accent;
        }else if(d==CapePattern.Design.NOVA){
            double px=x-26,py=y-20,r=Math.hypot(px,py),planet=3+n*7;
            if(r<planet)c=mix(0x0A1328,art,.4+.4*(1-(px+planet)/(planet*2)));
            double a=(p-.5)*1.5,rx=px*Math.cos(a)+py*Math.sin(a),ry=-px*Math.sin(a)+py*Math.cos(a);
            if(Math.abs(Math.hypot(rx/1.8,ry*2)-planet*1.8)<.35)c=mix(accent,0xFFFFFF,.3);
            int gx=(int)(x/2),gy=(int)(y/2);if(hash(gx,gy)<s*330&&Math.hypot(x-gx*2-1,y-gy*2-1)<.2)c=art;
            if(y>39&&Math.abs(y-(47+x*.3))<.3)c=mix(accent,art,.4);
        }else if(d==CapePattern.Design.LOTUS){
            int petals=4+(int)(p*7);for(int k=0;k<petals;k++){
                double angle=(k-(petals-1)/2d)*(.2+s*.25),xx=u*Math.cos(angle)+(y-47)*Math.sin(angle),yy=-u*Math.sin(angle)+(y-47)*Math.cos(angle);
                if(xx*xx/8+Math.pow((yy+7)/10,2)<1&&yy<0)c=mix(art,accent,k/(double)petals);
            }
            for(int k=0;k<1+(int)(n*6);k++)if(Math.abs(Math.hypot(u,(y-48)*3)-(7+k*4))<.35)c=mix(accent,art,.2);
        }else if(d==CapePattern.Design.DRAGON){
            double center=20+9*Math.sin((y-10)/48*Math.PI*(1+p*2));
            if(y>14&&y<56&&Math.abs(x-center)<1.4+(56-y)*.025)c=mix(art,accent,y/64);
            if(Math.pow((x-14)/4,2)+Math.pow((y-15)/3,2)<1||x>7&&x<14&&y>13&&y<16||y>8&&y<14&&Math.abs(x-12)<.65)c=art;
            if(Math.hypot(x-14,y-13)<.8)c=0x080C16;
            if(y>20&&y<51&&Math.abs(x-center+2.2)<1.5&&y%5<1)c=accent;
            double wing=(x-20)/(.5+s);if(x>20&&wing<17&&y>20+wing*.2&&y<35-wing*.6&&Math.sin(wing*1.1)>.1)c=mix(art,accent,.4);
            int gy=(int)((y-8)/6);if(x>35&&x<38&&y>8&&y<56&&gy<n*8&&Math.abs(Math.abs(x-36.5)-Math.abs((y-8)%6-3)*.4)<.3)c=accent;
        }else if(d==CapePattern.Design.TIDAL){
            int layers=2+(int)(s*6);double height=1+p*7;
            for(int k=0;k<layers;k++){
                double wave=12+k*(45d/layers)+height*Math.sin(x*.15+k*1.4)+Math.sin(x*.4+k);
                if(y>wave)c=mix(0x071B31,accent,.12+k/(double)layers*.22);
                if(Math.abs(y-wave)<.45)c=mix(art,accent,.5);
                if(y>wave&&y<wave+2&&hash((int)(x*2),k)<n*700)c=art;
            }
        }else if(d==CapePattern.Design.RIFT){
            double center=19+5*Math.sin(y*.2)+2*Math.sin(y*.73),width=.3+p*1.8;
            if(Math.abs(x-center)<width)c=mix(art,0xFFFFFF,.5);
            else if(Math.abs(x-center)<width+1)c=accent;
            int branches=2+(int)(s*8);
            for(int k=0;k<branches;k++){double yy=6+k*(52d/branches),branch=yy+Math.abs(x-center)*(.5+(k%2)*.2);if(Math.abs(y-branch)<.3&&Math.abs(x-center)<8+k%6)c=accent;}
            int gx=(int)(x/4),gy=(int)(y/5);if(hash(gx,gy)<n*210&&Math.abs(x-gx*4-2)+Math.abs(y-gy*5-2)<1)c=art;
        }
        return c==0?0:0xFF000000|c;
    }
}
