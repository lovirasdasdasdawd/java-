package dev.donuttracker.core;

import java.util.regex.Pattern;

public final class ChatParser {
    public enum Result { BALANCE, PAYMENT, BELOW_MINIMUM, IGNORED }
    private final Settings settings;
    public ChatParser(Settings settings) { this.settings = settings; settings.validatePatterns(); }
    public Result accept(String message, Tracker tracker, PaymentQueue queue) {
        String line = message.replaceAll("§[0-9A-FK-ORXa-fk-orx]", "").strip();
        var balance = Pattern.compile(settings.balancePattern).matcher(line);
        if (balance.matches()) { tracker.balance(Money.parse(balance.group(1))); return Result.BALANCE; }
        var payment = Pattern.compile(settings.paymentPattern).matcher(line);
        if (payment.matches()) {
            // Incoming payments are NOT added to the balance: a later balance line is authoritative.
            return queue.add(payment.group(1), Money.parse(payment.group(2)), settings.minimumPayment) ? Result.PAYMENT : Result.BELOW_MINIMUM;
        }
        return Result.IGNORED;
    }
}
