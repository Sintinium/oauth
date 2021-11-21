package com.sintinium.oauth.gui.profile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;

import javax.annotation.Nullable;

public class ProfileList extends ExtendedList<ProfileEntry> {
    private Screen screen;
    public ProfileList(Screen screen, Minecraft minecraft, int width, int height, int topPadding, int bottomPadding, int lineHeight) {
        super(minecraft, width, height, topPadding, bottomPadding, lineHeight);
    }

    @Override
    public void setSelected(@Nullable ProfileEntry pEntry) {
        super.setSelected(pEntry);
        if (pEntry != null) {
            pEntry.onSelected();
        }
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }
}
