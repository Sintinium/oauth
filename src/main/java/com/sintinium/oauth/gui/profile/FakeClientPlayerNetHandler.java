package com.sintinium.oauth.gui.profile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S40PacketDisconnect;

public class FakeClientPlayerNetHandler extends NetHandlerPlayClient {
    private static FakeClientPlayerNetHandler instance;

    public static FakeClientPlayerNetHandler getInstance() {
        if (instance == null) {
            instance = new FakeClientPlayerNetHandler();
        }
        return instance;
    }

    public FakeClientPlayerNetHandler() {
        super(Minecraft.getMinecraft(), null, null);
    }
    
    @Override
    public void handleUpdateTileEntity(S35PacketUpdateTileEntity p_147273_1_) {}
    
    @Override
    public void addToSendQueue(Packet p_147297_1_) {}
    
    @Override
    public void handleDisconnect(S40PacketDisconnect p_147253_1_) {}
    
    @Override
    public void handlePlayerPosLook(S08PacketPlayerPosLook p_147258_1_) {}
    
    @Override
    public void handleJoinGame(S01PacketJoinGame p_147282_1_) {}
}
