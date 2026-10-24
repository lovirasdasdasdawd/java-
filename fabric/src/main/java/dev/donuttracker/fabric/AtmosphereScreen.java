package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class AtmosphereScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left,top,w,h;
    private boolean matched;
    AtmosphereScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Ocean & style"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(480,width-16);h=Math.min(272,height-12);left=(width-w)/2;top=(height-h)/2;
        int x=left+14,cw=w-28,half=(cw-6)/2;
        addRenderableWidget(new MenuWidgets.Toggle(x,top+42,cw,24,"Ocean ambience",()->mod.settings.oceanEnabled,CoastalStyle::accent,()->change(()->mod.settings.oceanEnabled=!mod.settings.oceanEnabled)));
        String[] names={"Shoreline","Open sea","Rainy coast"};
        for(int i=0;i<3;i++){var choice=Settings.OceanTrack.values()[i];int cell=(cw-8)/3;
            addRenderableWidget(new MenuWidgets.Action(x+i*(cell+4),top+72,cell,20,names[i],CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.oceanTrack==choice,-1,()->change(()->mod.settings.oceanTrack=choice)));
        }
        addRenderableWidget(new MenuWidgets.Slider(x,top+98,cw,34,"Ocean volume",mod.settings.oceanVolume,v->{mod.settings.oceanVolume=v;mod.save();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Toggle(x,top+138,half,24,"In-game audio",()->mod.settings.oceanInGame,CoastalStyle::accent,()->change(()->mod.settings.oceanInGame=!mod.settings.oceanInGame)));
        addRenderableWidget(new MenuWidgets.Toggle(x+half+6,top+138,cw-half-6,24,"Menu motion",()->mod.settings.menuMotion,CoastalStyle::accent,()->change(()->mod.settings.menuMotion=!mod.settings.menuMotion)));
        String[] palettes={"Sea glass","Dusk","Warm paper"};
        for(int i=0;i<3;i++){var choice=Settings.MenuPalette.values()[i];int cell=(cw-8)/3;
            addRenderableWidget(new MenuWidgets.Action(x+i*(cell+4),top+176,cell,20,palettes[i],CoastalStyle::accent,MenuWidgets.Kind.TAB,()->mod.settings.menuPalette==choice,-1,()->change(()->{mod.settings.menuPalette=choice;matched=false;})));
        }
        addRenderableWidget(new MenuWidgets.Action(x,top+h-26,68,20,"Back",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        addRenderableWidget(new MenuWidgets.Action(left+w-168,top+h-26,154,20,matched?"Colours applied":"Match HUD colours",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{CoastalStyle.matchHud(mod.settings);matched=true;mod.save();rebuildWidgets();}));
    }
    private void change(Runnable action){action.run();CoastalStyle.configure(mod.settings);mod.save();rebuildWidgets();}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);CoastalStyle.panel(g,left,top,w,h);
        TrackerHud.text(g,"Ocean & style",left+14,top+12,CoastalStyle.ink());
        TrackerHud.scaled(g,"TIDELINE  /  "+mod.oceanStatus(),left+14,top+27,.6f,CoastalStyle.muted());
        TrackerHud.scaled(g,"MENU PALETTE",left+14,top+166,.55f,CoastalStyle.muted());
        if(h>=260)TrackerHud.scaled(g,"Menu palette changes keep custom HUD colours.",left+14,top+211,.7f,CoastalStyle.muted());
        super.render(g,mx,my,delta);
    }
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
