package me.jarva.oauth.gui;



import me.jarva.oauth.gui.components.OAuthButton;
import me.jarva.oauth.gui.profile.ProfileSelectionScreen;
import me.jarva.oauth.util.GuiUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif

public class LoginLoadingScreen extends OAuthScreen {

    private final String loadingText = "Loading";
    private int dots = 0;
    private String renderText = loadingText;

    private int tick = 0;
    private final Runnable onCancel;
    private final boolean isMicrosoft;
    private final AtomicReference<String> updateText = new AtomicReference<>();

    public LoginLoadingScreen(Runnable onCancel, boolean isMicrosoft) {
        super(Component.literal("Logging in"));
        this.onCancel = onCancel;
        this.isMicrosoft = isMicrosoft;

        if (this.isMicrosoft) {
            updateText.set("Check your browser");
        } else {
            updateText.set("Authorizing you with Mojang");
        }
    }

    public void updateText(String text) {
        updateText.set(text);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new OAuthButton(this.width / 2 - 100, this.height / 2 + 60, 200, 20, CommonComponents.GUI_CANCEL, (p_213029_1_) -> {
            onCancel.run();
            setScreen(new ProfileSelectionScreen());
        }));
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
        if (tick % 20 != 0) return;
        dots++;
        if (dots > 3) {
            dots = 0;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(loadingText);
        for (int i = 0; i < dots; i++) {
            builder.append(".");
        }
        renderText = builder.toString();
    }

    @Override
    #if POST_CURRENT_MC_1_20_1
    public void render(@NotNull GuiGraphics graphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
    #else
    public void render(@NotNull PoseStack graphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
    #endif
        this.renderBackground(graphics);
        GuiUtils.drawCentered(graphics, Component.literal(renderText), this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        if (this.isMicrosoft) {
            GuiUtils.drawCentered(graphics, Component.literal(updateText.get()), this.width / 2, this.height / 2 - 28, 0xFFFFFF);
        }
        super.render(graphics, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
