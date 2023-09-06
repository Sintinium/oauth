package me.jarva.oauth.util;

import com.mojang.authlib.Environment;
import com.mojang.authlib.EnvironmentParser;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.response.UserAttributesResponse;
import net.minecraft.client.Minecraft;

import java.net.URL;

public class MultiplayerAllowedUtil {
    public static boolean isMultiplayerDisabled(String accessToken) {
        Environment environment = EnvironmentParser.getEnvironmentFromProperties().orElse(YggdrasilEnvironment.PROD.getEnvironment());
        MinecraftClient client = new MinecraftClient(accessToken, Minecraft.getInstance().getProxy());
        URL routePrivileges = HttpAuthenticationService.constantURL(environment.getServicesHost() + "/player/attributes");

        try {
            UserAttributesResponse response = client.get(routePrivileges, UserAttributesResponse.class);
            UserAttributesResponse.Privileges privileges = response.getPrivileges();
            if (privileges != null && !privileges.getMultiplayerServer()) {
                return true;
            }
        } catch (MinecraftClientHttpException e) {
            e.printStackTrace();
            return false;
        } catch (MinecraftClientException e) {
            throw e;
        }
        return false;
    }
}
