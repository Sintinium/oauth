package me.jarva.oauth.gui.profile;

import me.jarva.oauth.profile.IProfile;
import me.jarva.oauth.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

import javax.annotation.Nullable;
import java.util.UUID;

public class ProfileList extends ObjectSelectionList<ProfileEntry> {
    private final ProfileSelectionScreen profileSelectionScreen;

    public ProfileList(ProfileSelectionScreen screen, Minecraft minecraft, int width, int height, int topPadding, int bottomPadding, int lineHeight) {
        super(minecraft, width, height, topPadding, bottomPadding, lineHeight);
        this.profileSelectionScreen = screen;
    }

    public ProfileSelectionScreen getProfileSelectionScreen() {
        return profileSelectionScreen;
    }

    public void loadProfiles(UUID selected) {
        setSelected(null);
        children().clear();

        for (IProfile profile : ProfileManager.getInstance().getProfiles()) {
            ProfileEntry entry = new ProfileEntry(this, profile);
            children().add(entry);
            if (profile.getUUID().equals(selected)) {
                setSelected(entry);
            }
        }
    }

    public void loadProfiles() {
        loadProfiles(Minecraft.getInstance().getUser().getGameProfile().getId());
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
