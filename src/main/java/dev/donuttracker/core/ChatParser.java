package dev.donuttracker.core;

import java.util.regex.Pattern;

public final class ChatParser {
    public enum Result { BALANCE, PAYMENT, BELOW_MINIMUM, IGNORED }
    private final Settings settings;
    public ChatParser(Settings settings) { this.settings = settings; settings.validatePatterns(); }
    public Result accept(String message, Tracker tracker, PaymentQueue queue) {
        String line = message.replaceAll("§[0-9A-FK-ORXa-fk-orx]", "").replaceAll("[\\p{Z}\\s]+", " ").strip();
        var balance = Pattern.compile(settings.balancePattern).matcher(line);
        if (balance.matches()) { tracker.balance(Money.parse(balance.group(1))); return Result.BALANCE; }
        var payment = Pattern.compile(settings.paymentPattern).matcher(line);
        if (payment.matches()) {
            var amount = Money.parse(payment.group(2));
            boolean queued = queue.add(payment.group(1), amount, settings.minimumPayment);
            // Queue minimum only filters the queue, never the session's received total.
            if (amount.signum() > 0) tracker.incomingPayment(amount);
            return queued ? Result.PAYMENT : Result.BELOW_MINIMUM;
        }
        return Result.IGNORED;
    }
}
