package com.sintinium.oauth.gui.profile;

import net.minecraft.world.DimensionType;

import java.util.OptionalLong;

public class FakeDimensionType extends DimensionType {

    private static DimensionType instance;

    public static DimensionType getInstance() {
        if (instance == null) {
            instance = new FakeDimensionType();
        }
        return instance;
    }

    private FakeDimensionType() {
        super(OptionalLong.empty(), true, false, false, false, 1.0, false, false, false, false, 0, DimensionType.OVERWORLD_LOCATION.location(), DimensionType.OVERWORLD_EFFECTS, 1f);
    }

}
