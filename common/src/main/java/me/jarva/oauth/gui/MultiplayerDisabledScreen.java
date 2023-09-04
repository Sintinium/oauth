package me.jarva.oauth.gui;

import com.google.common.base.Splitter;
#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif
import me.jarva.oauth.events.JoinMultiplayerScreenHandler;
import me.jarva.oauth.gui.components.OAuthButton;
import me.jarva.oauth.util.AgnosticUtils;
import me.jarva.oauth.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

public class MultiplayerDisabledScreen extends OAuthScreen {
    public MultiplayerDisabledScreen() {
        super(Component.literal("multiplayer_disabled"));
    }

    @Override
    protected void init() {
        JoinMultiplayerScreenHandler.warned = true;
        this.addRenderableWidget(new OAuthButton(this.width / 2 - 100, this.height / 2 + 38, 200, 20, Component.literal("Open Privacy Settings"), button -> {
            AgnosticUtils.openUri("https://account.xbox.com/en-us/Settings");
        }));
        this.addRenderableWidget(new OAuthButton(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, button -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }));
    }

    @Override
    #if POST_CURRENT_MC_1_20_1
    public void render(@NotNull GuiGraphics graphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
    #else
    public void render(@NotNull PoseStack graphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
    #endif
        Font font = Minecraft.getInstance().font;
        this.renderBackground(graphics);

        GuiUtils.drawCentered(graphics, Component.literal("Multiplayer Disabled").setStyle(Style.EMPTY.withBold(true)), this.width / 2, this.height / 2 - 40, 0xFFFFFF);

        String message = "This account is not allowed to play multiplayer.\nTo enable multiplayer go to your account settings and enable it.\nIf this error persists try deleting the profile.";
        Iterable<String> messages = Splitter.on('\n').split(message);
        int index = 0;
        for (String m : messages) {
            GuiUtils.drawShadow(graphics, Component.literal(m), this.width / 2 - font.width(m) / 2, (this.height / 2 - 24) + (index * 12), 0xFF4444);
            index++;
        }
    }
}
