package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import java.util.ArrayList;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Local, non-positional streaming ambience. No network requests or game commands. */
final class OceanAmbience {
    private Loop current;
    private final ArrayList<Loop> retiring=new ArrayList<>();
    private long retryAt;
    static boolean moduleMenu(Object screen){return screen instanceof TrackerScreen||screen instanceof ModuleSettingsScreen||screen instanceof ColourPickerScreen||screen instanceof HudEditorScreen||screen instanceof AtmosphereScreen;}
    void tick(Minecraft client,Settings settings){
        long now=System.currentTimeMillis();
        retiring.removeIf(Loop::isStopped);
        boolean desired=settings.oceanShouldPlay(moduleMenu(client.screen),client.player!=null);
        if(current!=null&&(current.track!=settings.oceanTrack||!desired)){
            current.target=0;retiring.add(current);current=null;
            while(retiring.size()>2){var old=retiring.removeFirst();client.getSoundManager().stop(old);}
        }
        if(current!=null&&now-current.started>1500&&!client.getSoundManager().isActive(current)){
            client.getSoundManager().stop(current);current=null;retryAt=now+5000;
        }
        if(desired&&current==null&&now>=retryAt){
            var id=Identifier.fromNamespaceAndPath("donuttracker","ocean."+settings.oceanTrack.name().toLowerCase(Locale.ROOT));
            if(client.getSoundManager().getSoundEvent(id)!=null){
                current=new Loop(settings.oceanTrack,id,(float)settings.oceanVolume,now);client.getSoundManager().play(current);
            }else retryAt=now+5000;
        }
        if(current!=null)current.target=(float)Math.clamp(settings.oceanVolume,0,1);
    }
    String status(Minecraft client,Settings settings){
        if(!settings.oceanEnabled||settings.oceanVolume<=0)return "Ambience off";
        if(client.options.getSoundSourceVolume(SoundSource.MASTER)==0||client.options.getSoundSourceVolume(SoundSource.AMBIENT)==0)return "Muted by Minecraft audio";
        if(current!=null&&client.getSoundManager().isActive(current))return "Playing: "+switch(current.track){case SHORELINE->"Shoreline";case OPEN_SEA->"Open sea";case RAINY_COAST->"Rainy coast";case AURORA_DRIFT->"Aurora drift";case DEEP_SPACE->"Deep space";case CITY_RAIN->"City rain";case FOREST_BREEZE->"Forest breeze";case DESERT_WIND->"Desert wind";case SNOW_HUSH->"Snow hush";case ABYSSAL_DRIFT->"Abyssal drift";case GARDEN_CHIMES->"Garden chimes";case DISTANT_STORM->"Distant storm";case EMBER_WIND->"Ember wind";case MARSH_NIGHT->"Marsh night";case NIGHT_RAIL->"Night rail";};
        return "Starting ambience";
    }
    void stop(Minecraft client){if(current!=null)client.getSoundManager().stop(current);for(var old:retiring)client.getSoundManager().stop(old);current=null;retiring.clear();retryAt=0;}
    private static final class Loop extends AbstractTickableSoundInstance {
        final Settings.OceanTrack track;
        final long started;
        float target;
        Loop(Settings.OceanTrack track,Identifier id,float target,long now){
            super(SoundEvent.createVariableRangeEvent(id),SoundSource.AMBIENT,RandomSource.create());
            this.track=track;this.started=now;this.target=target;
            volume=.001f;pitch=1;looping=true;delay=0;relative=true;attenuation=SoundInstance.Attenuation.NONE;
        }
        @Override public void tick(){volume+=(target-volume)*.1f;if(target==0&&volume<.0015f)stop();}
    }
}
