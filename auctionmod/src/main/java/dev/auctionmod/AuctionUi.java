package dev.auctionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.function.DoubleConsumer;
import net.minecraft.network.chat.Component;

final class AuctionUi {
    static final int CYAN=0xFF6EDDED,INK=0xFFEDF1F5,MUTED=0xFFA0A8B5,YELLOW=0xFFF0D66D;
    static void text(GuiGraphics g,String s,int x,int y,int c){g.drawString(Minecraft.getInstance().font,s,x,y,c,false);}
    static void round(GuiGraphics g,int x,int y,int w,int h,int c){
        int radius=Math.max(0,Math.min(10,Math.min(w/2,h/2)));
        for(int row=0;row<h;row++){
            double dy=row<radius?radius-row-.5:row>=h-radius?row-(h-radius)+.5:0;
            int inset=dy==0?0:Math.max(0,(int)Math.ceil(radius-Math.sqrt(radius*radius-dy*dy)));
            if(w>2*inset)g.fill(x+inset,y+row,x+w-inset,y+row+1,c);
        }
    }
    static void panel(GuiGraphics g,int x,int y,int w,int h,boolean glass){round(g,x,y,w,h,glass?0x3040536A:0xFF40536A);round(g,x+1,y+1,w-2,h-2,glass?0x9A151E2B:0xFF111925);g.fill(x+6,y+1,x+w-6,y+2,glass?0x807FC2CF:0xFF293C50);}
    static final class Action extends Button.Plain {
        private final boolean primary;
        Action(int x,int y,int w,int h,String text,boolean primary,Runnable action){super(x,y,w,h,Component.literal(text),b->action.run(),DEFAULT_NARRATION);this.primary=primary;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){int x=getX(),y=getY(),w=getWidth(),h=getHeight();round(g,x,y,w,h,!active?0xFF24303F:primary?isHoveredOrFocused()?0xFF91E8EF:CYAN:isHoveredOrFocused()?0xFF33485B:0xFF243346);String s=Minecraft.getInstance().font.plainSubstrByWidth(getMessage().getString(),w-10);text(g,s,x+(w-Minecraft.getInstance().font.width(s))/2,y+(h-8)/2,!active?0xFF748090:primary?0xFF102631:INK);}
    }
    static final class ScaleSlider extends AbstractSliderButton {
        private final DoubleConsumer setter;
        ScaleSlider(int x,int y,int w,int h,double scale,DoubleConsumer setter){super(x,y,w,h,Component.literal("HUD size"),(scale-.4)/1.2);this.setter=setter;updateMessage();}
        @Override protected void updateMessage(){setMessage(Component.literal("HUD size: "+Math.round((.4+value*1.2)*100)+"%"));}
        @Override protected void applyValue(){setter.accept(.4+value*1.2);}
        @Override public void renderWidget(GuiGraphics g,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth();round(g,x,y,w,getHeight(),0xFF243346);text(g,getMessage().getString(),x+9,y+6,INK);int start=x+12,end=x+w-12,yy=y+getHeight()-9,knob=start+(int)((end-start)*value);g.fill(start,yy,end,yy+2,0xFF47566A);g.fill(start,yy,knob,yy+2,CYAN);round(g,knob-3,yy-3,7,8,INK);
        }
        private void pick(double x){setValue(Math.clamp((x-getX()-12)/Math.max(1,getWidth()-24),0,1));}
        @Override public void onClick(MouseButtonEvent e,boolean twice){pick(e.x());}
        @Override protected void onDrag(MouseButtonEvent e,double dx,double dy){pick(e.x());}
        @Override public void onRelease(MouseButtonEvent e){pick(e.x());super.onRelease(e);}
    }

}
