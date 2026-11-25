package com.clickablelinks.mixin;

import com.clickablelinks.LinkifyUtil;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    /**
     * Every chat/system message goes through this method (addMessage(Text) calls it too),
     * so we turn the links into clickable text right before it is stored.
     */
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text clickablelinks$linkify(Text message) {
        return LinkifyUtil.linkify(message);
    }
}
