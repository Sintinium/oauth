package com.sintinium.oauth.login;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import com.sintinium.oauth.profile.MicrosoftProfile;
import com.sintinium.oauth.profile.MojangProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

public class LoginUtil {

    public static String lastMojangUsername = null;
    public static boolean needsRefresh = true;
    public static boolean wasOnline = false;
    private static long lastCheck = -1L;

    private static final YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString());
    private static final YggdrasilUserAuthentication userAuth = (YggdrasilUserAuthentication) authService.createUserAuthentication(Agent.MINECRAFT);
    private static final YggdrasilMinecraftSessionService minecraftSessionService = (YggdrasilMinecraftSessionService) authService.createMinecraftSessionService();

    public static void updateOnlineStatus() {
        needsRefresh = true;
        isOnline();
    }

    public static boolean isOnline() {
        if (!needsRefresh && System.currentTimeMillis() - lastCheck < 1000 * 10) {
            return wasOnline;
        }
        User session = Minecraft.getInstance().getUser();
        String uuid = UUID.randomUUID().toString();
        needsRefresh = false;
        lastCheck = System.currentTimeMillis();
        try {
            minecraftSessionService.joinServer(session.getGameProfile(), session.getAccessToken(), uuid);
            if (minecraftSessionService.hasJoinedServer(session.getGameProfile(), uuid, null).isComplete()) {
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

    public static GameProfile getGameProfile(User session) {
        String serverId = UUID.randomUUID().toString();
        needsRefresh = false;
        lastCheck = System.currentTimeMillis();
        try {
            minecraftSessionService.joinServer(session.getGameProfile(), session.getAccessToken(), serverId);
            GameProfile profile = minecraftSessionService.hasJoinedServer(session.getGameProfile(), serverId, null);
            if (profile.isComplete()) {
                return profile;
            }
        } catch (AuthenticationException e) {
            return null;
        }
        return null;
    }

    public static void loginMs(MicrosoftProfile profile) throws WrongMinecraftVersionException {
        User session = new User(profile.getName(), profile.getUUID().toString(), profile.getAccessToken(), Optional.empty(), Optional.empty(), User.Type.MSA);
        setSession(session);
    }

    public static MojangProfile tryGetMojangProfile(String username, String password) throws AuthenticationException {
        if (password.isEmpty()) {
            return null;
        }
        userAuth.setUsername(username);
        userAuth.setPassword(password);
        userAuth.logIn();

        String name = userAuth.getSelectedProfile().getName();
        UUID uuid = userAuth.getSelectedProfile().getId();
        String token = userAuth.getAuthenticatedToken();
        UserType type = userAuth.getUserType();
        boolean isOnline = userAuth.canPlayOnline();

        userAuth.logOut();

        if (!isOnline) {
            return null;
        }

        return new MojangProfile(name, password, uuid, type);
    }

    public static void loginOffline(String username) throws WrongMinecraftVersionException {
        User session = new User(username, UUID.nameUUIDFromBytes(username.getBytes()).toString(), "NotValid", Optional.empty(), Optional.empty(), User.Type.LEGACY);
        setSession(session);
    }

    public static boolean loginMojangOrLegacy(String username, String password) throws AuthenticationException, WrongMinecraftVersionException {
        if (password.isEmpty()) {
            return false;
        }
        userAuth.setUsername(username);
        userAuth.setPassword(password);
        userAuth.logIn();

        String name = userAuth.getSelectedProfile().getName();
        String uuid = UUIDTypeAdapter.fromUUID(userAuth.getSelectedProfile().getId());
        String token = userAuth.getAuthenticatedToken();
        UserType type = userAuth.getUserType();

        boolean isOnline = userAuth.canPlayOnline();
        userAuth.logOut();

        User session = new User(name, uuid, token, Optional.empty(), Optional.empty(), User.Type.byName(type.getName()));
        setSession(session);
        lastMojangUsername = username;
        return isOnline;
    }

    public static void setSession(User session) throws WrongMinecraftVersionException {
        Field field = ObfuscationReflectionHelper.findField(Minecraft.class, "f_90998_");
        field.setAccessible(true);
        try {
            field.set(Minecraft.getInstance(), session);
        } catch (IllegalAccessException e) {
            throw new WrongMinecraftVersionException("Missing session field. Are you using SRG mappings?");
        }
        needsRefresh = true;
        updateOnlineStatus();
    }

    public static class WrongMinecraftVersionException extends Exception {
        public WrongMinecraftVersionException() {
        }

        public WrongMinecraftVersionException(String message) {
            super(message);
        }

        public WrongMinecraftVersionException(String message, Throwable cause) {
            super(message, cause);
        }

        public WrongMinecraftVersionException(Throwable cause) {
            super(cause);
        }

        public WrongMinecraftVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
