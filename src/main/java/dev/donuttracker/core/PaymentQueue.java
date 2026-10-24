package dev.donuttracker.core;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.List;
import java.util.regex.Pattern;

public final class PaymentQueue {
    public record Payment(String player, BigDecimal amount, long receivedAt, int returnMultiplier) {
        public Payment(String player, BigDecimal amount, long receivedAt) { this(player, amount, receivedAt, 0); }
        public Payment {
            if (returnMultiplier != 0 && returnMultiplier != 2 && returnMultiplier != 3) throw new IllegalArgumentException("Invalid return multiplier.");
        }
        public boolean returnRequested() { return returnMultiplier != 0; }
    }
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
    Payment reserveReturn(Payment expected, int multiplier) {
        if (expected == null || payments.peek() != expected) throw new IllegalStateException("The active payment changed. Select the current player again.");
        if (expected.returnRequested()) throw new IllegalStateException("A return command was already requested for this payment.");
        Payment reserved = new Payment(expected.player(), expected.amount(), expected.receivedAt(), multiplier);
        payments.removeFirst(); payments.addFirst(reserved); return reserved;
    }
    void undoReservation(Payment reserved, Payment original) {
        if (payments.peek() == reserved) { payments.removeFirst(); payments.addFirst(original); }
    }
    public int waiting() { return Math.max(0, payments.size() - 1); }
    public List<Payment> all() { return List.copyOf(payments); }
    public void clear() { payments.clear(); }
    public void restore(List<Payment> saved) {
        payments.clear();
        saved.stream().limit(500).filter(p -> NAME.matcher(p.player()).matches() && p.amount().signum() > 0).forEach(payments::add);
    }
}
