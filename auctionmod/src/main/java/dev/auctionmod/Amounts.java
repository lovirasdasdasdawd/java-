package dev.auctionmod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Pattern;

/** Exact decimal money; no floating point rounding in bid comparisons. */
public final class Amounts {
    private static final Pattern VALID=Pattern.compile("(?i)(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?[KMBT]?");
    private Amounts(){}
    public static BigDecimal parse(String input){
        if(input==null)throw new IllegalArgumentException("Amount is required.");
        String s=input.strip().replaceFirst("^\\$\\s*","");
        if(s.length()>64||!VALID.matcher(s).matches())throw new IllegalArgumentException("Use a positive amount, e.g. 1000, 1k or 1.5m.");
        char suffix=Character.toUpperCase(s.charAt(s.length()-1));int exponent=switch(suffix){case 'K'->3;case 'M'->6;case 'B'->9;case 'T'->12;default->0;};
        if(exponent>0)s=s.substring(0,s.length()-1);
        BigDecimal result=new BigDecimal(s.replace(",","")).scaleByPowerOfTen(exponent);
        if(result.signum()<0||result.precision()>50)throw new IllegalArgumentException("Amount is too large.");
        return result;
    }
    public static String format(BigDecimal value){
        String[] suffixes={"","K","M","B","T"};int index=0;BigDecimal scaled=value;
        while(scaled.abs().compareTo(new BigDecimal("1000"))>=0&&index<4){scaled=scaled.movePointLeft(3);index++;}
        return scaled.setScale(2,RoundingMode.DOWN).stripTrailingZeros().toPlainString()+suffixes[index];
    }
    public static int duration(String input){
        String s=input.strip();long seconds;
        try{
            if(s.matches("[0-9]{1,6}"))seconds=Long.parseLong(s);
            else if(s.matches("[0-9]{1,4}:[0-5][0-9]")){String[] parts=s.split(":");seconds=Math.addExact(Math.multiplyExact(Long.parseLong(parts[0]),60),Long.parseLong(parts[1]));}
            else throw new IllegalArgumentException("Use seconds or mm:ss, e.g. 90 or 01:30.");
        }catch(ArithmeticException|NumberFormatException ex){throw new IllegalArgumentException("Time is too large.");}
        if(seconds<1||seconds>86400)throw new IllegalArgumentException("Time must be between 1 second and 24 hours.");
        return (int)seconds;
    }
    public static String time(long seconds){return String.format(Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);}
}
