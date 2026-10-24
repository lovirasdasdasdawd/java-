package dev.donuttracker.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.regex.Pattern;

public final class Settings {
    public enum Theme { CLEAN, NEON }
    public enum Layout { COMPACT, STACKED, MINIMAL }
    public static final class Hud {
        public boolean enabled = true, showProfit = true, showTimer = true, showRate = true, showIcon = true;
        public Theme theme = Theme.CLEAN;
        public Layout layout = Layout.COMPACT;
        public int accent = 0xCB8CF6, text = 0xE9EAF2, positive = 0x39DA94, negative = 0xFF657A, background = 0x10131F;
        public double scale = 1.0, opacity = .90;
        public int x = 16, y = 16;
    }
    public final Hud profit = new Hud(), queue = new Hud();
    public BigDecimal minimumPayment = new BigDecimal("100000");
    // Anchored patterns: capture only confirmed balance/payment lines, never arbitrary chat amounts.
    public String balancePattern = "(?i)^(?:you have|(?:your )?balance\\s*[:»])\\s*\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*$";
    public static final String LEGACY_PAYMENT_PATTERN = "(?i)^([A-Za-z0-9_]{1,16})\\s+(?:paid you|sent you)\\s+\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*!?$";
    public static final String DEFAULT_PAYMENT_PATTERN = "(?i)^([A-Za-z0-9_]{1,16})\\s+(?:paid you|sent you)\\s+\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*\\$?\\s*[!.]?$";
    public String paymentPattern = DEFAULT_PAYMENT_PATTERN;
    // One amount capture shared by outgoing confirmations and explicit monetary loss lines.
    public String expensePattern = "(?i)^you\\s+(?:(?:have\\s+)?(?:successfully\\s+)?(?:paid|sent)\\s+(?:[A-Za-z0-9_]{1,16}\\s+|(?=\\$|[0-9]))|lost\\s+)\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*\\$?(?:\\s+to\\s+[A-Za-z0-9_]{1,16})?\\s*[!.]?$";
    public Settings() { queue.x = 280; queue.y = 16; queue.accent = 0x70CFEA; }
    public static int color(String hex) {
        String s = hex.strip().replaceFirst("^#", "");
        if (!s.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Use a six-digit HEX colour, e.g. #CB8CF6.");
        return Integer.parseInt(s, 16);
    }
    public void validatePatterns() {
        if (Pattern.compile(balancePattern).matcher("").groupCount() != 1) throw new IllegalArgumentException("Balance pattern needs one capture group: amount.");
        if (Pattern.compile(expensePattern).matcher("").groupCount() != 1) throw new IllegalArgumentException("Expense pattern needs one capture group: amount.");
        if (Pattern.compile(paymentPattern).matcher("").groupCount() != 2) throw new IllegalArgumentException("Payment pattern needs two capture groups: player, amount.");
    }
    public static Settings load(Path file) throws IOException {
        Settings s = new Settings();
        if (!Files.exists(file)) return s;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file)) { p.load(r); }
        try {
            read(p, "profit.", s.profit); read(p, "queue.", s.queue);
            s.minimumPayment = Money.parse(p.getProperty("queue.minimum", "100K"));
            if (s.minimumPayment.signum() < 0) throw new IllegalArgumentException("Minimum payment must be nonnegative.");
            s.balancePattern = p.getProperty("balance.pattern", s.balancePattern);
            s.expensePattern = p.getProperty("expense.pattern", s.expensePattern);
            s.paymentPattern = p.getProperty("payment.pattern", s.paymentPattern);
            if (s.paymentPattern.equals(LEGACY_PAYMENT_PATTERN)) s.paymentPattern = DEFAULT_PAYMENT_PATTERN;
            s.validatePatterns();
        } catch (IllegalArgumentException e) { throw new IOException("Invalid settings in " + file + ": " + e.getMessage(), e); }
        return s;
    }
    private static void read(Properties p, String k, Hud h) {
        h.enabled = Boolean.parseBoolean(p.getProperty(k + "enabled", "true"));
        h.theme = Theme.valueOf(p.getProperty(k + "theme", "CLEAN"));
        h.layout = Layout.valueOf(p.getProperty(k + "layout", "COMPACT"));
        h.accent = color(p.getProperty(k + "accent", String.format("%06X", h.accent)));
        h.text = color(p.getProperty(k + "text", "E9EAF2")); h.positive = color(p.getProperty(k + "positive", "39DA94"));
        h.negative = color(p.getProperty(k + "negative", "FF657A")); h.background = color(p.getProperty(k + "background", "10131F"));
        h.scale = bounded(p.getProperty(k + "scale", "1"), .5, 2);
        h.opacity = bounded(p.getProperty(k + "opacity", ".9"), 0, 1);
        h.x = Math.max(0, Integer.parseInt(p.getProperty(k + "x", "" + h.x)));
        h.y = Math.max(0, Integer.parseInt(p.getProperty(k + "y", "" + h.y)));
        h.showProfit = Boolean.parseBoolean(p.getProperty(k + "showProfit", "true"));
        h.showTimer = Boolean.parseBoolean(p.getProperty(k + "showTimer", "true"));
        h.showRate = Boolean.parseBoolean(p.getProperty(k + "showRate", "true"));
        h.showIcon = Boolean.parseBoolean(p.getProperty(k + "showIcon", "true"));
    }
    private static double bounded(String value, double min, double max) {
        double n = Double.parseDouble(value);
        if (!Double.isFinite(n)) throw new IllegalArgumentException("Size/opacity must be finite.");
        return Math.max(min, Math.min(max, n));
    }
    public void save(Path file) throws IOException {
        validatePatterns();
        Properties p = new Properties(); write(p, "profit.", profit); write(p, "queue.", queue);
        p.setProperty("queue.minimum", minimumPayment.toPlainString());
        p.setProperty("balance.pattern", balancePattern); p.setProperty("payment.pattern", paymentPattern); p.setProperty("expense.pattern", expensePattern);
        atomicSave(file, p, "Donut Tracker — per-HUD design and server detection patterns");
    }
    private static void write(Properties p, String k, Hud h) {
        p.setProperty(k + "enabled", "" + h.enabled); p.setProperty(k + "theme", h.theme.name()); p.setProperty(k + "layout", h.layout.name());
        p.setProperty(k + "accent", String.format("%06X", h.accent)); p.setProperty(k + "text", String.format("%06X", h.text));
        p.setProperty(k + "positive", String.format("%06X", h.positive)); p.setProperty(k + "negative", String.format("%06X", h.negative));
        p.setProperty(k + "background", String.format("%06X", h.background));
        p.setProperty(k + "scale", "" + h.scale); p.setProperty(k + "opacity", "" + h.opacity);
        p.setProperty(k + "x", "" + h.x); p.setProperty(k + "y", "" + h.y);
        p.setProperty(k + "showProfit", "" + h.showProfit); p.setProperty(k + "showTimer", "" + h.showTimer);
        p.setProperty(k + "showRate", "" + h.showRate); p.setProperty(k + "showIcon", "" + h.showIcon);
    }
    static void atomicSave(Path file, Properties p, String comment) throws IOException {
        Path absolute = file.toAbsolutePath(); Files.createDirectories(absolute.getParent());
        Path temp = Files.createTempFile(absolute.getParent(), "donut-", ".tmp");
        try {
            try (Writer w = Files.newBufferedWriter(temp)) { p.store(w, comment); }
            try { Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) { Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
