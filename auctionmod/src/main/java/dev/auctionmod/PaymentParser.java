package dev.auctionmod;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

public final class PaymentParser {
    /** Capture group 1 = IGN, group 2 = amount. Also copied into config/auctionmod.json on first launch. */
    public static final String DEFAULT_PAYMENT_REGEX="(?i)^([A-Za-z0-9_]{1,16})\\s+paid you\\s+\\$\\s*((?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?[KMBT]?)\\s*\\$?\\s*[!.]?$";
    public record Payment(String player,BigDecimal amount){}
    private final Pattern pattern;
    public PaymentParser(String regex){
        if(regex==null||regex.length()>4096)throw new IllegalArgumentException("Payment regex is missing or too long.");
        pattern=Pattern.compile(regex);
        if(pattern.matcher("").groupCount()!=2)throw new IllegalArgumentException("Payment regex must have two capture groups: player, amount.");
    }
    public Optional<Payment> parse(String raw){
        if(raw==null||raw.length()>2048)return Optional.empty();
        String clean=raw.replaceAll("(?i)§[0-9A-FK-ORX]","").replace('\u00A0',' ').strip();
        var m=pattern.matcher(clean);if(!m.matches())return Optional.empty();
        try{String player=m.group(1);if(player==null||!player.matches("[A-Za-z0-9_]{1,16}"))return Optional.empty();BigDecimal amount=Amounts.parse(m.group(2));return amount.signum()>0?Optional.of(new Payment(player,amount)):Optional.empty();}
        catch(IllegalArgumentException ex){return Optional.empty();}
    }
}
