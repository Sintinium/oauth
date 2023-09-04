package me.jarva.oauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

#if POST_MC_1_16_5
import dev.architectury.platform.Platform;
#else
import me.shedaniel.architectury.platform.Platform;
#endif

public class OAuth {
    public static final String MOD_ID = "oauth";
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void init() {

    }
}
