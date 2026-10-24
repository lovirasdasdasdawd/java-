package dev.auctionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

final class AuctionUi {
    static final int CYAN=0xFF6EDDED,INK=0xFFEDF1F5,MUTED=0xFFA0A8B5,YELLOW=0xFFF0D66D;
    static void text(GuiGraphics g,String s,int x,int y,int c){g.drawString(Minecraft.getInstance().font,s,x,y,c,false);}
    static void round(GuiGraphics g,int x,int y,int w,int h,int c){g.fill(x+3,y,x+w-3,y+h,c);g.fill(x+1,y+1,x+w-1,y+h-1,c);g.fill(x,y+3,x+w,y+h-3,c);}
    static void panel(GuiGraphics g,int x,int y,int w,int h,boolean glass){round(g,x,y,w,h,glass?0x3040536A:0xFF40536A);round(g,x+1,y+1,w-2,h-2,glass?0x9A151E2B:0xFF111925);g.fill(x+6,y+1,x+w-6,y+2,glass?0x807FC2CF:0xFF293C50);}
    static final class Action extends Button.Plain {
        private final boolean primary;
        Action(int x,int y,int w,int h,String text,boolean primary,Runnable action){super(x,y,w,h,Component.literal(text),b->action.run(),DEFAULT_NARRATION);this.primary=primary;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){int x=getX(),y=getY(),w=getWidth(),h=getHeight();round(g,x,y,w,h,!active?0xFF24303F:primary?isHoveredOrFocused()?0xFF91E8EF:CYAN:isHoveredOrFocused()?0xFF33485B:0xFF243346);String s=Minecraft.getInstance().font.plainSubstrByWidth(getMessage().getString(),w-10);text(g,s,x+(w-Minecraft.getInstance().font.width(s))/2,y+(h-8)/2,!active?0xFF748090:primary?0xFF102631:INK);}
    }
}
