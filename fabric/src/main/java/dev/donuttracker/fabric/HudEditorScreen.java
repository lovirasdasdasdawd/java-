package dev.donuttracker.fabric;

import dev.donuttracker.core.Settings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Direct placement, alignment guides, and precise keyboard nudges. */
final class HudEditorScreen extends Screen {
    private final DonutTrackerClient mod;
    private final Screen parent;
    private Settings.Hud dragging, selected, only;
    private double dx,dy;
    HudEditorScreen(DonutTrackerClient mod,Screen parent){super(Component.literal("Arrange HUDs"));this.mod=mod;this.parent=parent;}
    HudEditorScreen(DonutTrackerClient mod,Screen parent,Settings.Hud selected){this(mod,parent);this.selected=selected;this.only=selected;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){addRenderableWidget(new MenuWidgets.Action(width/2-35,height-29,70,21,"Done",()->mod.settings.profit.accent,MenuWidgets.Kind.PRIMARY,this::onClose));}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0x70090C10);
        if(dragging!=null){
            g.fill(width/2,0,width/2+1,height,0x307FAD9D);g.fill(0,height/2,width,height/2+1,0x307FAD9D);
        }
        TrackerHud.render(g,mod,true);
        for(var h:new Settings.Hud[]{mod.settings.profit,mod.settings.queue}){
            int x=TrackerHud.x(h,width),y=TrackerHud.y(h,height),w=(int)(TrackerHud.width()*h.scale),ht=(int)(TrackerHud.height(h)*h.scale);
            if(h==selected){
                TrackerHud.border(g,x-2,y-2,x+w+2,y+ht+2,0xCC000000|h.accent);
                String label=x+", "+y;int yy=y>15?y-12:y+ht+5;
                g.drawString(font,label,x+2,Math.min(height-45,yy),0xFFE2E8E4,false);
            }
        }
        String hint=dragging!=null?"Release to place":selected==null?"Drag a HUD to place it":"Arrow keys to nudge  /  Drag to move";
        int tw=font.width(hint);TrackerHud.rounded(g,width/2-tw/2-10,height-52,width/2+tw/2+10,height-33,0xF0181D24);
        g.drawCenteredString(font,hint,width/2,height-46,0xFFDCE3E0);
        super.render(g,mx,my,delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(super.mouseClicked(e,twice))return true;
        if(e.button()!=0)return false;
        for(var h:new Settings.Hud[]{mod.settings.queue,mod.settings.profit}){
            if(only!=null&&only!=h)continue;
            int x=TrackerHud.x(h,width),y=TrackerHud.y(h,height);
            if(e.x()>=x&&e.x()<=x+TrackerHud.width()*h.scale&&e.y()>=y&&e.y()<=y+TrackerHud.height(h)*h.scale){
                dragging=selected=h;setDragging(true);dx=e.x()-x;dy=e.y()-y;return true;
            }
        }
        return false;
    }
    private void place(double x,double y){
        if(dragging==null)return;
        int w=(int)(TrackerHud.width()*dragging.scale),h=(int)(TrackerHud.height(dragging)*dragging.scale);
        dragging.x=snapped((int)(x-dx),w,width);dragging.y=snapped((int)(y-dy),h,height);
    }
    private int snapped(int pos,int length,int total){
        for(int anchor:new int[]{8,(total-length)/2,total-length-8})if(Math.abs(pos-anchor)<=3)return Math.max(0,anchor);
        return Math.clamp(pos,0,Math.max(0,total-length));
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double ax,double ay){if(dragging==null)return super.mouseDragged(e,ax,ay);place(e.x(),e.y());return true;}
    @Override public boolean mouseReleased(MouseButtonEvent e){if(dragging==null)return super.mouseReleased(e);place(e.x(),e.y());dragging=null;setDragging(false);mod.save();return true;}
    @Override public boolean keyPressed(KeyEvent e){
        if(selected!=null){
            int dx=e.key()==GLFW.GLFW_KEY_LEFT?-1:e.key()==GLFW.GLFW_KEY_RIGHT?1:0;
            int dy=e.key()==GLFW.GLFW_KEY_UP?-1:e.key()==GLFW.GLFW_KEY_DOWN?1:0;
            if(dx!=0||dy!=0){selected.x=Math.clamp(TrackerHud.x(selected,width)+dx,0,Math.max(0,width-(int)(TrackerHud.width()*selected.scale)));selected.y=Math.clamp(TrackerHud.y(selected,height)+dy,0,Math.max(0,height-(int)(TrackerHud.height(selected)*selected.scale)));mod.save();return true;}
        }
        return super.keyPressed(e);
    }
    @Override public void onClose(){mod.save();minecraft.setScreen(parent);}
}
