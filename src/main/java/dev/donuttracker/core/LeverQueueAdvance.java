package dev.donuttracker.core;

import java.io.IOException;

/** One queued receipt per observed lever transition, never a later replacement receipt. */
public final class LeverQueueAdvance {
    private PaymentQueue.Payment expected;
    private boolean initialPower;
    private long armedAt;
    public boolean pending(){return expected!=null;}
    public void arm(PaymentQueue.Payment payment,boolean powered,long now){
        if(expected!=null||payment==null)return;
        expected=payment;initialPower=powered;armedAt=now;
    }
    public void cancel(){expected=null;}
    public boolean observe(boolean leverPresent,boolean powered,long now,PaymentQueue queue,ReturnPayments.Persist persist)throws IOException{
        if(expected==null)return false;
        if(!leverPresent||now-armedAt>3000||queue.active()!=expected){cancel();return false;}
        if(powered==initialPower)return false;
        cancel();
        var before=queue.all();queue.next();
        try{persist.run();}catch(IOException|RuntimeException ex){queue.restore(before);throw ex;}
        return true;
    }
}
