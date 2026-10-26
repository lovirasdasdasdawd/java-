package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Separate scene and sound pages keep atmosphere controls easy to scan. */
final class AtmosphereScreen extends Screen {
    private final MenuMotion transition=new MenuMotion();
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left,top,w,h,section,scenePage,soundPage;
    private boolean matched;
    AtmosphereScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Atmosphere"));this.mod=mod;this.parent=parent;scenePage=mod.settings.backgroundScene.ordinal()/5;soundPage=mod.settings.oceanTrack.ordinal()/6;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(500,width-16);h=Math.min(260,height-12);left=(width-w)/2;top=(height-h)/2;
        int x=left+14,cw=w-28;
        for(int i=0;i<4;i++){int page=i;addRenderableWidget(new MenuWidgets.Action(left+w-314+i*75,top+10,71,22,i==0?"Scenes":i==1?"Details":i==2?"Sound":"Panels",CoastalStyle::accent,MenuWidgets.Kind.TAB,()->section==page,-1,()->{section=page;transition.page();rebuildWidgets();}));}
        if(section==0)scene(x,cw);else if(section==1)details(x,cw);else if(section==2)sound(x,cw);else panels(x,cw);
        addRenderableWidget(new MenuWidgets.Action(x,top+h-26,68,20,"Back",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        if(section==1)addRenderableWidget(new MenuWidgets.Action(left+w-168,top+h-26,154,20,matched?"Colours applied":"Match HUD colours",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{CoastalStyle.matchHud(mod.settings);matched=true;mod.save();rebuildWidgets();}));
        else addRenderableWidget(new MenuWidgets.Action(left+w-82,top+h-26,68,20,"Done",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,this::onClose));
    }
    private void scene(int x,int cw){
        var scenes=Settings.BackgroundScene.values();
        int cell=(cw-12)/3;
        for(int i=0;i<Math.min(5,scenes.length-scenePage*5);i++)addRenderableWidget(new SceneButton(x+(i%3)*(cell+6),top+49+(i/3)*64,cell,57,scenes[scenePage*5+i]));
        pager(x,top+180,cw,scenePage,(scenes.length+4)/5,false);
        addRenderableWidget(new MenuWidgets.Toggle(x+2*(cell+6),top+125,cell,26,"Glass panels",()->mod.settings.menuGlass,CoastalStyle::accent,()->change(()->mod.settings.menuGlass=!mod.settings.menuGlass)));
        addRenderableWidget(new MenuWidgets.Action(x+2*(cell+6),top+155,cell,20,"Open / close: "+switch(mod.settings.uiMotion){case FULL->"Full";case REDUCED->"Reduced";case OFF->"Off";},CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->change(()->mod.settings.uiMotion=Settings.UiMotion.values()[(mod.settings.uiMotion.ordinal()+1)%Settings.UiMotion.values().length])));
    }
    private void details(int x,int cw){
        int half=(cw-8)/2;var d=mod.settings.sceneDesign();
        addRenderableWidget(new MenuWidgets.Toggle(x,top+49,half,24,"Scene animation",()->mod.settings.menuMotion,CoastalStyle::accent,()->change(()->mod.settings.menuMotion=!mod.settings.menuMotion)));
        addRenderableWidget(new MenuWidgets.Slider(x+half+8,top+49,cw-half-8,32,"Motion speed",d.speed,v->{d.speed=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Slider(x,top+86,half,32,"Scene intensity",d.intensity,v->{d.intensity=v;if(mod.settings.backgroundScene==Settings.BackgroundScene.AURORA)mod.settings.auroraGlow=v;CoastalStyle.configure(mod.settings);mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Slider(x+half+8,top+86,cw-half-8,32,"Particle density",d.density,v->{d.density=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        int[] colours={0x68EFBE,0xA9B8FA,0xE7AD68,0xF2AE91,0x88CADB,0xE2D6B5};int cell=(cw-105)/6;
        for(int i=0;i<colours.length;i++){int c=colours[i];addRenderableWidget(new TintButton(x+i*(cell+5),top+132,cell,19,c));}
        addRenderableWidget(new MenuWidgets.Action(x+cw-72,top+132,72,19,"Custom tint",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new ColourPickerScreen(mod,this,"Scene tint",d.tint,c->{d.tint=c;mod.save();}))));
        addRenderableWidget(new MenuWidgets.Action(x,top+172,cw,23,"Interface palette: "+CoastalStyle.paletteName(mod.settings.menuPalette)+"  >",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->{section=3;rebuildWidgets();}));
    }
    private final class TintButton extends Button.Plain {
        private final int colour;
        TintButton(int x,int y,int w,int h,int colour){super(x,y,w,h,Component.literal(String.format("Scene tint #%06X",colour)),b->change(()->mod.settings.sceneDesign().tint=colour),DEFAULT_NARRATION);this.colour=colour;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){int x=getX(),y=getY();TrackerHud.rounded(g,x,y,x+getWidth(),y+getHeight(),0xFF000000|colour);if(mod.settings.sceneDesign().tint==colour||isHoveredOrFocused())TrackerHud.border(g,x-1,y-1,x+getWidth()+1,y+getHeight()+1,CoastalStyle.ink());}
    }
    private void panels(int x,int cw){
        addRenderableWidget(new MenuWidgets.Toggle(x,top+49,(cw-8)/2,25,"Glass panels",()->mod.settings.menuGlass,CoastalStyle::accent,()->change(()->mod.settings.menuGlass=!mod.settings.menuGlass)));
        addRenderableWidget(new MenuWidgets.Toggle(x+(cw-8)/2+8,top+49,cw-(cw-8)/2-8,25,"Christmas GUI",()->mod.settings.christmasGui,CoastalStyle::accent,()->change(()->mod.settings.christmasGui=!mod.settings.christmasGui)));
        var opacity=new MenuWidgets.Slider(x,top+82,cw,36,"Glass opacity",(mod.settings.menuOpacity-.3)/.7,v->{mod.settings.menuOpacity=.3+.7*v;CoastalStyle.configure(mod.settings);mod.save();},v->Math.round((.3+.7*v)*100)+"%",CoastalStyle::accent);opacity.active=mod.settings.menuGlass;addRenderableWidget(opacity);
        addRenderableWidget(new MenuWidgets.Action(x,top+124,cw,22,"Open / close: "+switch(mod.settings.uiMotion){case FULL->"Full";case REDUCED->"Reduced";case OFF->"Off";},CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->change(()->mod.settings.uiMotion=Settings.UiMotion.values()[(mod.settings.uiMotion.ordinal()+1)%Settings.UiMotion.values().length])));
        int cell=(cw-16)/5;
        var palettes=Settings.MenuPalette.values();
        for(int i=0;i<palettes.length;i++)addRenderableWidget(new PaletteButton(x+(i%5)*(cell+4),top+150+(i/5)*25,cell,23,palettes[i]));
    }
    private void sound(int x,int cw){
        addRenderableWidget(new MenuWidgets.Toggle(x,top+49,cw,24,"Background sound",()->mod.settings.oceanEnabled,CoastalStyle::accent,()->change(()->mod.settings.oceanEnabled=!mod.settings.oceanEnabled)));
        Settings.OceanTrack[] choices=Settings.OceanTrack.values();int cell=(cw-8)/3;
        for(int i=0;i<Math.min(6,choices.length-soundPage*6);i++){int index=soundPage*6+i;var choice=choices[index];addRenderableWidget(new MenuWidgets.Action(x+(i%3)*(cell+4),top+77+(i/3)*24,cell,22,soundName(choice),CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.oceanTrack==choice,-1,()->change(()->mod.settings.oceanTrack=choice)));}
        pager(x,top+125,cw,soundPage,(choices.length+5)/6,true);
        addRenderableWidget(new MenuWidgets.Slider(x,top+149,cw,28,"Ambience volume",mod.settings.oceanVolume,v->{mod.settings.oceanVolume=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Toggle(x,top+181,cw,18,"Also play while in game",()->mod.settings.oceanInGame,CoastalStyle::accent,()->change(()->mod.settings.oceanInGame=!mod.settings.oceanInGame)));
    }
    private void pager(int x,int yy,int cw,int page,int pages,boolean sound){
        var prev=new MenuWidgets.Action(x,yy,68,18,"< Previous",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->{if(sound)soundPage--;else scenePage--;transition.page();rebuildWidgets();});prev.active=page>0;addRenderableWidget(prev);
        var next=new MenuWidgets.Action(x+cw-68,yy,68,18,"Next >",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->{if(sound)soundPage++;else scenePage++;transition.page();rebuildWidgets();});next.active=page<pages-1;addRenderableWidget(next);
    }
    private void change(Runnable action){action.run();CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);transition.begin(g,left,top,w,h);CoastalStyle.panel(g,left,top,w,h);
        TrackerHud.text(g,"Atmosphere",left+14,top+13,CoastalStyle.ink());
        TrackerHud.scaled(g,section==0?Settings.BackgroundScene.values().length+" SCENES / PAGE "+(scenePage+1)+" OF "+((Settings.BackgroundScene.values().length+4)/5):section==1?(mod.settings.christmasGui?"CHRISTMAS":sceneName(mod.settings.backgroundScene))+" / YOUR DETAILS":section==2?mod.oceanStatus():"INTERFACE / PALETTES & GLASS",left+14,top+32,.6f,CoastalStyle.muted());
        if(section==1)TrackerHud.scaled(g,"INTERFACE PALETTE",left+14,top+160,.55f,CoastalStyle.muted());
        else if(section==2&&h>=250)TrackerHud.scaled(g,"Minecraft Master and Ambient volume also apply.",left+14,top+205,.65f,CoastalStyle.muted());
        if(section==0||section==2){String page="Page "+((section==0?scenePage:soundPage)+1)+" / "+(section==0?(Settings.BackgroundScene.values().length+4)/5:(Settings.OceanTrack.values().length+5)/6);TrackerHud.scaled(g,page,left+w/2-22,top+(section==0?186:131),.75f,CoastalStyle.muted());}
        super.render(g,mx,my,delta);
        transition.end(g);
    }
    private final class SceneButton extends Button.Plain {
        private final Settings.BackgroundScene scene;
        SceneButton(int x,int y,int w,int h,Settings.BackgroundScene scene){super(x,y,w,h,Component.literal(sceneName(scene)),b->{mod.settings.christmasGui=false;mod.settings.backgroundScene=scene;mod.settings.menuPalette=switch(scene){case AURORA,SPACE->Settings.MenuPalette.AURORA;case OCEAN,FOREST->Settings.MenuPalette.SEA_GLASS;case CITY,DESERT,SAKURA,STORM->Settings.MenuPalette.DUSK;case SNOW,RAILWAY->Settings.MenuPalette.AURORA;case VOLCANO->Settings.MenuPalette.DUSK;case MARSH->Settings.MenuPalette.SEA_GLASS;case ABYSS->Settings.MenuPalette.SEA_GLASS;case LIGHTHOUSE->Settings.MenuPalette.COFFEE;case RIVER,BAMBOO->Settings.MenuPalette.MOSS;case CLOUDS->Settings.MenuPalette.LAVENDER;case MOON->Settings.MenuPalette.GLACIER;};mod.settings.oceanTrack=switch(scene){case AURORA->Settings.OceanTrack.AURORA_DRIFT;case OCEAN->Settings.OceanTrack.SHORELINE;case SPACE->Settings.OceanTrack.DEEP_SPACE;case CITY->Settings.OceanTrack.CITY_RAIN;case FOREST->Settings.OceanTrack.FOREST_BREEZE;case DESERT->Settings.OceanTrack.DESERT_WIND;case SNOW->Settings.OceanTrack.SNOW_HUSH;case ABYSS->Settings.OceanTrack.ABYSSAL_DRIFT;case SAKURA->Settings.OceanTrack.GARDEN_CHIMES;case STORM->Settings.OceanTrack.DISTANT_STORM;case VOLCANO->Settings.OceanTrack.EMBER_WIND;case MARSH->Settings.OceanTrack.MARSH_NIGHT;case RAILWAY->Settings.OceanTrack.NIGHT_RAIL;case LIGHTHOUSE->Settings.OceanTrack.HARBOR_WIND;case RIVER->Settings.OceanTrack.RIVER_FLOW;case CLOUDS->Settings.OceanTrack.CLOUD_DRIFT;case MOON->Settings.OceanTrack.LAKE_NIGHT;case BAMBOO->Settings.OceanTrack.BAMBOO_RUSTLE;};matched=false;soundPage=mod.settings.oceanTrack.ordinal()/6;CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();},DEFAULT_NARRATION);this.scene=scene;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean selected=mod.settings.backgroundScene==scene;
            TrackerHud.rounded(g,x,y,x+w,y+h,isHoveredOrFocused()?CoastalStyle.hover():CoastalStyle.surface());
            TrackerHud.border(g,x,y,x+w,y+h,selected?CoastalStyle.accent():CoastalStyle.line());
            TrackerHud.scaled(g,getMessage().getString(),x+7,y+8,.8f,CoastalStyle.ink());
            TrackerHud.scaled(g,switch(scene){case AURORA->"Polar sky";case OCEAN->"Slow tides";case SPACE->"Quiet orbit";case CITY->"After hours";case FOREST->"Amber trails";case DESERT->"Evening dunes";case SNOW->"Winter silence";case ABYSS->"Below the surface";case SAKURA->"Petals & lanterns";case STORM->"Windward shore";case VOLCANO->"Ash & embers";case MARSH->"Fireflies at dusk";case RAILWAY->"Last train home";case LIGHTHOUSE->"Harbor after dusk";case RIVER->"Water through pines";case CLOUDS->"Above the weather";case MOON->"Still silver water";case BAMBOO->"A sheltered path";},x+7,y+21,.55f,CoastalStyle.muted());
            int px=x+5,py=y+32;
            if(scene==Settings.BackgroundScene.AURORA)AuroraBackdrop.draw(g,px,py,w-10,17,false,.9f);
            else if(scene!=Settings.BackgroundScene.OCEAN)WorldBackdrop.draw(g,px,py,w-10,17,scene,false,mod.settings.sceneDesigns.get(scene));
            else {g.fillGradient(px,py,px+w-10,py+17,0xFF123341,0xFF38747A);for(int row=0;row<3;row++)CoastalStyle.wave(g,px,py+5+row*4,w-10,2,0xFF83C7BB,row);}
            if(selected)g.fill(x+5,y+h-3,x+w-5,y+h-2,CoastalStyle.accent());
        }
    }
    private static String sceneName(Settings.BackgroundScene s){return switch(s){case AURORA->"Aurora";case OCEAN->"Ocean";case SPACE->"Space";case CITY->"Rainy city";case FOREST->"Autumn forest";case DESERT->"Desert dusk";case SNOW->"Snowfall";case ABYSS->"Deep ocean";case SAKURA->"Sakura garden";case STORM->"Storm coast";case VOLCANO->"Volcanic night";case MARSH->"Firefly marsh";case RAILWAY->"Midnight railway";case LIGHTHOUSE->"Lighthouse";case RIVER->"River valley";case CLOUDS->"Cloudscape";case MOON->"Moonlit lake";case BAMBOO->"Bamboo grove";};}
    static String soundName(Settings.OceanTrack track){return switch(track){case SHORELINE->"Shoreline";case OPEN_SEA->"Open sea";case RAINY_COAST->"Rainy coast";case AURORA_DRIFT->"Aurora drift";case DEEP_SPACE->"Deep space";case CITY_RAIN->"City rain";case FOREST_BREEZE->"Forest breeze";case DESERT_WIND->"Desert wind";case SNOW_HUSH->"Snow hush";case ABYSSAL_DRIFT->"Abyssal drift";case GARDEN_CHIMES->"Garden chimes";case DISTANT_STORM->"Distant storm";case EMBER_WIND->"Ember wind";case MARSH_NIGHT->"Marsh night";case NIGHT_RAIL->"Night rail";case HARBOR_WIND->"Harbor wind";case RIVER_FLOW->"River flow";case CLOUD_DRIFT->"Cloud drift";case LAKE_NIGHT->"Lake at night";case BAMBOO_RUSTLE->"Bamboo rustle";};}
    private final class PaletteButton extends Button.Plain {
        private final Settings.MenuPalette palette;
        PaletteButton(int x,int y,int w,int h,Settings.MenuPalette palette){super(x,y,w,h,Component.literal(CoastalStyle.paletteName(palette)),b->change(()->{mod.settings.menuPalette=palette;mod.settings.christmasGui=false;matched=false;}),DEFAULT_NARRATION);this.palette=palette;}
        @Override protected void renderContents(GuiGraphics g,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth(),h=getHeight(),accent=CoastalStyle.paletteAccent(palette);
            TrackerHud.rounded(g,x,y,x+w,y+h,CoastalStyle.palettePanel(palette));
            TrackerHud.border(g,x,y,x+w,y+h,mod.settings.menuPalette==palette||isHoveredOrFocused()?accent:CoastalStyle.line());
            g.fill(x+5,y+5,x+11,y+11,accent);g.fill(x+12,y+5,x+18,y+11,palette==Settings.MenuPalette.PAPER?0xFF243F48:0xFFE6EDF8);
            TrackerHud.scaled(g,CoastalStyle.paletteName(palette),x+5,y+15,.65f,accent);
            if(mod.settings.menuPalette==palette)g.fill(x+w-6,y+5,x+w-3,y+8,accent);
        }
    }
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
