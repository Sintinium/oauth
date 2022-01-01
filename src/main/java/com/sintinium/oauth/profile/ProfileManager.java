package com.sintinium.oauth.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.Util;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ProfileManager {

    private static ProfileManager INSTANCE = null;
    private final List<IProfile> profiles = new ArrayList<>();
    private final Map<UUID, GameProfile> gameProfiles = new HashMap<>();
    private File saveFile;

    public static ProfileManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfileManager();
            INSTANCE.migrateProfileFile();

            INSTANCE.saveFile = INSTANCE.getSaveFile();
            try {
                INSTANCE.loadProfiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    /**
     * Deletes any profile files inside /config and moves it to ./minecraft/oauthprofiles.json
     */
    private void migrateProfileFile() {
        // File already migrated no need to do anything
        if (new File(getSaveFilePath()).exists()) {
            return;
        }

        // Deletes old oauth-client.toml if it still exists
        File clientFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "oauth-client.toml");
        if (clientFile.exists()) {
            clientFile.delete();
        }

        // Moves the old profile file to the new location
        File oldFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "oauth/profiles.json");
        if (oldFile.exists()) {
            File newFile = getSaveFile();
            try {
                FileUtils.copyFile(oldFile, newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            oldFile.delete();
            oldFile.getParentFile().delete();
        }
    }

    private String getSaveFilePath() {
        String filePath = System.getProperty("user.home");
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            filePath += "\\AppData\\Roaming\\.minecraft\\oauthprofiles.json";
        } else if (Util.getPlatform() == Util.OS.OSX) {
            filePath += "/Library/Application Support/.minecraft/oauthprofiles.json";
        } else {
            filePath += "/.minecraft/oauthprofiles.json";
        }
        return filePath;
    }

    private File getSaveFile() {
        File file = new File(getSaveFilePath());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                OutputStream stream = new FileOutputStream(file);
                IOUtils.write("[]", stream, Charset.defaultCharset());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
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
            IProfile profile;
            try {
                switch (type) {
                    case "mojang" -> profile = MojangProfile.deserialize(array.get(i).getAsJsonObject());
                    case "microsoft" -> profile = MicrosoftProfile.deserialize(array.get(i).getAsJsonObject());
                    case "offline" -> profile = OfflineProfile.deserialize(array.get(i).getAsJsonObject());
                    default -> profile = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (profile == null) {
                continue;
            }
            addProfile(profile, false);
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
        this.addProfile(profile, true);
    }

    private void addProfile(IProfile profile, boolean shouldSave) {
        profiles.removeIf(p -> p.getUUID().equals(profile.getUUID()));
        profiles.add(profile);
        if (!gameProfiles.containsKey(profile.getUUID())) {
            gameProfiles.put(profile.getUUID(), new GameProfile(profile.getUUID(), profile.getName()));
        }
        if (!shouldSave) return;
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

    public GameProfile getGameProfileOrNull(UUID uuid) {
        return gameProfiles.getOrDefault(uuid, null);
    }
}
