package dev.donuttracker.core;

/** Coalesces payment-triggered balance reads; profit still uses authoritative balances. */
public final class PaymentBalanceSync {
    private boolean pending;

    public void received(ChatParser.Result result, boolean running) {
        if (result == ChatParser.Result.BALANCE) pending = false;
        else if (running && (result == ChatParser.Result.PAYMENT || result == ChatParser.Result.BELOW_MINIMUM)) pending = true;
    }

    public boolean shouldRequest(boolean connected, boolean running, boolean waitingForEndpoint, long now, long lastRequest) {
        return pending && connected && running && !waitingForEndpoint && now - lastRequest >= 2500;
    }

    public void requested() { pending = false; }
    public void reset() { pending = false; }
}
