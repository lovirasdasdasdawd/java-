package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class HudEditorScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private Settings.Hud dragging;
    private double dx, dy;
    HudEditorScreen(DonutTrackerClient mod, Screen parent) { super(Component.literal("Move HUDs")); this.mod = mod; this.parent = parent; }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x60060912);
        TrackerHud.render(g, mod, true);
        g.drawCenteredString(font, "Drag either HUD. Escape saves and returns. Disabled HUDs appear while editing.", width / 2, height - 22, 0xFFFFFFFF);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (e.button() != 0) return false;
        for (var h : new Settings.Hud[]{mod.settings.queue, mod.settings.profit}) {
            int x = TrackerHud.x(h, width), y = TrackerHud.y(h, height);
            if (e.x() >= x && e.x() <= x + TrackerHud.width() * h.scale && e.y() >= y && e.y() <= y + TrackerHud.height(h) * h.scale) {
                dragging = h; dx = e.x() - x; dy = e.y() - y; return true;
            }
        }
        return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e, double amountX, double amountY) {
        if (dragging == null) return false;
        dragging.x = Math.max(0, Math.min((int)(width - TrackerHud.width() * dragging.scale), (int)(e.x() - dx)));
        dragging.y = Math.max(0, Math.min((int)(height - TrackerHud.height(dragging) * dragging.scale), (int)(e.y() - dy))); return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) { dragging = null; mod.save(); return true; }
    @Override public void onClose() { mod.save(); minecraft.setScreen(parent); }
}
