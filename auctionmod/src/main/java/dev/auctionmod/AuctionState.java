package dev.auctionmod;

import java.math.BigDecimal;
import java.util.function.LongSupplier;

/** One fixed wall-clock deadline. Every read and bid refreshes expiration before proceeding. */
public final class AuctionState {
    public enum Phase { IDLE, RUNNING, ENDED, CANCELLED }
    public record Bid(String player,BigDecimal amount){}
    public record Snapshot(Phase phase,Bid top,long remainingSeconds){}
    private final LongSupplier clock;
    private Phase phase=Phase.IDLE;
    private BigDecimal minimum=BigDecimal.ONE;
    private Bid top;
    private long endTime;
    private boolean notificationPending;
    public AuctionState(){this(System::currentTimeMillis);}
    public AuctionState(LongSupplier clock){this.clock=clock;}
    public synchronized void start(BigDecimal minimum,int seconds){
        refresh();if(phase==Phase.RUNNING)throw new IllegalStateException("Stop the current auction first.");
        if(minimum==null||minimum.signum()<=0||seconds<1||seconds>86400)throw new IllegalArgumentException("Positive minimum and a duration of 1–86400 seconds required.");
        endTime=Math.addExact(clock.getAsLong(),Math.multiplyExact((long)seconds,1000));this.minimum=minimum;top=null;phase=Phase.RUNNING;notificationPending=false;
    }
    public synchronized boolean offer(PaymentParser.Payment payment){
        refresh();if(phase!=Phase.RUNNING||payment==null||payment.amount().signum()<=0||payment.amount().compareTo(minimum)<0)return false;
        if(top!=null&&payment.amount().compareTo(top.amount())<=0)return false;
        // Check again immediately before mutation: parsing/other work cannot admit a late bid.
        refresh();if(phase!=Phase.RUNNING)return false;
        top=new Bid(payment.player(),payment.amount());return true;
    }
    private void refresh(){if(phase==Phase.RUNNING&&clock.getAsLong()>=endTime){phase=Phase.ENDED;notificationPending=true;}}
    public synchronized Phase phase(){refresh();return phase;}
    public synchronized Bid top(){refresh();return top;}
    public synchronized Snapshot snapshot(){long now=clock.getAsLong();if(phase==Phase.RUNNING&&now>=endTime){phase=Phase.ENDED;notificationPending=true;}return new Snapshot(phase,top,phase==Phase.RUNNING?Math.max(0,(endTime-now+999)/1000):0);}
    public synchronized long remainingSeconds(){return snapshot().remainingSeconds();}
    public synchronized long endTime(){return endTime;}
    public synchronized void cancel(){refresh();if(phase==Phase.RUNNING){phase=Phase.CANCELLED;notificationPending=false;}}
    public synchronized boolean consumeEndNotification(){refresh();boolean result=notificationPending;notificationPending=false;return result;}
    public synchronized void reset(){phase=Phase.IDLE;top=null;notificationPending=false;endTime=0;}
}
