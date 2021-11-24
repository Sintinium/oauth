package com.sintinium.oauth.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileManager {

    private static ProfileManager INSTANCE = null;
    private final List<IProfile> profiles = new ArrayList<>();
    private File saveFile;

    public static ProfileManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfileManager();
            INSTANCE.saveFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "oauth/profiles.json");
            if (!INSTANCE.saveFile.exists()) {
                try {
                    INSTANCE.saveFile.getParentFile().mkdirs();
                    INSTANCE.saveFile.createNewFile();
                    OutputStream stream = new FileOutputStream(INSTANCE.saveFile);
                    IOUtils.write("[]", stream, Charset.defaultCharset());
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                INSTANCE.loadProfiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    public void loadProfiles() throws IOException {
        JsonArray array;
        try (InputStream stream = new FileInputStream(saveFile)) {
            array = new JsonParser().parse(IOUtils.toString(stream, Charset.defaultCharset())).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            array = new JsonArray();
        }

        for (int i = 0; i < array.size(); i++) {
            String type = array.get(i).getAsJsonObject().get("type").getAsString();
            IProfile profile = null;
            try {
                switch (type) {
                    case "mojang":
                        profile = MojangProfile.deserialize(array.get(i).getAsJsonObject());
                        break;
                    case "microsoft":
                        profile = MicrosoftProfile.deserialize(array.get(i).getAsJsonObject());
                        break;
                    case "offline":
                        profile = OfflineProfile.deserialize(array.get(i).getAsJsonObject());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            profiles.add(profile);
        }
    }

    public void save() throws IOException {
        JsonArray array = new JsonArray();
        for (IProfile profile : profiles) {
            array.add(profile.serialize());
        }
        OutputStream stream = new FileOutputStream(saveFile);
        IOUtils.write(array.toString(), stream, Charset.defaultCharset());
        stream.close();
    }

    public List<IProfile> getProfiles() {
        return profiles;
    }

    public void addProfile(IProfile profile) {
        profiles.removeIf(p -> p.getUUID().equals(profile.getUUID()));
        profiles.add(profile);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeProfile(UUID uuid) {
        profiles.removeIf(profile -> profile.getUUID().equals(uuid));
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IProfile getProfile(UUID uuid) {
        for (IProfile profile : profiles) {
            if (profile.getUUID().equals(uuid)) {
                return profile;
            }
        }
        return null;
    }

}
