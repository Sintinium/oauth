package me.jarva.oauth.util;

#if POST_CURRENT_MC_1_20_1
import net.minecraft.client.gui.GuiGraphics;
#else
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
#endif
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiUtils {
    #if POST_CURRENT_MC_1_20_1
    public static void drawShadow(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void drawCentered(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawCenteredString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void scale(GuiGraphics graphics, float scale) {
        graphics.pose().scale(scale, scale, scale);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation location, int x, int y, float textureX, float textureY, int width, int height, int dX, int dY) {
        graphics.blit(location, x, y, textureX, textureY, width, height, dX, dY);
    }

    #else

    public static void drawShadow(PoseStack graphics, Component text, int x, int y, int color) {
        Minecraft.getInstance().font.draw(graphics, text, x, y, color);
    }

    public static void drawCentered(PoseStack graphics, Component text, int x, int y, int color) {
        Font font = Minecraft.getInstance().font;
        font.draw(graphics, text, x - (font.width(text) / 2) , y - (font.lineHeight / 2),color);
    }

    public static void scale(PoseStack graphics, float scale) {
        graphics.scale(scale, scale, scale);
    }

    public static void blit(PoseStack graphics, ResourceLocation location, int x, int y, float textureX, float textureY, int width, int height, int dX, int dY) {
        Screen.blit(graphics, x, y, textureX, textureY, width, height, dX, dY);
    }
    #endif
}
