package dev.donuttracker.fabric;

import dev.donuttracker.core.CapePattern;
import dev.donuttracker.core.CapeTuning;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The same module shell, with actual cape texels and direct colour pickers. */
final class CapesScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left,top,w,h,cw,page;

    private String status="Local cosmetic / visible to you only";
    CapesScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Capes"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(480,width-16);h=Math.min(270,height-12);left=(width-w)/2;top=(height-h)/2;cw=w-183;
        var c=CapeCosmetics.config;
        CapeCosmetics.ensure();
        addRenderableWidget(new MenuWidgets.Toggle(left+14,top+38,cw/2-3,20,"Cape",()->c.enabled,CoastalStyle::accent,()->change(()->c.enabled=!c.enabled)));
        addRenderableWidget(new MenuWidgets.Toggle(left+17+cw/2,top+38,cw-cw/2-3,20,"Glow",()->c.glowing,CoastalStyle::accent,()->change(()->c.glowing=!c.glowing)));
        addRenderableWidget(new MenuWidgets.Action(left+14,top+63,24,20,"<",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->design(-1)));
        addRenderableWidget(new MenuWidgets.Action(left+42,top+63,cw-56,20,(c.design.ordinal()+1)+"/"+CapePattern.Design.values().length+"  "+designName(c.design),CoastalStyle::accent,MenuWidgets.Kind.CHOICE,()->design(1)));
        addRenderableWidget(new MenuWidgets.Action(left+cw-10,top+63,24,20,">",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->design(1)));
        for(int i=0;i<3;i++){int section=i;int cell=(cw-8)/3;addRenderableWidget(new MenuWidgets.Action(left+14+i*(cell+4),top+87,cell,18,i==0?"Shape":i==1?"Colours":"Glow",CoastalStyle::accent,MenuWidgets.Kind.TAB,()->page==section,-1,()->{page=section;rebuildWidgets();}));}
        if(page==0){
            var labels=CapeTuning.names(c.design);
            for(int i=0;i<3;i++){int control=i;addRenderableWidget(new MenuWidgets.Slider(left+14,top+110+i*30,cw,28,labels[i],c.shape().value(i),v->{c.shapes.put(c.design,c.shape().with(control,v));save();CapeCosmetics.refresh();},v->shapeValue(c.design,control,v),CoastalStyle::accent));}
        }else if(page==1){
            colour("Cloth",c.cloth,top+111,v->c.cloth=v);colour("Artwork tint",c.frosting,top+141,v->c.frosting=v);colour("Detail colour",c.accent,top+171,v->c.accent=v);
        }else{
            addRenderableWidget(new MenuWidgets.Slider(left+14,top+110,cw,28,"Glow strength",c.glow,v->{c.glow=v;save();CapeCosmetics.refresh();},v->Math.round(v*100)+"%",CoastalStyle::accent));
            addRenderableWidget(new MenuWidgets.Slider(left+14,top+148,cw,28,"Aura width",c.aura,v->{c.aura=v;save();CapeCosmetics.refresh();},v->Math.round(v*100)+"%",CoastalStyle::accent));
            addRenderableWidget(new MenuWidgets.Toggle(left+14,top+183,cw,18,"Emissive artwork",()->c.glowing,CoastalStyle::accent,()->change(()->c.glowing=!c.glowing)));
        }
        addRenderableWidget(new MenuWidgets.Action(left+14,top+h-24,75,19,"< Modules",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        addRenderableWidget(new MenuWidgets.Action(left+w-149,top+h-24,135,19,"View in F5",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{save();minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);minecraft.setScreen(null);}));
    }
    private static String designName(CapePattern.Design d){return switch(d){case CLASSIC->"Donut";case ORBIT->"Orbit";case CROWN->"Crown";case WOLF->"Night Wolf";case PHOENIX->"Phoenix";case VOID_SKULL->"Void Skull";case NOVA->"Nova";case LOTUS->"Lotus";case DRAGON->"Dragon";case TIDAL->"Tidal";case RIFT->"Rift";};}
    private static String shapeValue(CapePattern.Design d,int i,double v){
        if(d==CapePattern.Design.NOVA&&i==0)return Math.round((v-.5)*86)+" deg";
        if(d==CapePattern.Design.LOTUS&&i==0)return ""+(4+(int)(v*7));
        if(d==CapePattern.Design.LOTUS&&i==2)return ""+(1+(int)(v*6));
        if(d==CapePattern.Design.VOID_SKULL&&i==2)return ""+(int)(v*10);
        if(d==CapePattern.Design.TIDAL&&i==1)return ""+(2+(int)(v*6));
        if(d==CapePattern.Design.RIFT&&i==1)return ""+(2+(int)(v*8));
        if(d==CapePattern.Design.DRAGON&&i==0)return String.format(java.util.Locale.ROOT,"%.1f",1+v*2);
        return Math.round(v*100)+"%";
    }
    private void design(int step){change(()->{
        var c=CapeCosmetics.config;c.design=CapePattern.Design.values()[Math.floorMod(c.design.ordinal()+step,CapePattern.Design.values().length)];
        switch(c.design){case WOLF->{c.frosting=0xBC65FF;c.accent=0xA242FF;}case PHOENIX->{c.frosting=0xFFCE6D;c.accent=0xFF7047;}case VOID_SKULL->{c.frosting=0xD5D0FF;c.accent=0x9964FF;}case NOVA->{c.frosting=0xB998FF;c.accent=0x76DCFF;}case LOTUS->{c.frosting=0xF5A4DA;c.accent=0xCB75F2;}case DRAGON->{c.frosting=0x92EECE;c.accent=0x35BAA0;}case TIDAL->{c.frosting=0xB5F5EE;c.accent=0x3296CE;}case RIFT->{c.frosting=0xEBC7FF;c.accent=0x9C4BFB;}default->{}}
    });}
    private void colour(String name,int value,int yy,java.util.function.IntConsumer setter){
        addRenderableWidget(new MenuWidgets.Action(left+14,yy,cw,20,name+"  #"+String.format("%06X",value),CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new ColourPickerScreen(mod,this,"Cape "+name,value,v->{setter.accept(v);save();CapeCosmetics.refresh();}))));
    }
    private void change(Runnable r){r.run();save();CapeCosmetics.refresh();rebuildWidgets();}
    private void save(){try{CapeCosmetics.config.save();status="Saved / local cosmetic";}catch(Exception ex){status=ex.getMessage();}}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);CoastalStyle.panel(g,left,top,w,h);
        TrackerHud.text(g,"Capes",left+14,top+14,CoastalStyle.ink());TrackerHud.scaled(g,"CAPE COLLECTION / LOCAL ONLY",left+14,top+31,.6f,CoastalStyle.muted());
        int px=left+w-153,py=top+38,pw=139,ph=Math.min(170,h-80);
        TrackerHud.rounded(g,px,py,px+pw,py+ph,CoastalStyle.surface());TrackerHud.border(g,px,py,px+pw,py+ph,CoastalStyle.line());
        float scale=Math.min(1.8f,(ph-23)/64f);int dx=px+(int)((pw-40*scale)/2),dy=py+11;
        g.blit(CapeCosmetics.PREVIEW,dx,dy,dx+(int)(40*scale),dy+(int)(64*scale),1/64f,11/64f,1/32f,17/32f);
        super.render(g,mx,my,delta);
        if(page==1){int[] colors={CapeCosmetics.config.cloth,CapeCosmetics.config.frosting,CapeCosmetics.config.accent};for(int i=0;i<3;i++)TrackerHud.rounded(g,left+cw-2,top+116+i*30,left+cw+8,top+126+i*30,0xFF000000|colors[i]);}
        if(h>250)TrackerHud.scaled(g,minecraft.font.plainSubstrByWidth(status.startsWith("Local")||status.startsWith("Saved")?CapeTuning.description(CapeCosmetics.config.design):status,(int)((w-28)/.6)),left+14,top+h-40,.6f,CoastalStyle.muted());
    }
    @Override public void onClose(){save();minecraft.setScreen(parent);}
}
