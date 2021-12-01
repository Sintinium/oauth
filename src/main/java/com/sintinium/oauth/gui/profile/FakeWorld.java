package com.sintinium.oauth.gui.profile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

public class FakeWorld extends ClientLevel {

    private static FakeWorld instance;

    public static FakeWorld getInstance() {
        if (instance == null) {
            instance = new FakeWorld();
        }
        return instance;
    }

    public FakeWorld() {
        super(FakeClientPlayNetHandler.getInstance(), new ClientLevelData(Difficulty.EASY, false, true), Level.OVERWORLD, FakeDimensionType.getInstance(), 0, 0, () -> null, new LevelRenderer(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers()), false, 0L);
    }
}
