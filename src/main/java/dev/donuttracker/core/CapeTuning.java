package dev.donuttracker.core;

import java.util.Properties;

/** Three independently persisted geometric controls for each cape composition. */
public record CapeTuning(double primary,double secondary,double density) {
    public static final CapeTuning DEFAULT=new CapeTuning(.5,.5,.5);
    public CapeTuning {primary=bounded(primary);secondary=bounded(secondary);density=bounded(density);}
    private static double bounded(double v){if(!Double.isFinite(v))throw new IllegalArgumentException("Non-finite cape setting");return Math.clamp(v,0,1);}
    public double value(int i){return i==0?primary:i==1?secondary:density;}
    public CapeTuning with(int i,double v){return new CapeTuning(i==0?v:primary,i==1?v:secondary,i==2?v:density);}
    public static String[] names(CapePattern.Design d){return switch(d){
        case WOLF->new String[]{"Portrait scale","Portrait height","Sparkles"};
        case PHOENIX->new String[]{"Wing span","Tail length","Embers"};
        case VOID_SKULL->new String[]{"Jaw length","Eye size","Chain links"};
        case NOVA->new String[]{"Orbit tilt","Star field","Planet size"};
        case LOTUS->new String[]{"Petal count","Bloom opening","Water ripples"};
        case DRAGON->new String[]{"Dragon coils","Wing span","Runes"};
        case TIDAL->new String[]{"Wave height","Wave layers","Sea foam"};
        case RIFT->new String[]{"Crack width","Branches","Floating shards"};
        case CHRISTMAS_TREE->new String[]{"Tree height","Fairy lights","Presents"};
        case WINTER_NIGHT->new String[]{"Snowfall","Snowdrifts","Moon size"};
        case GINGERBREAD->new String[]{"Cookie size","Icing width","Candy buttons"};
        default->new String[]{"Emblem size","Vertical stretch","Stitch intensity"};
    };}
    public static String description(CapePattern.Design d){return switch(d){case WOLF->"Portrait / diagonal silk / claw marks";case PHOENIX->"Wide wings / ember fall / fire hem";case VOID_SKULL->"Bone sigil / hanging chains";case NOVA->"Off-centre planet / full star field";case LOTUS->"Low bloom / pond / concentric ripples";case DRAGON->"Coiling silhouette / wing / rune column";case TIDAL->"Full-cloth layered ocean / foam";case RIFT->"Edge-to-edge fissures / broken shards";case CHRISTMAS_TREE->"Evergreen / fairy lights / wrapped gifts";case WINTER_NIGHT->"Moonlight / snowfall / deep snowdrifts";case GINGERBREAD->"Baked cookie / sugar icing / candy";default->"Donut emblem / stitched cloth";};}
    private static String key(CapePattern.Design d,int i){return "design."+d.name()+"."+names(d)[i].toLowerCase(java.util.Locale.ROOT).replace(' ','.');}
    public static CapeTuning read(Properties p,CapePattern.Design d){return new CapeTuning(Double.parseDouble(p.getProperty(key(d,0),"0.5")),Double.parseDouble(p.getProperty(key(d,1),"0.5")),Double.parseDouble(p.getProperty(key(d,2),"0.5")));}
    public void write(Properties p,CapePattern.Design d){for(int i=0;i<3;i++)p.setProperty(key(d,i),Double.toString(value(i)));}
}
