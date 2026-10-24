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
        for(int i=0;i<3;i++){int page=i;addRenderableWidget(new MenuWidgets.Action(left+w-239+i*75,top+10,71,22,i==0?"Scenes":i==1?"Details":"Sound",CoastalStyle::accent,MenuWidgets.Kind.TAB,()->section==page,-1,()->{section=page;rebuildWidgets();}));}
        if(section==0)scene(x,cw);else if(section==1)details(x,cw);else sound(x,cw);
        addRenderableWidget(new MenuWidgets.Action(x,top+h-26,68,20,"Back",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        if(section==1)addRenderableWidget(new MenuWidgets.Action(left+w-168,top+h-26,154,20,matched?"Colours applied":"Match HUD colours",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{CoastalStyle.matchHud(mod.settings);matched=true;mod.save();rebuildWidgets();}));
        else addRenderableWidget(new MenuWidgets.Action(left+w-82,top+h-26,68,20,"Done",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,this::onClose));
    }
    private void scene(int x,int cw){
        var scenes=new Settings.BackgroundScene[]{Settings.BackgroundScene.AURORA,Settings.BackgroundScene.OCEAN,Settings.BackgroundScene.SPACE,Settings.BackgroundScene.CITY,Settings.BackgroundScene.FOREST};
        int cell=(cw-12)/3;
        for(int i=0;i<scenes.length;i++)addRenderableWidget(new SceneButton(x+(i%3)*(cell+6),top+49+(i/3)*64,cell,57,scenes[i]));
    }
    private void details(int x,int cw){
        int half=(cw-8)/2;var d=mod.settings.sceneDesign();
        addRenderableWidget(new MenuWidgets.Toggle(x,top+49,half,24,"Animation",()->mod.settings.menuMotion,CoastalStyle::accent,()->change(()->mod.settings.menuMotion=!mod.settings.menuMotion)));
        addRenderableWidget(new MenuWidgets.Slider(x+half+8,top+49,cw-half-8,32,"Motion speed",d.speed,v->{d.speed=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Slider(x,top+86,half,32,"Scene intensity",d.intensity,v->{d.intensity=v;if(mod.settings.backgroundScene==Settings.BackgroundScene.AURORA)mod.settings.auroraGlow=v;CoastalStyle.configure(mod.settings);mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Slider(x+half+8,top+86,cw-half-8,32,"Particle density",d.density,v->{d.density=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        int[] colours={0x68EFBE,0xA9B8FA,0xE7AD68,0xF2AE91,0x88CADB,0xE2D6B5};int cell=(cw-105)/6;
        for(int i=0;i<colours.length;i++){int c=colours[i];addRenderableWidget(new TintButton(x+i*(cell+5),top+132,cell,19,c));}
        addRenderableWidget(new MenuWidgets.Action(x+cw-72,top+132,72,19,"Custom tint",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new ColourPickerScreen(mod,this,"Scene tint",d.tint,c->{d.tint=c;mod.save();}))));
        String[] names={"Aurora","Sea glass","Dusk","Paper"};
        Settings.MenuPalette[] choices={Settings.MenuPalette.AURORA,Settings.MenuPalette.SEA_GLASS,Settings.MenuPalette.DUSK,Settings.MenuPalette.PAPER};cell=(cw-12)/4;
        for(int i=0;i<choices.length;i++){var choice=choices[i];addRenderableWidget(new MenuWidgets.Action(x+i*(cell+4),top+172,cell,20,names[i],CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.menuPalette==choice,-1,()->change(()->{mod.settings.menuPalette=choice;matched=false;})));}
    }
    private final class TintButton extends Button.Plain {
        private final int colour;
        TintButton(int x,int y,int w,int h,int colour){super(x,y,w,h,Component.literal(String.format("Scene tint #%06X",colour)),b->change(()->mod.settings.sceneDesign().tint=colour),DEFAULT_NARRATION);this.colour=colour;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){int x=getX(),y=getY();TrackerHud.rounded(g,x,y,x+getWidth(),y+getHeight(),0xFF000000|colour);if(mod.settings.sceneDesign().tint==colour||isHoveredOrFocused())TrackerHud.border(g,x-1,y-1,x+getWidth()+1,y+getHeight()+1,CoastalStyle.ink());}
    }
    private void sound(int x,int cw){
        addRenderableWidget(new MenuWidgets.Toggle(x,top+49,cw,24,"Background sound",()->mod.settings.oceanEnabled,CoastalStyle::accent,()->change(()->mod.settings.oceanEnabled=!mod.settings.oceanEnabled)));
        String[] names={"Aurora drift","Shoreline","Open sea","Rainy coast","Deep space","City rain","Forest breeze"};
        Settings.OceanTrack[] choices={Settings.OceanTrack.AURORA_DRIFT,Settings.OceanTrack.SHORELINE,Settings.OceanTrack.OPEN_SEA,Settings.OceanTrack.RAINY_COAST,Settings.OceanTrack.DEEP_SPACE,Settings.OceanTrack.CITY_RAIN,Settings.OceanTrack.FOREST_BREEZE};int cell=(cw-12)/4;
        for(int i=0;i<choices.length;i++){var choice=choices[i];addRenderableWidget(new MenuWidgets.Action(x+(i%4)*(cell+4),top+79+(i/4)*26,cell,23,names[i],CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.oceanTrack==choice,-1,()->change(()->mod.settings.oceanTrack=choice)));}
        addRenderableWidget(new MenuWidgets.Slider(x,top+137,cw,32,"Ambience volume",mod.settings.oceanVolume,v->{mod.settings.oceanVolume=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Toggle(x,top+176,cw,22,"Also play while in game",()->mod.settings.oceanInGame,CoastalStyle::accent,()->change(()->mod.settings.oceanInGame=!mod.settings.oceanInGame)));
    }
    private void change(Runnable action){action.run();CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);CoastalStyle.panel(g,left,top,w,h);
        TrackerHud.text(g,"Atmosphere",left+14,top+13,CoastalStyle.ink());
        TrackerHud.scaled(g,section==0?"FIVE PLACES TO MAKE YOUR OWN":section==1?sceneName(mod.settings.backgroundScene)+" / YOUR DETAILS":mod.oceanStatus(),left+14,top+32,.6f,CoastalStyle.muted());
        if(section==1)TrackerHud.scaled(g,"INTERFACE PALETTE",left+14,top+160,.55f,CoastalStyle.muted());
        else if(section==2&&h>=250)TrackerHud.scaled(g,"Minecraft Master and Ambient volume also apply.",left+14,top+205,.65f,CoastalStyle.muted());
        super.render(g,mx,my,delta);
    }
    private final class SceneButton extends Button.Plain {
        private final Settings.BackgroundScene scene;
        SceneButton(int x,int y,int w,int h,Settings.BackgroundScene scene){super(x,y,w,h,Component.literal(sceneName(scene)),b->{mod.settings.backgroundScene=scene;mod.settings.menuPalette=switch(scene){case AURORA,SPACE->Settings.MenuPalette.AURORA;case OCEAN,FOREST->Settings.MenuPalette.SEA_GLASS;case CITY->Settings.MenuPalette.DUSK;};mod.settings.oceanTrack=switch(scene){case AURORA->Settings.OceanTrack.AURORA_DRIFT;case OCEAN->Settings.OceanTrack.SHORELINE;case SPACE->Settings.OceanTrack.DEEP_SPACE;case CITY->Settings.OceanTrack.CITY_RAIN;case FOREST->Settings.OceanTrack.FOREST_BREEZE;};matched=false;CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();},DEFAULT_NARRATION);this.scene=scene;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean selected=mod.settings.backgroundScene==scene;
            TrackerHud.rounded(g,x,y,x+w,y+h,isHoveredOrFocused()?CoastalStyle.hover():CoastalStyle.surface());
            TrackerHud.border(g,x,y,x+w,y+h,selected?CoastalStyle.accent():CoastalStyle.line());
            TrackerHud.scaled(g,getMessage().getString(),x+7,y+8,.8f,CoastalStyle.ink());
            TrackerHud.scaled(g,switch(scene){case AURORA->"Polar sky";case OCEAN->"Slow tides";case SPACE->"Quiet orbit";case CITY->"After hours";case FOREST->"Amber trails";},x+7,y+21,.55f,CoastalStyle.muted());
            int px=x+5,py=y+32;
            if(scene==Settings.BackgroundScene.AURORA)AuroraBackdrop.draw(g,px,py,w-10,17,false,.9f);
            else if(scene!=Settings.BackgroundScene.OCEAN)WorldBackdrop.draw(g,px,py,w-10,17,scene,false,mod.settings.sceneDesigns.get(scene));
            else {g.fillGradient(px,py,px+w-10,py+17,0xFF123341,0xFF38747A);for(int row=0;row<3;row++)CoastalStyle.wave(g,px,py+5+row*4,w-10,2,0xFF83C7BB,row);}
            if(selected)g.fill(x+5,y+h-3,x+w-5,y+h-2,CoastalStyle.accent());
        }
    }
    private static String sceneName(Settings.BackgroundScene s){return switch(s){case AURORA->"Aurora";case OCEAN->"Ocean";case SPACE->"Space";case CITY->"Rainy city";case FOREST->"Autumn forest";};}
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
