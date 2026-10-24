package dev.donuttracker.fabric;

import dev.donuttracker.core.Money;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Session overview. Its drawing and controls are intentionally local to this screen. */
final class TrackerScreen extends Screen {
    private static final int INK=0xFFEAF0F1, MUTED=0xFF89959F, LINE=0xFF2D363E;
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left,top,panelWidth,panelHeight,bodyY,bodyHeight,trackerWidth,queueX,queueWidth;
    TrackerScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Donut Tracker"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        panelWidth=Math.min(620,width-20);panelHeight=Math.min(240,height-16);
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;bodyY=top+57;bodyHeight=panelHeight-108;
        trackerWidth=(panelWidth-44)*55/100;queueX=left+trackerWidth+28;queueWidth=panelWidth-trackerWidth-44;
        control(left+16,bodyY+bodyHeight-24,trackerWidth,23,"Tracker settings",3,()->minecraft.setScreen(new ModuleSettingsScreen(mod,this,false)));
        control(queueX,bodyY+bodyHeight-24,queueWidth,23,"Queue settings",3,()->minecraft.setScreen(new ModuleSettingsScreen(mod,this,true)));
        int actionWidth=Math.min(150,(panelWidth-44)/2);
        control(left+16,top+panelHeight-36,actionWidth,24,mod.tracker.running()?"End session":"Start session",0,()->{mod.sessionCommand(!mod.tracker.running());rebuildWidgets();});
        control(left+24+actionWidth,top+panelHeight-36,actionWidth,24,"Arrange HUDs",1,()->minecraft.setScreen(new HudEditorScreen(mod,this)));
        control(left+panelWidth-103,top+17,87,21,"Back to game",2,this::onClose);
    }
    private void control(int x,int y,int w,int h,String label,int kind,Runnable action){addRenderableWidget(new OverviewButton(x,y,w,h,label,kind,action));}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xB8080B0F);
        TrackerHud.rounded(g,left-2,top+4,left+panelWidth+2,top+panelHeight+5,0x55000000);
        TrackerHud.rounded(g,left,top,left+panelWidth,top+panelHeight,0xFC11171D);
        TrackerHud.border(g,left,top,left+panelWidth,top+panelHeight,0xFF343E46);
        TrackerHud.rounded(g,left+16,top+15,left+40,top+39,0xFF242A34);
        ring(g,left+22,top+21,0xFF000000|mod.settings.profit.accent);
        label(g,"Donut Tracker",left+49,top+15,1.05f,INK);
        label(g,"SESSION OVERVIEW",left+49,top+30,.6f,MUTED);
        g.fill(left+16,top+47,left+panelWidth-16,top+48,LINE);
        card(g,left+16,trackerWidth);card(g,queueX,queueWidth);
        tracker(g);queue(g);
        if(panelWidth>=520)label(g,mod.tracker.running()?"Session running":"Session inactive",left+panelWidth-154,top+panelHeight-26,.75f,MUTED);
        super.render(g,mx,my,delta);
    }
    private void card(GuiGraphics g,int x,int w){
        TrackerHud.rounded(g,x,bodyY,x+w,bodyY+bodyHeight,0xFF192128);
        TrackerHud.border(g,x,bodyY,x+w,bodyY+bodyHeight,0xFF303B43);
    }
    private void tracker(GuiGraphics g){
        int x=left+29,w=trackerWidth-26;
        label(g,"NET PROFIT",x,bodyY+12,.7f,MUTED);
        String state=mod.tracker.running()?"LIVE":"IDLE";
        int stateWidth=Math.round(font.width(state)*.65f);
        g.fill(x+w-stateWidth-8,bodyY+13,x+w-stateWidth-5,bodyY+16,mod.tracker.running()?0xFF000000|mod.settings.profit.positive:0xFF687680);
        label(g,state,x+w-stateWidth,bodyY+12,.65f,MUTED);
        String amount=Money.signed(mod.tracker.profit());
        int profitColour=0xFF000000|(mod.tracker.profit().signum()<0?mod.settings.profit.negative:mod.settings.profit.positive);
        label(g,amount,x,bodyY+29,Math.min(2.8f,w/(float)Math.max(1,font.width(amount))),profitColour);
        int metricY=bodyY+bodyHeight-58,mid=x+w/2+4;
        label(g,"DURATION",x,metricY,.58f,MUTED);
        label(g,mod.tracker.duration(),x,metricY+12,.85f,INK);
        g.fill(mid-9,metricY,mid-8,metricY+21,LINE);
        label(g,"PER HOUR",mid,metricY,.58f,MUTED);
        String rate=Money.signed(mod.tracker.perHour());
        float rateScale=Math.min(.85f,(w/2-4)/(float)Math.max(1,font.width(rate)));
        label(g,rate,mid,metricY+12,rateScale,mod.tracker.perHour().signum()<0?profitColour:INK);
    }
    private void queue(GuiGraphics g){
        int x=queueX+13,w=queueWidth-26,accent=0xFF000000|mod.settings.queue.accent;
        label(g,"PAYMENT QUEUE",x,bodyY+12,.7f,MUTED);
        var active=mod.queue.active();
        if(active==null){
            TrackerHud.rounded(g,x,bodyY+31,x+25,bodyY+56,0xFF242F36);
            // A quiet empty-state inbox, rather than an unexplained dash.
            g.fill(x+6,bodyY+38,x+19,bodyY+39,0xFF647580);
            g.fill(x+5,bodyY+39,x+6,bodyY+49,0xFF647580);g.fill(x+19,bodyY+39,x+20,bodyY+49,0xFF647580);
            g.fill(x+6,bodyY+49,x+19,bodyY+50,0xFF647580);g.fill(x+10,bodyY+45,x+15,bodyY+46,0xFF647580);
            label(g,"No payments yet",x+33,bodyY+33,Math.min(.9f,(w-33f)/Math.max(1,font.width("No payments yet"))),INK);
            label(g,"Waiting for a payment",x+33,bodyY+47,Math.min(.65f,(w-33f)/Math.max(1,font.width("Waiting for a payment"))),MUTED);
        }else{
            TrackerHud.rounded(g,x,bodyY+30,x+26,bodyY+56,0xFF26333C);
            var connection=minecraft.getConnection();var info=connection==null?null:connection.getPlayerInfo(active.player());
            if(info!=null)PlayerFaceRenderer.draw(g,info.getSkin(),x+2,bodyY+32,22);
            else label(g,active.player().substring(0,1),x+8,bodyY+38,1.2f,accent);
            label(g,font.plainSubstrByWidth(active.player(),Math.max(1,(int)((w-34)/.85f))),x+34,bodyY+31,.85f,INK);
            String amount="+"+Money.format(active.amount());
            label(g,amount,x+34,bodyY+45,Math.min(1.25f,(w-34f)/Math.max(1,font.width(amount))),0xFF000000|mod.settings.queue.positive);
        }
        int y=bodyY+bodyHeight-57;
        label(g,mod.queue.waiting()+" waiting",x,y,.75f,INK);
        String next=mod.queue.waiting()==0?"Queue is clear":"Next: "+mod.queue.all().get(1).player();
        label(g,font.plainSubstrByWidth(next,Math.max(1,(int)(w/.65f))),x,y+13,.65f,MUTED);
    }
    private void label(GuiGraphics g,String text,int x,int y,float scale,int colour){
        g.pose().pushMatrix();g.pose().translate((float)x,(float)y);g.pose().scale(scale,scale);
        g.drawString(font,text,0,0,colour,false);g.pose().popMatrix();
    }
    private void ring(GuiGraphics g,int x,int y,int colour){
        TrackerHud.border(g,x,y,x+12,y+12,colour);g.fill(x+5,y+4,x+7,y+7,colour);g.fill(x+8,y+1,x+10,y+3,0xFF242A34);
    }
    private final class OverviewButton extends Button.Plain {
        private final int kind;
        OverviewButton(int x,int y,int w,int h,String text,int kind,Runnable action){super(x,y,w,h,Component.literal(text),button->action.run(),DEFAULT_NARRATION);this.kind=kind;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean hover=isHoveredOrFocused();
            if(kind==0){
                int colour=mod.tracker.running()?(hover?0xFFF1B4B9:0xFFE6A3A9):(hover?0xFFBCEBDD:0xFFA7DCCB);
                TrackerHud.rounded(g,x,y,x+w,y+h,colour);
                if(mod.tracker.running())g.fill(x+11,y+9,x+17,y+15,0xFF1A2926);
                else for(int i=0;i<6;i++)g.fill(x+11+i,y+8+i/2,x+12+i,y+16-i/2,0xFF1A2926);
            }else if(kind==1){
                TrackerHud.rounded(g,x,y,x+w,y+h,hover?0xFF2B3740:0xFF1B252D);
                TrackerHud.border(g,x,y,x+w,y+h,hover?0xFF80959F:0xFF43515B);
                g.fill(x+11,y+8,x+12,y+17,MUTED);g.fill(x+7,y+12,x+16,y+13,MUTED);
            }else if(kind==3){
                g.fill(x+12,y,x+w-12,y+1,LINE);
                if(hover)TrackerHud.rounded(g,x+3,y+2,x+w-3,y+h-2,0xFF26333C);
                label(g,getMessage().getString(),x+13,y+8,.8f,hover?INK:MUTED);
                label(g,">",x+w-20,y+8,.8f,hover?0xFF000000|mod.settings.profit.accent:MUTED);return;
            }
            String text=getMessage().getString();float scale=kind==2?.75f:.85f;
            int textX=kind==2?x+(w-Math.round(font.width(text)*scale))/2:x+24+(w-24-Math.round(font.width(text)*scale))/2;
            label(g,text,textX,y+(h-7)/2,scale,kind==0?0xFF152821:hover?INK:MUTED);
            if(kind==2&&hover)g.fill(textX,y+h-3,textX+Math.round(font.width(text)*scale),y+h-2,MUTED);
        }
    }
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
