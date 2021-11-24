package com.sintinium.oauth.gui.profile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class FakeWorld extends ClientWorld {

    private static FakeWorld instance;

    public static FakeWorld getInstance() {
        if (instance == null) {
            instance = new FakeWorld();
        }
        return instance;
    }

    public FakeWorld() {
        super(FakeClientPlayNetHandler.getInstance(), new ClientWorldInfo(Difficulty.EASY, false, true), World.OVERWORLD, FakeDimensionType.getInstance(), 0, () -> null, new WorldRenderer(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers()), false, 0L);
    }
}
