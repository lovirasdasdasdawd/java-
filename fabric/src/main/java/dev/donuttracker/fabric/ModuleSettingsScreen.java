package dev.donuttracker.fabric;

import dev.donuttracker.core.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

final class ModuleSettingsScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int tab, section, left, top, panelWidth, panelHeight;
    private int historyPage;
    private int previewX, previewY, previewWidth;
    private final List<Caption> captions = new ArrayList<>();
    private record Caption(String text, int x, int y, int color) {}
    ModuleSettingsScreen(DonutTrackerClient mod, Screen parent, boolean payment) { super(Component.literal(payment ? "Payment Queue" : "Profit Tracker")); this.mod = mod; this.parent = parent; this.tab = payment ? 1 : 0; }
    @Override public boolean isPauseScreen() { return false; }
    private Settings.Hud hud() { return tab == 1 ? mod.settings.queue : mod.settings.profit; }
    @Override protected void init() {
        captions.clear(); previewWidth = 0; panelWidth = Math.min(600, width - 16); panelHeight = Math.min(280, height - 12);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        int y = top + 31;
        String[] pages = tab == 0 ? new String[]{"Design", "Colours", "Display", "Session", "Messages", "History"} : new String[]{"Design", "Colours", "Display", "Queue", "Messages", "Position"};
        int cell = (panelWidth - 20) / pages.length;
        for (int i = 0; i < pages.length; i++) { int page = i; addRenderableWidget(new MenuWidgets.Action(left + 10 + i * cell,y,cell-2,20,pages[i],()->hud().accent,MenuWidgets.Kind.TAB,()->section==page,-1,()->{section=page;rebuildWidgets();})); }
        y += 27;
        switch (section) {
            case 0 -> design(y);
            case 1 -> colors(y);
            case 2 -> display(y);
            case 3 -> { if (tab == 0) session(y); else queue(y); }
            case 4 -> detection(y);
            case 5 -> { if (tab == 0) history(y); else position(y); }
            case 6 -> position(y);
        }
        addRenderableWidget(new MenuWidgets.Action(left+panelWidth-114,top+panelHeight-26,104,20,"Done",()->hud().accent,MenuWidgets.Kind.PRIMARY,()->{mod.save();minecraft.setScreen(null);}));
        button("Back to modules",left+10,top+panelHeight-26,104,this::onClose);
    }
    private void design(int y) {
        Settings.Hud h=hud();int x=left+12,total=panelWidth-24,w=total>=500?(total-16)*3/5:total,half=(w-6)/2;
        addRenderableWidget(new MenuWidgets.Toggle(x,y,half,30,"HUD visible",()->h.enabled,()->h.accent,()->change(()->h.enabled=!h.enabled)));
        int themeX=x+half+6,themeW=w-half-6;
        caption("STYLE",themeX,y+1,0xFF8F9CB2);
        for(int i=0;i<2;i++) { var choice=Settings.Theme.values()[i];int index=i;
            addRenderableWidget(new MenuWidgets.Action(themeX+index*(themeW/2),y+12,themeW/2-2,18,choice==Settings.Theme.CLEAN?"Clean":"Neon",()->h.accent,MenuWidgets.Kind.TAB,()->h.theme==choice,-1,()->change(()->h.theme=choice)));
        }
        int cardW=(w-8)/3;
        for(int i=0;i<3;i++) { var choice=Settings.Layout.values()[i];String label=switch(choice){case COMPACT->"Compact";case STACKED->"Stacked";case MINIMAL->"Minimal";};
            addRenderableWidget(new MenuWidgets.Action(x+i*(cardW+4),y+35,cardW,31,label,()->h.accent,MenuWidgets.Kind.CHOICE,()->h.layout==choice,i,()->change(()->h.layout=choice)));
        }
        addRenderableWidget(new MenuWidgets.Slider(x,y+72,half,37,"HUD size",(h.scale-.5)/1.5,v->{h.scale=.5+v*1.5;mod.save();},v->Math.round((.5+v*1.5)*100)+"%",()->h.accent));
        addRenderableWidget(new MenuWidgets.Slider(x+half+6,y+72,w-half-6,37,"Opacity",h.opacity,v->{h.opacity=v;mod.save();},v->Math.round(v*100)+"%",()->h.accent));
        addRenderableWidget(new MenuWidgets.Action(x,y+115,half,22,"Move HUD...",()->h.accent,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new HudEditorScreen(mod,this,h))));
        addRenderableWidget(new MenuWidgets.Action(x+half+6,y+115,w-half-6,22,"Clean preset",()->h.accent,MenuWidgets.Kind.PRIMARY,()->change(()->{
            h.theme=Settings.Theme.CLEAN;h.accent=tab==0?0xB8A5F4:0x9BCEDB;h.text=0xE9EAF2;h.positive=0x8AE8C3;h.negative=0xF08091;h.background=0x171B24;h.opacity=.9;
        })));
        if(total>=500) { previewX=x+w+12;previewY=y;previewWidth=total-w-12; }
    }
    private void display(int y) {
        Settings.Hud h=hud();int x=left+12,w=panelWidth-24;
        if(tab==0) {
            addRenderableWidget(new MenuWidgets.Toggle(x,y,w,27,"Session profit",()->h.showProfit,()->h.accent,()->change(()->h.showProfit=!h.showProfit)));y+=33;
            addRenderableWidget(new MenuWidgets.Toggle(x,y,w,27,"Elapsed time",()->h.showTimer,()->h.accent,()->change(()->h.showTimer=!h.showTimer)));y+=33;
            addRenderableWidget(new MenuWidgets.Toggle(x,y,w,27,"Money per hour",()->h.showRate,()->h.accent,()->change(()->h.showRate=!h.showRate)));y+=33;
        }
        addRenderableWidget(new MenuWidgets.Toggle(x,y,w,27,"Module icon",()->h.showIcon,()->h.accent,()->change(()->h.showIcon=!h.showIcon)));
    }
    private void colors(int y) {
        Settings.Hud h = hud(); int x = left + 12, w = panelWidth - 24;
        String[] names = {"Accent", "Text", tab == 0 ? "Profit" : "Payment", "Loss", "Background"};
        int[] values = {h.accent, h.text, h.positive, h.negative, h.background};
        for (int i = 0; i < names.length; i++) {
            caption(names[i], x, y + 6, 0xFFE3DBEE); int index = i;
            addRenderableWidget(new ColourButton(x + w / 3, y, w * 2 / 3 - 3, 20, values[i], "Choose  #" + String.format("%06X", values[i]), () -> minecraft.setScreen(new ColourPickerScreen(mod, this, names[index], values[index], c -> {
                switch (index) { case 0 -> h.accent = c; case 1 -> h.text = c; case 2 -> h.positive = c; case 3 -> h.negative = c; case 4 -> h.background = c; }
            })))); y += 23;
        }
        caption("Click a coloured swatch to open the visual picker.", x, y + 4, 0xFFA7B4CB);
    }
    private void position(int y) {
        int x = left + 12, w = panelWidth - 24; Settings.Hud h = hud();
        caption("Drag the actual box anywhere on your screen.", x, y + 4, 0xFFA7B4CB); y += 20;
        button("Drag this HUD", x, y, w - 3, () -> minecraft.setScreen(new HudEditorScreen(mod, this, h))); y += 25;
        caption("X / Y position", x, y + 5, 0xFFE3DBEE);
        EditBox px = input("" + h.x, x + w / 3, y, w / 4, 6), py = input("" + h.y, x + w * 2 / 3, y, w / 4, 6); y += 25;
        button("Apply position", x, y, w / 2 - 3, () -> mod.act(() -> { h.x = Math.max(0, Integer.parseInt(px.getValue())); h.y = Math.max(0, Integer.parseInt(py.getValue())); }));
        button("Reset position", x + w / 2, y, w / 2 - 3, () -> change(() -> { h.x = tab == 0 ? 16 : 210; h.y = 16; }));
    }
    private void session(int y) {
        int x = left + 12, w = panelWidth - 24;
        caption("Start / End requests a fresh /bal before recording.", x, y + 3, 0xFFA7B4CB); y += 16;
        button("Start (/bal)", x, y, w / 2 - 3, () -> mod.act(mod::startFresh));
        button("End (/bal)", x + w / 2, y, w / 2 - 3, () -> mod.act(mod::stopFresh)); y += 23;
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
        caption(tab == 0 ? "Balance pattern (1 capture: amount)" : "Payment pattern (2 captures: IGN, amount)", x, y, 0xFFA7B4CB); y += 15;
        EditBox pattern = input(tab == 0 ? mod.settings.balancePattern : mod.settings.paymentPattern, x, y, w - 3, 1024); y += 25;
        button("Save this module's pattern", x, y, w - 3, () -> mod.act(() -> {
            String oldB = mod.settings.balancePattern, oldP = mod.settings.paymentPattern;
            if (tab == 0) mod.settings.balancePattern = pattern.getValue(); else mod.settings.paymentPattern = pattern.getValue();
            try { mod.settings.validatePatterns(); } catch (RuntimeException ex) { mod.settings.balancePattern = oldB; mod.settings.paymentPattern = oldP; throw ex; }
            mod.status = "Detection pattern saved.";
        })); y += 25;
        if (tab == 0) {
            button("Read /bal now", x, y, w - 3, () -> mod.act(mod::requestBalance));
        }
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
        addRenderableWidget(new MenuWidgets.Action(x,y,w,20,label,()->hud().accent,MenuWidgets.Kind.SECONDARY,action));
    }
    private EditBox input(String value, int x, int y, int w, int max) {
        EditBox field = new EditBox(font, x, y, w, 20, Component.literal("Value")); field.setMaxLength(max); field.setValue(value); addRenderableWidget(field); return field;
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xAC060912);
        TrackerHud.rounded(g, left, top, left + panelWidth, top + panelHeight, 0xF50E1320);
        TrackerHud.border(g, left, top, left + panelWidth, top + panelHeight, 0xFF30394B);
        TrackerHud.rounded(g,left+12,top+9,left+17,top+20,0xFF000000|hud().accent);
        g.drawString(font,tab==0?"Tracker settings":"Payment Queue settings",left+24,top+11,0xFFE8EDF6,false);
        String saved="Auto-saved";g.drawString(font,saved,left+panelWidth-font.width(saved)-14,top+11,0xFF8D9BB2,false);
        g.fill(left+12,top+26,left+panelWidth-12,top+27,0xFF252D3D);
        if(previewWidth>0) {
            int ph=Math.min(151,top+panelHeight-34-previewY);
            TrackerHud.rounded(g,previewX,previewY,previewX+previewWidth,previewY+ph,0xFF141B29);
            TrackerHud.border(g,previewX,previewY,previewX+previewWidth,previewY+ph,0xFF2A3447);
            g.drawString(font,"LIVE PREVIEW",previewX+12,previewY+11,0xFF91A1B9,false);
            float scale=Math.min(.88f,Math.min((previewWidth-20f)/TrackerHud.width(),(ph-58f)/TrackerHud.height(hud())));
            float factor=scale/(float)hud().scale;
            g.pose().pushMatrix();g.pose().translate(previewX+(previewWidth-TrackerHud.width()*scale)/2-TrackerHud.x(hud(),width)*factor,previewY+32-TrackerHud.y(hud(),height)*factor);g.pose().scale(factor,factor);
            TrackerHud.draw(g,mod,hud(),tab==1,true);g.pose().popMatrix();
            g.drawString(font,hud().enabled?"Visible in game":"Hidden in game",previewX+12,previewY+ph-15,0xFF8D9BB2,false);
        }
        for (var c : captions) g.drawString(font, c.text(), c.x(), c.y(), c.color(), false);
        int available=Math.max(50,panelWidth-240);
        String status=font.width(mod.status)>available?font.plainSubstrByWidth(mod.status,available-font.width("..."))+"...":mod.status;
        g.drawString(font, status, left + 120, top + panelHeight - 19, 0xFFA7B4CB, false);
        super.render(g, mx, my, delta);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; } return super.keyPressed(event);
    }
    @Override public void onClose() { mod.save(); minecraft.setScreen(parent); }
}
