package me.jarva.oauth.gui.profile;

import net.minecraft.tags.BlockTags;
#if POST_MC_1_18_2
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
#endif
import net.minecraft.world.level.dimension.DimensionType;

import java.util.OptionalLong;

public class FakeDimensionType {

    public static DimensionType getInstance() {
        #if POST_MC_1_18_2
        var monsterSettings = new DimensionType.MonsterSettings(false, false, ConstantInt.ZERO, 0);
        return new DimensionType(OptionalLong.empty(), true, false, false, false, 1.0, false, false, 0, 256, 256, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS, 1f, monsterSettings);
        #else
        return DimensionType.create(OptionalLong.empty(), true, false, false, false, 1.0, false, false, false, false, false, 0, 16, 0, BlockTags.INFINIBURN_OVERWORLD, DimensionType.OVERWORLD_EFFECTS, 1f);
        #endif
    }

}
