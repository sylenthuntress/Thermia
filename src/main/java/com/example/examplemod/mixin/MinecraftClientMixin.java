package com.example.examplemod.mixin;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        
        ExampleMod.LOGGER.info("This line is printed by an example mod common mixin!");
        ExampleMod.LOGGER.info("MC Version: {}", MinecraftClient.getInstance().getVersionType());
    }
}