package com.sintinium.oauth.profile;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ProfileManager {

    private static ProfileManager INSTANCE = null;
    private List<IProfile> profiles = new ArrayList<>();
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
        InputStream stream = new FileInputStream(saveFile);
        JSONArray array = new JSONArray(IOUtils.toString(stream, Charset.defaultCharset()));
        stream.close();
        for (int i = 0; i < array.length(); i++) {
            String type = array.getJSONObject(i).getString("type");
            IProfile profile = null;
            try {
                switch (type) {
                    case "mojang":
                        profile = MojangProfile.deserialize(array.getJSONObject(i));
                        break;
                    case "microsoft":
                        profile = MicrosoftProfile.deserialize(array.getJSONObject(i));
                        break;
                    case "offline":
                        profile = OfflineProfile.deserialize(array.getJSONObject(i));
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
        JSONArray array = new JSONArray();
        for (IProfile profile : profiles) {
            array.put(profile.serialize());
        }
        OutputStream stream = new FileOutputStream(saveFile);
        IOUtils.write(array.toString(), stream, Charset.defaultCharset());
        stream.close();
    }

    public List<IProfile> getProfiles() {
        return profiles;
    }

    public void addProfile(IProfile profile) {
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
