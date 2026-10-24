package dev.auctionmod;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AuctionScreen extends Screen {
    private final AuctionModClient mod;
    private final Screen parent;
    private final List<ItemStack> items=new ArrayList<>(),filtered=new ArrayList<>();
    private EditBox search,minimum,time;
    private AuctionUi.Action done,glass;
    private ItemStack selected=ItemStack.EMPTY;
    private int left,top,w,h,gridX,gridY,gridBottom,columns,rows,scrollRow;
    private String query="",minimumText,timeText,error="";
    private static final int CELL=25;
    public AuctionScreen(AuctionModClient mod,Screen parent){
        super(Component.literal("Choose item image"));this.mod=mod;this.parent=parent;
        minimumText=mod.config.minimum;timeText=Amounts.time(mod.config.durationSeconds);
        for(var item:BuiltInRegistries.ITEM)if(item!=Items.AIR)items.add(item.getDefaultInstance());
        items.sort(Comparator.comparing(s->s.getHoverName().getString().toLowerCase(Locale.ROOT)));
        var id=Identifier.tryParse(mod.config.selectedItem);if(id!=null)selected=BuiltInRegistries.ITEM.getOptional(id).map(i->i.getDefaultInstance()).orElse(ItemStack.EMPTY);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        w=Math.min(560,width-16);h=Math.min(340,height-16);left=(width-w)/2;top=(height-h)/2;
        gridX=left+12;gridY=top+70;gridBottom=top+h-100;columns=Math.max(1,(w-32)/CELL);rows=Math.max(1,(gridBottom-gridY)/CELL);
        search=new EditBox(font,left+12,top+42,w-24,20,Component.literal("Search all items"));search.setMaxLength(80);search.setHint(Component.literal("Search items or blocks..."));search.setValue(query);search.setResponder(s->{query=s;scrollRow=0;filter();});addRenderableWidget(search);
        int half=(w-30)/2;
        minimum=new EditBox(font,left+12,top+h-80,half,20,Component.literal("Minimum bid"));minimum.setMaxLength(64);minimum.setValue(minimumText);minimum.setResponder(s->minimumText=s);addRenderableWidget(minimum);
        time=new EditBox(font,left+18+half,top+h-80,w-half-30,20,Component.literal("Auction time"));time.setMaxLength(12);time.setValue(timeText);time.setResponder(s->timeText=s);addRenderableWidget(time);
        glass=new AuctionUi.Action(left+w-96,top+9,84,20,mod.config.glassPanels?"Glass: On":"Glass: Off",false,()->{mod.config.glassPanels=!mod.config.glassPanels;glass.setMessage(Component.literal(mod.config.glassPanels?"Glass: On":"Glass: Off"));try{mod.saveConfig();error="";}catch(Exception ex){error=ex.getMessage();}});addRenderableWidget(glass);
        done=new AuctionUi.Action(left+w-180,top+h-26,94,20,"Done",true,this::apply);addRenderableWidget(done);
        addRenderableWidget(new AuctionUi.Action(left+w-80,top+h-26,68,20,"Back",false,this::onClose));
        filter();refreshControls();setInitialFocus(search);
    }
    private void filter(){String needle=query.strip().toLowerCase(Locale.ROOT);filtered.clear();for(var stack:items)if(stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)||BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().contains(needle))filtered.add(stack);scrollRow=Math.clamp(scrollRow,0,maxScroll());}
    private int maxScroll(){return Math.max(0,(filtered.size()+columns-1)/columns-rows);}
    private void refreshControls(){boolean chosen=!selected.isEmpty();minimum.visible=time.visible=chosen;minimum.active=time.active=chosen;boolean running=mod.auction.phase()==AuctionState.Phase.RUNNING;done.active=running||chosen;done.setMessage(Component.literal(running?"Stop auction":"Done"));}
    private void apply(){
        if(mod.auction.phase()==AuctionState.Phase.RUNNING){mod.auction.cancel();error="Auction cancelled.";refreshControls();return;}
        try{mod.start(selected,minimumText,timeText);minecraft.setScreen(null);}catch(Exception ex){error=ex.getMessage()==null?"Could not start auction.":ex.getMessage();}
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(super.mouseClicked(e,twice))return true;
        if(e.button()==0&&e.x()>=gridX&&e.x()<gridX+columns*CELL&&e.y()>=gridY&&e.y()<gridY+rows*CELL){int index=(scrollRow+(int)(e.y()-gridY)/CELL)*columns+(int)(e.x()-gridX)/CELL;if(index<filtered.size()){selected=filtered.get(index).copy();error="";refreshControls();return true;}}
        return false;
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(mx>=gridX&&mx<left+w-8&&my>=gridY&&my<gridBottom){scrollRow=Math.clamp(scrollRow-(int)Math.signum(vertical)*Math.max(1,(int)Math.ceil(Math.abs(vertical))),0,maxScroll());return true;}
        return super.mouseScrolled(mx,my,horizontal,vertical);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        refreshControls();g.fill(0,0,width,height,mod.config.glassPanels?0x70060B14:0xB5060B14);AuctionUi.panel(g,left,top,w,h,mod.config.glassPanels);
        AuctionUi.text(g,"CHOOSE ITEM IMAGE",left+12,top+11,AuctionUi.CYAN);AuctionUi.text(g,"All items and blocks - scroll to browse",left+12,top+27,AuctionUi.MUTED);
        g.fill(gridX-1,gridY-2,left+w-12,gridBottom,0x70101824);
        ItemStack hovered=ItemStack.EMPTY;
        g.enableScissor(gridX,gridY,left+w-16,gridY+rows*CELL);
        for(int row=0;row<rows;row++)for(int col=0;col<columns;col++){
            int index=(row+scrollRow)*columns+col;if(index>=filtered.size())continue;var stack=filtered.get(index);int x=gridX+col*CELL,y=gridY+row*CELL;boolean same=!selected.isEmpty()&&stack.is(selected.getItem()),hover=mx>=x&&mx<x+CELL&&my>=y&&my<y+CELL;
            if(same){AuctionUi.round(g,x+1,y+1,CELL-2,CELL-2,AuctionUi.CYAN);g.fill(x+2,y+2,x+CELL-2,y+CELL-2,0xFF203C4D);}else if(hover)g.fill(x+1,y+1,x+CELL-1,y+CELL-1,0xFF304152);
            g.renderItem(stack,x+4,y+4);if(hover)hovered=stack;
        }
        g.disableScissor();
        if(filtered.isEmpty())AuctionUi.text(g,"No matching items",gridX+5,gridY+8,AuctionUi.MUTED);
        if(maxScroll()>0){int track=rows*CELL,thumb=Math.max(12,track*rows/Math.max(rows,(filtered.size()+columns-1)/columns));int sy=gridY+(track-thumb)*scrollRow/maxScroll();g.fill(left+w-14,gridY,left+w-11,gridY+track,0xFF263648);g.fill(left+w-14,sy,left+w-11,sy+thumb,AuctionUi.CYAN);}
        if(!selected.isEmpty()){AuctionUi.text(g,"Minimum (1000 / 1k / 1m)",left+12,top+h-92,AuctionUi.MUTED);AuctionUi.text(g,"Time (seconds / mm:ss)",time.getX(),top+h-92,AuctionUi.MUTED);}
        String counter=filtered.size()+" items";AuctionUi.text(g,"Click an item to use its image",left+12,top+h-52,AuctionUi.MUTED);AuctionUi.text(g,counter,left+w-12-font.width(counter),top+h-52,AuctionUi.INK);
        String status=error.isEmpty()?mod.configError:error;if(!status.isEmpty())AuctionUi.text(g,font.plainSubstrByWidth(status,w-24),left+12,top+h-39,0xFFF09A9D);
        super.render(g,mx,my,delta);
        if(!hovered.isEmpty())g.setTooltipForNextFrame(font,hovered,mx,my);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
}
