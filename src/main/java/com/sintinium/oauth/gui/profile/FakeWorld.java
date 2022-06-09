package com.sintinium.oauth.gui.profile;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

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
//        var registry = new MappedRegistry<DimensionType>(ResourceKey.createRegistryKey(new ResourceLocation("OAuth")), Lifecycle.experimental(), null);
        super(FakeClientPlayNetHandler.getInstance(), new ClientLevelData(Difficulty.EASY, false, true), Level.OVERWORLD, new FakeHolder<>(FakeDimensionType.getInstance()), 0, 0, () -> null, new LevelRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().renderBuffers()), false, 0L);
    }

    public static record FakeHolder<T>(T value) implements Holder<T> {
        public boolean isBound() {
            return true;
        }

        public boolean is(ResourceLocation p_205727_) {
            return false;
        }

        public boolean is(ResourceKey<T> p_205725_) {
            return false;
        }

        public boolean is(TagKey<T> p_205719_) {
            return false;
        }

        public boolean is(Predicate<ResourceKey<T>> p_205723_) {
            return false;
        }

        public Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        public Optional<ResourceKey<T>> unwrapKey() {
            var constructor = ObfuscationReflectionHelper.findConstructor(ResourceKey.class, ResourceLocation.class, ResourceLocation.class);
            constructor.setAccessible(true);
            try {
                return Optional.of(constructor.newInstance(new ResourceLocation(""), new ResourceLocation("")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

        public Holder.Kind kind() {
            return Holder.Kind.DIRECT;
        }

        public String toString() {
            return "Direct{" + this.value + "}";
        }

        public boolean isValidInRegistry(Registry<T> p_205721_) {
            return true;
        }

        public Stream<TagKey<T>> tags() {
            return Stream.of();
        }
    }
}
