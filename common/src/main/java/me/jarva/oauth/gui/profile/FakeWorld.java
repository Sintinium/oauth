package me.jarva.oauth.gui.profile;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
#if POST_MC_1_19_2
import net.minecraft.core.HolderOwner;
#endif
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FakeWorld extends ClientLevel {

    private static FakeWorld instance;

    public static FakeWorld getInstance() {
        if (instance == null) {
            instance = new FakeWorld();
        }
        return instance;
    }

    public FakeWorld() {
        #if POST_MC_1_18_2
        super(FakeClientPlayNetHandler.getInstance(), new ClientLevelData(Difficulty.EASY, false, true), Level.OVERWORLD, new FakeHolder<>(FakeDimensionType.getInstance()), 0, 0, () -> null, new LevelRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().renderBuffers()), false, 0L);
        #else
        super(FakeClientPlayNetHandler.getInstance(), new ClientLevelData(Difficulty.EASY, false, true), Level.OVERWORLD, new Holder.Direct<>(FakeDimensionType.getInstance()), 0, 0, () -> null, new LevelRenderer(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers()), false, 0L);
        #endif
    }

    public static record FakeHolder<T>(T value) implements Holder<T> {
        public boolean isBound() {
            return true;
        }

        public boolean is(@NotNull ResourceLocation p_205727_) {
            return false;
        }

        public boolean is(@NotNull ResourceKey<T> p_205725_) {
            return false;
        }

        public boolean is(@NotNull TagKey<T> p_205719_) {
            return false;
        }

        public boolean is(@NotNull Predicate<ResourceKey<T>> p_205723_) {
            return false;
        }

        public @NotNull Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        public @NotNull Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of(new ResourceKey<T>(new ResourceLocation(""), new ResourceLocation("")));
        }

        public @NotNull Kind kind() {
            return Kind.DIRECT;
        }

        #if POST_MC_1_19_2
        @Override
        public boolean canSerializeIn(@NotNull HolderOwner<T> p_255833_) {
            return false;
        }
        #endif

        public String toString() {
            return "Direct{" + this.value + "}";
        }

        public boolean isValidInRegistry(Registry<T> p_205721_) {
            return true;
        }

        public @NotNull Stream<TagKey<T>> tags() {
            return Stream.of();
        }
    }
}
