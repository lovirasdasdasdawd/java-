package dev.donuttracker.fabric;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class TrackerScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left, top, panelWidth;
    TrackerScreen(DonutTrackerClient mod, Screen parent) { super(Component.literal("Donut Tracker Modules")); this.mod = mod; this.parent = parent; }
    @Override public boolean isPauseScreen() { return false; }
    @Override protected void init() {
        panelWidth = Math.min(560, width - 24); left = (width - panelWidth) / 2; top = Math.max(12, (height - 200) / 2);
        int gap = 12, card = (panelWidth - gap) / 2;
        addRenderableWidget(Button.builder(Component.literal("Open Tracker settings"), b -> minecraft.setScreen(new ModuleSettingsScreen(mod, this, false))).bounds(left + 10, top + 116, card - 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Open Payment settings"), b -> minecraft.setScreen(new ModuleSettingsScreen(mod, this, true))).bounds(left + card + gap + 10, top + 116, card - 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Move HUD boxes"), b -> minecraft.setScreen(new HudEditorScreen(mod, this))).bounds(left, top + 151, panelWidth / 2 - 5, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save & close"), b -> onClose()).bounds(left + panelWidth / 2 + 5, top + 151, panelWidth / 2 - 5, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Payment actions [P] - return 2x / 3x"), b -> minecraft.setScreen(new PaymentActionScreen(mod, this))).bounds(left, top + 176, panelWidth, 20).build());
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xB0060912);
        g.drawString(font, "DONUT TRACKER  /  MODULES", left, top, 0xFFE2BEFF, false);
        int card = (panelWidth - 12) / 2;
        card(g, left, card, "TRACKER", "Profit, timer & money/hour", "Design / Colours / Session", mod.settings.profit.accent);
        card(g, left + card + 12, card, "PAYMENT QUEUE", "Player heads, IGN & payments", "Design / Colours / Queue", mod.settings.queue.accent);
        super.render(g, mx, my, delta);
    }
    private void card(GuiGraphics g, int x, int w, String title, String description, String detail, int color) {
        TrackerHud.rounded(g, x, top + 25, x + w, top + 141, 0xF3101423);
        TrackerHud.border(g, x, top + 25, x + w, top + 141, 0xFF000000 | color);
        g.fill(x + 12, top + 40, x + 22, top + 50, 0xFF000000 | color);
        g.drawString(font, title, x + 29, top + 41, 0xFF000000 | color, false);
        g.drawString(font, font.plainSubstrByWidth(description, w - 20), x + 10, top + 65, 0xFFE9EAF2, false);
        g.drawString(font, font.plainSubstrByWidth(detail, w - 20), x + 10, top + 83, 0xFFA7B4CB, false);
    }
    @Override public void onClose() { mod.save(); minecraft.setScreen(parent); }
}
