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
    public static double emissionWeight(CapePattern.Design d,int color,double x,double y,CapeTuning t){
        int r=color>>16&255,g=color>>8&255,b=color&255;
        if(d==CapePattern.Design.CHRISTMAS_TREE&&g>r*1.25&&g>b*1.1)return .12;
        if(d==CapePattern.Design.WINTER_NIGHT&&y>50-t.secondary()*9+3*Math.sin(x*.13))return .18;
        if(d==CapePattern.Design.GINGERBREAD&&!(r>235&&g>210&&b>170)&&!(r>g*1.5&&r>200))return .08;
        return 1;
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
        else if(d==CapePattern.Design.CHRISTMAS_TREE){
            double height=26+p*18,top=49-height,level=(y-top)/height;
            double branch=Math.max(0,level)*13*(.7+.3*((level*4)%1));
            if(y>top&&y<49&&Math.abs(u)<branch){
                c=mix(0x082B1A,art,.35+.2*(1-level));
                double wire=top+height*.2+((x+20)%9)*.7+(int)((y-top)/(height/4))*(height/4);
                if(Math.abs(y-wire)<.3)c=mix(art,0xF4E6A6,.4);
                int gx=(int)(x/2),gy=(int)(y/2);if(hash(gx,gy)<s*650&&Math.hypot(x-gx*2-1,y-gy*2-1)<.45)c=(gx+gy)%2==0?accent:0xFFE5A0;
            }
            if(Math.abs(u)<1.5&&y>48&&y<54)c=0x976135;
            if(Math.abs(u)<.45&&Math.abs(y-(top-2))<2.5||Math.abs(y-(top-2))<.45&&Math.abs(u)<2)c=0xFFEBA8;
            int gifts=1+(int)(n*5);for(int k=0;k<gifts;k++){double gx=5+k*6,gy=55-k%2*2;if(x>gx&&x<gx+5&&y>gy&&y<gy+5)c=Math.abs(x-gx-2.5)<.4||Math.abs(y-gy-2)<.4?0xFFEBA8:accent;}
        }else if(d==CapePattern.Design.WINTER_NIGHT){
            double moon=2+n*5,mx=x-30,my=y-11;
            if(mx*mx+my*my<moon*moon)c=art;
            if((mx-1.8)*(mx-1.8)+(my-1.2)*(my-1.2)<moon*moon*.65&&mx*mx+my*my<moon*moon)c=0x0A1730;
            double ridge=49-s*9+3*Math.sin(x*.13);
            if(y>ridge)c=mix(0x254B69,art,.3+(y-ridge)/40);
            if(Math.abs(y-ridge)<.45)c=art;
            int gx=(int)(x/2),gy=(int)(y/2.5);if(y<ridge&&hash(gx,gy)<p*500&&Math.hypot(x-gx*2-1,y-gy*2.5-1)<.25+.25*(gy%3==0?1:0))c=art;
            for(int k=0;k<4;k++){double tx=3+k*11,ty=ridge-4-k%2*5;if(y>ty&&y<ridge&&Math.abs(x-tx)<(y-ty)*.25)c=0x0A2430;}
        }else if(d==CapePattern.Design.GINGERBREAD){
            double scale=.7+p*.6,xx=u/scale,yy=(y-30)/scale+30,line=.2+s*.8;
            boolean head=xx*xx+(yy-18)*(yy-18)<36,body=Math.abs(xx)<5.5&&yy>23&&yy<42;
            boolean arms=Math.abs(xx)>4&&Math.abs(xx)<13&&Math.abs(yy-(27+Math.abs(xx)*.4))<2.3;
            boolean legs=yy>40&&yy<53&&(Math.abs(xx-3.4)<2.6||Math.abs(xx+3.4)<2.6);
            if(head||body||arms||legs)c=mix(0x713A22,art,.6);
            if(head&&Math.abs(Math.hypot(xx,yy-18)-5)<line||body&&(Math.abs(Math.abs(xx)-4.5)<line||Math.abs(yy-25)<line)||legs&&Math.abs(yy-51)<line)c=0xFFF0CF;
            if(head&&(Math.hypot(Math.abs(xx)-2,yy-17)<.6||Math.abs(yy-(21-.12*xx*xx))<line*.7&&Math.abs(xx)<2.2))c=0x3B1821;
            int buttons=2+(int)(n*4);for(int k=0;k<buttons;k++)if(Math.hypot(xx,yy-28-k*12d/(buttons-1))<.8)c=accent;
        }
        return c==0?0:0xFF000000|c;
    }
}
