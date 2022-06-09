package com.sintinium.oauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sintinium.oauth.gui.profile.ProfileSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class LoginTypeScreen extends OAuthScreen {

    private final Runnable onMojang;
    private final Runnable onMicrosoft;

    public LoginTypeScreen(Runnable onMojang, Runnable onMicrosoft) {
        super(Component.literal("Select Account Type"));
        this.onMojang = onMojang;
        this.onMicrosoft = onMicrosoft;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 - 20 - 2, 200, 20, Component.literal("Mojang Login"), p_onPress_1_ -> {
            this.onMojang.run();
        }));
        final List<Component> msTooltip = new ArrayList<>();
        msTooltip.add(Component.literal("Will open your browser to login to Microsoft."));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 2, 200 /*- 52*/, 20, Component.literal("Microsoft Login"), (p_213031_1_) -> {
//            final MicrosoftLogin login = new MicrosoftLogin();
//            LoginLoadingScreen loadingScreen = new LoginLoadingScreen(new ProfileSelectionScreen(), this, login::cancelLogin, true);
//            login.setUpdateStatusConsumer(loadingScreen::updateText);
//            Thread thread = new Thread(() -> {
//                login.login(() -> {
//                    LoginUtil.updateOnlineStatus();
//                    OAuth.getInstance().setScreen(new ProfileSelectionScreen());
//                });
//            }, "Oauth microsoft");
//            if (login.getErrorMsg() != null) {
//                System.err.println(login.getErrorMsg());
//            }
//            OAuth.getInstance().setScreen(loadingScreen);
//            thread.setDaemon(true);
//            thread.start();
            this.onMicrosoft.run();
        }, (button, matrix, x, y) -> {
            renderComponentTooltip(matrix, msTooltip, x, y, Minecraft.getInstance().font);
        }));

//        final List<ITextProperties> logoutTooltip = new ArrayList<>();
//        this.addButton(new Button(this.width / 2 - 100 + 200 - 50, this.height / 2 + 2, 50, 20, new StringTextComponent("Logout"), p_onPress_1_ -> {
//            MicrosoftLogin.logout();
//        }, (button, matrix, x, y) -> {
//            renderWrappedToolTip(matrix,
//                    logoutTooltip,
//                    x, y, this.font
//            );
//        }));

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, (p_213029_1_) -> {
            setScreen(new ProfileSelectionScreen());
        }));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(p_230430_1_);
        drawCenteredString(p_230430_1_, this.font, this.title, this.width / 2, this.height / 2 - 60, 16777215);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
