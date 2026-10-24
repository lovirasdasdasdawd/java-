package dev.donuttracker.fabric;

import dev.donuttracker.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;

final class TrackerHud {
    private TrackerHud() {}
    static int height(Settings.Hud h) { return h.layout == Settings.Layout.MINIMAL ? 30 : h.layout == Settings.Layout.STACKED ? 82 : 58; }
    static int width() { return 180; }
    static int x(Settings.Hud h, int screenWidth) { return Math.max(0, Math.min(h.x, screenWidth - (int)(width() * h.scale))); }
    static int y(Settings.Hud h, int screenHeight) { return Math.max(0, Math.min(h.y, screenHeight - (int)(height(h) * h.scale))); }
    static void render(GuiGraphics g, DonutTrackerClient mod, boolean editing) {
        draw(g, mod, mod.settings.profit, false, editing); draw(g, mod, mod.settings.queue, true, editing);
    }
    static void draw(GuiGraphics g, DonutTrackerClient mod, Settings.Hud h, boolean payment, boolean editing) {
        Minecraft mc = Minecraft.getInstance(); if (!h.enabled && !editing) return;
        g.pose().pushMatrix(); g.pose().translate((float)x(h, g.guiWidth()), (float)y(h, g.guiHeight())); g.pose().scale((float)h.scale, (float)h.scale);
        int w = width(), height = height(h), accent = 0xFF000000 | h.accent;
        if (h.theme == Settings.Theme.NEON) for (int spread = 5; spread > 0; spread--) border(g, -spread, -spread, w + spread, height + spread, 0x17000000 | h.accent);
        rounded(g, 0, 0, w, height, ((int)(255 * h.opacity) << 24) | h.background);
        border(g, 0, 0, w, height, accent);
        if (h.showIcon) donut(g, 8, 7, accent);
        text(g, payment ? "Payment Queue" : "Donut Tracker", h.showIcon ? 23 : 8, 8, accent);
        if (!payment && mod.tracker.running()) text(g, "LIVE", w - 28, 8, 0xFF000000 | h.positive);
        int valueColor = 0xFF000000 | (!payment && mod.tracker.profit().signum() < 0 ? h.negative : h.positive);
        if (payment) {
            var active = mod.queue.active();
            if (h.layout == Settings.Layout.MINIMAL) {
                text(g, active == null ? "No payments" : active.player() + " +" + Money.format(active.amount()), 8, 20, valueColor);
            } else {
                if (active != null) {
                    var info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(active.player());
                    if (info != null) PlayerFaceRenderer.draw(g, info.getSkin(), 8, 24, 24);
                    else { rounded(g, 8, 24, 32, 48, 0xFF282B40); text(g, active.player().substring(0, 1), 16, 32, accent); }
                    text(g, active.player(), 39, 25, 0xFF000000 | h.text);
                    text(g, "+" + Money.format(active.amount()), 39, 39, valueColor);
                } else text(g, "Waiting for payments", 8, 29, 0xFF000000 | h.text);
                text(g, "" + mod.queue.waiting() + " waiting", w - 66, 8, accent);
                if (h.layout == Settings.Layout.STACKED) {
                    var waiting = mod.queue.all().stream().skip(1).limit(2).toList();
                    for (int i = 0; i < waiting.size(); i++) text(g, waiting.get(i).player() + "  " + Money.format(waiting.get(i).amount()), 8, 56 + i * 11, 0xFF000000 | h.text);
                }
            }
        } else {
            if (h.showProfit) text(g, Money.signed(mod.tracker.profit()), 8, 22, valueColor);
            if (h.layout != Settings.Layout.MINIMAL) {
                if (h.showTimer) text(g, mod.tracker.duration(), h.layout == Settings.Layout.STACKED ? 8 : w - 58, h.layout == Settings.Layout.STACKED ? 39 : 23, 0xFF000000 | h.text);
                if (h.showRate) text(g, Money.signed(mod.tracker.perHour()) + "/hour", 8, h.layout == Settings.Layout.STACKED ? 54 : 42, accent);
                if (h.layout == Settings.Layout.STACKED) text(g, "Balance: " + (mod.tracker.hasBalance() ? Money.format(mod.tracker.balance()) : "run /bal"), 8, 68, 0xFF000000 | h.text);
            }
        }
        g.pose().popMatrix();
    }
    static void text(GuiGraphics g, String s, int x, int y, int color) { g.drawString(Minecraft.getInstance().font, s, x, y, color, false); }
    static void rounded(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        g.fill(x1 + 3, y1, x2 - 3, y2, c); g.fill(x1, y1 + 3, x1 + 3, y2 - 3, c); g.fill(x2 - 3, y1 + 3, x2, y2 - 3, c);
    }
    static void border(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        g.fill(x1 + 3, y1, x2 - 3, y1 + 1, c); g.fill(x1 + 3, y2 - 1, x2 - 3, y2, c);
        g.fill(x1, y1 + 3, x1 + 1, y2 - 3, c); g.fill(x2 - 1, y1 + 3, x2, y2 - 3, c);
    }
    private static void donut(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 2, y, x + 8, y + 2, color); g.fill(x, y + 2, x + 2, y + 8, color);
        g.fill(x + 8, y + 2, x + 10, y + 8, color); g.fill(x + 2, y + 8, x + 8, y + 10, color);
        g.fill(x + 4, y + 4, x + 6, y + 6, color);
    }
}
