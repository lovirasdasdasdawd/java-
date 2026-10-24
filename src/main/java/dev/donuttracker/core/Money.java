package dev.donuttracker.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Money {
    private static final Pattern VALUE = Pattern.compile("[+-]?(?:[0-9]+(?:\\.[0-9]+)?|\\.[0-9]+)[KMBT]?", Pattern.CASE_INSENSITIVE);
    private Money() {}
    public static BigDecimal parse(String input) {
        String s = input.strip().replace("$", "").replace(",", "").strip().toUpperCase(Locale.ROOT);
        if (!VALUE.matcher(s).matches()) throw new IllegalArgumentException("Enter an amount such as 100K, 2.75M or 100000.");
        char suffix = s.charAt(s.length() - 1);
        int power = switch (suffix) { case 'K' -> 3; case 'M' -> 6; case 'B' -> 9; case 'T' -> 12; default -> 0; };
        BigDecimal value = new BigDecimal(power == 0 ? s : s.substring(0, s.length() - 1));
        return value.scaleByPowerOfTen(power).setScale(2, RoundingMode.HALF_UP);
    }
    public static String format(BigDecimal value) {
        BigDecimal abs = value.abs();
        String[] suffixes = {"", "K", "M", "B", "T"};
        int unit = 0;
        while (abs.compareTo(new BigDecimal("1000")) >= 0 && unit < 4) { abs = abs.movePointLeft(3); unit++; }
        abs = abs.setScale(2, RoundingMode.HALF_UP);
        if (abs.compareTo(new BigDecimal("1000")) >= 0 && unit < 4) { abs = abs.movePointLeft(3).setScale(2, RoundingMode.HALF_UP); unit++; }
        return (value.signum() < 0 ? "-" : "") + abs.stripTrailingZeros().toPlainString() + suffixes[unit];
    }
    public static String signed(BigDecimal value) { return (value.signum() >= 0 ? "+" : "") + format(value); }
}
