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
        Settings liveSettings = new Settings(); PaymentQueue liveQueue = new PaymentQueue();
        ChatParser liveParser = new ChatParser(liveSettings); Tracker live = new Tracker(clock);
        live.startPayments(); rejects(live::startPayments, "sstart cannot reset an active session");
        liveParser.accept("Alex paid you $1M $",live,liveQueue);
        check(live.profit().compareTo(Money.parse("1M"))==0,"first payment immediately visible");
        now.addAndGet(3_600_000);
        liveParser.accept("Steve paid you $2M",live,liveQueue);
        liveParser.accept("Small paid you $1",live,liveQueue);
        check(live.profit().compareTo(Money.parse("3000001"))==0,"all receipts count including below queue threshold");
        check(live.perHour().compareTo(live.profit())==0,"live hourly total");
        liveParser.accept("You Have $ 99M",live,liveQueue);
        check(live.profit().compareTo(Money.parse("3000001"))==0,"balance refresh does not double count receipt session");
        check(live.stop().profit().compareTo(Money.parse("3000001"))==0,"receipt total saved to history");
        liveParser.accept("Alex paid you $9M",live,liveQueue); live.balance(Money.parse("100M"));
        check(live.profit().compareTo(Money.parse("3000001"))==0,"stopped total stays frozen");
        live.startPayments(); check(live.profit().signum()==0,"new receipt session starts at zero"); live.stop();
        Tracker estimated = new Tracker(clock); estimated.balance(Money.parse("10M")); estimated.start();
        liveParser.accept("Alex paid you $1M",estimated,new PaymentQueue());
        check(estimated.profit().compareTo(Money.parse("1M"))==0,"normal sessions also update immediately");
        estimated.balance(Money.parse("11M"));
        check(estimated.profit().compareTo(Money.parse("1M"))==0,"authoritative balance replaces estimate without double count");
        Tracker net = new Tracker(clock); PaymentQueue netQueue = new PaymentQueue(); net.startPayments();
        liveParser.accept("Alex paid you $3M",net,netQueue);
        check(Money.signed(net.profit()).equals("+3M"),"net example begins at +3M");
        check(liveParser.accept("You paid Steve $2M",net,netQueue)==ChatParser.Result.EXPENSE,"outgoing confirmed");
        check(Money.signed(net.profit()).equals("+1M"),"+3M minus 2M equals +1M");
        liveParser.accept("You paid Steve $2M",net,netQueue);
        check(Money.signed(net.profit()).equals("-1M"),"another 2M produces -1M");
        check(netQueue.all().size()==1,"expenses never enter payment queue");
        net.reset();net.startPayments();
        liveParser.accept("You successfully paid $3M to Steve!",net,netQueue);
        check(Money.signed(net.profit()).equals("-3M"),"loss from zero displayed -3M");
        net.reset();net.startPayments();
        liveParser.accept("You sent $2M to Steve",net,netQueue);
        liveParser.accept("You lost $1M!",net,netQueue);
        check(Money.signed(net.profit()).equals("-3M"),"confirmed sends and monetary losses subtract");
        for(String line : new String[]{"Not enough money to pay $2M", "You cannot pay yourself $2M", "<Steve> You paid Steve $2M", "Steve: You lost $3M", "/pay Steve 2M", "You lost 3 experience levels"})
            check(liveParser.accept(line,net,netQueue)==ChatParser.Result.IGNORED,"unconfirmed or unrelated message ignored: "+line);
        check(net.stop().profit().compareTo(Money.parse("-3M"))==0,"negative history result");
        liveParser.accept("You paid Steve $1M",net,netQueue);
        check(Money.signed(net.profit()).equals("-3M"),"stopped net result stays frozen");
        Tracker reconciled = new Tracker(clock);reconciled.balance(Money.parse("10M"));reconciled.startPayments();
        liveParser.accept("Alex paid you $3M",reconciled,netQueue);
        liveParser.accept("You paid Steve $2M",reconciled,netQueue);
        reconciled.balance(Money.parse("11M"));
        check(Money.signed(reconciled.profit()).equals("+1M"),"balance refresh does not double count income or expenses");
        reconciled.balance(Money.parse("7M"));
        check(Money.signed(reconciled.profit()).equals("-3M"),"manual balance catches other unreported losses");
        now.addAndGet(3_600_000);
        check(Money.signed(reconciled.perHour()).equals("-3M"),"negative hourly rate");
        PaymentQueue leverQueue=new PaymentQueue();
        leverQueue.add("First",Money.parse("1M"),BigDecimal.ZERO);leverQueue.add("Second",Money.parse("2M"),BigDecimal.ZERO);
        LeverQueueAdvance lever=new LeverQueueAdvance();var receipt=leverQueue.active();
        lever.arm(receipt,false,1000);
        check(!lever.observe(true,false,1100,leverQueue,()->{}),"click without a state transition does not advance");
        check(lever.observe(true,true,1200,leverQueue,()->{}),"lever turning on advances once");
        check(leverQueue.active().player().equals("Second"),"next player becomes active");
        check(!lever.observe(true,true,1300,leverQueue,()->{}),"same transition cannot remove two players");
        lever.arm(leverQueue.active(),true,2000);
        check(lever.observe(true,false,2100,leverQueue,()->{})&&leverQueue.active()==null,"turning off also advances and empty queue stays safe");
        lever.arm(null,false,2200);check(!lever.pending(),"empty queue click does not capture future arrival");
        leverQueue.add("Original",Money.parse("1M"),BigDecimal.ZERO);leverQueue.add("Next",Money.parse("1M"),BigDecimal.ZERO);
        lever.arm(leverQueue.active(),false,3000);leverQueue.next();
        check(!lever.observe(true,true,3100,leverQueue,()->{})&&leverQueue.active().player().equals("Next"),"manual Next while waiting never removes a different player");
        lever.arm(leverQueue.active(),false,4000);
        check(!lever.observe(true,true,7101,leverQueue,()->{})&&leverQueue.active()!=null,"timed out interaction does not advance");
        lever.arm(leverQueue.active(),false,8000);
        check(!lever.observe(false,true,8100,leverQueue,()->{}),"removed lever cancels");
        receipt=leverQueue.active();lever.arm(receipt,false,9000);
        try{lever.observe(true,true,9100,leverQueue,()->{throw new java.io.IOException("disk full");});throw new AssertionError("save should fail");}
        catch(java.io.IOException expected){check(leverQueue.active()==receipt&&!lever.pending(),"failed save restores exact receipt and cancels pending click");}
        lever.arm(receipt,false,10000);lever.cancel();check(!lever.observe(true,true,10100,leverQueue,()->{}),"disconnect or disabled setting cancels interaction");
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
        check(parser.accept("You paid Steve $100M", t, q) == ChatParser.Result.EXPENSE, "outgoing payment recognized");
        rejects(() -> q.add("bad name", Money.parse("1"), BigDecimal.ZERO), "bad IGN");
        check(!q.add("Steve", Money.parse("-1"), BigDecimal.ZERO), "negative payment ignored");
        rejects(()->net.outgoingPayment(BigDecimal.ZERO),"nonpositive outgoing rejected");
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
        Settings customExpense = new Settings();customExpense.expensePattern="^DEBIT ([0-9]+)$";
        var expenseFile=dir.resolve("expenses.properties");customExpense.save(expenseFile);
        check(Settings.load(expenseFile).expensePattern.equals(customExpense.expensePattern),"custom expense pattern persisted");
        Tracker customNet=new Tracker(clock);customNet.startPayments();
        new ChatParser(Settings.load(expenseFile)).accept("DEBIT 3000000",customNet,new PaymentQueue());
        check(Money.signed(customNet.profit()).equals("-3M"),"custom expense pattern works");
        var negativeFile=dir.resolve("negative-history.properties");StateStore.save(negativeFile,net,netQueue);
        Tracker negativeRestored=new Tracker();StateStore.load(negativeFile,negativeRestored,new PaymentQueue());
        check(Money.signed(negativeRestored.history().getFirst().profit()).equals("-3M"),"negative net history survives reload");
        Settings leverSettings=new Settings();leverSettings.advanceOnLever=false;
        var leverFile=dir.resolve("lever-settings.properties");leverSettings.save(leverFile);
        check(!Settings.load(leverFile).advanceOnLever,"lever preference survives restart");
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
