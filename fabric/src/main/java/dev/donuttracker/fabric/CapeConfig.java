package dev.donuttracker.fabric;

import dev.donuttracker.core.CapePattern;
import dev.donuttracker.core.CapeTuning;
import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

final class CapeConfig {
    boolean enabled=false,glowing=true;
    int cloth=0x101625,frosting=0xC995F4,accent=0x72E7DC;
    double glow=.7,aura=.65;
    CapePattern.Design design=CapePattern.Design.CLASSIC;
    final java.util.EnumMap<CapePattern.Design,CapeTuning> shapes=new java.util.EnumMap<>(CapePattern.Design.class);
    CapeConfig(){for(var d:CapePattern.Design.values())shapes.put(d,CapeTuning.DEFAULT);}
    CapeTuning shape(){return shapes.get(design);}
    private boolean writable=true;
    private final Path file=FabricLoader.getInstance().getConfigDir().resolve("donut-tracker-capes.properties");
    static CapeConfig load(){
        var c=new CapeConfig();if(!Files.exists(c.file))return c;
        try(var reader=Files.newBufferedReader(c.file)){
            var p=new Properties();p.load(reader);c.enabled=Boolean.parseBoolean(p.getProperty("enabled","false"));c.glowing=Boolean.parseBoolean(p.getProperty("glowing","true"));
            c.cloth=Integer.parseInt(p.getProperty("cloth","101625"),16)&0xFFFFFF;c.frosting=Integer.parseInt(p.getProperty("frosting","C995F4"),16)&0xFFFFFF;c.accent=Integer.parseInt(p.getProperty("accent","72E7DC"),16)&0xFFFFFF;
            c.design=CapePattern.Design.valueOf(p.getProperty("design","CLASSIC"));c.glow=Double.parseDouble(p.getProperty("glow","0.7"));if(!Double.isFinite(c.glow))throw new IOException("Invalid glow");c.glow=Math.clamp(c.glow,0,1);c.aura=Double.parseDouble(p.getProperty("aura","0.65"));if(!Double.isFinite(c.aura))throw new IOException("Invalid aura");c.aura=Math.clamp(c.aura,0,1);for(var d:CapePattern.Design.values())c.shapes.put(d,CapeTuning.read(p,d));
        }catch(Exception ex){c.enabled=false;c.writable=false;org.slf4j.LoggerFactory.getLogger("donuttracker").warn("Cape configuration could not be read; original preserved",ex);}
        return c;
    }
    void save()throws IOException{
        if(!writable)throw new IOException("Invalid cape config preserved. Fix or remove donut-tracker-capes.properties first.");
        var p=new Properties();p.setProperty("enabled",Boolean.toString(enabled));p.setProperty("glowing",Boolean.toString(glowing));p.setProperty("cloth",String.format("%06X",cloth));p.setProperty("frosting",String.format("%06X",frosting));p.setProperty("accent",String.format("%06X",accent));p.setProperty("design",design.name());p.setProperty("glow",Double.toString(glow));p.setProperty("aura",Double.toString(aura));for(var d:CapePattern.Design.values())shapes.get(d).write(p,d);
        Files.createDirectories(file.getParent());var tmp=Files.createTempFile(file.getParent(),"cape-",".tmp");
        try{try(var writer=Files.newBufferedWriter(tmp)){p.store(writer,"Local cape cosmetic; no uploads or server messages");}try{Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(tmp);}
    }
}
