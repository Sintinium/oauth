package me.jarva.oauth.profile;

import com.google.gson.JsonObject;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import me.jarva.oauth.util.EncryptionUtil;
import me.jarva.oauth.util.LoginUtil;

import java.util.UUID;

public class MojangProfile implements IProfile {
    private String name;
    private final String email;
    private final String password;
    private final UUID uuid;
    private final UserType userType;

    public MojangProfile(String name, String email, String password, UUID uuid, UserType userType) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.uuid = uuid;
        this.userType = userType;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    public static MojangProfile deserialize(JsonObject json) throws Exception {
        String name = json.get("name").getAsString();
        String email = name;
        if (json.has("email")) {
            email = json.get("email").getAsString();
        }
        String password = EncryptionUtil.decryptString(json.get("password").getAsString(), ProfileManager.getInstance().getDecryptionKey());
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        UserType userType = UserType.byName(json.get("userType").getAsString());

        return new MojangProfile(name, email, password, uuid, userType);
    }

    @Override
    public boolean login() throws AuthenticationException, LoginUtil.WrongMinecraftVersionException {
        if (!LoginUtil.loginMojangOrLegacy(this.email, this.password)) {
            return false;
        }
        LoginUtil.needsRefresh = true;
        return LoginUtil.isOnline();
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", typeName());
        json.addProperty("name", this.name);
        json.addProperty("email", this.email);
        json.addProperty("password", EncryptionUtil.encryptString(this.password, ProfileManager.getInstance().getEncryptionKey()));
        json.addProperty("uuid", this.uuid.toString());
        json.addProperty("userType", this.userType.getName());
        return json;
    }

    public static String typeName() {
        return "mojang";
    }

    @Override
    public UserType getUserType() {
        return this.userType;
    }
}
