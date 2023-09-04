package me.jarva.oauth.gui.profile;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.OptionalLong;

public class FakeDimensionType {

    public static DimensionType getInstance() {
        var monsterSettings = new DimensionType.MonsterSettings(false, false, ConstantInt.ZERO, 0);
        return new DimensionType(OptionalLong.empty(), true, false, false, false, 1.0, false, false, 0, 256, 256, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS, 1f, monsterSettings);
    }

}
