package dev.donuttracker.core;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.List;
import java.util.regex.Pattern;

public final class PaymentQueue {
    public record Payment(String player, BigDecimal amount, long receivedAt) {}
    private final ArrayDeque<Payment> payments = new ArrayDeque<>();
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    public boolean add(String player, BigDecimal amount, BigDecimal minimum) {
        if (!NAME.matcher(player).matches()) throw new IllegalArgumentException("Use a valid Minecraft player name.");
        if (amount.signum() <= 0 || amount.compareTo(minimum) < 0) return false;
        if (payments.size() >= 500) throw new IllegalStateException("Queue is full (500 players). Advance or clear it first.");
        payments.add(new Payment(player, amount, System.currentTimeMillis())); return true;
    }
    public Payment active() { return payments.peek(); }
    public Payment next() { return payments.poll(); }
    public int waiting() { return Math.max(0, payments.size() - 1); }
    public List<Payment> all() { return List.copyOf(payments); }
    public void clear() { payments.clear(); }
    public void restore(List<Payment> saved) {
        payments.clear();
        saved.stream().limit(500).filter(p -> NAME.matcher(p.player()).matches() && p.amount().signum() > 0).forEach(payments::add);
    }
}
