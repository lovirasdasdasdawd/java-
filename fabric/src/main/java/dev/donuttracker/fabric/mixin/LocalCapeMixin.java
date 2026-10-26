package dev.donuttracker.fabric.mixin;

import dev.donuttracker.fabric.CapeCosmetics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class LocalCapeMixin {
    @Inject(method="getSkin",at=@At("RETURN"),cancellable=true)
    private void donuttracker$cape(CallbackInfoReturnable<PlayerSkin> cir){if((Object)this==Minecraft.getInstance().player)cir.setReturnValue(CapeCosmetics.skin(cir.getReturnValue()));}
}
