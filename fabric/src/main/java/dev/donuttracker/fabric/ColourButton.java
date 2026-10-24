package dev.donuttracker.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

final class ColourButton extends Button.Plain {
    private final int colour;
    ColourButton(int x, int y, int w, int h, int colour, String label, Runnable action) {
        super(x, y, w, h, Component.literal(label), b -> action.run(), DEFAULT_NARRATION); this.colour = colour;
    }
    @Override protected void renderContents(GuiGraphics g, int mx, int my, float delta) {
        int x = getX(), y = getY();
        TrackerHud.rounded(g, x, y, x + getWidth(), y + getHeight(), 0xFF000000 | colour);
        TrackerHud.border(g, x, y, x + getWidth(), y + getHeight(), isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF85899F);
        int light = ((colour >> 16 & 255) * 299 + (colour >> 8 & 255) * 587 + (colour & 255) * 114) / 1000;
        g.drawCenteredString(Minecraft.getInstance().font, getMessage(), x + getWidth() / 2, y + (getHeight() - 8) / 2, light > 150 ? 0xFF10121B : 0xFFFFFFFF);
    }
}
