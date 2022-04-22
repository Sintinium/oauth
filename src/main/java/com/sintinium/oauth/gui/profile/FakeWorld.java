package com.sintinium.oauth.gui.profile;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.SaveHandlerMP;

public class FakeWorld extends World {
    private static FakeWorld instance;

    public static FakeWorld getInstance() {
        if (instance == null) {
            instance = new FakeWorld();
        }
        return instance;
    }

    public FakeWorld() {
        super(new SaveHandlerMP(), "FakeWorldServer", new WorldProviderSurface(), new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT), new Profiler());
        this.difficultySetting = EnumDifficulty.PEACEFUL;
        this.mapStorage = new MapStorage((ISaveHandler)null);
        this.isRemote = true;
        this.finishSetup();
        this.setSpawnLocation(8, 64, 8);
    }

	@Override
	protected IChunkProvider createChunkProvider() {
		return new FakeChunkProvider(this);
	}

	@Override
	protected int func_152379_p() {
        return Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
	}

	@Override
	public Entity getEntityByID(int p_73045_1_) {
        return null;
	}
	
	private static class FakeChunkProvider implements IChunkProvider {
		private final World worldObj;
		
		public FakeChunkProvider(World world) {
			worldObj = world;
		}

		@Override
		public boolean canSave() {
			return false;
		}

		@Override
		public boolean chunkExists(int p_73149_1_, int p_73149_2_) {
	        return true;
		}

		@Override
		public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_,
				int p_147416_5_) {
	        if ("Stronghold".equals(p_147416_2_))
	            return new ChunkPosition(0, 0, 0);
			return null;
		}

		@Override
		public int getLoadedChunkCount() {
			return 0;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_) {
			return Lists.newArrayList();
		}

		@Override
		public Chunk loadChunk(int p_73158_1_, int p_73158_2_) {
	        return this.provideChunk(p_73158_1_, p_73158_2_);
		}

		@Override
		public String makeString() {
	        return "FakeLevelSource";
		}

		@Override
		public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
		}

		@Override
		public Chunk provideChunk(int p_73154_1_, int p_73154_2_) {
	        Chunk chunk = new Chunk(this.worldObj, p_73154_1_, p_73154_2_);
	        chunk.generateSkylightMap();
	        byte[] abyte = chunk.getBiomeArray();
	        Arrays.fill(abyte, (byte)0);
			return chunk;
		}

		@Override
		public void recreateStructures(int p_82695_1_, int p_82695_2_) {
		}

		@Override
		public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
			return true;
		}

		@Override
		public void saveExtraData() {
		}

		@Override
		public boolean unloadQueuedChunks() {
			return false;
		}
		
	}
}
