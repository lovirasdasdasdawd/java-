package dev.donuttracker.fabric;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Distinct controls: actions, navigation, radio cards, switches and labelled sliders. */
final class MenuWidgets {
    enum Kind { SECONDARY, PRIMARY, TAB, CHOICE, NAV }
    private MenuWidgets() {}
    static int alpha(int color) { return 0xFF000000 | color; }
    static void text(GuiGraphics g,String s,int x,int y,int color) { g.drawString(Minecraft.getInstance().font,s,x,y,color,false); }
    static class Action extends Button.Plain {
        final IntSupplier accent;
        final BooleanSupplier selected;
        final Kind kind;
        final int illustration;
        Action(int x,int y,int w,int h,String label,IntSupplier accent,Kind kind,BooleanSupplier selected,int illustration,Runnable action) {
            super(x,y,w,h,Component.literal(label),b -> action.run(),DEFAULT_NARRATION);
            this.accent=accent;this.kind=kind;this.selected=selected;this.illustration=illustration;
        }
        Action(int x,int y,int w,int h,String label,IntSupplier accent,Kind kind,Runnable action) { this(x,y,w,h,label,accent,kind,()->false,-1,action); }
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta) {
            int x=getX(),y=getY(),w=getWidth(),h=getHeight(),a=active?CoastalStyle.controlAccent(accent.getAsInt()):CoastalStyle.line();boolean chosen=selected.getAsBoolean(),hover=isHoveredOrFocused();
            int bg=kind==Kind.PRIMARY ? CoastalStyle.accent() : chosen?CoastalStyle.hover():hover?CoastalStyle.hover():CoastalStyle.surface();
            if(kind==Kind.NAV) {
                if(chosen||hover)TrackerHud.rounded(g,x,y,x+w,y+h,chosen?CoastalStyle.hover():CoastalStyle.surface());
                if(chosen)g.fill(x,y+6,x+2,y+h-6,a);
            } else if(kind==Kind.TAB) {
                if(chosen||hover)TrackerHud.rounded(g,x,y,x+w,y+h,chosen?CoastalStyle.hover():CoastalStyle.surface());
                if(chosen)g.fill(x+8,y+h-2,x+w-8,y+h,a);
            } else {
                TrackerHud.rounded(g,x,y,x+w,y+h,bg);
                TrackerHud.border(g,x,y,x+w,y+h,kind==Kind.PRIMARY?CoastalStyle.accent():chosen||hover?a:CoastalStyle.line());
            }
            if(illustration>=0) miniature(g,x+w/2-13,y+5,a,illustration);
            String label=Minecraft.getInstance().font.plainSubstrByWidth(getMessage().getString(),Math.max(10,w-12));
            int tx=kind==Kind.NAV?x+12:x+(w-Minecraft.getInstance().font.width(label))/2;
            text(g,label,tx,illustration>=0?y+h-12:y+(h-8)/2,!active?0xFF6C7488:kind==Kind.PRIMARY?CoastalStyle.primaryInk():chosen?a:CoastalStyle.ink());
        }
        private static void miniature(GuiGraphics g,int x,int y,int color,int layout) {
            if(layout==0) { g.fill(x,y,x+12,y+4,color);g.fill(x+16,y,x+27,y+2,0xFF8996AB);g.fill(x+16,y+4,x+25,y+6,0xFF8996AB); }
            else if(layout==1) { g.fill(x+5,y,x+22,y+3,color);g.fill(x+8,y+5,x+20,y+6,0xFF8996AB);g.fill(x+5,y+8,x+23,y+9,0xFF8996AB); }
            else { g.fill(x+6,y+2,x+22,y+5,color); }
        }
    }
    static final class Toggle extends Button.Plain {
        private final String label;
        private final BooleanSupplier enabled;
        private final IntSupplier accent;
        Toggle(int x,int y,int w,int h,String label,BooleanSupplier enabled,IntSupplier accent,Runnable action) {
            super(x,y,w,h,Component.literal(label+": "+(enabled.getAsBoolean()?"On":"Off")),b->action.run(),DEFAULT_NARRATION);
            this.label=label;this.enabled=enabled;this.accent=accent;
        }
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta) {
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean on=enabled.getAsBoolean();
            TrackerHud.rounded(g,x,y,x+w,y+h,isHoveredOrFocused()?CoastalStyle.hover():CoastalStyle.surface());
            TrackerHud.border(g,x,y,x+w,y+h,isHoveredOrFocused()?CoastalStyle.controlAccent(accent.getAsInt()):CoastalStyle.line());
            text(g,Minecraft.getInstance().font.plainSubstrByWidth(label,Math.max(30,w-61)),x+9,y+(h-8)/2,CoastalStyle.ink());
            int sx=x+w-42,sy=y+(h-14)/2;
            TrackerHud.rounded(g,sx,sy,sx+33,sy+14,on?CoastalStyle.controlAccent(accent.getAsInt()):CoastalStyle.line());
            TrackerHud.rounded(g,sx+(on?20:2),sy+2,sx+(on?31:13),sy+12,on?CoastalStyle.panel():CoastalStyle.ink());
        }
    }
    static final class Slider extends AbstractSliderButton {
        private final String label;
        private final DoubleConsumer setter;
        private final DoubleFunction<String> format;
        private final IntSupplier accent;
        Slider(int x,int y,int w,int h,String label,double initial,DoubleConsumer setter,DoubleFunction<String> format,IntSupplier accent) {
            super(x,y,w,h,Component.literal(label+": "+format.apply(initial)),initial);
            this.label=label;this.setter=setter;this.format=format;this.accent=accent;
        }
        @Override public void renderWidget(GuiGraphics g,int mx,int my,float delta) {
            int x=getX(),y=getY(),w=getWidth(),h=getHeight(),a=active?CoastalStyle.controlAccent(accent.getAsInt()):CoastalStyle.line();
            TrackerHud.rounded(g,x,y,x+w,y+h,CoastalStyle.surface());
            if(isFocused()||isHoveredOrFocused())TrackerHud.border(g,x,y,x+w,y+h,isFocused()?a:CoastalStyle.line());
            text(g,label,x+9,y+7,CoastalStyle.muted());
            String v=format.apply(value);text(g,v,x+w-9-Minecraft.getInstance().font.width(v),y+7,active?CoastalStyle.ink():CoastalStyle.muted());
            int start=x+12,end=x+w-12,track=y+h-10,knob=start+(int)Math.round((end-start)*value);
            TrackerHud.rounded(g,start,track,end,track+3,CoastalStyle.line());
            if(knob>start)TrackerHud.rounded(g,start,track,knob,track+3,a);
            TrackerHud.rounded(g,knob-3,track-3,knob+4,track+6,active?CoastalStyle.ink():CoastalStyle.line());
        }
        private void pick(double x) { setValue(Math.clamp((x-getX()-12)/Math.max(1,getWidth()-24),0,1)); }
        @Override public void onClick(MouseButtonEvent e,boolean twice) { pick(e.x()); }
        @Override protected void onDrag(MouseButtonEvent e,double dx,double dy) { pick(e.x()); }
        @Override public void onRelease(MouseButtonEvent e) { pick(e.x());super.onRelease(e); }
        @Override protected void updateMessage() { if(format!=null)setMessage(Component.literal(label+": "+format.apply(value))); }
        @Override protected void applyValue() { setter.accept(value); }
    }
}
