package dev.donuttracker.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public final class Tracker {
    public record Session(long startedAt, long endedAt, BigDecimal startingBalance, BigDecimal endingBalance) {
        public BigDecimal profit() { return endingBalance.subtract(startingBalance); }
    }
    private final Clock clock;
    private BigDecimal balance, start, finishedProfit = BigDecimal.ZERO, received = BigDecimal.ZERO;
    private boolean paymentsOnly;
    private long started, elapsed;
    private boolean running, hasSession;
    private final List<Session> history = new ArrayList<>();
    public Tracker() { this(Clock.systemUTC()); }
    public Tracker(Clock clock) { this.clock = clock; }
    public void balance(BigDecimal value) {
        if (value.signum() < 0) throw new IllegalArgumentException("Balance cannot be negative.");
        balance = value;
    }
    public boolean hasBalance() { return balance != null; }
    public BigDecimal balance() { return balance == null ? BigDecimal.ZERO : balance; }
    public void start() {
        if (running) throw new IllegalStateException("A session is already running.");
        if (!hasBalance()) throw new IllegalStateException("Read or enter your current balance before starting.");
        paymentsOnly = false; received = BigDecimal.ZERO;
        start = balance; started = clock.millis(); elapsed = 0; hasSession = true; running = true;
    }
    /** Starts a received-payments total without requesting or requiring /bal. */
    public void startPayments() {
        if (running) throw new IllegalStateException("A session is already running.");
        start = balance(); received = BigDecimal.ZERO; paymentsOnly = true;
        started = clock.millis(); elapsed = 0; hasSession = true; running = true;
    }
    public void incomingPayment(BigDecimal amount) {
        if (amount.signum() <= 0) throw new IllegalArgumentException("Payment must be positive.");
        if (!running) return;
        received = received.add(amount);
        if (!paymentsOnly) balance = balance.add(amount);
    }
    public Session stop() {
        if (!running) throw new IllegalStateException("No active session.");
        finishedProfit = profit();
        elapsed = Math.max(0, clock.millis() - started); running = false;
        Session session = new Session(started, started + elapsed, start, start.add(finishedProfit));
        history.add(0, session);
        if (history.size() > 100) history.remove(history.size() - 1);
        return session;
    }
    public void reset() { running = false; hasSession = false; start = null; elapsed = 0; received = BigDecimal.ZERO; finishedProfit = BigDecimal.ZERO; paymentsOnly = false; }
    public boolean running() { return running; }
    public BigDecimal profit() { return !hasSession ? BigDecimal.ZERO : !running ? finishedProfit : paymentsOnly ? received : balance.subtract(start); }
    public long elapsedMillis() { return running ? Math.max(0, clock.millis() - started) : elapsed; }
    public BigDecimal perHour() {
        long ms = elapsedMillis();
        return ms == 0 ? BigDecimal.ZERO : profit().multiply(new BigDecimal("3600000")).divide(BigDecimal.valueOf(ms), 2, RoundingMode.HALF_UP);
    }
    public String duration() {
        long seconds = elapsedMillis() / 1000;
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }
    public List<Session> history() { return List.copyOf(history); }
    public void restoreHistory(List<Session> sessions) { history.clear(); history.addAll(sessions.subList(0, Math.min(sessions.size(), 100))); }
}
