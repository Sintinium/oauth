package com.sintinium.oauth.gui.profile;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class FakeClientPlayNetHandler extends ClientPacketListener {

    private static FakeClientPlayNetHandler instance;

    public static FakeClientPlayNetHandler getInstance() {
        if (instance == null) {
            instance = new FakeClientPlayNetHandler();
        }
        return instance;
    }

    public FakeClientPlayNetHandler() {
        super(Minecraft.getInstance(), null, new Connection(PacketFlow.CLIENTBOUND), null, Minecraft.getInstance().getUser().getGameProfile(), null);
    }

    @Override
    public RegistryAccess registryAccess() {
        return new FakeRegistryAccess();
    }

    public class FakeRegistryAccess implements RegistryAccess.Frozen {

        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> p_123085_) {
            return Optional.empty();
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return null;
        }

        @Override
        public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> p_175516_) {
            return new FakeRegistry<>();
        }
    }

    public class FakeRegistry<T> implements Registry<T> {

        @Override
        public ResourceKey<? extends Registry<T>> key() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getKey(T p_123006_) {
            return null;
        }

        @Override
        public Optional<ResourceKey<T>> getResourceKey(T p_123008_) {
            return Optional.empty();
        }

        @Override
        public int getId(@Nullable T p_122977_) {
            return 0;
        }

        @Nullable
        @Override
        public T byId(int p_122651_) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Nullable
        @Override
        public T get(@Nullable ResourceKey<T> p_122980_) {
            return null;
        }

        @Nullable
        @Override
        public T get(@Nullable ResourceLocation p_123002_) {
            return null;
        }

        @Override
        public Lifecycle lifecycle(T p_123012_) {
            return null;
        }

        @Override
        public Lifecycle registryLifecycle() {
            return null;
        }

        @Override
        public Set<ResourceLocation> keySet() {
            return null;
        }

        @Override
        public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
            return null;
        }

        @Override
        public Set<ResourceKey<T>> registryKeySet() {
            return null;
        }

        @Override
        public Optional<Holder.Reference<T>> getRandom(RandomSource p_235781_) {
            return Optional.empty();
        }

        @Override
        public boolean containsKey(ResourceLocation p_123011_) {
            return false;
        }

        @Override
        public boolean containsKey(ResourceKey<T> p_175475_) {
            return false;
        }

        @Override
        public Registry<T> freeze() {
            return null;
        }

        @Override
        public Holder.Reference<T> createIntrusiveHolder(T p_206068_) {
            return null;
        }

        @Override
        public Optional<Holder.Reference<T>> getHolder(int p_206051_) {
            return Optional.empty();
        }

        @Override
        public Holder.Reference<T> getHolderOrThrow(ResourceKey<T> p_249087_) {
            return Holder.Reference.createStandAlone(null, null);
        }

        @Override
        public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> p_206050_) {
            return Optional.empty();
        }

        @Override
        public Holder<T> wrapAsHolder(T p_263382_) {
            return null;
        }

        @Override
        public Stream<Holder.Reference<T>> holders() {
            return null;
        }

        @Override
        public Optional<HolderSet.Named<T>> getTag(TagKey<T> p_206052_) {
            return Optional.empty();
        }

        @Override
        public HolderSet.Named<T> getOrCreateTag(TagKey<T> p_206045_) {
            return null;
        }

        @Override
        public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
            return null;
        }

        @Override
        public Stream<TagKey<T>> getTagNames() {
            return null;
        }

        @Override
        public void resetTags() {

        }

        @Override
        public void bindTags(Map<TagKey<T>, List<Holder<T>>> p_205997_) {

        }

        @Override
        public HolderOwner<T> holderOwner() {
            return null;
        }

        @Override
        public HolderLookup.RegistryLookup<T> asLookup() {
            return null;
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return null;
        }
    }
}
