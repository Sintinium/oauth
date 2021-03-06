package com.sintinium.oauth.gui;

import com.google.common.base.Splitter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sintinium.oauth.GuiEventHandler;
import com.sintinium.oauth.util.AgnosticUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class MultiplayerDisabledScreen extends OAuthScreen {
    public MultiplayerDisabledScreen() {
        super(new TextComponent("multiplayer_disabled"));
    }

    @Override
    protected void init() {
        GuiEventHandler.warned = true;
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 38, 200, 20, new TextComponent("Open Privacy Settings"), p_onPress_1_ -> {
            AgnosticUtils.openUri("https://account.xbox.com/en-us/Settings");
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, p_onPress_1_ -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }));
    }

    @Override
    public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        Font font = Minecraft.getInstance().font;
        this.renderBackground(p_230430_1_);
        drawCenteredString(p_230430_1_, Minecraft.getInstance().font, new TextComponent("Multiplayer Disabled").setStyle(Style.EMPTY.withBold(true)), this.width / 2, this.height / 2 - 40, 0xFFFFFF);

        String message = "This account is not allowed to play multiplayer.\nTo enabled multiplayer go to your account settings and enable it.\nIf this error persists try deleting the profile.";
        Iterable<String> messages = Splitter.on("\n").split(message);
        int index = 0;
        for (String m : messages) {
            font.drawShadow(p_230430_1_, m, this.width / 2f - font.width(m) / 2f, (this.height / 2f - 24f) + (index * 12f), 0xFF4444);
            index++;
        }

        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
