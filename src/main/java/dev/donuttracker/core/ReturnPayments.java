package dev.donuttracker.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;

public final class ReturnPayments {
    @FunctionalInterface public interface Persist { void run() throws IOException; }
    private ReturnPayments() {}

    public static String command(PaymentQueue.Payment payment, int multiplier) {
        if (multiplier != 2 && multiplier != 3) throw new IllegalArgumentException("Choose 2x or 3x.");
        if (payment == null) throw new IllegalStateException("No active payment.");
        if (!payment.player().matches("[A-Za-z0-9_]{1,16}") || payment.amount().signum() <= 0) throw new IllegalArgumentException("Invalid payment.");
        if (payment.returnRequested()) throw new IllegalStateException("Return command already requested for this payment.");
        String amount = payment.amount().multiply(BigDecimal.valueOf(multiplier)).stripTrailingZeros().toPlainString();
        return "pay " + payment.player() + " " + amount;
    }

    public static String send(PaymentQueue queue, PaymentQueue.Payment expected, int multiplier, Persist persist, Consumer<String> sender) throws IOException {
        String command = command(expected, multiplier);
        PaymentQueue.Payment reserved = queue.reserveReturn(expected, multiplier);
        try { persist.run(); }
        catch (IOException | RuntimeException ex) { queue.undoReservation(reserved, expected); throw ex; }
        // Reserve durably before dispatch. A send error is ambiguous: never auto-retry it.
        sender.accept(command);
        return command;
    }
}
