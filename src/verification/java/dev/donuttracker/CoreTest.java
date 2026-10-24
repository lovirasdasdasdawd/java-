package dev.donuttracker;

import dev.donuttracker.core.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.*;
import java.util.concurrent.atomic.AtomicLong;

public final class CoreTest {
    private static int checks;
    private static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
    private static void rejects(Runnable action, String message) {
        checks++; try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { return; } throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        check(Money.parse("$2,311,178,968").equals(new BigDecimal("2311178968.00")), "comma balance");
        for (String[] example : new String[][]{{"100000", "100K"}, {"100000000", "100M"}, {"100000000000", "100B"}, {"1000000000000", "1T"}, {"1500", "1.5K"}, {"2750000", "2.75M"}, {"-3200000000", "-3.2B"}, {"999999", "1M"}, {"0", "0"}})
            check(Money.format(Money.parse(example[0])).equals(example[1]), "format " + example[0]);
        rejects(() -> Money.parse("NaN"), "NaN rejected"); rejects(() -> Money.parse("1.2.3M"), "invalid amount");
        AtomicLong now = new AtomicLong(1000);
        Clock clock = new Clock() { public ZoneId getZone() { return ZoneOffset.UTC; } public Clock withZone(ZoneId zone) { return this; } public Instant instant() { return Instant.ofEpochMilli(now.get()); } };
        Tracker t = new Tracker(clock); rejects(t::start, "must know balance");
        t.balance(Money.parse("500K")); t.start(); rejects(t::start, "no duplicate start");
        now.addAndGet(1_800_000); t.balance(Money.parse("1.75M"));
        check(Money.format(t.profit()).equals("1.25M"), "balance difference"); check(Money.format(t.perHour()).equals("2.5M"), "hourly rate");
        check(t.duration().equals("00:30:00"), "timer"); var first = t.stop(); now.addAndGet(50_000);
        check(t.duration().equals("00:30:00"), "timer freezes"); check(first.profit().equals(t.profit()), "saved result"); rejects(t::stop, "no duplicate stop");
        t.start(); t.balance(Money.parse("1M")); now.addAndGet(60_000); t.stop();
        check(Money.signed(t.profit()).equals("-750K"), "loss"); check(t.history().size() == 2, "history");
        t.reset(); check(t.profit().signum() == 0 && !t.running(), "reset"); rejects(() -> t.balance(Money.parse("-1")), "negative balance");
        Settings s = new Settings(); PaymentQueue q = new PaymentQueue(); ChatParser parser = new ChatParser(s);
        check(parser.accept("You Have $ §a2,311,178,968", t, q) == ChatParser.Result.BALANCE, "user balance format");
        check(t.balance().equals(Money.parse("2311178968")), "parsed exact balance");
        check(parser.accept("Imunknown67 paid you $ §a 1", t, q) == ChatParser.Result.BELOW_MINIMUM, "minimum filters small payment");
        s.minimumPayment = Money.parse("1");
        check(parser.accept("Imunknown67 paid you $ §a 1", t, q) == ChatParser.Result.PAYMENT, "user payment format");
        check(q.active().player().equals("Imunknown67"), "IGN"); check(q.active().amount().equals(Money.parse("1")), "payment value");
        check(parser.accept("OtherPlayer paid you $ 100M", t, q) == ChatParser.Result.PAYMENT, "second payment");
        check(t.balance().equals(Money.parse("2311178968")), "payment never double counted as balance");
        check(q.waiting() == 1, "waiting count"); q.next(); check(q.active().player().equals("OtherPlayer"), "FIFO next");
        for (int i = 0; i < 6; i++) q.add("Player" + i, Money.parse("100K"), s.minimumPayment);
        check(q.waiting() == 6, "4+ waiting players supported");
        check(parser.accept("<Player> Imunknown67 paid you $ 100B", t, q) == ChatParser.Result.IGNORED, "player chat cannot spoof anchored server line");
        check(parser.accept("You paid Steve $100M", t, q) == ChatParser.Result.IGNORED, "outgoing payment ignored");
        rejects(() -> q.add("bad name", Money.parse("1"), BigDecimal.ZERO), "bad IGN");
        check(!q.add("Steve", Money.parse("-1"), BigDecimal.ZERO), "negative payment ignored");
        var dir = Files.createTempDirectory("donut-tests-"); var settingsPath = dir.resolve("settings.properties"); var statePath = dir.resolve("state.properties");
        s.profit.theme = Settings.Theme.NEON; s.queue.theme = Settings.Theme.CLEAN;
        s.profit.accent = Settings.color("#AABBCC"); s.profit.scale = 1.75; s.profit.opacity = .3; s.profit.x = 52;
        s.queue.layout = Settings.Layout.MINIMAL; s.queue.enabled = false; s.profit.showRate = false;
        s.save(settingsPath); s.save(settingsPath); Settings loaded = Settings.load(settingsPath);
        check(loaded.profit.theme == Settings.Theme.NEON && loaded.queue.theme == Settings.Theme.CLEAN, "independent clean/neon saved");
        check(loaded.profit.accent == 0xAABBCC && loaded.profit.scale == 1.75 && loaded.profit.opacity == .3 && loaded.profit.x == 52, "colour/size/opacity/position saved");
        check(loaded.queue.layout == Settings.Layout.MINIMAL && !loaded.queue.enabled && !loaded.profit.showRate, "layout and display flags saved");
        check(loaded.minimumPayment.equals(Money.parse("1")), "payment threshold saved");
        rejects(() -> Settings.color("#XYZ"), "invalid hex"); s.paymentPattern = "("; rejects(s::validatePatterns, "invalid regex");
        StateStore.save(statePath, t, q); StateStore.save(statePath, t, q);
        Tracker restored = new Tracker(); PaymentQueue restoredQueue = new PaymentQueue(); StateStore.load(statePath, restored, restoredQueue);
        check(restored.history().size() == 2, "session persistence"); check(restoredQueue.all().equals(q.all()), "queue persistence");
        check(!restored.hasBalance() && !restored.running(), "stale balance/running state never restored");
        // Return commands are tested with fake senders; these tests never pay on a server.
        PaymentQueue returns = new PaymentQueue(); returns.add("1arp_", Money.parse("1M"), BigDecimal.ZERO);
        var original = returns.active(); var sent = new java.util.ArrayList<String>();
        check(ReturnPayments.command(original,2).equals("pay 1arp_ 2000000"), "screenshot IGN: 1M returns 2M");
        check(ReturnPayments.command(original,3).equals("pay 1arp_ 3000000"), "1M returns 3M");
        check(ReturnPayments.command(new PaymentQueue.Payment("Alex",Money.parse("1.25"),1),3).equals("pay Alex 3.75"), "decimal return is exact");
        check(ReturnPayments.command(new PaymentQueue.Payment("Steve",Money.parse("100B"),1),3).equals("pay Steve 300000000000"), "large return uses plain decimal amount");
        rejects(() -> ReturnPayments.command(original,4), "only 2x or 3x allowed");
        rejects(() -> ReturnPayments.command(null,2), "empty queue cannot pay");
        rejects(() -> ReturnPayments.command(new PaymentQueue.Payment("Alex pay Evil",Money.parse("1M"),1),2), "IGN cannot inject a command");
        rejects(() -> ReturnPayments.command(new PaymentQueue.Payment("Alex",BigDecimal.ZERO,1),2), "zero return rejected");
        var payoutFile = dir.resolve("returns.properties");
        String dispatched = ReturnPayments.send(returns,original,2,() -> StateStore.save(payoutFile,restored,returns),sent::add);
        check(dispatched.equals("pay 1arp_ 2000000") && sent.size()==1, "click dispatches exactly one command");
        check(returns.active().returnMultiplier()==2, "active entry marked requested");
        rejects(() -> { try { ReturnPayments.send(returns,original,3,() -> {},sent::add); } catch(java.io.IOException ex){throw new RuntimeException(ex);} }, "stale second click cannot send 3x");
        rejects(() -> { try { ReturnPayments.send(returns,returns.active(),2,() -> {},sent::add); } catch(java.io.IOException ex){throw new RuntimeException(ex);} }, "same entry cannot pay again");
        check(sent.size()==1,"double click never dispatches twice");
        PaymentQueue loadedReturns = new PaymentQueue(); StateStore.load(payoutFile,new Tracker(),loadedReturns);
        check(loadedReturns.active().returnMultiplier()==2,"return marker persists across restart");
        rejects(() -> ReturnPayments.command(loadedReturns.active(),3),"restart cannot resend marked entry");
        PaymentQueue nextQueue = new PaymentQueue(); nextQueue.add("Alex",Money.parse("1M"),BigDecimal.ZERO); var stale = nextQueue.active(); nextQueue.next(); nextQueue.add("Steve",Money.parse("1M"),BigDecimal.ZERO);
        rejects(() -> { try { ReturnPayments.send(nextQueue,stale,2,() -> {},sent::add); } catch(java.io.IOException ex){throw new RuntimeException(ex);} }, "changed active player prevents stale click");
        check(sent.size()==1,"stale click never pays the next IGN");
        PaymentQueue failedSave = new PaymentQueue(); failedSave.add("Alex",Money.parse("1M"),BigDecimal.ZERO);
        try { ReturnPayments.send(failedSave,failedSave.active(),2,() -> {throw new java.io.IOException("disk full");},sent::add); throw new AssertionError("must fail saving"); }
        catch(java.io.IOException expected) { check(sent.size()==1 && !failedSave.active().returnRequested(),"failed persistence sends nothing and restores entry"); }
        PaymentQueue ambiguousSend = new PaymentQueue(); ambiguousSend.add("Steve",Money.parse("1M"),BigDecimal.ZERO);
        try { ReturnPayments.send(ambiguousSend,ambiguousSend.active(),3,() -> {},command -> {throw new IllegalStateException("connection interrupted");}); throw new AssertionError("must fail send"); }
        catch(IllegalStateException expected) { check(ambiguousSend.active().returnMultiplier()==3,"ambiguous send remains blocked to prevent duplicate return"); }
        PaymentQueue chatQueue = new PaymentQueue();chatQueue.add("FirstPlayer",Money.parse("10K"),BigDecimal.ZERO);chatQueue.add("1arp_",Money.parse("1M"),BigDecimal.ZERO);
        var chatReceipt = chatQueue.all().getLast();var firstReceipt = chatQueue.active();
        ReturnPayments.send(chatQueue,chatReceipt,3,() -> {},sent::add);
        check(sent.getLast().equals("pay 1arp_ 3000000"),"chat button pays its exact receipt even when not the active HUD player");
        check(chatQueue.active()==firstReceipt && chatQueue.all().getLast().returnMultiplier()==3,"return from chat preserves queue order and marks only clicked receipt");
        Settings chatSettings = new Settings(); PaymentQueue screenshotQueue = new PaymentQueue();
        check(new ChatParser(chatSettings).accept("1arp_ paid you $1M $",new Tracker(),screenshotQueue)==ChatParser.Result.PAYMENT,"payment suffix currency accepted");
        check(new ChatParser(chatSettings).accept("1arp_ paid you $\u00a01M",new Tracker(),screenshotQueue)==ChatParser.Result.PAYMENT,"nonbreaking space in styled message accepted");
        var legacySettings = dir.resolve("legacy.properties"); var properties = new java.util.Properties(); properties.setProperty("payment.pattern",Settings.LEGACY_PAYMENT_PATTERN);
        try(var writer=Files.newBufferedWriter(legacySettings)){properties.store(writer,"");}
        check(Settings.load(legacySettings).paymentPattern.equals(Settings.DEFAULT_PAYMENT_PATTERN),"known old default pattern upgraded");
        properties.setProperty("payment.pattern","^CUSTOM ([A-Za-z0-9_]+) ([0-9]+)$"); try(var writer=Files.newBufferedWriter(legacySettings)){properties.store(writer,"");}
        check(Settings.load(legacySettings).paymentPattern.equals(properties.getProperty("payment.pattern")),"custom detection pattern preserved");
        var legacyQueue = dir.resolve("legacy-queue.properties"); Files.writeString(legacyQueue,"payment.0=Alex;1000000;1\n");
        PaymentQueue migratedQueue = new PaymentQueue();StateStore.load(legacyQueue,new Tracker(),migratedQueue);
        check(!migratedQueue.active().returnRequested(),"old queue file remains readable");
        var oldPolling = dir.resolve("old-polling.properties");Files.writeString(oldPolling,"balance.poll=true\nqueue.minimum=1\n");
        Settings noAuto = Settings.load(oldPolling);noAuto.save(oldPolling);
        var updatedProperties = new java.util.Properties();try(var reader=Files.newBufferedReader(oldPolling)){updatedProperties.load(reader);}
        check(!updatedProperties.containsKey("balance.poll"),"legacy automatic-polling setting is retired");
        check(noAuto.minimumPayment.equals(Money.parse("1")),"retiring auto polling preserves unrelated settings");
        System.out.println("PASS: " + checks + " assertions — formatting, real chat examples, tracking, losses, queue, themes and persistence.");
    }
}
