package dev.donuttracker.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.donuttracker.fabric.CapeCosmetics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeGlowMixin {
    @Shadow @Final private HumanoidModel<AvatarRenderState> model;
    @Inject(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void donuttracker$glow(PoseStack pose,SubmitNodeCollector collector,int light,AvatarRenderState state,float a,float b,CallbackInfo ci){
        if(CapeCosmetics.glows(state.skin))collector.submitModel(model,state,pose,RenderTypes.eyes(CapeCosmetics.GLOW),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,state.outlineColor,null);
    }
}
