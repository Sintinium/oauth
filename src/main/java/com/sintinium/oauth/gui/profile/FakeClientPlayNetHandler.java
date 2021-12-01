package com.sintinium.oauth.gui.profile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

public class FakeClientPlayNetHandler extends ClientPacketListener {

    private static FakeClientPlayNetHandler instance;

    public static FakeClientPlayNetHandler getInstance() {
        if (instance == null) {
            instance = new FakeClientPlayNetHandler();
        }
        return instance;
    }

    public FakeClientPlayNetHandler() {
        super(Minecraft.getInstance(), null, new Connection(PacketFlow.CLIENTBOUND), Minecraft.getInstance().getUser().getGameProfile(), null);
    }


}
