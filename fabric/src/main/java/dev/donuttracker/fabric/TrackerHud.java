package dev.donuttracker.fabric;

import dev.donuttracker.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;

/** Compact pixel instruments. Geometry is shared with placement and hit testing. */
final class TrackerHud {
    private TrackerHud(){}
    static int height(Settings.Hud h){return h.layout==Settings.Layout.MINIMAL?29:h.layout==Settings.Layout.STACKED?94:68;}
    static int width(){return 180;}
    static int x(Settings.Hud h,int sw){return Math.max(0,Math.min(h.x,sw-(int)(width()*h.scale)));}
    static int y(Settings.Hud h,int sh){return Math.max(0,Math.min(h.y,sh-(int)(height(h)*h.scale)));}
    static void render(GuiGraphics g,DonutTrackerClient mod,boolean editing){draw(g,mod,mod.settings.profit,false,editing);draw(g,mod,mod.settings.queue,true,editing);}
    static void draw(GuiGraphics g,DonutTrackerClient mod,Settings.Hud h,boolean payment,boolean editing){
        if(!h.enabled&&!editing)return;
        g.pose().pushMatrix();g.pose().translate((float)x(h,g.guiWidth()),(float)y(h,g.guiHeight()));g.pose().scale((float)h.scale,(float)h.scale);
        int w=width(),ht=height(h),ink=opaque(h.text),muted=blend(h.text,h.background,.56f),accent=opaque(h.accent);
        if(h.theme==Settings.Theme.NEON)for(int n=3;n>0;n--)border(g,-n,-n,w+n,ht+n,0x14000000|h.accent);
        g.fill(2,ht,w+2,ht+2,((int)(65*h.opacity)<<24));
        g.fill(0,0,w,ht,((int)((h.theme==Settings.Theme.GLASS?145:255)*h.opacity)<<24)|h.background);
        border(g,0,0,w,ht,((int)(160*h.opacity)<<24)|(blend(h.text,h.background,.22f)&0xFFFFFF));
        g.fill(0,0,2,ht,accent);g.fill(2,0,w,1,((int)(100*h.opacity)<<24)|h.accent);
        if(h.layout==Settings.Layout.MINIMAL){
            if(h.showIcon){if(payment)people(g,8,8,accent);else donut(g,8,8,accent);}
            int start=h.showIcon?26:9;
            if(payment){var a=mod.queue.active();fit(g,a==null?"Queue empty":a.player(),start,10,.85f,70,ink);if(a!=null)right(g,"+"+Money.format(a.amount()),w-8,10,Math.min(.9f,65f/Math.max(1,Minecraft.getInstance().font.width("+"+Money.format(a.amount())))),opaque(h.positive));}
            else {if(h.showProfit)fit(g,Money.signed(mod.tracker.profit()),start,9,1.2f,90,opaque(mod.tracker.profit().signum()<0?h.negative:h.positive));if(h.showTimer)right(g,mod.tracker.duration(),w-8,11,.75f,muted);}
            g.pose().popMatrix();return;
        }
        if(h.showIcon){if(payment)people(g,8,7,accent);else donut(g,8,6,accent);}
        scaled(g,payment?"PAYMENTS":"SESSION",h.showIcon?26:9,8,.75f,muted);
        right(g,payment?mod.queue.waiting()+" waiting":mod.tracker.running()?"REC":"IDLE",w-9,8,.75f,payment?muted:mod.tracker.running()?accent:muted);
        if(payment)payment(g,mod,h,ink,muted,accent);else profit(g,mod,h,ink,muted);
        g.pose().popMatrix();
    }
    private static void profit(GuiGraphics g,DonutTrackerClient mod,Settings.Hud h,int ink,int muted){
        int colour=opaque(mod.tracker.profit().signum()<0?h.negative:h.positive);
        if(h.showProfit)fit(g,Money.signed(mod.tracker.profit()),9,25,1.8f,162,colour);
        g.fill(9,48,171,49,blend(h.text,h.background,.18f));
        if(h.showTimer)scaled(g,mod.tracker.duration(),9,55,.8f,ink);
        if(h.showRate)right(g,Money.signed(mod.tracker.perHour())+"/h",171,55,.8f,mod.tracker.perHour().signum()<0?opaque(h.negative):muted);
        if(h.layout==Settings.Layout.STACKED){scaled(g,"Balance",9,78,.8f,muted);right(g,mod.tracker.hasBalance()?Money.format(mod.tracker.balance()):"Not read",171,78,.8f,ink);}
    }
    private static void payment(GuiGraphics g,DonutTrackerClient mod,Settings.Hud h,int ink,int muted,int accent){
        var a=mod.queue.active();
        if(a==null){scaled(g,"Queue empty",9,27,1f,ink);scaled(g,"Ready for the next player",9,55,.7f,muted);}
        else {
            int start=9;
            if(h.showIcon){var cn=Minecraft.getInstance().getConnection();var info=cn==null?null:cn.getPlayerInfo(a.player());g.fill(9,23,33,47,blend(h.text,h.background,.12f));if(info!=null)PlayerFaceRenderer.draw(g,info.getSkin(),10,24,22);else centered(g,a.player().substring(0,1),21,30,1f,accent);start=40;}
            fit(g,a.player(),start,25,.9f,130,ink);fit(g,"+"+Money.format(a.amount()),start,37,1f,130,opaque(h.positive));
            var list=mod.queue.all();g.fill(9,49,171,50,blend(h.text,h.background,.18f));
            if(list.size()>1){fit(g,"Next: "+list.get(1).player(),9,55,.75f,105,muted);right(g,Money.format(list.get(1).amount()),171,55,.75f,ink);}else scaled(g,"No players waiting",9,55,.75f,muted);
            if(h.layout==Settings.Layout.STACKED)for(int i=2;i<Math.min(4,list.size());i++){var next=list.get(i);scaled(g,next.player(),9,72+(i-2)*11,.75f,muted);right(g,Money.format(next.amount()),171,72+(i-2)*11,.75f,ink);}
        }
    }
    private static void fit(GuiGraphics g,String s,int x,int y,float max,int width,int c){scaled(g,s,x,y,Math.min(max,width/(float)Math.max(1,Minecraft.getInstance().font.width(s))),c);}
    private static int opaque(int c){return 0xFF000000|c;}
    private static int blend(int a,int b,float n){return CoastalStyle.mix(0xFF000000|b,0xFF000000|a,n);}
    static void text(GuiGraphics g, String s, int x, int y, int color) { g.drawString(Minecraft.getInstance().font, s, x, y, color, false); }
    static void scaled(GuiGraphics g, String s, int x, int y, float scale, int color) {
        g.pose().pushMatrix(); g.pose().translate((float)x, (float)y); g.pose().scale(scale, scale);
        text(g, s, 0, 0, color); g.pose().popMatrix();
    }
    private static void centered(GuiGraphics g, String s, int x, int y, float scale, int color) { scaled(g, s, x - Math.round(Minecraft.getInstance().font.width(s) * scale / 2), y, scale, color); }
    private static void right(GuiGraphics g, String s, int x, int y, float scale, int color) { scaled(g, s, x - Math.round(Minecraft.getInstance().font.width(s) * scale), y, scale, color); }
    private static int inset(int row, int height, int radius) {
        double distance = row < radius ? radius - row - .5 : row >= height - radius ? row - (height - radius) + .5 : 0;
        return distance == 0 ? 0 : Math.max(0, (int)Math.ceil(radius - Math.sqrt(radius * radius - distance * distance)));
    }
    static void rounded(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int ht = y2 - y1, radius = Math.max(0, Math.min(1, Math.min((x2-x1)/2, ht/2)));
        for (int y = 0; y < ht; y++) { int dx = inset(y, ht, radius); g.fill(x1 + dx, y1+y, x2-dx, y1+y+1, color); }
    }
    static void border(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int ht = y2-y1, radius = Math.max(0, Math.min(1, Math.min((x2-x1)/2, ht/2)));
        for (int y = 0; y < ht; y++) {
            int dx = inset(y, ht, radius);
            if (y == 0 || y == ht-1) g.fill(x1+dx,y1+y,x2-dx,y1+y+1,color);
            else { int inner = 1 + inset(y-1,ht-2,Math.max(0,radius-1)); g.fill(x1+dx,y1+y,x1+inner,y1+y+1,color); g.fill(x2-inner,y1+y,x2-dx,y1+y+1,color); }
        }
    }
    private static void donut(GuiGraphics g,int x,int y,int c){border(g,x,y,x+11,y+11,c);g.fill(x+5,y+4,x+7,y+7,c);}
    private static void people(GuiGraphics g, int x, int y, int c) { rounded(g,x+1,y,x+5,y+4,c); rounded(g,x+7,y+1,x+11,y+5,c); rounded(g,x-1,y+6,x+7,y+10,c); rounded(g,x+7,y+7,x+13,y+10,c); }
    private static void clock(GuiGraphics g, int x, int y, int c) { border(g,x,y,x+9,y+9,c); g.fill(x+4,y+2,x+5,y+5,c); g.fill(x+4,y+4,x+7,y+5,c); }
}
