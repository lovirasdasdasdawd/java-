package dev.donuttracker.fabric;

import dev.donuttracker.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;

/** Calm, translucent cards with a clear information hierarchy. */
final class TrackerHud {
    private TrackerHud() {}
    static int height(Settings.Hud h) { return h.layout == Settings.Layout.MINIMAL ? 43 : h.layout == Settings.Layout.STACKED ? 128 : 105; }
    static int width() { return 200; }
    static int x(Settings.Hud h, int screenWidth) { return Math.max(0, Math.min(h.x, screenWidth - (int)(width() * h.scale))); }
    static int y(Settings.Hud h, int screenHeight) { return Math.max(0, Math.min(h.y, screenHeight - (int)(height(h) * h.scale))); }
    static void render(GuiGraphics g, DonutTrackerClient mod, boolean editing) {
        draw(g, mod, mod.settings.profit, false, editing); draw(g, mod, mod.settings.queue, true, editing);
    }
    static void draw(GuiGraphics g, DonutTrackerClient mod, Settings.Hud h, boolean payment, boolean editing) {
        if (!h.enabled && !editing) return;
        g.pose().pushMatrix();
        g.pose().translate((float)x(h, g.guiWidth()), (float)y(h, g.guiHeight()));
        g.pose().scale((float)h.scale, (float)h.scale);
        int w = width(), ht = height(h), accent = opaque(h.accent), ink = opaque(h.text), muted = blend(h.text, h.background, .56f);
        if (h.theme == Settings.Theme.NEON) for (int spread = 5; spread > 0; spread--) border(g, -spread, -spread, w + spread, ht + spread, 0x11000000 | h.accent);
        rounded(g, 0, 0, w, ht, ((int)(255 * h.opacity) << 24) | h.background);
        border(g, 0, 0, w, ht, h.theme == Settings.Theme.NEON ? accent : blend(h.text, h.background, .23f));
        if (h.showIcon) { if (payment) people(g, 11, 11, accent); else donut(g, 11, 10, accent); }
        scaled(g, payment ? "PAYMENT QUEUE" : "DONUT TRACKER", h.showIcon ? 28 : 11, 12, .75f, ink);
        if (payment) {
            String waiting = mod.queue.waiting() + " waiting";
            int pillW = Math.max(42, Math.round(Minecraft.getInstance().font.width(waiting) * .65f) + 12);
            rounded(g, w - pillW - 10, 8, w - 10, 22, blend(h.text, h.background, .07f));
            border(g, w - pillW - 10, 8, w - 10, 22, blend(h.text, h.background, .18f));
            scaled(g, waiting, w - pillW - 4, 12, .65f, muted);
        } else {
            rounded(g, w - 39, 12, w - 35, 16, mod.tracker.running() ? opaque(h.positive) : muted);
            scaled(g, mod.tracker.running() ? "LIVE" : "IDLE", w - 30, 12, .7f, muted);
        }
        if (payment) payment(g, mod, h, ink, muted, accent);
        else profit(g, mod, h, ink, muted);
        g.pose().popMatrix();
    }
    private static void profit(GuiGraphics g, DonutTrackerClient mod, Settings.Hud h, int ink, int muted) {
        int value = opaque(mod.tracker.profit().signum() < 0 ? h.negative : h.positive);
        if (h.layout == Settings.Layout.MINIMAL) {
            if (h.showProfit) scaled(g, Money.signed(mod.tracker.profit()), 11, 26, 1.2f, value);
            if (h.showTimer) right(g, mod.tracker.duration(), width() - 11, 28, .7f, muted);
            return;
        }
        if (h.showProfit) {
            String amount = Money.signed(mod.tracker.profit());
            float scale = Math.min(2.0f, 176f / Math.max(1, Minecraft.getInstance().font.width(amount)));
            centered(g, amount, width() / 2, 32, scale, value);
            centered(g, "SESSION PROFIT", width() / 2, 55, .62f, muted);
        }
        divider(g, 70, h);
        if (h.showTimer) {
            clock(g, 12, 78, muted);
            scaled(g, mod.tracker.duration(), 25, 79, .8f, ink);
        }
        if (h.showRate) right(g, Money.signed(mod.tracker.perHour()) + " / hr", width() - 11, 79, .75f, mod.tracker.perHour().signum() < 0 ? opaque(h.negative) : ink);
        divider(g, 91, h);
        scaled(g, "Balance", 11, 97, .65f, muted);
        right(g, mod.tracker.hasBalance() ? Money.format(mod.tracker.balance()) : "Run /bal", width() - 11, 97, .65f, ink);
        if (h.layout == Settings.Layout.STACKED) scaled(g, mod.tracker.running() ? "Tracking your current session" : "Start a session in Right Shift", 11, 115, .7f, muted);
    }
    private static void payment(GuiGraphics g, DonutTrackerClient mod, Settings.Hud h, int ink, int muted, int accent) {
        var active = mod.queue.active();
        int value = opaque(h.positive);
        if (h.layout == Settings.Layout.MINIMAL) {
            scaled(g, active == null ? "Waiting for payments" : active.player(), 11, 29, .8f, ink);
            if (active != null) right(g, "+" + Money.format(active.amount()), width() - 11, 29, .85f, value);
            return;
        }
        if (active == null) {
            centered(g, "No payments yet", width() / 2, 44, 1, ink);
            centered(g, "Incoming payments appear here", width() / 2, 61, .65f, muted);
        } else {
            rounded(g, 10, 34, 44, 68, blend(h.text, h.background, .08f));
            var connection = Minecraft.getInstance().getConnection();
            var info = connection == null ? null : connection.getPlayerInfo(active.player());
            if (info != null) PlayerFaceRenderer.draw(g, info.getSkin(), 13, 37, 28);
            else centered(g, active.player().substring(0, 1), 27, 43, 1.7f, accent);
            String amount = "+" + Money.format(active.amount());
            float amountScale = .92f;
            int amountW = (int)Math.ceil(Minecraft.getInstance().font.width(amount) * amountScale);
            int nameWidth = Math.max(30, width() - 11 - amountW - 9 - 51);
            String name = Minecraft.getInstance().font.plainSubstrByWidth(active.player(), (int)(nameWidth / .85f));
            scaled(g, name, 51, 42, .85f, ink);
            scaled(g, "CURRENT PLAYER", 51, 57, .58f, muted);
            right(g, amount, width() - 11, 44, amountScale, value);
        }
        divider(g, 79, h);
        var waiting = mod.queue.all().stream().skip(1).toList();
        String next = waiting.isEmpty() ? "Queue is clear" : "Next: " + waiting.getFirst().player() + "  -  " + Money.format(waiting.getFirst().amount());
        scaled(g, Minecraft.getInstance().font.plainSubstrByWidth(next, 235), 11, 90, .7f, muted);
        // Navigation stays in the module menu; the gameplay HUD never steals mouse input.
        border(g, width() - 24, 85, width() - 10, 99, blend(h.text, h.background, .20f));
        scaled(g, ">", width() - 20, 88, .9f, muted);
        if (h.layout == Settings.Layout.STACKED) {
            for (int i = 1; i < Math.min(3, waiting.size()); i++) {
                var p = waiting.get(i);
                scaled(g, p.player() + "  -  " + Money.format(p.amount()), 11, 105 + (i - 1) * 12, .7f, muted);
            }
        }
    }
    private static void divider(GuiGraphics g, int y, Settings.Hud h) { g.fill(11, y, width() - 11, y + 1, blend(h.text, h.background, .15f)); }
    private static int opaque(int c) { return 0xFF000000 | c; }
    private static int blend(int foreground, int background, float amount) {
        int r = Math.round((foreground >> 16 & 255) * amount + (background >> 16 & 255) * (1-amount));
        int green = Math.round((foreground >> 8 & 255) * amount + (background >> 8 & 255) * (1-amount));
        int b = Math.round((foreground & 255) * amount + (background & 255) * (1-amount));
        return 0xFF000000 | r << 16 | green << 8 | b;
    }
    static void text(GuiGraphics g, String s, int x, int y, int color) { g.drawString(Minecraft.getInstance().font, s, x, y, color, false); }
    private static void scaled(GuiGraphics g, String s, int x, int y, float scale, int color) {
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
        int ht = y2 - y1, radius = Math.max(0, Math.min(7, Math.min((x2-x1)/2, ht/2)));
        for (int y = 0; y < ht; y++) { int dx = inset(y, ht, radius); g.fill(x1 + dx, y1+y, x2-dx, y1+y+1, color); }
    }
    static void border(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int ht = y2-y1, radius = Math.max(0, Math.min(7, Math.min((x2-x1)/2, ht/2)));
        for (int y = 0; y < ht; y++) {
            int dx = inset(y, ht, radius);
            if (y == 0 || y == ht-1) g.fill(x1+dx,y1+y,x2-dx,y1+y+1,color);
            else { int inner = 1 + inset(y-1,ht-2,Math.max(0,radius-1)); g.fill(x1+dx,y1+y,x1+inner,y1+y+1,color); g.fill(x2-inner,y1+y,x2-dx,y1+y+1,color); }
        }
    }
    private static void donut(GuiGraphics g, int x, int y, int c) { rounded(g,x,y,x+11,y+11,c); rounded(g,x+4,y+4,x+7,y+7,0xFF171B27); g.fill(x+7,y+2,x+9,y+4,0xFF171B27); }
    private static void people(GuiGraphics g, int x, int y, int c) { rounded(g,x+1,y,x+5,y+4,c); rounded(g,x+7,y+1,x+11,y+5,c); rounded(g,x-1,y+6,x+7,y+10,c); rounded(g,x+7,y+7,x+13,y+10,c); }
    private static void clock(GuiGraphics g, int x, int y, int c) { border(g,x,y,x+9,y+9,c); g.fill(x+4,y+2,x+5,y+5,c); g.fill(x+4,y+4,x+7,y+5,c); }
}
