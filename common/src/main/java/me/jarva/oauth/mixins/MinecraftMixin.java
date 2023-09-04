package me.jarva.oauth.mixins;


import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftMixin {

    @Accessor
    User getUser();

    @Accessor
    @Mutable
    void setUser(User user);

    @Accessor
    @Mutable
    void setUserApiService(UserApiService userApiService);

    @Accessor
    @Mutable
    void setPlayerSocialManager(PlayerSocialManager playerSocialManager);

    @Accessor
    @Mutable
    void setProfileKeyPairManager(ProfileKeyPairManager profileKeyPairManager);

    @Accessor
    @Mutable
    void setReportingContext(ReportingContext reportingContext);

}
