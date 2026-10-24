package dev.donuttracker.desktop;

import dev.donuttracker.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/** A functional, draggable preview. It does not impersonate a Minecraft connection. */
public final class HudPreview extends JPanel {
    private final Settings settings;
    private final Tracker tracker;
    private final PaymentQueue queue;
    private final Runnable save;
    private Settings.Hud dragging;
    private int dx, dy;
    public HudPreview(Settings settings, Tracker tracker, PaymentQueue queue, Runnable save) {
        this.settings = settings; this.tracker = tracker; this.queue = queue; this.save = save;
        setPreferredSize(new Dimension(680, 162)); setBackground(new Color(0x090C15));
        MouseAdapter mouse = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                for (Settings.Hud h : new Settings.Hud[]{settings.queue, settings.profit}) {
                    if (bounds(h).contains(e.getPoint()) && h.enabled) { dragging = h; dx = e.getX() - h.x; dy = e.getY() - h.y; break; }
                }
            }
            public void mouseDragged(MouseEvent e) {
                if (dragging == null) return;
                dragging.x = Math.max(0, Math.min(getWidth() - bounds(dragging).width, e.getX() - dx));
                dragging.y = Math.max(0, Math.min(getHeight() - bounds(dragging).height, e.getY() - dy)); repaint();
            }
            public void mouseReleased(MouseEvent e) { if (dragging != null) { dragging = null; save.run(); } }
        };
        addMouseListener(mouse); addMouseMotionListener(mouse);
    }
    private Rectangle bounds(Settings.Hud h) {
        return new Rectangle(h.x, h.y, (int)(235 * h.scale), (int)((h.layout == Settings.Layout.MINIMAL ? 53 : h.layout == Settings.Layout.STACKED ? 112 : 84) * h.scale));
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x171C2C));
        for (int x = 12; x < getWidth(); x += 24) for (int y = 12; y < getHeight(); y += 24) g.fillOval(x, y, 2, 2);
        draw(g, settings.profit, false); draw(g, settings.queue, true); g.dispose();
    }
    private void draw(Graphics2D base, Settings.Hud h, boolean payment) {
        if (!h.enabled) return;
        Graphics2D g = (Graphics2D) base.create(); g.translate(h.x, h.y); g.scale(h.scale, h.scale);
        int height = h.layout == Settings.Layout.MINIMAL ? 53 : h.layout == Settings.Layout.STACKED ? 112 : 84;
        if (h.theme == Settings.Theme.NEON) {
            for (int i = 8; i > 0; i--) { g.setColor(new Color((h.accent & 0xFFFFFF) | (12 << 24), true)); g.setStroke(new BasicStroke(i * 2f)); g.drawRoundRect(1, 1, 232, height - 2, 14, 14); }
        }
        g.setColor(new Color((h.background & 0xFFFFFF) | ((int)(255 * h.opacity) << 24), true));
        g.fill(new RoundRectangle2D.Double(0, 0, 234, height, 14, 14));
        g.setStroke(new BasicStroke(1)); g.setColor(new Color(h.accent)); g.drawRoundRect(0, 0, 233, height - 1, 14, 14);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        if (h.showIcon) {
            g.setStroke(new BasicStroke(2)); g.drawOval(12, 12, 13, 13); g.drawOval(17, 17, 3, 3);
            g.fillOval(16, 10, 3, 3); g.fillOval(24, 16, 3, 3);
        }
        g.drawString(payment ? "Payment Queue" : "Donut Tracker", h.showIcon ? 33 : 12, 23);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        g.setColor(new Color(payment ? h.accent : tracker.running() ? h.positive : h.text));
        g.drawString(payment ? queue.waiting() + " WAITING" : tracker.running() ? "LIVE" : "IDLE", 174, 23);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(new Color(tracker.profit().signum() < 0 && !payment ? h.negative : h.positive));
        if (payment) {
            var active = queue.active();
            g.drawString(active == null ? "No payments" : "+" + Money.format(active.amount()), 12, 46);
        } else if (h.showProfit) g.drawString(Money.signed(tracker.profit()), 12, 46);
        if (h.layout != Settings.Layout.MINIMAL) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10)); g.setColor(new Color(h.text));
            if (payment) g.drawString(queue.active() == null ? "Waiting for an incoming payment" : queue.active().player(), 12, 66);
            else {
                if (h.showTimer) g.drawString(tracker.duration(), h.layout == Settings.Layout.STACKED ? 12 : 166, h.layout == Settings.Layout.STACKED ? 67 : 46);
                if (h.showRate) { g.setColor(new Color(h.accent)); g.drawString(Money.signed(tracker.perHour()) + "/hour", 12, h.layout == Settings.Layout.STACKED ? 88 : 68); }
            }
        }
        g.dispose();
    }
}
