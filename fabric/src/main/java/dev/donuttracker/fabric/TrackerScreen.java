package dev.donuttracker.fabric;

import dev.donuttracker.core.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

final class TrackerScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int tab, section, left, top, panelWidth, panelHeight;
    private int historyPage;
    private final List<Caption> captions = new ArrayList<>();
    private record Caption(String text, int x, int y, int color) {}
    TrackerScreen(DonutTrackerClient mod, Screen parent) { super(Component.literal("Donut Tracker")); this.mod = mod; this.parent = parent; }
    @Override public boolean isPauseScreen() { return false; }
    private Settings.Hud hud() { return tab == 1 ? mod.settings.queue : mod.settings.profit; }
    @Override protected void init() {
        captions.clear(); panelWidth = Math.min(600, width - 16); panelHeight = Math.min(360, height - 12);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        int quarter = (panelWidth - 20) / 4;
        String[] names = {"Profit Tracker", "Payment Queue", "Detection", "History"};
        for (int i = 0; i < 4; i++) { final int index = i; button(names[i], left + 10 + quarter * i, top + 28, quarter - 3, () -> { tab = index; section = 0; rebuildWidgets(); }); }
        int y = top + 55;
        if (tab < 2) {
            int third = (panelWidth - 20) / 4;
            String[] sections = {"Design", "Colours", "Display", tab == 0 ? "Session" : "Queue"};
            for (int i = 0; i < 4; i++) { int index = i; button(sections[i], left + 10 + third * i, y, third - 3, () -> { section = index; rebuildWidgets(); }); }
            y += 25;
            if (section == 0) design(y); else if (section == 1) colors(y); else if (section == 2) display(y); else if (tab == 0) session(y); else queue(y);
        } else if (tab == 2) detection(y); else history(y);
        button("Save & close", left + panelWidth - 114, top + panelHeight - 26, 104, this::onClose);
        button("Move HUDs", left + 10, top + panelHeight - 26, 104, () -> { mod.save(); minecraft.setScreen(new HudEditorScreen(mod, this)); });
    }
    private void design(int y) {
        Settings.Hud h = hud(); int x = left + 12, w = panelWidth - 24, half = w / 2;
        button("HUD: " + (h.enabled ? "ON" : "OFF"), x, y, half - 3, () -> change(() -> h.enabled = !h.enabled));
        button("Style: " + h.theme, x + half, y, half - 3, () -> change(() -> h.theme = h.theme == Settings.Theme.CLEAN ? Settings.Theme.NEON : Settings.Theme.CLEAN));
        y += 23;
        button("Layout: " + h.layout, x, y, w - 3, () -> change(() -> h.layout = Settings.Layout.values()[(h.layout.ordinal() + 1) % 3])); y += 23;
        slider("Size", x, y, w - 3, (h.scale - .5) / 1.5, v -> h.scale = .5 + v * 1.5, v -> Math.round((.5 + v * 1.5) * 100) + "%"); y += 23;
        slider("Opacity", x, y, w - 3, h.opacity, v -> h.opacity = v, v -> Math.round(v * 100) + "%"); y += 23;
    }
    private void display(int y) {
        Settings.Hud h = hud(); int x = left + 12, w = panelWidth - 24, half = w / 2;
        if (tab == 0) {
            button("Profit: " + on(h.showProfit), x, y, half - 3, () -> change(() -> h.showProfit = !h.showProfit));
            button("Timer: " + on(h.showTimer), x + half, y, half - 3, () -> change(() -> h.showTimer = !h.showTimer)); y += 23;
            button("Money/hour: " + on(h.showRate), x, y, half - 3, () -> change(() -> h.showRate = !h.showRate));
        }
        button("Donut icon: " + on(h.showIcon), x + (tab == 0 ? half : 0), y, half - 3, () -> change(() -> h.showIcon = !h.showIcon));
    }
    private void colors(int y) {
        Settings.Hud h = hud(); int x = left + 12, w = panelWidth - 24;
        String[] names = {"Accent", "Text", "Profit / payment", "Loss", "Background"};
        int[] values = {h.accent, h.text, h.positive, h.negative, h.background};
        for (int i = 0; i < names.length; i++) {
            caption(names[i], x, y + 6, 0xFFE3DBEE); EditBox input = input(String.format("#%06X", values[i]), x + w / 2, y, Math.max(65, w / 2 - 58), 7);
            int index = i;
            button("Set", x + w - 53, y, 50, () -> mod.act(() -> {
                int color = Settings.color(input.getValue());
                switch (index) { case 0 -> h.accent = color; case 1 -> h.text = color; case 2 -> h.positive = color; case 3 -> h.negative = color; case 4 -> h.background = color; }
                mod.status = names[index] + " colour saved.";
            })); y += 20;
        }
        int[] preset = {0xCB8CF6, 0xF277D7, 0x70CFEA, 0x39DA94}; String[] labels = {"Purple", "Pink", "Cyan", "Green"};
        for (int i = 0; i < 4; i++) { int c = preset[i]; button(labels[i], x + i * w / 4, y, w / 4 - 3, () -> change(() -> h.accent = c)); }
    }
    private void session(int y) {
        int x = left + 12, w = panelWidth - 24;
        caption("Start / End requests a fresh /bal before recording.", x, y + 3, 0xFFA7B4CB); y += 16;
        button("Start tracking", x, y, w / 2 - 3, () -> mod.act(mod::startFresh));
        button("End tracking", x + w / 2, y, w / 2 - 3, () -> mod.act(mod::stopFresh)); y += 23;
        caption("Manual balance (fallback)", x, y + 6, 0xFFE3DBEE);
        EditBox balance = input(mod.tracker.hasBalance() ? mod.tracker.balance().toPlainString() : "", x + w / 2, y, w / 2 - 58, 30);
        button("Set", x + w - 53, y, 50, () -> mod.act(() -> { mod.tracker.balance(Money.parse(balance.getValue())); mod.status = "Manual balance recorded."; })); y += 23;
        button("Start manual", x, y, w / 2 - 3, () -> mod.act(() -> { mod.cancelPending(); mod.tracker.start(); }));
        button("End at last balance", x + w / 2, y, w / 2 - 3, () -> mod.act(() -> { mod.cancelPending(); mod.tracker.stop(); })); y += 23;
        button("Reset session", x, y, w - 3, () -> mod.act(() -> { mod.cancelPending(); mod.tracker.reset(); mod.status = "Session reset. Completed history retained."; }));
    }
    private void queue(int y) {
        int x = left + 12, w = panelWidth - 24;
        caption("Minimum payment", x, y + 6, 0xFFE3DBEE);
        EditBox minimum = input(mod.settings.minimumPayment.toPlainString(), x + w / 2, y, w / 2 - 58, 30);
        button("Set", x + w - 53, y, 50, () -> mod.act(() -> {
            var amount = Money.parse(minimum.getValue()); if (amount.signum() < 0) throw new IllegalArgumentException("Minimum cannot be negative."); mod.settings.minimumPayment = amount; mod.status = "Payment minimum saved.";
        })); y += 23;
        button("Next player →", x, y, w / 2 - 3, () -> mod.act(() -> { mod.queue.next(); mod.status = mod.queue.waiting() + " players waiting."; }));
        button("Clear queue", x + w / 2, y, w / 2 - 3, () -> mod.act(mod.queue::clear)); y += 25;
        caption("Manual entry: IGN / amount", x, y + 2, 0xFFA7B4CB); y += 14;
        EditBox name = input("", x, y, w / 2 - 3, 16), amount = input("100K", x + w / 2, y, w / 2 - 3, 30); y += 23;
        button("Add payment", x, y, w - 3, () -> mod.act(() -> {
            mod.status = mod.queue.add(name.getValue().strip(), Money.parse(amount.getValue()), mod.settings.minimumPayment) ? "Payment added." : "Below minimum payment.";
        }));
    }
    private void detection(int y) {
        int x = left + 12, w = panelWidth - 24;
        caption("Balance regex (1 capture: amount)", x, y, 0xFFA7B4CB); y += 12;
        EditBox balance = input(mod.settings.balancePattern, x, y, w - 3, 1024); y += 24;
        caption("Payment regex (2 captures: IGN, amount)", x, y, 0xFFA7B4CB); y += 12;
        EditBox payment = input(mod.settings.paymentPattern, x, y, w - 3, 1024); y += 24;
        button("Save patterns", x, y, w - 3, () -> mod.act(() -> {
            String oldB = mod.settings.balancePattern, oldP = mod.settings.paymentPattern;
            mod.settings.balancePattern = balance.getValue(); mod.settings.paymentPattern = payment.getValue();
            try { mod.settings.validatePatterns(); } catch (RuntimeException ex) { mod.settings.balancePattern = oldB; mod.settings.paymentPattern = oldP; throw ex; }
            mod.status = "Patterns saved.";
        })); y += 23;
        button("Auto /bal every 15s: " + on(mod.settings.pollBalance), x, y, w - 3, () -> change(() -> mod.settings.pollBalance = !mod.settings.pollBalance)); y += 23;
        button("Read /bal now", x, y, w - 3, () -> mod.act(mod::requestBalance));
    }
    private void history(int y) {
        int x = left + 12, count = mod.tracker.history().size(), pageSize = Math.max(1, (panelHeight - 120) / 24);
        historyPage = Math.min(historyPage, Math.max(0, (count - 1) / pageSize));
        if (count == 0) caption("No completed sessions yet.", x, y + 8, 0xFFA7B4CB);
        for (int i = 0; i < pageSize; i++) {
            int index = historyPage * pageSize + i; if (index >= count) break;
            var session = mod.tracker.history().get(index);
            String date = java.time.Instant.ofEpochMilli(session.endedAt()).toString().substring(0, 16);
            caption(date + "  " + Money.signed(session.profit()) + "  " + (session.endedAt() - session.startedAt()) / 1000 + "s", x, y + i * 24 + 4, 0xFFE3DBEE);
        }
        button("Previous", x, top + panelHeight - 51, 90, () -> { historyPage = Math.max(0, historyPage - 1); rebuildWidgets(); });
        button("Next", x + 94, top + panelHeight - 51, 90, () -> { if ((historyPage + 1) * pageSize < count) historyPage++; rebuildWidgets(); });
    }
    private void change(Runnable action) { mod.act(action); rebuildWidgets(); }
    private static String on(boolean enabled) { return enabled ? "ON" : "OFF"; }
    private void caption(String text, int x, int y, int c) { captions.add(new Caption(text, x, y, c)); }
    private void button(String label, int x, int y, int w, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> action.run()).bounds(x, y, w, 20).build());
    }
    private EditBox input(String value, int x, int y, int w, int max) {
        EditBox field = new EditBox(font, x, y, w, 20, Component.literal("Value")); field.setMaxLength(max); field.setValue(value); addRenderableWidget(field); return field;
    }
    private void slider(String label, int x, int y, int w, double initial, DoubleConsumer setter, java.util.function.DoubleFunction<String> format) {
        addRenderableWidget(new AbstractSliderButton(x, y, w, 20, Component.literal(label + ": " + format.apply(initial)), initial) {
            @Override protected void updateMessage() { setMessage(Component.literal(label + ": " + format.apply(value))); }
            @Override protected void applyValue() { setter.accept(value); mod.save(); }
        });
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xAC060912);
        TrackerHud.rounded(g, left, top, left + panelWidth, top + panelHeight, 0xF50D1020);
        TrackerHud.border(g, left, top, left + panelWidth, top + panelHeight, 0xFF000000 | (tab == 1 ? mod.settings.queue.accent : mod.settings.profit.accent));
        g.drawString(font, "DONUT TRACKER  /  " + (tab < 2 ? hud().theme : "SETTINGS"), left + 12, top + 11, 0xFF000000 | hud().accent, false);
        for (var c : captions) g.drawString(font, c.text(), c.x(), c.y(), c.color(), false);
        String status = font.plainSubstrByWidth(mod.status, Math.max(50, panelWidth - 240));
        g.drawString(font, status, left + 120, top + panelHeight - 19, 0xFFA7B4CB, false);
        super.render(g, mx, my, delta);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; } return super.keyPressed(event);
    }
    @Override public void onClose() { mod.save(); minecraft.setScreen(parent); }
}
