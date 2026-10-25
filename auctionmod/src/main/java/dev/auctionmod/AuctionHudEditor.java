package dev.auctionmod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Drag hit testing uses the exact scaled render bounds. Editing never starts or stops an auction. */
public final class AuctionHudEditor extends Screen {
    private final AuctionModClient mod;
    private final Screen parent;
    private boolean dragging;
    private double dx,dy;
    private String error="";
    public AuctionHudEditor(AuctionModClient mod,Screen parent){super(Component.literal("Move auction HUD"));this.mod=mod;this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        addRenderableWidget(new AuctionUi.Action(width/2-90,height-29,100,20,"Reset position",false,()->{mod.config.hudX=12;mod.config.hudY=12;mod.config.hudPinned=false;save();}));
        addRenderableWidget(new AuctionUi.Action(width/2+16,height-29,74,20,"Done",true,this::onClose));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0x75060B14);AuctionHud.draw(g,mod,true);var b=AuctionHud.bounds(width,height,mod);
        g.fill(b.x()-2,b.y()-2,b.x()+b.width()+2,b.y()-1,AuctionUi.CYAN);g.fill(b.x()-2,b.y()+b.height()+1,b.x()+b.width()+2,b.y()+b.height()+2,AuctionUi.CYAN);
        String hint="Drag to move | Scroll to resize | Arrows to nudge";AuctionUi.round(g,width/2-font.width(hint)/2-6,height-52,font.width(hint)+12,18,0xEE111925);AuctionUi.text(g,hint,width/2-font.width(hint)/2,height-47,AuctionUi.INK);
        if(!error.isEmpty())AuctionUi.text(g,font.plainSubstrByWidth(error,width-20),10,height-65,0xFFF09A9D);
        super.render(g,mx,my,delta);
    }
    private AuctionHud.Bounds pin(){var b=AuctionHud.bounds(width,height,mod);mod.config.hudX=b.x();mod.config.hudY=b.y();mod.config.hudPinned=true;return b;}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(super.mouseClicked(e,twice))return true;if(e.button()!=0)return false;var b=AuctionHud.bounds(width,height,mod);
        if(e.x()>=b.x()&&e.x()<=b.x()+b.width()&&e.y()>=b.y()&&e.y()<=b.y()+b.height()){pin();dragging=true;setDragging(true);dx=e.x()-b.x();dy=e.y()-b.y();return true;}return false;
    }
    private void place(double x,double y){var b=AuctionHud.bounds(width,height,mod);mod.config.hudX=Math.clamp((int)(x-dx),0,Math.max(0,width-b.width()));mod.config.hudY=Math.clamp((int)(y-dy),0,Math.max(0,height-b.height()));}
    @Override public boolean mouseDragged(MouseButtonEvent e,double xx,double yy){if(!dragging)return super.mouseDragged(e,xx,yy);place(e.x(),e.y());return true;}
    @Override public boolean mouseReleased(MouseButtonEvent e){if(!dragging)return super.mouseReleased(e);place(e.x(),e.y());dragging=false;setDragging(false);save();return true;}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(vertical==0)return false;pin();mod.config.hudScale=Math.clamp(mod.config.hudScale+Math.signum(vertical)*.05,.4,1.6);return true;}
    @Override public boolean keyPressed(KeyEvent e){int xx=e.key()==GLFW.GLFW_KEY_LEFT?-1:e.key()==GLFW.GLFW_KEY_RIGHT?1:0,yy=e.key()==GLFW.GLFW_KEY_UP?-1:e.key()==GLFW.GLFW_KEY_DOWN?1:0;if(xx!=0||yy!=0){pin();var b=AuctionHud.bounds(width,height,mod);mod.config.hudX=Math.clamp(b.x()+xx,0,Math.max(0,width-b.width()));mod.config.hudY=Math.clamp(b.y()+yy,0,Math.max(0,height-b.height()));save();return true;}return super.keyPressed(e);}
    private boolean save(){try{mod.saveConfig();error="";return true;}catch(Exception ex){error=ex.getMessage();return false;}}
    @Override public void onClose(){if(save()){if(parent instanceof AuctionScreen screen)screen.resumeVisuals();minecraft.setScreen(parent);}}
}
