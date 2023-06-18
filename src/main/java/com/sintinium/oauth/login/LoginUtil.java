package com.sintinium.oauth.login;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import com.sintinium.oauth.GuiEventHandler;
import com.sintinium.oauth.mixin.MinecraftMixin;
import com.sintinium.oauth.profile.MicrosoftProfile;
import com.sintinium.oauth.profile.MojangProfile;
import com.sintinium.oauth.util.MultiplayerAllowedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.AccountProfileKeyPairManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

public class LoginUtil {

    public static String lastMojangUsername = null;
    public static boolean needsRefresh = true;
    public static boolean wasOnline = false;
    private static long lastCheck = -1L;

    private static final YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString());
    private static final YggdrasilUserAuthentication userAuth = (YggdrasilUserAuthentication) authService.createUserAuthentication(Agent.MINECRAFT);
    private static boolean isMultiplayerDisabled = false;

    public static void updateOnlineStatus() {
        needsRefresh = true;
        isOnline();
    }

    public static boolean isOnline() {
        if (!needsRefresh && System.currentTimeMillis() - lastCheck < 1000 * 10) {
            return wasOnline;
        }
        User session = Minecraft.getInstance().getUser();
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        String hash = new BigInteger(bytes).toString(16);
        needsRefresh = false;
        lastCheck = System.currentTimeMillis();
        GuiEventHandler.warned = false;
        try {
            isMultiplayerDisabled = MultiplayerAllowedUtil.isMultiplayerDisabled(session.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MicrosoftLogin microsoftLogin = new MicrosoftLogin();
            microsoftLogin.getMinecraftProfile(Minecraft.getInstance().getUser().getAccessToken());
            wasOnline = true;
            return true;
        } catch (Exception e) {
            wasOnline = false;
            return false;
        }
    }

    public static void setOnline(boolean isOnline) {
        LoginUtil.wasOnline = isOnline;
    }

    public static boolean isMultiplayerDisabled() {
        return isMultiplayerDisabled;
    }

    public static GameProfile getGameProfile(User session) {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        String hash = new BigInteger(bytes).toString(16);

        needsRefresh = false;
        lastCheck = System.currentTimeMillis();
        try {
            Minecraft.getInstance().getMinecraftSessionService().joinServer(session.getGameProfile(), session.getAccessToken(), hash);
            GameProfile profile = Minecraft.getInstance().getMinecraftSessionService().hasJoinedServer(session.getGameProfile(), hash, null);
            if (profile != null && profile.isComplete()) {
                return profile;
            }
        } catch (AuthenticationException e) {
            return null;
        }
        return null;
    }

    public static void loginMs(MicrosoftProfile profile) throws WrongMinecraftVersionException, AuthenticationException {
        User session = new User(profile.getName(), profile.getUUID().toString(), profile.getAccessToken(), Optional.empty(), Optional.empty(), User.Type.MSA);
        UserApiService apiService = authService.createUserApiService(profile.getAccessToken());
        setSession(session, true);
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

        return new MojangProfile(name, username, password, uuid, type);
    }

    public static void loginOffline(String username) throws WrongMinecraftVersionException {
        User session = new User(username, UUID.nameUUIDFromBytes(username.getBytes()).toString(), "NotValid", Optional.empty(), Optional.empty(), User.Type.LEGACY);
        setSession(session, false);
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
        setSession(session, false);
        lastMojangUsername = username;
        return isOnline;
    }

    public static void setSession(User session, boolean onlineLogin) throws WrongMinecraftVersionException {
        MinecraftMixin mc = (MinecraftMixin) Minecraft.getInstance();
        mc.setUser(session);

        UserApiService apiService = UserApiService.OFFLINE;
        if (onlineLogin) {
            YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) Minecraft.getInstance().getMinecraftSessionService();
            YggdrasilAuthenticationService authenticationService = sessionService.getAuthenticationService();
            try {
                apiService = authenticationService.createUserApiService(session.getAccessToken());
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }

        mc.setUserApiService(apiService);
        mc.setPlayerSocialManager(new PlayerSocialManager(Minecraft.getInstance(), apiService));
        mc.setProfileKeyPairManager(new AccountProfileKeyPairManager(apiService, session.getProfileId(), Minecraft.getInstance().gameDirectory.toPath()));
        mc.setReportingContext(ReportingContext.create(ReportEnvironment.local(), apiService));

        Minecraft.getInstance().getProfileProperties().clear();
        Minecraft.getInstance().getProfileProperties();

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
