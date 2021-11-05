package com.sintinium.oauth.gui.profile;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;

import javax.annotation.Nullable;
import java.util.UUID;

public class FakeClientPlayNetHandler extends ClientPlayNetHandler {

    private static FakeClientPlayNetHandler instance;

    public static FakeClientPlayNetHandler getInstance() {
        if (instance == null) {
            instance = new FakeClientPlayNetHandler();
        }
        return instance;
    }

    public FakeClientPlayNetHandler() {
        super(Minecraft.getInstance(), null, new NetworkManager(PacketDirection.CLIENTBOUND), Minecraft.getInstance().getUser().getGameProfile());
    }


}
