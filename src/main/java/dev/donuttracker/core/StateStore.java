package dev.donuttracker.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

public final class StateStore {
    private StateStore() {}
    public static void save(Path file, Tracker tracker, PaymentQueue queue) throws IOException {
        Properties p = new Properties(); int i = 0;
        for (var s : tracker.history()) p.setProperty("session." + i++, s.startedAt() + ";" + s.endedAt() + ";" + s.startingBalance() + ";" + s.endingBalance());
        i = 0;
        for (var q : queue.all()) p.setProperty("payment." + i++, q.player() + ";" + q.amount() + ";" + q.receivedAt() + ";" + q.returnMultiplier());
        Settings.atomicSave(file, p, "Completed sessions and pending payments. Live balances are not restored.");
    }
    public static void load(Path file, Tracker tracker, PaymentQueue queue) throws IOException {
        if (!Files.exists(file)) return;
        Properties p = new Properties(); try (var r = Files.newBufferedReader(file)) { p.load(r); }
        var sessions = new ArrayList<Tracker.Session>(); var payments = new ArrayList<PaymentQueue.Payment>();
        try {
            for (int i = 0; i < 100 && p.containsKey("session." + i); i++) {
                String[] v = p.getProperty("session." + i).split(";");
                sessions.add(new Tracker.Session(Long.parseLong(v[0]), Long.parseLong(v[1]), Money.parse(v[2]), Money.parse(v[3])));
            }
            for (int i = 0; i < 500 && p.containsKey("payment." + i); i++) {
                String[] v = p.getProperty("payment." + i).split(";");
                payments.add(new PaymentQueue.Payment(v[0], Money.parse(v[1]), Long.parseLong(v[2]), v.length >= 4 ? Integer.parseInt(v[3]) : 0));
            }
        } catch (RuntimeException e) { throw new IOException("Invalid history/queue data in " + file, e); }
        tracker.restoreHistory(sessions); queue.restore(payments);
    }
}
