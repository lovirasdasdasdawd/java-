package dev.donuttracker.fabric;

import dev.donuttracker.core.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Releases the gameplay cursor so the on-card return buttons can be clicked. */
final class PaymentActionScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private PaymentQueue.Payment displayed;
    PaymentActionScreen(DonutTrackerClient mod, Screen parent) { super(Component.literal("Payment return controls")); this.mod = mod; this.parent = parent; }
    @Override public boolean isPauseScreen() { return false; }
    @Override protected void init() {
        displayed = mod.queue.active();
        Settings.Hud hud = mod.settings.queue;
        int x = TrackerHud.x(hud,width), y = TrackerHud.y(hud,height);
        for (int multiplier : new int[]{2,3}) {
            PaymentQueue.Payment expected = displayed;
            int bx = x + (int)Math.floor(TrackerHud.returnButtonX(multiplier)*hud.scale);
            int by = y + (int)Math.floor(TrackerHud.returnButtonY(hud)*hud.scale);
            int w = Math.max(12,(int)Math.ceil(24*hud.scale)), h = Math.max(10,(int)Math.ceil(14*hud.scale));
            Button button = new Button.Plain(bx,by,w,h,Component.literal(multiplier+"x"), b -> {
                mod.act(() -> mod.returnPayment(expected,multiplier)); rebuildWidgets();
            }, supplier -> supplier.get()) {
                @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta) {
                    int color = 0xFF000000 | hud.accent;
                    TrackerHud.rounded(g,getX(),getY(),getX()+getWidth(),getY()+getHeight(),active ? 0xFF242A39 : 0xFF1C202B);
                    TrackerHud.border(g,getX(),getY(),getX()+getWidth(),getY()+getHeight(),isHoveredOrFocused() && active ? color : 0xFF555D71);
                    g.pose().pushMatrix();g.pose().translate((float)(getX()+getWidth()/2),(float)(getY()+getHeight()/2));
                    float scale=(float)(hud.scale*.7);g.pose().scale(scale,scale);
                    g.drawString(font,getMessage(),-font.width(getMessage())/2,-4,active?color:0xFF737A8E,false);g.pose().popMatrix();
                }
            };
            button.active = expected != null && !expected.returnRequested();
            button.visible = expected != null && !expected.returnRequested();
            if (button.active) button.setTooltip(Tooltip.create(Component.literal("Sends /"+ReturnPayments.command(expected,multiplier))));
            addRenderableWidget(button);
        }
        addRenderableWidget(Button.builder(Component.literal("Next player"),b -> { mod.queue.next();mod.save();rebuildWidgets(); }).bounds(width/2-106,height-30,102,20).build());
        addRenderableWidget(Button.builder(Component.literal("Close [P / Esc]"),b -> onClose()).bounds(width/2+4,height-30,102,20).build());
    }
    @Override public void tick() { if (displayed != mod.queue.active()) rebuildWidgets(); }
    @Override public void render(GuiGraphics g,int mx,int my,float delta) {
        TrackerHud.draw(g,mod,mod.settings.profit,false,false);
        TrackerHud.draw(g,mod,mod.settings.queue,true,true);
        g.drawCenteredString(font,"Click 2x / 3x to send /pay to the displayed IGN",width/2,height-61,0xFFFFFFFF);
        g.drawCenteredString(font,font.plainSubstrByWidth(mod.status,Math.max(100,width-20)),width/2,height-47,0xFFA7B4CB);
        super.render(g,mx,my,delta);
    }
    @Override public boolean keyPressed(KeyEvent e) { if (e.key()==GLFW.GLFW_KEY_P) { onClose();return true; }return super.keyPressed(e); }
    @Override public void onClose() { mod.save();minecraft.setScreen(parent); }
}
