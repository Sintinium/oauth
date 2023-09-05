package me.jarva.oauth.gui.components;

#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
#endif
import me.jarva.oauth.util.ComponentUtils;
import me.jarva.oauth.util.GuiUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextWidget extends Button {
    public TextWidget(int x, int y, int width, int height, String text) {
        #if POST_MC_1_19_2
        super(x, y, width, height, ComponentUtils.literal(text), button -> {}, Button.DEFAULT_NARRATION);
        #else
        super(x, y, width, height, ComponentUtils.literal(text), button -> {});
        #endif
    }

    private int fgColor = 0xFFFFFF;


    #if POST_MC_1_19_2
    @Override
    #if POST_CURRENT_MC_1_20_1
    protected void renderWidget(GuiGraphics graphics, int i, int j, float f) {
    #else
    public void renderWidget(PoseStack graphics, int i, int j, float f) {
    #endif
        GuiUtils.drawShadow(graphics, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    #else

    @Override
    public void renderButton(PoseStack graphics, int i, int j, float f) {
        GuiUtils.drawShadow(graphics, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
    }
    #endif



    public int getFGColor() {
        return fgColor;
    }

    public void setFGColor(int fgColor) {
        this.fgColor = fgColor;
    }
}
