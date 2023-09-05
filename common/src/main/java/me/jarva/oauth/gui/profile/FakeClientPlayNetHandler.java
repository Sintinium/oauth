package me.jarva.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
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
        #if POST_MC_1_19_2
        super(Minecraft.getInstance(), null, new Connection(PacketFlow.CLIENTBOUND), null, null, null);
        #else
        super(Minecraft.getInstance(), null, new Connection(PacketFlow.CLIENTBOUND), null, null);
        #endif
    }

    @Override
    public RegistryAccess registryAccess() {
        return new FakeRegistry();
    }

    @Override
    public GameProfile getLocalGameProfile() {
        return new GameProfile(UUID.randomUUID(), "null");
    }

    private class FakeRegistry implements RegistryAccess {
        #if PRE_CURRENT_MC_1_19_2
        @Override
        public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.empty();
        }
        #endif
        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> p_123085_) {
            return Optional.empty();
        }

        #if POST_MC_1_19_2
        @Override
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256275_) {
            return Optional.empty();
        }
        #endif

        @Override
        public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> p_175516_) {
            #if POST_MC_1_19_2
            return new FakeRegistoryObj<>();
            #else
            return new FakeRegistoryObj<>((ResourceKey<? extends Registry<E>>) p_175516_);
            #endif
        }

        #if PRE_CURRENT_MC_1_19_2
        @Override
        public Stream<RegistryEntry<?>> ownedRegistries() {
            return null;
        }
        #endif

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return null;
        }

        @Override
        public Frozen freeze() {
            return null;
        }

        #if POST_MC_1_19_2
        @Override
        public Lifecycle allRegistriesLifecycle() {
            return null;
        }
        #endif
    }

    #if POST_MC_1_19_2
    private class FakeRegistoryObj<T> implements Registry<T> {
    #else
    private class FakeRegistoryObj<T> extends Registry<T> {
        protected FakeRegistoryObj(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
            super(resourceKey, lifecycle);
        }

        protected FakeRegistoryObj(ResourceKey<? extends Registry<T>> resourceKey) {
            super(resourceKey, Lifecycle.stable());
        }
    #endif


        @Override
        public Holder.Reference<T> getHolderOrThrow(ResourceKey<T> p_249087_) {
            return null;
        }

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

        #if PRE_CURRENT_MC_1_19_2
        @Override
        public Holder<T> getOrCreateHolderOrThrow(ResourceKey<T> resourceKey) {
            return null;
        }

        @Override
        public DataResult<Holder<T>> getOrCreateHolder(ResourceKey<T> resourceKey) {
            return null;
        }

        @Override
        public boolean isKnownTagName(TagKey<T> tagKey) {
            return false;
        }
        #endif

        @Override
        public Holder.Reference<T> createIntrusiveHolder(T p_206068_) {
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

        #if POST_MC_1_19_2
        @Override
        public Optional<Holder.Reference<T>> getRandom(RandomSource p_235781_) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder.Reference<T>> getHolder(int p_206051_) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> p_206050_) {
            return Optional.empty();
        }

        @Override
        public Lifecycle registryLifecycle() {
            return null;
        }

        @Override
        public Holder<T> wrapAsHolder(T p_263382_) {
            return null;
        }

        @Override
        public HolderOwner<T> holderOwner() {
            return null;
        }

        @Override
        public HolderLookup.RegistryLookup<T> asLookup() {
            return null;
        }

        #else

        @Override
        public Optional<Holder<T>> getRandom(RandomSource randomSource) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder<T>> getHolder(int i) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder<T>> getHolder(ResourceKey<T> resourceKey) {
            return Optional.empty();
        }

        @Override
        public Lifecycle elementsLifecycle() {
            return null;
        }

        #endif

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return null;
        }
    }
}
