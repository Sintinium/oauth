package me.jarva.oauth.gui.components;

#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif
import me.jarva.oauth.util.GuiUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextWidget extends Button {
    public TextWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, Component.literal(text), button -> {}, Button.DEFAULT_NARRATION);
    }

    private int fgColor = 0xFFFFFF;



    @Override
    #if POST_CURRENT_MC_1_20_1
    protected void renderWidget(GuiGraphics graphics, int i, int j, float f) {
    #else
    public void renderWidget(PoseStack graphics, int i, int j, float f) {
    #endif
        GuiUtils.drawShadow(graphics, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public int getFGColor() {
        return fgColor;
    }

    public void setFGColor(int fgColor) {
        this.fgColor = fgColor;
    }
}
