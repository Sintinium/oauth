package com.sintinium.oauth.login;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

public class LoginUtil {

    public static String lastMojangUsername = null;
    public static boolean needsRefresh = true;
    public static boolean wasOnline = false;
    private static long lastCheck = -1L;

    private static YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
    private static YggdrasilUserAuthentication userAuth = (YggdrasilUserAuthentication) authService.createUserAuthentication(Agent.MINECRAFT);
    private static YggdrasilMinecraftSessionService minecraftSessionService = (YggdrasilMinecraftSessionService) authService.createMinecraftSessionService();

    public static void updateOnlineStatus() {
        needsRefresh = true;
        isOnline();
    }

    public static boolean isOnline() {
        if (!needsRefresh && System.currentTimeMillis() - lastCheck < 1000 * 10) {
            return wasOnline;
        }
        Session session = Minecraft.getMinecraft().getSession();
        String uuid = UUID.randomUUID().toString();
        needsRefresh = false;
        lastCheck = System.currentTimeMillis();
        try {
            minecraftSessionService.joinServer(session.getProfile(), session.getToken(), uuid);
            GameProfile mssProfile = minecraftSessionService.hasJoinedServer(session.getProfile(),
                uuid, null);
            if (mssProfile != null && mssProfile.isComplete()) {
                wasOnline = true;
                return true;
            } else {
                wasOnline = false;
                return false;
            }
        } catch (AuthenticationException e) {
            wasOnline = false;
            return false;
        }
    }

    public static void loginMs(MicrosoftLogin.MinecraftProfile profile) {
        Session session = new Session(profile.name, profile.id, profile.token.accessToken, Session.Type.MOJANG.name());
        setSession(session);
    }

    public static Optional<Boolean> loginMojangOrLegacy(String username, String password) {
        try {
            if (password.isEmpty()) {
                Session session = new Session(username, UUID.nameUUIDFromBytes(username.getBytes()).toString(), null, UserType.LEGACY.getName());
                setSession(session);
                return Optional.of(true);
            }
            userAuth.setUsername(username);
            userAuth.setPassword(password);
            userAuth.logIn();

            String name = userAuth.getSelectedProfile().getName();
            String uuid = UUIDTypeAdapter.fromUUID(userAuth.getSelectedProfile().getId());
            String token = userAuth.getAuthenticatedToken();
            String type = userAuth.getUserType().getName();
            userAuth.logOut();

            Session session = new Session(name, uuid, token, type);
            setSession(session);
            lastMojangUsername = username;
            return Optional.of(true);
        } catch (AuthenticationUnavailableException e) {
            return Optional.empty();
        } catch (AuthenticationException e) {
            return Optional.of(false);
        }
    }

    private static void setSession(Session session) {
        needsRefresh = true;
        updateOnlineStatus();
        Field field = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71449_j");
        field.setAccessible(true);
        try {
            field.set(Minecraft.getMinecraft(), session);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
