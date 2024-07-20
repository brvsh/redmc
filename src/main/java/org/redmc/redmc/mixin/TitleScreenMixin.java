package org.redmc.redmc.mixin;

import org.redmc.redmc.RedMCClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "initWidgetsNormal(II)V")
    private void onInitWidgetsNormal(int Y, int spacingY, CallbackInfo ci) {
        Text message = Text.of("redmc v" + RedMCClient.getModVersion("redmc"));
        this.addDrawableChild(new TextWidget(20, 10, textRenderer.getWidth(message), textRenderer.fontHeight, message, textRenderer));
    }
}