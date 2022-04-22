package com.sintinium.oauth.gui.profile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.sintinium.oauth.profile.IProfile;
import com.sintinium.oauth.profile.ProfileManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public class ProfileList extends GuiListExtended {
    private final List<ProfileEntry> entries = Lists.newArrayList();
    private ProfileEntry selected = null;
    private final ProfileSelectionScreen screen;

    public ProfileList(ProfileSelectionScreen screen, int width, int height, int top, int bottom, int lineHeight) {
        super(screen.mc, width, height, top, bottom, lineHeight);
        this.screen = screen;
    }

    public ProfileSelectionScreen getProfileSelectionScreen() {
        return screen;
    }

    public void setSelected(@Nullable ProfileEntry pEntry) {
    	selected = pEntry;
        if (pEntry != null) {
            pEntry.onSelected();
        }
    }

    public int getIndex(ProfileEntry pEntry) {
    	return entries.indexOf(pEntry);
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    /**
     * Returns true if the element passed in is currently selected
     */
    @Override
    protected boolean isSelected(int index) {
    	return entries.get(index) == selected;
    }
    
    public ProfileEntry getSelected() {
    	return selected;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    @Override
    public ProfileEntry getListEntry(int index) {
    	return entries.get(index);
    }

    public List<ProfileEntry> getEntryList() {
    	return Collections.unmodifiableList(entries);
    }

    public void remove(ProfileEntry entry) {
    	entries.remove(entry);
    }

    public void loadProfiles(UUID selected) {
        setSelected(null);
        entries.clear();

        for (IProfile profile : ProfileManager.getInstance().getProfiles()) {
            ProfileEntry entry = new ProfileEntry(this, profile);
            entries.add(entry);
            if (profile.getUUID().equals(selected)) {
                setSelected(entry);
            }
        }
    }

    public void loadProfiles() {
        loadProfiles(Minecraft.getMinecraft().getSession().func_148256_e().getId());
    }
}
