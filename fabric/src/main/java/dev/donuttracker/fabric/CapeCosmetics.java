package dev.donuttracker.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import dev.donuttracker.core.CapePattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/** Local-only skin patch. Leaves original body, model and elytra completely intact. */
public final class CapeCosmetics {
    public static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("donuttracker","cape/local"),GLOW=Identifier.fromNamespaceAndPath("donuttracker","cape/glow");
    static CapeConfig config;
    private static DynamicTexture cloth,glow;
    static void initialize(){config=CapeConfig.load();}
    static void refresh(){
        if(cloth==null){cloth=new DynamicTexture(()->"Donut cape",new NativeImage(CapePattern.WIDTH,CapePattern.HEIGHT,true));glow=new DynamicTexture(()->"Donut cape glow",new NativeImage(CapePattern.WIDTH,CapePattern.HEIGHT,true));Minecraft.getInstance().getTextureManager().register(TEXTURE,cloth);Minecraft.getInstance().getTextureManager().register(GLOW,glow);}
        upload(cloth,false);upload(glow,true);
    }
    private static void upload(DynamicTexture texture,boolean emissive){var pixels=CapePattern.pixels(config.design,config.cloth,config.frosting,config.accent,config.glow,emissive);var image=texture.getPixels();for(int y=0;y<CapePattern.HEIGHT;y++)for(int x=0;x<CapePattern.WIDTH;x++)image.setPixel(x,y,pixels[y*CapePattern.WIDTH+x]);texture.upload();}
    static void ensure(){if(cloth==null)refresh();}
    public static PlayerSkin skin(PlayerSkin original){
        if(config==null||!config.enabled)return original;
        ensure();return new PlayerSkin(original.body(),new ClientAsset.ResourceTexture(TEXTURE,TEXTURE),original.elytra(),original.model(),original.secure());
    }
    public static boolean glows(PlayerSkin skin){return config!=null&&config.enabled&&config.glowing&&config.glow>0&&skin!=null&&skin.cape()!=null&&TEXTURE.equals(skin.cape().texturePath());}
}
