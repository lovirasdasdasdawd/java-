package dev.donuttracker.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import dev.donuttracker.core.CapePattern;
import dev.donuttracker.core.CapeStudio;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/** Local-only skin patch. Leaves original body, model and elytra completely intact. */
public final class CapeCosmetics {
    public static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("donuttracker","cape/local"),GLOW=Identifier.fromNamespaceAndPath("donuttracker","cape/glow");
    public static final Identifier PREVIEW=Identifier.fromNamespaceAndPath("donuttracker","cape/preview");
    static CapeConfig config;
    private static DynamicTexture cloth,glow,preview;
    private static int[] wolf;
    static void initialize(){config=CapeConfig.load();}
    static void refresh(){
        if(cloth==null){cloth=texture(TEXTURE,"Cape cloth");glow=texture(GLOW,"Cape glow");preview=texture(PREVIEW,"Cape preview");}
        if(wolf==null){
            try(var input=CapeCosmetics.class.getResourceAsStream("/assets/donuttracker/textures/capes/night_wolf.png");var original=NativeImage.read(input);var small=new NativeImage(144,144,true)){
                original.resizeSubRectTo(0,0,original.getWidth(),original.getHeight(),small);wolf=small.getPixels();
            }catch(java.io.IOException ex){throw new IllegalStateException("Bundled wolf cape artwork could not be loaded",ex);}
        }
        var images=CapeStudio.create(config.design,config.cloth,config.frosting,config.accent,config.glow,config.aura,config.glowing,wolf,144,144,config.shape());
        upload(cloth,images.cloth());upload(glow,images.glow());upload(preview,images.preview());
    }
    private static DynamicTexture texture(Identifier id,String name){var texture=new DynamicTexture(()->name,new NativeImage(CapeStudio.WIDTH,CapeStudio.HEIGHT,true));Minecraft.getInstance().getTextureManager().register(id,texture);return texture;}
    private static void upload(DynamicTexture texture,int[] pixels){var image=texture.getPixels();for(int y=0;y<CapeStudio.HEIGHT;y++)for(int x=0;x<CapeStudio.WIDTH;x++)image.setPixel(x,y,pixels[y*CapeStudio.WIDTH+x]);texture.upload();}
    static void ensure(){if(cloth==null)refresh();}
    public static PlayerSkin skin(PlayerSkin original){
        if(config==null||!config.enabled)return original;
        ensure();return new PlayerSkin(original.body(),new ClientAsset.ResourceTexture(TEXTURE,TEXTURE),original.elytra(),original.model(),original.secure());
    }
    public static boolean glows(PlayerSkin skin){return config!=null&&config.enabled&&config.glowing&&config.glow>0&&skin!=null&&skin.cape()!=null&&TEXTURE.equals(skin.cape().texturePath());}
}
