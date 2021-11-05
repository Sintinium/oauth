package com.sintinium.oauth.gui.profile;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;

public class FakeWorld extends ClientWorld {

    private static FakeWorld instance;

    public static FakeWorld getInstance() {
        if (instance == null) {
            instance = new FakeWorld();
        }
        return instance;
    }

    public FakeWorld() {
        super(FakeClientPlayNetHandler.getInstance(), new ClientWorldInfo(Difficulty.EASY, false, true), null, FakeDimensionType.getInstance(), 0, () -> null, null, false, 0L);
    }
}
