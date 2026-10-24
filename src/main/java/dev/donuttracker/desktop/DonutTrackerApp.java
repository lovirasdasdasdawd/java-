package dev.donuttracker.desktop;

import dev.donuttracker.core.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/** Executable design companion; the Fabric subproject provides the in-game integration. */
public final class DonutTrackerApp {
    private static final Color BG = new Color(0x0B0E19), PANEL = new Color(0x131827), TEXT = new Color(0xE9EAF2), MUTED = new Color(0x929CB4), ACCENT = new Color(0xCB8CF6);
    private final Path settingsFile, stateFile;
    private final Settings settings;
    private final Tracker tracker = new Tracker();
    private final PaymentQueue queue = new PaymentQueue();
    private final JPanel root = new JPanel(new BorderLayout(18, 18));
    private final JLabel status = new JLabel("Desktop companion • manual data • Minecraft mod built separately");
    private final HudPreview preview;
    private final JTabbedPane tabs = new JTabbedPane();
    private DonutTrackerApp(Path directory) throws IOException {
        settingsFile = directory.resolve("settings.properties"); stateFile = directory.resolve("sessions.properties");
        settings = Settings.load(settingsFile); StateStore.load(stateFile, tracker, queue);
        preview = new HudPreview(settings, tracker, queue, this::save);
        root.setBackground(BG); root.setBorder(new EmptyBorder(22, 24, 16, 24));
        JPanel header = panel(new BorderLayout());
        JLabel title = label("◉  DONUT TRACKER", 24, ACCENT); title.setFont(title.getFont().deriveFont(Font.BOLD));
        header.add(title, BorderLayout.NORTH); header.add(label("Your session. Your style.   /   RIGHT SHIFT · SETTINGS", 11, MUTED), BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);
        JPanel body = panel(new BorderLayout(0, 16));
        JPanel live = panel(new BorderLayout(0, 8)); live.add(label("LIVE HUD PREVIEW   ·   Drag each card to reposition", 11, MUTED), BorderLayout.NORTH); live.add(preview);
        body.add(live, BorderLayout.NORTH);
        tabs.addTab("Profit Tracker", design(settings.profit, false));
        tabs.addTab("Payment Queue", design(settings.queue, true));
        tabs.addTab("Sessions", sessions()); tabs.addTab("Detection", detection());
        body.add(tabs); root.add(body);
        JPanel footer = panel(new BorderLayout(16, 0)); footer.add(status); footer.add(button("Save my design", this::save), BorderLayout.EAST); status.setForeground(MUTED); root.add(footer, BorderLayout.SOUTH);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "settings");
        root.getActionMap().put("settings", new AbstractAction() { public void actionPerformed(ActionEvent e) { tabs.setSelectedIndex(0); tabs.requestFocusInWindow(); } });
    }
    private JPanel design(Settings.Hud h, boolean payment) {
        JPanel content = column();
        JPanel row = row(); JCheckBox enabled = check("HUD enabled", h.enabled, () -> { h.enabled = !h.enabled; changed(); }); row.add(enabled);
        row.add(label("Style", 12, MUTED)); JComboBox<Settings.Theme> theme = new JComboBox<>(Settings.Theme.values()); theme.setSelectedItem(h.theme);
        theme.addActionListener(e -> { h.theme = (Settings.Theme) theme.getSelectedItem(); changed(); }); row.add(theme);
        row.add(label("Layout", 12, MUTED)); JComboBox<Settings.Layout> layout = new JComboBox<>(Settings.Layout.values()); layout.setSelectedItem(h.layout);
        layout.addActionListener(e -> { h.layout = (Settings.Layout) layout.getSelectedItem(); changed(); }); row.add(layout); content.add(row);
        JPanel colors = row(); colors.add(label("Colours", 12, MUTED));
        colors.add(colorButton("Accent", h.accent, c -> h.accent = c)); colors.add(colorButton("Text", h.text, c -> h.text = c));
        colors.add(colorButton("Profit +", h.positive, c -> h.positive = c)); colors.add(colorButton("Loss −", h.negative, c -> h.negative = c));
        colors.add(colorButton("Background", h.background, c -> h.background = c)); content.add(colors);
        JPanel presets = row(); presets.add(label("Quick palette", 12, MUTED));
        int[] palette = {0xCB8CF6, 0xF277D7, 0x70CFEA, 0x39DA94, 0xFFCC74};
        for (int color : palette) { JButton swatch = button("●", () -> { h.accent = color; changed(); }); swatch.setForeground(new Color(color)); presets.add(swatch); } content.add(presets);
        JSlider scale = new JSlider(50, 200, (int)(h.scale * 100)); JSlider opacity = new JSlider(0, 100, (int)(h.opacity * 100));
        JLabel scaleLabel = label("HUD size  " + scale.getValue() + "%", 12, TEXT), opacityLabel = label("Background opacity  " + opacity.getValue() + "%", 12, TEXT);
        scale.addChangeListener(e -> { h.scale = scale.getValue() / 100.0; scaleLabel.setText("HUD size  " + scale.getValue() + "%"); preview.repaint(); if (!scale.getValueIsAdjusting()) save(); });
        opacity.addChangeListener(e -> { h.opacity = opacity.getValue() / 100.0; opacityLabel.setText("Background opacity  " + opacity.getValue() + "%"); preview.repaint(); if (!opacity.getValueIsAdjusting()) save(); });
        JPanel sliders = new JPanel(new GridLayout(2, 2, 12, 0)); sliders.setOpaque(false); sliders.add(scaleLabel); sliders.add(opacityLabel); sliders.add(scale); sliders.add(opacity); content.add(sliders);
        JPanel toggles = row();
        if (!payment) {
            toggles.add(check("Profit", h.showProfit, () -> { h.showProfit = !h.showProfit; changed(); }));
            toggles.add(check("Timer", h.showTimer, () -> { h.showTimer = !h.showTimer; changed(); }));
            toggles.add(check("Money/hour", h.showRate, () -> { h.showRate = !h.showRate; changed(); }));
        }
        toggles.add(check("Donut icon", h.showIcon, () -> { h.showIcon = !h.showIcon; changed(); }));
        toggles.add(button("Reset position", () -> { h.x = payment ? 280 : 16; h.y = 16; changed(); })); content.add(toggles);
        content.add(label(payment ? "PAYMENT QUEUE   /   One active player, up to 499 waiting" : "SESSION CONTROLS   /   Set a starting balance before tracking", 11, MUTED));
        if (payment) {
            JPanel controls = row(); JTextField name = field("", 10), amount = field("100K", 8), minimum = field(settings.minimumPayment.toPlainString(), 8);
            controls.add(label("IGN", 12, TEXT)); controls.add(name); controls.add(label("Paid", 12, TEXT)); controls.add(amount);
            controls.add(button("Add payment", () -> safely(() -> { boolean added = queue.add(name.getText().strip(), Money.parse(amount.getText()), settings.minimumPayment); status.setText(added ? "Payment added." : "Below the minimum payment."); changed(); })));
            controls.add(button("Next player →", () -> { queue.next(); changed(); })); content.add(controls);
            JPanel minRow = row(); minRow.add(label("Minimum payment", 12, MUTED)); minRow.add(minimum);
            minRow.add(button("Apply", () -> safely(() -> { var value = Money.parse(minimum.getText()); if (value.signum() < 0) throw new IllegalArgumentException("Minimum must be nonnegative."); settings.minimumPayment = value; changed(); })));
            minRow.add(button("Clear queue", () -> { queue.clear(); changed(); })); content.add(minRow);
        } else {
            JPanel controls = row(); JTextField balance = field("", 10); controls.add(label("Current balance", 12, TEXT)); controls.add(balance);
            controls.add(button("Update", () -> safely(() -> { tracker.balance(Money.parse(balance.getText())); status.setText("Balance updated: " + Money.format(tracker.balance())); changed(); })));
            controls.add(button("▶ Start", () -> safely(() -> { tracker.start(); changed(); status.setText("Tracking started. Update the balance to calculate profit."); })));
            controls.add(button("■ End", () -> safely(() -> { tracker.stop(); changed(); status.setText("Session saved. Profit: " + Money.signed(tracker.profit())); })));
            controls.add(button("Reset", () -> { tracker.reset(); changed(); })); content.add(controls);
        }
        content.add(label("Clean uses subtle borders. Neon adds an accent glow. Both HUDs have independent settings.", 11, MUTED));
        return content;
    }
    private JPanel sessions() {
        JPanel p = panel(new BorderLayout(0, 12)); p.setBorder(new EmptyBorder(18, 14, 14, 14));
        JTextArea history = textArea(""); history.setEditable(false);
        Runnable refresh = () -> {
            StringBuilder s = new StringBuilder("COMPLETED SESSIONS\n\n");
            for (var session : tracker.history()) s.append(java.time.Instant.ofEpochMilli(session.endedAt())).append("   ").append(Money.signed(session.profit())).append("   ").append((session.endedAt() - session.startedAt()) / 1000).append("s\n");
            if (tracker.history().isEmpty()) s.append("No completed sessions yet. Start and end a session to record it."); history.setText(s.toString());
        };
        p.add(new JScrollPane(history)); p.add(button("Refresh history", refresh), BorderLayout.SOUTH); tabs.addChangeListener(e -> refresh.run()); refresh.run(); return p;
    }
    private JPanel detection() {
        JPanel p = column();
        p.add(label("SERVER MESSAGE MATCHING", 13, ACCENT));
        p.add(label("Defaults are examples, not confirmed DonutSMP formats. Edit the regex for your server.", 12, MUTED));
        JTextField balance = field(settings.balancePattern, 50), payment = field(settings.paymentPattern, 50), message = field("Balance: $1.25M", 50);
        p.add(label("Balance regex · one group: amount", 12, TEXT)); p.add(balance);
        p.add(label("Payment regex · two groups: player, amount", 12, TEXT)); p.add(payment);
        p.add(button("Save detection patterns", () -> safely(() -> {
            String oldB = settings.balancePattern, oldP = settings.paymentPattern;
            settings.balancePattern = balance.getText(); settings.paymentPattern = payment.getText();
            try { settings.validatePatterns(); } catch (RuntimeException ex) { settings.balancePattern = oldB; settings.paymentPattern = oldP; throw ex; }
            changed(); status.setText("Detection patterns saved.");
        })));
        p.add(label("Test a message locally (updates this companion only)", 12, MUTED)); p.add(message);
        p.add(button("Process message", () -> safely(() -> { var result = new ChatParser(settings).accept(message.getText(), tracker, queue); changed(); status.setText("Message result: " + result); })));
        return p;
    }
    private JButton colorButton(String name, int color, java.util.function.IntConsumer setter) {
        JButton b = button(name, () -> {}); b.setForeground(name.equals("Background") ? TEXT : new Color(color));
        b.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(root, name + " colour — #RRGGBB", String.format("#%06X", b.getForeground().getRGB() & 0xFFFFFF));
            if (input != null) safely(() -> { int c = Settings.color(input); setter.accept(c); b.setForeground(new Color(c)); changed(); });
        }); return b;
    }
    private void safely(Runnable action) { try { action.run(); } catch (RuntimeException ex) { status.setText(ex.getMessage()); Toolkit.getDefaultToolkit().beep(); } }
    private void changed() { preview.repaint(); save(); }
    private void save() {
        try { settings.save(settingsFile); StateStore.save(stateFile, tracker, queue); }
        catch (IOException ex) { status.setText("Could not save: " + ex.getMessage()); }
    }
    private static JPanel panel(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    private static JPanel row() { return panel(new FlowLayout(FlowLayout.LEFT, 8, 6)); }
    private static JPanel column() { JPanel p = panel(null); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(12, 10, 8, 10)); return p; }
    private static JLabel label(String text, int size, Color color) { JLabel l = new JLabel(text); l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size)); l.setForeground(color); return l; }
    private static JButton button(String title, Runnable action) { JButton b = new JButton(title); b.setFocusPainted(false); b.addActionListener(e -> action.run()); return b; }
    private static JCheckBox check(String text, boolean selected, Runnable action) { JCheckBox c = new JCheckBox(text, selected); c.setOpaque(false); c.addActionListener(e -> action.run()); return c; }
    private static JTextField field(String text, int columns) { JTextField f = new JTextField(text, columns); f.setMaximumSize(new Dimension(1000, 30)); return f; }
    private static JTextArea textArea(String text) { JTextArea a = new JTextArea(text); a.setBackground(PANEL); a.setForeground(TEXT); a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); a.setBorder(new EmptyBorder(12, 12, 12, 12)); return a; }
    private static void appearance() {
        UIManager.put("Panel.background", BG); UIManager.put("Label.foreground", TEXT); UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("Button.background", PANEL); UIManager.put("Button.foreground", TEXT);
        UIManager.put("TextField.background", PANEL); UIManager.put("TextField.foreground", TEXT); UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("ComboBox.background", PANEL); UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("TabbedPane.background", BG); UIManager.put("TabbedPane.foreground", TEXT); UIManager.put("TabbedPane.selected", PANEL);
        UIManager.put("TabbedPane.contentAreaColor", PANEL); UIManager.put("Slider.background", BG); UIManager.put("Slider.foreground", ACCENT);
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--help")) {
            System.out.println("Donut Tracker desktop companion\njava -jar donut-tracker-1.0.0.jar [--config DIRECTORY]\n--render FILE.png  Render the actual UI headlessly for validation.\nFor Minecraft use the separate Fabric JAR in .minecraft/mods."); return;
        }
        boolean render = args.length == 2 && args[0].equals("--render");
        if (!render && GraphicsEnvironment.isHeadless()) { System.err.println("A graphical desktop is required. Use --help or --render FILE.png; install the Fabric JAR for in-game HUDs."); System.exit(2); }
        Path directory = args.length == 2 && args[0].equals("--config") ? Path.of(args[1]) : Path.of(System.getProperty("user.home"), ".donut-tracker");
        if (render) directory = java.nio.file.Files.createTempDirectory("donut-render-");
        Path config = directory;
        SwingUtilities.invokeAndWait(() -> {
            try {
                appearance(); DonutTrackerApp app = new DonutTrackerApp(config);
                if (render) {
                    app.root.setSize(1020, 790); layoutRecursively(app.root);
                    BufferedImage image = new BufferedImage(1020, 790, BufferedImage.TYPE_INT_ARGB); Graphics2D g = image.createGraphics(); app.root.printAll(g); g.dispose(); ImageIO.write(image, "png", Path.of(args[1]).toFile());
                    System.out.println("Rendered real settings UI to " + args[1]); return;
                }
                JFrame frame = new JFrame("Donut Tracker · Desktop companion"); frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setContentPane(app.root); frame.setSize(1020, 790); frame.setMinimumSize(new Dimension(920, 740)); frame.setLocationRelativeTo(null);
                Timer timer = new Timer(250, e -> app.preview.repaint()); timer.start();
                frame.addWindowListener(new WindowAdapter() { public void windowClosed(WindowEvent e) { timer.stop(); app.save(); } }); frame.setVisible(true);
            } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
        });
    }
    private static void layoutRecursively(Container c) { c.doLayout(); for (Component child : c.getComponents()) if (child instanceof Container container) layoutRecursively(container); }
}
