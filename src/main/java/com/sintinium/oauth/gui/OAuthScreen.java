package com.sintinium.oauth.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class OAuthScreen extends Screen {
    public List<Runnable> toRun = new ArrayList<>();

    protected OAuthScreen(Component p_96550_) {
        super(p_96550_);
    }

    @Override
    public void tick() {
        for (Runnable runnable : toRun) {
            runnable.run();
        }
        toRun.clear();
    }
}
