package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Separate scene and sound pages keep atmosphere controls easy to scan. */
final class AtmosphereScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left,top,w,h,section;
    private boolean matched;
    AtmosphereScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Atmosphere"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(500,width-16);h=Math.min(260,height-12);left=(width-w)/2;top=(height-h)/2;
        int x=left+14,cw=w-28;
        for(int i=0;i<2;i++){int page=i;addRenderableWidget(new MenuWidgets.Action(left+w-184+i*85,top+10,81,22,i==0?"Backdrop":"Sound",CoastalStyle::accent,MenuWidgets.Kind.TAB,()->section==page,-1,()->{section=page;rebuildWidgets();}));}
        if(section==0)scene(x,cw);else sound(x,cw);
        addRenderableWidget(new MenuWidgets.Action(x,top+h-26,68,20,"Back",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        if(section==0)addRenderableWidget(new MenuWidgets.Action(left+w-168,top+h-26,154,20,matched?"Colours applied":"Match HUD colours",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{CoastalStyle.matchHud(mod.settings);matched=true;mod.save();rebuildWidgets();}));
        else addRenderableWidget(new MenuWidgets.Action(left+w-82,top+h-26,68,20,"Done",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,this::onClose));
    }
    private void scene(int x,int cw){
        int half=(cw-8)/2;
        addRenderableWidget(new SceneButton(x,top+49,half,50,Settings.BackgroundScene.AURORA));
        addRenderableWidget(new SceneButton(x+half+8,top+49,cw-half-8,50,Settings.BackgroundScene.OCEAN));
        addRenderableWidget(new MenuWidgets.Toggle(x,top+114,half,24,"Animate scene",()->mod.settings.menuMotion,CoastalStyle::accent,()->change(()->mod.settings.menuMotion=!mod.settings.menuMotion)));
        var glow=new MenuWidgets.Slider(x+half+8,top+107,cw-half-8,37,"Aurora glow",mod.settings.auroraGlow,v->{mod.settings.auroraGlow=v;CoastalStyle.configure(mod.settings);mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent);
        glow.active=mod.settings.backgroundScene==Settings.BackgroundScene.AURORA;addRenderableWidget(glow);
        String[] names={"Aurora","Sea glass","Dusk","Paper"};
        Settings.MenuPalette[] choices={Settings.MenuPalette.AURORA,Settings.MenuPalette.SEA_GLASS,Settings.MenuPalette.DUSK,Settings.MenuPalette.PAPER};
        int cell=(cw-12)/4;
        for(int i=0;i<choices.length;i++){var choice=choices[i];addRenderableWidget(new MenuWidgets.Action(x+i*(cell+4),top+171,cell,20,names[i],CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.menuPalette==choice,-1,()->change(()->{mod.settings.menuPalette=choice;matched=false;})));}
    }
    private void sound(int x,int cw){
        addRenderableWidget(new MenuWidgets.Toggle(x,top+49,cw,24,"Background sound",()->mod.settings.oceanEnabled,CoastalStyle::accent,()->change(()->mod.settings.oceanEnabled=!mod.settings.oceanEnabled)));
        String[] names={"Aurora drift","Shoreline","Open sea","Rainy coast"};
        Settings.OceanTrack[] choices={Settings.OceanTrack.AURORA_DRIFT,Settings.OceanTrack.SHORELINE,Settings.OceanTrack.OPEN_SEA,Settings.OceanTrack.RAINY_COAST};int cell=(cw-12)/4;
        for(int i=0;i<choices.length;i++){var choice=choices[i];addRenderableWidget(new MenuWidgets.Action(x+i*(cell+4),top+82,cell,23,names[i],CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.oceanTrack==choice,-1,()->change(()->mod.settings.oceanTrack=choice)));}
        addRenderableWidget(new MenuWidgets.Slider(x,top+115,cw,36,"Ambience volume",mod.settings.oceanVolume,v->{mod.settings.oceanVolume=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Toggle(x,top+162,cw,24,"Also play while in game",()->mod.settings.oceanInGame,CoastalStyle::accent,()->change(()->mod.settings.oceanInGame=!mod.settings.oceanInGame)));
    }
    private void change(Runnable action){action.run();CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);CoastalStyle.panel(g,left,top,w,h);
        TrackerHud.text(g,"Atmosphere",left+14,top+13,CoastalStyle.ink());
        TrackerHud.scaled(g,section==0?"SCENE PRESETS / COLOURS + SOUND":mod.oceanStatus(),left+14,top+32,.6f,CoastalStyle.muted());
        if(section==0)TrackerHud.scaled(g,"INTERFACE PALETTE",left+14,top+158,.55f,CoastalStyle.muted());
        else if(h>=250)TrackerHud.scaled(g,"Minecraft Master and Ambient volume also apply.",left+14,top+199,.65f,CoastalStyle.muted());
        super.render(g,mx,my,delta);
    }
    private final class SceneButton extends Button.Plain {
        private final Settings.BackgroundScene scene;
        SceneButton(int x,int y,int w,int h,Settings.BackgroundScene scene){super(x,y,w,h,Component.literal(scene==Settings.BackgroundScene.AURORA?"Aurora":"Ocean"),b->{mod.settings.backgroundScene=scene;mod.settings.menuPalette=scene==Settings.BackgroundScene.AURORA?Settings.MenuPalette.AURORA:Settings.MenuPalette.SEA_GLASS;mod.settings.oceanTrack=scene==Settings.BackgroundScene.AURORA?Settings.OceanTrack.AURORA_DRIFT:Settings.OceanTrack.SHORELINE;matched=false;CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();},DEFAULT_NARRATION);this.scene=scene;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean selected=mod.settings.backgroundScene==scene;
            TrackerHud.rounded(g,x,y,x+w,y+h,isHoveredOrFocused()?CoastalStyle.hover():CoastalStyle.surface());
            TrackerHud.border(g,x,y,x+w,y+h,selected?CoastalStyle.accent():CoastalStyle.line());
            TrackerHud.text(g,getMessage().getString(),x+10,y+10,CoastalStyle.ink());
            TrackerHud.scaled(g,scene==Settings.BackgroundScene.AURORA?"Northern lights":"Ocean contours",x+10,y+25,.6f,CoastalStyle.muted());
            int px=x+w-65,py=y+9;
            if(scene==Settings.BackgroundScene.AURORA)AuroraBackdrop.draw(g,px,py,55,29,false,.9f);
            else {g.fillGradient(px,py,px+55,py+29,0xFF123341,0xFF38747A);for(int row=0;row<3;row++)CoastalStyle.wave(g,px,py+12+row*5,55,2,0xFF83C7BB,row);}
            if(selected)g.fill(x+10,y+h-6,x+30,y+h-4,CoastalStyle.accent());
        }
    }
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
