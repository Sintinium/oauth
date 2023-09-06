package me.jarva.oauth.util;

#if PRE_CURRENT_MC_1_19_2
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
#else
import org.joml.Quaternionf;
#endif

import com.mojang.blaze3d.vertex.PoseStack;
import me.jarva.oauth.gui.profile.FakePlayer;
import net.minecraft.util.Mth;

public class QuaternionUtil {
    #if PRE_CURRENT_MC_1_19_2
    public static void rotatePlayerStack(PoseStack pose, float rotX) {
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees((float) Math.atan(rotX / 40F));
        quaternion.mul(quaternion1);
        pose.mulPose(quaternion);
    }
    #else
    public static void rotatePlayerStack(PoseStack pose, float rotX) {
        Quaternionf quaternion = (new Quaternionf()).rotateZ(3.1415927f);
        Quaternionf quaternion1 = (new Quaternionf()).rotateX((float) Math.toRadians(rotX / 40F));
        quaternion.mul(quaternion1);
        pose.mulPose(quaternion);
    }
    #endif

    #if PRE_CURRENT_MC_1_19_2
    public static void rotatePlayerModel(PoseStack pose) {
        pose.mulPose(Vector3f.YP.rotationDegrees(180 - Mth.lerp(1f, FakePlayer.getInstance().yBodyRotO, FakePlayer.getInstance().yBodyRot)));
    }
    #else
    public static void rotatePlayerModel(PoseStack pose) {
        pose.mulPose((new Quaternionf()).rotateY((float) Math.toRadians(180 - Mth.lerp(1f, FakePlayer.getInstance().yBodyRotO, FakePlayer.getInstance().yBodyRot))));
    }
    #endif
}
