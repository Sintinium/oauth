package me.jarva.oauth.util;

#if PRE_CURRENT_MC_1_19_2
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
#else
import com.mojang.math.Axis;
import org.joml.Quaternionf;
#endif

public class AxisUtil {
    #if PRE_CURRENT_MC_1_19_2
    public static Quaternion getZNRotation(float deg) {
        return Vector3f.ZN.rotationDegrees(deg);
    }
    #else
    public static Quaternionf getZNRotation(float deg) {
        return Axis.ZN.rotationDegrees(deg);
    }
    #endif

    #if PRE_CURRENT_MC_1_19_2
    public static Quaternion getXPRotation(float deg) {
        return Vector3f.ZN.rotationDegrees(deg);
    }
    #else
    public static Quaternionf getXPRotation(float deg) {
        return Axis.ZN.rotationDegrees(deg);
    }
    #endif
}
