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
    public enum UiMotion { FULL, REDUCED, OFF }
    public UiMotion uiMotion=UiMotion.FULL;
    public enum MenuPalette { SEA_GLASS, DUSK, PAPER, AURORA, SLATE, COFFEE, LAVENDER, MOSS, GLACIER, ROSE }
    public enum OceanTrack { SHORELINE, OPEN_SEA, RAINY_COAST, AURORA_DRIFT, DEEP_SPACE, CITY_RAIN, FOREST_BREEZE, DESERT_WIND, SNOW_HUSH, ABYSSAL_DRIFT, GARDEN_CHIMES, DISTANT_STORM, EMBER_WIND, MARSH_NIGHT, NIGHT_RAIL, HARBOR_WIND, RIVER_FLOW, CLOUD_DRIFT, LAKE_NIGHT, BAMBOO_RUSTLE }
    public enum BackgroundScene { OCEAN, AURORA, SPACE, CITY, FOREST, DESERT, SNOW, ABYSS, SAKURA, STORM, VOLCANO, MARSH, RAILWAY, LIGHTHOUSE, RIVER, CLOUDS, MOON, BAMBOO }
    public static final class SceneDesign {
        public double intensity=.75, density=.6, speed=.4;
        public int tint=0xA9B8FA;
    }
    public final java.util.EnumMap<BackgroundScene,SceneDesign> sceneDesigns=new java.util.EnumMap<>(BackgroundScene.class);
    public SceneDesign sceneDesign(){return sceneDesigns.get(backgroundScene);}
    public enum Theme { CLEAN, NEON, GLASS }
    public enum Layout { COMPACT, STACKED, MINIMAL }
    public static final class Hud {
        public boolean enabled = true, showProfit = true, showTimer = true, showRate = true, showIcon = true;
        public Theme theme = Theme.CLEAN;
        public Layout layout = Layout.COMPACT;
        public int accent = 0x90D7C2, text = 0xF3E8CB, positive = 0xA5E3B7, negative = 0xF4A296, background = 0x0D2733;
        public double scale = 1.0, opacity = .90;
        public int x = 16, y = 16;
    }
    public final Hud profit = new Hud(), queue = new Hud();
    public MenuPalette menuPalette = MenuPalette.AURORA;
    public BackgroundScene backgroundScene = BackgroundScene.AURORA;
    public OceanTrack oceanTrack = OceanTrack.AURORA_DRIFT;
    public boolean oceanEnabled = true, oceanInGame = false, menuMotion = true;
    public boolean menuGlass=false;
    public double menuOpacity=.76;
    public double auroraGlow = .85;
    public double oceanVolume = .18;
    public boolean oceanShouldPlay(boolean moduleMenu,boolean inGame){return oceanEnabled&&oceanVolume>0&&(moduleMenu||oceanInGame&&inGame);}
    public boolean advanceOnLever = true;
    public BigDecimal minimumPayment = new BigDecimal("100000");
    // Anchored patterns: capture only confirmed balance/payment lines, never arbitrary chat amounts.
    public String balancePattern = "(?i)^(?:you have|(?:your )?balance\\s*[:»])\\s*\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*$";
    public static final String LEGACY_PAYMENT_PATTERN = "(?i)^([A-Za-z0-9_]{1,16})\\s+(?:paid you|sent you)\\s+\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*!?$";
    public static final String DEFAULT_PAYMENT_PATTERN = "(?i)^([A-Za-z0-9_]{1,16})\\s+(?:paid you|sent you)\\s+\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*\\$?\\s*[!.]?$";
    public String paymentPattern = DEFAULT_PAYMENT_PATTERN;
    // One amount capture shared by outgoing confirmations and explicit monetary loss lines.
    public String expensePattern = "(?i)^you\\s+(?:(?:have\\s+)?(?:successfully\\s+)?(?:paid|sent)\\s+(?:[A-Za-z0-9_]{1,16}\\s+|(?=\\$|[0-9]))|lost\\s+)\\$?\\s*([0-9][0-9,.]*[KMBT]?)\\s*\\$?(?:\\s+to\\s+[A-Za-z0-9_]{1,16})?\\s*[!.]?$";
    public Settings() { for(var scene:BackgroundScene.values()){var d=new SceneDesign();if(scene==BackgroundScene.AURORA)d.intensity=.85;d.tint=switch(scene){case AURORA->0x68EFBE;case OCEAN->0x83C7BB;case SPACE->0xA9B8FA;case CITY->0xF2AE91;case FOREST->0xE7AD68;case DESERT->0xEBA56C;case SNOW->0xC6DBF1;case ABYSS->0x50CFC8;case SAKURA->0xE9A5BB;case STORM->0x93ACC6;case VOLCANO->0xF09059;case MARSH->0xB5D78C;case RAILWAY->0xA5B9E4;case LIGHTHOUSE->0xE7C694;case RIVER->0x92C4B4;case CLOUDS->0xD5C7ED;case MOON->0xB8D8E9;case BAMBOO->0xA8C58C;};sceneDesigns.put(scene,d);} queue.x = 280; queue.y = 16; queue.accent = 0xB6C6E9; }
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
            for(var scene:BackgroundScene.values()){var d=s.sceneDesigns.get(scene);String k="scene."+scene.name()+".";
                d.intensity=bounded(p.getProperty(k+"intensity",scene==BackgroundScene.AURORA?p.getProperty("menu.auroraGlow",".85"):".75"),0,1);d.density=bounded(p.getProperty(k+"density",".6"),0,1);d.speed=bounded(p.getProperty(k+"speed",".4"),0,1);d.tint=color(p.getProperty(k+"tint",String.format("%06X",d.tint)));}
            s.menuOpacity=bounded(p.getProperty("menu.panelOpacity",".76"),.3,1);
            s.uiMotion=UiMotion.valueOf(p.getProperty("menu.uiMotion","FULL"));
            s.menuGlass=Boolean.parseBoolean(p.getProperty("menu.glass","false"));
            s.menuPalette=MenuPalette.valueOf(p.getProperty("menu.palette",MenuPalette.SEA_GLASS.name()));
            s.auroraGlow=bounded(p.getProperty("menu.auroraGlow","0.85"),0,1);
            s.backgroundScene=BackgroundScene.valueOf(p.getProperty("menu.background",BackgroundScene.OCEAN.name()));
            s.oceanTrack=OceanTrack.valueOf(p.getProperty("ocean.track",OceanTrack.SHORELINE.name()));
            s.oceanEnabled=Boolean.parseBoolean(p.getProperty("ocean.enabled","true"));
            s.oceanInGame=Boolean.parseBoolean(p.getProperty("ocean.inGame","false"));
            s.menuMotion=Boolean.parseBoolean(p.getProperty("menu.motion","true"));
            s.oceanVolume=bounded(p.getProperty("ocean.volume","0.18"),0,1);
            read(p, "profit.", s.profit); read(p, "queue.", s.queue);
            s.advanceOnLever = Boolean.parseBoolean(p.getProperty("queue.leverAdvance", "true"));
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
        h.text = color(p.getProperty(k + "text", String.format("%06X", h.text))); h.positive = color(p.getProperty(k + "positive", String.format("%06X", h.positive)));
        h.negative = color(p.getProperty(k + "negative", String.format("%06X", h.negative))); h.background = color(p.getProperty(k + "background", String.format("%06X", h.background)));
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
        for(var scene:BackgroundScene.values()){var d=sceneDesigns.get(scene);String k="scene."+scene.name()+".";p.setProperty(k+"intensity",Double.toString(Math.clamp(d.intensity,0,1)));p.setProperty(k+"density",Double.toString(Math.clamp(d.density,0,1)));p.setProperty(k+"speed",Double.toString(Math.clamp(d.speed,0,1)));p.setProperty(k+"tint",String.format("%06X",d.tint));}
        p.setProperty("menu.panelOpacity",Double.toString(Math.clamp(menuOpacity,.3,1)));
        p.setProperty("menu.uiMotion",uiMotion.name());
        p.setProperty("menu.glass",Boolean.toString(menuGlass));
        p.setProperty("menu.palette",menuPalette.name());p.setProperty("menu.background",backgroundScene.name());p.setProperty("menu.auroraGlow",Double.toString(Math.clamp(auroraGlow,0,1)));p.setProperty("menu.motion",Boolean.toString(menuMotion));
        p.setProperty("ocean.enabled",Boolean.toString(oceanEnabled));p.setProperty("ocean.inGame",Boolean.toString(oceanInGame));
        p.setProperty("ocean.track",oceanTrack.name());p.setProperty("ocean.volume",Double.toString(Math.clamp(oceanVolume,0,1)));
        p.setProperty("queue.minimum", minimumPayment.toPlainString());
        p.setProperty("queue.leverAdvance",Boolean.toString(advanceOnLever));
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
