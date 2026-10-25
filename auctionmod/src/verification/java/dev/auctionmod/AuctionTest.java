package dev.auctionmod;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicLong;

public final class AuctionTest {
    static int checks;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static void invalid(Runnable action,String label){boolean threw=false;try{action.run();}catch(IllegalArgumentException ex){threw=true;}check(threw,label);}
    public static void main(String[] args)throws Exception{
        var parser=new PaymentParser(PaymentParser.DEFAULT_PAYMENT_REGEX);
        check(parser.parse("§aAlex paid you $§21.5M").orElseThrow().amount().compareTo(new BigDecimal("1500000"))==0,"formatted suffixed payment");
        check(parser.parse("Imunknown67 paid you $ 2,311,178,968").orElseThrow().amount().compareTo(new BigDecimal("2311178968"))==0,"commas and whitespace");
        check(parser.parse("Steve paid you $2b!").orElseThrow().player().equals("Steve"),"billion suffix and punctuation");
        check(new PaymentParser("^(Alex)(?: ([0-9]+))?$").parse("Alex").isEmpty(),"optional unmatched custom amount capture is ignored safely");
        check(parser.parse("<Alex> Steve paid you $5M").isEmpty(),"decorated player chat rejected");
        check(parser.parse("You paid Steve $4M").isEmpty(),"outgoing payment rejected");
        check(parser.parse("Alex paid you $0").isEmpty()&&parser.parse("Alex paid you $-1M").isEmpty(),"nonpositive bid ignored");
        check(parser.parse("Alex paid you $1,2,3").isEmpty(),"malformed grouping rejected");
        check(Amounts.parse("1k").compareTo(Amounts.parse("1000"))==0,"minimum equivalent formats");
        check(Amounts.duration("01:30")==90&&Amounts.duration("90")==90,"both duration formats");
        invalid(()->Amounts.duration("00:60"),"invalid seconds field");invalid(()->Amounts.duration("0"),"zero duration");invalid(()->Amounts.duration("86401"),"duration maximum");
        invalid(()->new PaymentParser("(one)"),"invalid regex capture count");
        AtomicLong clock=new AtomicLong(1000);var state=new AuctionState(clock::get);state.start(Amounts.parse("1k"),2);
        check(!state.offer(new PaymentParser.Payment("Alex",Amounts.parse("999"))),"below minimum ignored");
        check(state.offer(new PaymentParser.Payment("Alex",Amounts.parse("1k"))),"minimum accepted");
        check(!state.offer(new PaymentParser.Payment("Steve",Amounts.parse("1k"))),"equal payment ignored including duplicate delivery");
        check(state.offer(new PaymentParser.Payment("Steve",Amounts.parse("2k"))),"higher payment wins");
        clock.set(2999);check(state.offer(new PaymentParser.Payment("Alex",Amounts.parse("3k"))),"last millisecond before deadline accepted");
        clock.set(3000);check(!state.offer(new PaymentParser.Payment("Steve",Amounts.parse("10M"))),"exact deadline payment rejected without tick");
        check(state.snapshot().phase()==AuctionState.Phase.ENDED&&state.snapshot().remainingSeconds()==0,"HUD snapshot pairs ended state with zero remaining");
        check(state.phase()==AuctionState.Phase.ENDED&&state.top().player().equals("Alex"),"winner frozen at deadline");
        clock.set(100000);check(!state.offer(new PaymentParser.Payment("Steve",Amounts.parse("100M"))),"lagged late message rejected");
        check(state.consumeEndNotification()&&!state.consumeEndNotification(),"winner notification delivered once");
        state.start(BigDecimal.ONE,1);clock.addAndGet(1000);check(state.phase()==AuctionState.Phase.ENDED&&state.top()==null,"no bids end correctly");
        state.start(BigDecimal.ONE,20);state.cancel();check(state.phase()==AuctionState.Phase.CANCELLED&&!state.offer(new PaymentParser.Payment("Alex",Amounts.parse("2M"))),"cancel rejects later payments");
        state.reset();check(state.phase()==AuctionState.Phase.IDLE&&state.top()==null,"disconnect reset");
        // Two clock reads straddling expiry simulate work/lag during bid processing.
        AtomicLong advancing=new AtomicLong(0);var crossing=new AuctionState(()->advancing.getAndAdd(1));crossing.start(BigDecimal.ONE,1);advancing.set(crossing.endTime()-1);
        check(!crossing.offer(new PaymentParser.Payment("Alex",BigDecimal.TEN)),"deadline checked again before committing bid");
        var dir=Files.createTempDirectory("auction-config-test-");var path=dir.resolve("auctionmod.json");var config=AuctionConfig.load(path);config.minimum="1.25m";config.durationSeconds=90;config.glassPanels=true;config.paymentRegex="(?i)^PAY ([A-Za-z0-9_]+) ([0-9]+)$";config.save(path);var reloaded=AuctionConfig.load(path);
        check(reloaded.durationSeconds==90&&reloaded.minimum.equals("1.25m")&&reloaded.glassPanels,"JSON settings roundtrip");check(new PaymentParser(reloaded.paymentRegex).parse("PAY Alex 2000").isPresent(),"custom JSON regex is used");
        config.hudEnabled=false;config.hudScale=.55;config.hudPinned=true;config.hudX=76;config.hudY=109;config.save(path);var hudConfig=AuctionConfig.load(path);
        check(!hudConfig.hudEnabled&&hudConfig.hudScale==.55&&hudConfig.hudPinned&&hudConfig.hudX==76&&hudConfig.hudY==109,"hidden/scaled/dragged HUD settings persist");
        config.hudScale=9;config.save(path);check(AuctionConfig.load(path).hudScale==1.6,"HUD scale clamps to supported maximum");
        Files.writeString(path,"{\"hudScale\":\"NaN\"}");boolean badScale=false;try{AuctionConfig.load(path);}catch(java.io.IOException expected){badScale=true;}check(badScale,"non-finite scale config rejected safely");
        Files.delete(path);Files.delete(dir);
        System.out.println("PASS: "+checks+" auction assertions");
    }
}
