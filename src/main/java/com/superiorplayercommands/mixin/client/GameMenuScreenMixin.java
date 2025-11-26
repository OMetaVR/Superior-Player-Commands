package com.superiorplayercommands.mixin.client;

import com.superiorplayercommands.client.gui.ModSettingsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    private void addSPCButton(CallbackInfo ci) {
        // Add a small button in the corner for SPC settings
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§bSPC"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new ModSettingsScreen(this));
                }
            }
        ).dimensions(this.width - 62, 4, 58, 20).build());
    }
}
