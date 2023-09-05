package me.jarva.oauth.gui;

import com.google.common.base.Splitter;
#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif
import me.jarva.oauth.gui.components.OAuthButton;
import me.jarva.oauth.login.MicrosoftLogin;
import me.jarva.oauth.util.ComponentUtils;
import me.jarva.oauth.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

public class ErrorScreen extends OAuthScreen {

    private String message = null;
    private Throwable e = null;
    private boolean isInfo = false;

    public ErrorScreen(boolean isMs, String message) {
        super(ComponentUtils.literal("Error logging into " + (isMs ? "Microsoft." : "Mojang.")));
        this.message = message;
        System.err.println(message);
    }

    public ErrorScreen(boolean isMs, Throwable e) {
        super(ComponentUtils.literal("Error logging into " + (isMs ? "Microsoft." : "Mojang.")));
        this.e = e;
        e.printStackTrace();
    }

    public void setInfo() {
        this.isInfo = true;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new OAuthButton(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, p_onPress_1_ -> {
            setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }));
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static ErrorScreen microsoftExceptionScreen(MicrosoftLogin.BaseMicrosoftLoginException e) {
        ErrorScreen screen = null;
        if (e instanceof MicrosoftLogin.NoXboxAccountException) {
            screen = new ErrorScreen(true, "This account has no Microsoft/Xbox account. Please login through minecraft.net to create one.");
        } else if (e instanceof MicrosoftLogin.BannedCountryException) {
            screen = new ErrorScreen(true, "This account is from a country where Xbox Live is not available/banned.");
        } else if (e instanceof MicrosoftLogin.UnderageAccountException) {
            screen = new ErrorScreen(true, "This account is under 18 and doesn't work with 3rd party logins.\nEither change your account's age or have an adult setup a family group.");
        } else if (e instanceof MicrosoftLogin.NoAccountFoundException) {
            screen = new ErrorScreen(true, "This account doesn't own Minecraft.\nIf you're a gamepass user make sure to login through the new launcher first.");
        } else {
            throw new IllegalStateException("Unknown MicrosoftLoginException: " + e.getClass().getName());
        }
        screen.setInfo();
        return screen;
    }

    private String getMessage() {
        String result = "";
        if (message != null) {
            result = message;
        } else if (e != null) {
            result = ExceptionUtils.getStackTrace(e);
        } else {
            return "Error getting error message.";
        }
        return result;
    }

    @Override
    #if POST_CURRENT_MC_1_20_1
    public void render(@NotNull GuiGraphics graphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
    #else
    public void render(@NotNull PoseStack graphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
    #endif
        Font font = Minecraft.getInstance().font;
        this.renderBackground(graphics);
        if (isInfo) {
            GuiUtils.drawCentered(graphics, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);

            Iterable<String> messages = Splitter.on("\n").split(getMessage());
            int index = 0;
            for (String m : messages) {
                GuiUtils.drawShadow(graphics, ComponentUtils.literal(m), this.width / 2 - font.width(m) / 2, (this.height / 2 - 24) + (index * 12), 0xFF4444);
                index++;
            }
        } else if (getMessage().toLowerCase().contains("no such host is known") || getMessage().toLowerCase().contains("connection reset")) {
            GuiUtils.drawCentered(graphics, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            GuiUtils.drawCentered(graphics, ComponentUtils.literal("The servers could be down or it could be an internet problem."), this.width / 2, this.height / 2 - 28, 0xFFFFFF);
            GuiUtils.drawCentered(graphics, ComponentUtils.literal("If you believe this is a bug please create an issue at"), this.width / 2, this.height / 2 - 12, 0xFFFFFF);
            GuiUtils.drawCentered(graphics, ComponentUtils.literal("https://github.com/Sintinium/oauth with your latest log file."), this.width / 2, this.height / 2, 0xFFFFFF);
        } else {
            Component github = ComponentUtils.literal("Please create an issue at https://github.com/Sintinium/oauth with your log file.")
                    .setStyle(Style.EMPTY.withUnderlined(true));
            GuiUtils.drawCentered(graphics, ComponentUtils.literal("An error occurred. This could be a bug."), this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            GuiUtils.drawCentered(graphics, github, this.width / 2, this.height / 2 - 28, 0xFFFFFF);
            float scale = .5f;
            GuiUtils.scale(graphics, scale);
            String msg = getMessage();
//            if (OAuth.getInstance().modContainer != null) {
//                msg = "OAuth Forge v" + OAuth.getInstance().modContainer.getModInfo().getVersion().toString() + ": " + msg;
//            }
            Iterable<String> messages = Splitter.fixedLength(Math.round(80 * (1f / scale))).limit(12).split(msg);
            int index = 0;
            for (String m : messages) {
                GuiUtils.drawShadow(graphics, ComponentUtils.literal(m), (int) (this.width / 2 - font.width(m) / 2 * scale), (int) ((this.height / 2 - 16) * (1 / scale) + (index * 12)), 0xFF4444);
                index++;
            }
            GuiUtils.scale(graphics, 1f / scale);
        }

        super.render(graphics, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
