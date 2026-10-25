package dev.auctionmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.*;

public final class AuctionConfig {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public String paymentRegex=PaymentParser.DEFAULT_PAYMENT_REGEX;
    public String minimum="1k";
    public int durationSeconds=60;
    public String selectedItem="";
    public boolean notifyWinner=true,glassPanels=false;
    public int hudX=12,hudY=12;
    public boolean hudEnabled=true,hudPinned=false;
    public double hudScale=.8;
    public static AuctionConfig load(Path path)throws IOException{
        if(!Files.exists(path)){var config=new AuctionConfig();config.save(path);return config;}
        try{var config=JSON.fromJson(Files.readString(path),AuctionConfig.class);if(config==null)throw new IllegalArgumentException("Empty config.");config.validate();return config;}
        catch(RuntimeException ex){throw new IOException("Invalid auction config: "+ex.getMessage(),ex);}
    }
    private void validate(){if(!Double.isFinite(hudScale))throw new IllegalArgumentException("HUD scale must be finite.");hudScale=Math.clamp(hudScale,.4,1.6);new PaymentParser(paymentRegex);if(Amounts.parse(minimum).signum()<=0)throw new IllegalArgumentException("Minimum must be positive.");if(durationSeconds<1||durationSeconds>86400)throw new IllegalArgumentException("Duration must be 1–86400 seconds.");hudX=Math.max(0,hudX);hudY=Math.max(0,hudY);if(selectedItem==null)selectedItem="";}
    public void save(Path path)throws IOException{
        validate();Path target=path.toAbsolutePath();Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"auction-",".tmp");
        try{Files.writeString(temp,JSON.toJson(this)+"\n");try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}}
        finally{Files.deleteIfExists(temp);}
    }
}
