package dev.donuttracker.fabric;

import java.awt.Color;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import dev.donuttracker.core.Settings;

final class ColourPickerScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private final String label;
    private final IntConsumer setter;
    private float hue, saturation, brightness;
    private int left, top, size, dragging;
    private EditBox hex;
    ColourPickerScreen(DonutTrackerClient mod, Screen parent, String label, int colour, IntConsumer setter) {
        super(Component.literal("Choose " + label + " colour")); this.mod = mod; this.parent = parent; this.label = label; this.setter = setter;
        float[] hsv = Color.RGBtoHSB(colour >> 16 & 255, colour >> 8 & 255, colour & 255, null); hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2];
    }
    private int colour() { return Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF; }
    @Override public boolean isPauseScreen() { return false; }
    @Override protected void init() {
        size = Math.min(150, Math.min(height - 115, width - 160)); size = Math.max(60, size);
        left = (width - size - 124) / 2; top = Math.max(30, (height - size - 75) / 2);
        hex = new EditBox(font, left + size + 28, top + 53, 90, 20, Component.literal("HEX colour")); hex.setMaxLength(7); hex.setValue(String.format("#%06X", colour())); addRenderableWidget(hex);
        addRenderableWidget(Button.builder(Component.literal("Use HEX"), b -> mod.act(() -> { int c = Settings.color(hex.getValue()); float[] hsv = Color.RGBtoHSB(c >> 16 & 255, c >> 8 & 255, c & 255, null); hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2]; apply(); })).bounds(left + size + 28, top + 78, 90, 20).build());
        int[] colours = {0xCB8CF6,0xF277D7,0x70CFEA,0x39DA94,0xFF657A,0xFFCC74,0xFFFFFF,0x10131F};
        int cell = (size + 120) / colours.length;
        for (int i = 0; i < colours.length; i++) { int c = colours[i]; addRenderableWidget(new ColourButton(left + i * cell, top + size + 9, cell - 3, 18, c, "", () -> { float[] hsv = Color.RGBtoHSB(c >> 16 & 255, c >> 8 & 255, c & 255, null); hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2]; apply(); })); }
        addRenderableWidget(Button.builder(Component.literal("Done - save colour"), b -> onClose()).bounds(left, top + size + 34, size + 120, 20).build());
    }
    private void apply() { setter.accept(colour()); hex.setValue(String.format("#%06X", colour())); mod.save(); }
    private void pick(double x, double y) {
        if (dragging == 1) { saturation = (float)Math.clamp((x - left) / size, 0, 1); brightness = 1 - (float)Math.clamp((y - top) / size, 0, 1); }
        if (dragging == 2) hue = (float)Math.clamp((y - top) / size, 0, 1);
        apply();
    }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        if (e.button() == 0 && e.y() >= top && e.y() <= top + size) {
            if (e.x() >= left && e.x() <= left + size) dragging = 1;
            else if (e.x() >= left + size + 7 && e.x() <= left + size + 20) dragging = 2;
            if (dragging != 0) { setDragging(true); pick(e.x(), e.y()); return true; }
        }
        return super.mouseClicked(e, twice);
    }
    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging != 0) { pick(e.x(), e.y()); return true; } return super.mouseDragged(e, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) { dragging = 0; setDragging(false); return super.mouseReleased(e); }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0,0,width,height,0xEF0B0E19); g.drawString(font, label + " - click or drag to choose a colour", left, top - 19, 0xFFFFFFFF, false);
        for (int x = 0; x < size; x += 3) for (int y = 0; y < size; y += 3) g.fill(left+x,top+y,left+Math.min(size,x+3),top+Math.min(size,y+3),Color.HSBtoRGB(hue,(float)x/size,1-(float)y/size));
        for (int y = 0; y < size; y++) g.fill(left+size+7,top+y,left+size+20,top+y+1,Color.HSBtoRGB((float)y/size,1,1));
        int sx = left + Math.round(saturation * size), sy = top + Math.round((1-brightness)*size);
        TrackerHud.border(g,sx-3,sy-3,sx+4,sy+4,0xFFFFFFFF);
        g.fill(left+size+5,top+Math.round(hue*size)-1,left+size+22,top+Math.round(hue*size)+1,0xFFFFFFFF);
        g.fill(left+size+28,top,left+size+118,top+43,0xFF000000|colour());
        super.render(g,mx,my,delta);
    }
    @Override public void onClose() { apply(); minecraft.setScreen(parent); }
}
