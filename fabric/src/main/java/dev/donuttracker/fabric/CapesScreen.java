package dev.donuttracker.fabric;

import dev.donuttracker.core.CapePattern;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The same module shell, with actual cape texels and direct colour pickers. */
final class CapesScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private int left,top,w,h,cw;
    private int[] pixels;
    private String status="Local cosmetic / visible to you only";
    CapesScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Capes"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(480,width-16);h=Math.min(270,height-12);left=(width-w)/2;top=(height-h)/2;cw=w-183;
        var c=CapeCosmetics.config;
        pixels=CapePattern.pixels(c.design,c.cloth,c.frosting,c.accent,c.glow,false);
        addRenderableWidget(new MenuWidgets.Toggle(left+14,top+47,cw/2-3,23,"Cape",()->c.enabled,CoastalStyle::accent,()->change(()->c.enabled=!c.enabled)));
        addRenderableWidget(new MenuWidgets.Toggle(left+17+cw/2,top+47,cw-cw/2-3,23,"Glow",()->c.glowing,CoastalStyle::accent,()->change(()->c.glowing=!c.glowing)));
        int cell=(cw-8)/3;
        for(int i=0;i<3;i++){var d=CapePattern.Design.values()[i];addRenderableWidget(new MenuWidgets.Action(left+14+i*(cell+4),top+77,cell,20,d==CapePattern.Design.CLASSIC?"Donut":d==CapePattern.Design.ORBIT?"Orbit":"Crown",CoastalStyle::accent,MenuWidgets.Kind.TAB,()->c.design==d,-1,()->change(()->c.design=d)));}
        colour("Cloth",c.cloth,top+101,v->c.cloth=v);colour("Frosting",c.frosting,top+125,v->c.frosting=v);colour("Trim & stars",c.accent,top+149,v->c.accent=v);
        addRenderableWidget(new MenuWidgets.Slider(left+14,top+174,cw,28,"Glow strength",c.glow,v->{c.glow=v;save();CapeCosmetics.refresh();},v->Math.round(v*100)+"%",CoastalStyle::accent));
        addRenderableWidget(new MenuWidgets.Action(left+14,top+h-24,75,19,"< Modules",CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,this::onClose));
        addRenderableWidget(new MenuWidgets.Action(left+w-149,top+h-24,135,19,"View in F5",CoastalStyle::accent,MenuWidgets.Kind.PRIMARY,()->{save();minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);minecraft.setScreen(null);}));
    }
    private void colour(String name,int value,int yy,java.util.function.IntConsumer setter){
        addRenderableWidget(new MenuWidgets.Action(left+14,yy,cw,20,name+"  #"+String.format("%06X",value),CoastalStyle::accent,MenuWidgets.Kind.SECONDARY,()->minecraft.setScreen(new ColourPickerScreen(mod,this,"Cape "+name,value,v->{setter.accept(v);save();CapeCosmetics.refresh();}))));
    }
    private void change(Runnable r){r.run();save();CapeCosmetics.refresh();rebuildWidgets();}
    private void save(){try{CapeCosmetics.config.save();status="Saved / local cosmetic";}catch(Exception ex){status=ex.getMessage();}}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CoastalStyle.background(g,width,height);CoastalStyle.panel(g,left,top,w,h);
        TrackerHud.text(g,"Capes",left+14,top+14,CoastalStyle.ink());TrackerHud.scaled(g,"DONUT COLLECTION / REAR VIEW",left+14,top+31,.6f,CoastalStyle.muted());
        int px=left+w-153,py=top+47,pw=139,ph=Math.min(155,h-89);
        TrackerHud.rounded(g,px,py,px+pw,py+ph,CoastalStyle.surface());TrackerHud.border(g,px,py,px+pw,py+ph,CoastalStyle.line());
        float scale=Math.min(1.8f,(ph-23)/64f);int dx=px+(int)((pw-40*scale)/2),dy=py+11;
        // Render the real, cached atlas pixels; no decorative mock texture.
        g.pose().pushMatrix();g.pose().translate(dx,dy);g.pose().scale(scale,scale);
        for(int y=0;y<64;y++)for(int x=0;x<40;x++)g.fill(x,y,x+1,y+1,pixels[(y+4)*CapePattern.WIDTH+x+4]);
        g.pose().popMatrix();
        TrackerHud.scaled(g,"Local only / emissive trim",px+5,py+ph+8,.6f,CoastalStyle.muted());
        super.render(g,mx,my,delta);
        int[] colors={CapeCosmetics.config.cloth,CapeCosmetics.config.frosting,CapeCosmetics.config.accent};for(int i=0;i<3;i++)TrackerHud.rounded(g,left+cw-2,top+106+i*24,left+cw+8,top+116+i*24,0xFF000000|colors[i]);
        if(h>250)TrackerHud.scaled(g,minecraft.font.plainSubstrByWidth(status,(int)((w-28)/.6)),left+14,top+h-40,.6f,CoastalStyle.muted());
    }
    @Override public void onClose(){save();minecraft.setScreen(parent);}
}
