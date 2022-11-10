package com.sintinium.oauth.login;

import com.google.gson.*;
import com.mojang.util.UUIDTypeAdapter;
import com.sintinium.oauth.gui.ErrorScreen;
import com.sintinium.oauth.gui.OAuthScreen;
import com.sintinium.oauth.profile.MicrosoftProfile;
import com.sintinium.oauth.util.AgnosticUtils;
import com.sintinium.oauth.util.Lambdas;
import com.sintinium.oauth.util.NullUtils;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

//import org.json.*;

public class MicrosoftLogin {

    private final Logger LOGGER = LogManager.getLogger();

    private static final String msTokenUrl = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String authXbl = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String authXsts = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String minecraftAuth = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String minecraftProfile = "https://api.minecraftservices.com/minecraft/profile";
    private static final String clientId = "907a248d-3eb5-4d01-99d2-ff72d79c5eb1";
    private static final String redirectDict = "relogin";
    private static final String redirect = "http://localhost:26669/" + redirectDict;
    // https://wiki.vg/Microsoft_Authentication_Scheme
    private static final String msAuthUrl = new UrlBuilder("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize")
            .addParameter("client_id", clientId)
            .addParameter("response_type", "code")
            .addParameter("redirect_uri", redirect)
            .addParameter("scope", "XboxLive.signin%20offline_access")
            .addParameter("prompt", "select_account")
            .build();
    private final RequestConfig config = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).setConnectionRequestTimeout(30 * 1000).build();
    private final CloseableHttpClient client;
    private boolean isCancelled = false;
    private Consumer<String> updateStatus = s -> {
    };
    private CountDownLatch serverLatch = null;

    private final List<JsonObject> erroredResponses = new ArrayList<>();

    public MicrosoftLogin() {
        SSLContext sslContext = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("cacerts")) {
                keyStore.load(stream, "changeit".toCharArray());
            }

            sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "changeit".toCharArray());
            trustManagerFactory.init(keyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }


        client = HttpClientBuilder.create().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext)).setDefaultRequestConfig(config).build();
    }

    public void setUpdateStatusConsumer(Consumer<String> updateStatus) {
        this.updateStatus = updateStatus;
    }

    public MicrosoftProfile loginFromRefresh(String refreshToken) throws Exception {
        try {
            MsToken token = callIfNotCancelled(this::refreshMsToken, refreshToken);
            if (token == null) return null;

            return loginWithToken(token);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MicrosoftProfile login() throws Exception {
        try {
            String authorizeCode = callIfNotCancelled(this::authorizeUser);
            if (authorizeCode == null) return null;

            updateStatus.accept("Getting token from Microsoft");
            MsToken token = callIfNotCancelled(this::getMsToken, authorizeCode);
            if (token == null) return null;

            return loginWithToken(token);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private MicrosoftProfile loginWithToken(MsToken token) throws Exception {
        updateStatus.accept("Getting Xbox Live token");
        XblToken xblToken = callIfNotCancelled(this::getXblToken, token.accessToken);

        updateStatus.accept("Logging into Xbox Live");
        XstsToken xstsToken = callIfNotCancelled(this::getXstsToken, xblToken);

        updateStatus.accept("Getting your Minecraft token");
        MinecraftToken profile = callIfNotCancelled(() -> getMinecraftToken(xstsToken, xblToken));

        updateStatus.accept("Loading your profile");
        MinecraftProfile mcProfile = callIfNotCancelled(this::getMinecraftProfile, profile);

        if (mcProfile == null) {
            OAuthScreen.setScreen(new ErrorScreen(true, "Failed to create Minecraft Profile."));
            return null;
        }

        return new MicrosoftProfile(mcProfile.name, UUIDTypeAdapter.fromString(mcProfile.id), mcProfile.token.accessToken, token.refreshToken);
    }

    private void addResponseDebugInfo(JsonObject response) {
        this.erroredResponses.add(response);
    }

    public String getErroredResponses() {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (JsonObject response : erroredResponses) {
            sb.append(index).append(": ").append(hideElements(response.deepCopy(),
                    "access_token",
                    "refresh_token",
                    "Token",
                    "DisplayClaims",
                    "xui",
                    "uhs"
            )).append(System.lineSeparator());
            index++;
        }
        return sb.toString();
    }

    private JsonObject hideElements(JsonObject obj, final String... elements) {
        for (String element : elements) {
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                // If nested loop over it first.
                if (entry.getValue().isJsonObject()) {
                    hideElements(entry.getValue().getAsJsonObject(), elements);
                    continue;
                }

                // From here on we're only dealing with matched elements
                if (!entry.getKey().equalsIgnoreCase(element)) continue;

                if (entry.getValue().isJsonArray()) {
                    // Recursive consumer. Don't judge me.
                    Consumer<JsonArray> iterate = new Consumer<>() {
                        @Override
                        public void accept(JsonArray jsonElements) {
                            JsonArray array = new JsonArray();
                            for (JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
                                if (jsonElement.isJsonPrimitive()) {
                                    array.add(getHiddenString(jsonElement));
                                } else if (jsonElement.isJsonArray()) {
                                    accept(jsonElement.getAsJsonArray());
                                } else if (jsonElement.isJsonObject()) {
                                    array.add(hideElements(jsonElement.getAsJsonObject(), elements));
                                } else {
                                    array.add(jsonElement);
                                }
                            }
                            entry.setValue(array);
                        }
                    };
                    iterate.accept(entry.getValue().getAsJsonArray());
                }

                // Just hides the value.
                if (entry.getValue().isJsonPrimitive()) {
                    entry.setValue(new JsonPrimitive(getHiddenString(entry.getValue())));
                }

                if (entry.getValue().isJsonNull()) {
                    entry.setValue(JsonNull.INSTANCE);
                }
            }
        }

        return obj;
    }

    private String getHiddenString(JsonElement element) {
        String input = element.getAsString();
        if (input == null) return "null";
        if (StringUtils.isEmpty(input)) return "empty";

        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (char c : input.toCharArray()) {
            if (count <= 10)
                builder.append("?");
            count++;
        }
        if (count > 10)
            builder.append("...+").append(count - 10);
        return builder.toString();
    }

    private <T, R> R callIfNotCancelled(Lambdas.FunctionWithException<T, R> function, T value) throws Exception {
        if (isCancelled) return null;
        return function.apply(value);
    }

    private <T> T callIfNotCancelled(Lambdas.SupplierWithException<T> runnable) throws Exception {
        if (isCancelled) return null;
        return runnable.get();
    }

    public void cancelLogin() {
        isCancelled = true;
        if (serverLatch != null) {
            serverLatch.countDown();
        }
    }

    public static void logout() {
        AgnosticUtils.openUri("https://www.microsoft.com/mscomhp/onerf/signout");
    }

    // 1st
    private String authorizeUser() throws InterruptedException, IOException {
        this.serverLatch = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress(26669), 0);
        AtomicReference<String> msCode = new AtomicReference<>(null);
        server.createContext("/" + redirectDict, httpExchange -> {
            String code = httpExchange.getRequestURI().getQuery();
            if (code != null) {
                msCode.set(code.substring(code.indexOf('=') + 1));
            }
            String response = "You can now close your browser.";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream stream = httpExchange.getResponseBody();
            stream.write(response.getBytes());
            stream.close();
            this.serverLatch.countDown();
            server.stop(2);
        });
        server.setExecutor(null);
        server.start();

        AgnosticUtils.openUri(msAuthUrl);
        serverLatch.await();
        server.stop(2);
        if (this.isCancelled) {
            return null;
        }

        return msCode.get();
    }

    // 2nd
    private MsToken getMsToken(String authorizeCode) throws IOException {
        NullUtils.requireNotNull(authorizeCode, "authorizeCode");

        HttpPost post = new HttpPost(msTokenUrl);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("scope", "xboxlive.signin"));
        params.add(new BasicNameValuePair("code", authorizeCode));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("redirect_uri", redirect));
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        post.setHeader("Accept", "application/x-www-form-urlencoded");
        post.setHeader("Content-type", "application/x-www-form-urlencoded");
        HttpResponse response = client.execute(post);

        JsonObject obj = parseObject(response);
        if (!obj.has("access_token")) {
            throw new IllegalStateException("Missing access_token from obj.has(\"access_token\")");
        }
        JsonElement accessTokenElement = obj.get("access_token");
        NullUtils.requireNotNull(accessTokenElement, "accessTokenElement");
        String accessToken = accessTokenElement.getAsString();
        NullUtils.requireNotNull(accessToken, "accessToken");

        if (!obj.has("refresh_token")) {
            throw new IllegalStateException("Missing refresh_token from obj.has(\"refresh_token\")");
        }
        JsonElement refreshTokenElement = obj.get("refresh_token");
        NullUtils.requireNotNull(refreshTokenElement, "refreshTokenElement");
        String refreshToken = refreshTokenElement.getAsString();
        NullUtils.requireNotNull(refreshToken, "refreshToken");

        return new MsToken(accessToken, refreshToken);
    }

    private MsToken refreshMsToken(String refreshToken) throws IOException {
        NullUtils.requireNotNull(refreshToken, "refreshToken");

        HttpPost post = new HttpPost(msTokenUrl);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("redirect_uri", redirect));
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        post.setHeader("Accept", "application/x-www-form-urlencoded");
        post.setHeader("Content-type", "application/x-www-form-urlencoded");
        HttpResponse response = client.execute(post);

        JsonObject obj = parseObject(response);

        if (!obj.has("access_token")) return null;
        if (!obj.has("refresh_token")) return null;

        JsonElement accessTokenElement = obj.get("access_token");
        NullUtils.requireNotNull(accessTokenElement, "accessTokenElement");
        String accessToken = accessTokenElement.getAsString();
        NullUtils.requireNotNull(accessToken, "accessToken");

        JsonElement refreshTokenElement = obj.get("refresh_token");
        NullUtils.requireNotNull(refreshTokenElement, "refreshTokenElement");
        String newRefreshToken = refreshTokenElement.getAsString();
        NullUtils.requireNotNull(newRefreshToken, "newRefreshToken");

        return new MsToken(accessToken, newRefreshToken);
    }

    // 3
    private XblToken getXblToken(String accessToken) throws IOException {
        NullUtils.requireNotNull(accessToken, "accessToken");

        HttpPost post = new HttpPost(authXbl);

        JsonObject obj = new JsonObject();
        JsonObject props = new JsonObject();
        props.addProperty("AuthMethod", "RPS");
        props.addProperty("SiteName", "user.auth.xboxlive.com");
        props.addProperty("RpsTicket", "d=" + accessToken);
        obj.add("Properties", props);
        obj.addProperty("RelyingParty", "http://auth.xboxlive.com");
        obj.addProperty("TokenType", "JWT");

        StringEntity requestEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        HttpResponse response = client.execute(post);

        JsonObject responseObj = parseObject(response);
        if (!responseObj.has("Token")) {
            throw new IllegalStateException("Missing token from responseObj.has(\"Token\")");
        }
        JsonElement tokenElement = responseObj.get("Token");
        NullUtils.requireNotNull(tokenElement, "tokenElement");
        String token = tokenElement.getAsString();
        NullUtils.requireNotNull(token, "token");

        if (!responseObj.has("DisplayClaims")) {
            throw new IllegalStateException("Missing display claims from responseObj.has(\"DisplayClaims\")");
        }
        JsonElement displayClaimsElement = responseObj.get("DisplayClaims");
        NullUtils.requireNotNull(displayClaimsElement, "displayClaimsElement");
        JsonObject displayClaims = displayClaimsElement.getAsJsonObject();
        NullUtils.requireNotNull(displayClaims, "displayClaims");

        if (!displayClaims.has("xui")) {
            throw new IllegalStateException("Missing xui from displayClaims.has(\"xui\")");
        }

        JsonElement xuiElement = displayClaims.get("xui");
        NullUtils.requireNotNull(xuiElement, "xuiElement");
        JsonArray xuiArray = xuiElement.getAsJsonArray();
        NullUtils.requireNotNull(xuiArray, "xuiArray");
        if (AgnosticUtils.isEmpty(xuiArray)) {
            throw new IllegalStateException("xuiArray is empty");
        }
        JsonElement xuiFirst = xuiArray.get(0);
        NullUtils.requireNotNull(xuiFirst, "xuiFirst");
        JsonObject xuiFirstObject = xuiFirst.getAsJsonObject();

        if (!xuiFirstObject.has("uhs")) {
            throw new IllegalStateException("Missing uhs from xuiFirstObject.has(\"uhs\")");
        }

        JsonElement uhsElement = xuiFirstObject.get("uhs");
        NullUtils.requireNotNull(uhsElement, "uhsElement");
        String uhs = uhsElement.getAsString();
        NullUtils.requireNotNull(uhs, "uhs");

        return new XblToken(token, uhs);
    }

    private XstsToken getXstsToken(XblToken xblToken) throws IOException, BaseMicrosoftLoginException {
        NullUtils.requireNotNull(xblToken, "xblToken");
        NullUtils.requireNotNull(xblToken.token, "xblToken.token");

        HttpPost post = new HttpPost(authXsts);
        JsonObject obj = new JsonObject();
        JsonObject props = new JsonObject();
        JsonArray token = new JsonArray();
        token.add(xblToken.token);
        props.addProperty("SandboxId", "RETAIL");
        props.add("UserTokens", token);
        obj.add("Properties", props);
        obj.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        obj.addProperty("TokenType", "JWT");

        StringEntity entity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(entity);

        HttpResponse response = client.execute(post);

        JsonObject responseJson = parseObject(response);
        NullUtils.requireNotNull(responseJson, "responseJson");

        if (responseJson.has("XErr")) {
            long xErr = responseJson.get("XErr").getAsLong();
            if (xErr == 2148916233L) {
                throw new NoXboxAccountException();
            } else if (xErr == 2148916235L) {
                throw new BannedCountryException();
            } else if (xErr == 2148916238L) {
                throw new UnderageAccountException();
            }
            cancelLogin();
            return null;
        }

        if (!responseJson.has("Token")) {
            throw new IllegalStateException("Missing token from responseJson.has(\"Token\")");
        }

        JsonElement tokenElement = responseJson.get("Token");
        NullUtils.requireNotNull(tokenElement, "tokenElement");

        String tokenString = tokenElement.getAsString();
        NullUtils.requireNotNull(tokenString, "tokenString");

        return new XstsToken(tokenString);
    }

    private MinecraftToken getMinecraftToken(XstsToken xstsToken, XblToken xblToken) throws IOException {
        NullUtils.requireNotNull(xstsToken, "xstsToken");
        NullUtils.requireNotNull(xstsToken.token, "xstsToken.token");
        NullUtils.requireNotNull(xblToken, "xblToken");
        NullUtils.requireNotNull(xblToken.ush, "xblToken.ush");

        HttpPost post = new HttpPost(minecraftAuth);
        JsonObject obj = new JsonObject();
        obj.addProperty("identityToken", "XBL3.0 x=" + xblToken.ush + ";" + xstsToken.token);
        StringEntity entity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        JsonObject responseObj = parseObject(response);
        NullUtils.requireNotNull(responseObj, "responseObj");
        if (!responseObj.has("access_token")) {
            throw new IllegalStateException("Missing access_token from responseObj.has(\"access_token\")");
        }
        JsonElement accessTokenElement = responseObj.get("access_token");
        String accessToken = accessTokenElement.getAsString();
        NullUtils.requireNotNull(accessToken, "accessToken");
        return new MinecraftToken(accessToken);
    }

    public MinecraftProfile getMinecraftProfile(String accessToken) throws Exception {
        return getMinecraftProfile(new MinecraftToken(accessToken));
    }

    private MinecraftProfile getMinecraftProfile(MinecraftToken minecraftToken) throws Exception {
        HttpGet get = new HttpGet(minecraftProfile);
        get.setHeader("Authorization", "Bearer " + minecraftToken.accessToken);
        HttpResponse response = client.execute(get);
        JsonObject obj = parseObject(response);
        NullUtils.requireNotNull(obj, "obj");

        if (obj.has("error")) {
            throw new NoAccountFoundException();
        }

        if (!obj.has("name")) {
            throw new IllegalStateException("Missing name from obj.has(\"name\")");
        }
        JsonElement nameElement = obj.get("name");
        String name = nameElement.getAsString();
        NullUtils.requireNotNull(name, "name");

        if (!obj.has("id")) {
            throw new IllegalStateException("Missing id from obj.has(\"id\")");
        }
        JsonElement idElement = obj.get("id");
        String id = idElement.getAsString();
        NullUtils.requireNotNull(id, "id");

        return new MinecraftProfile(name, id, minecraftToken);
    }

    private JsonObject parseObject(HttpResponse entity) throws IOException {
        HttpEntity responseEntity = entity.getEntity();
        NullUtils.requireNotNull(responseEntity, "responseEntity");
        String responseString = EntityUtils.toString(responseEntity);
        NullUtils.requireNotNull(responseString, "responseString");

        if (entity.getStatusLine().getStatusCode() < 200 || entity.getStatusLine().getStatusCode() >= 300) {
            LOGGER.error("Received error code: " + entity.getStatusLine().getStatusCode());
            LOGGER.error("Response: " + responseString);
        }
        return parseObject(responseString);
    }

    private JsonObject parseObject(String str) {
        JsonElement json = AgnosticUtils.parseJson(str);
        NullUtils.requireNotNull(json, "json");
        JsonObject obj = json.getAsJsonObject();
        NullUtils.requireNotNull(obj, "obj");
        addResponseDebugInfo(obj);
        return obj;
    }

    private static class MsToken {
        public String accessToken;
        public String refreshToken;

        public MsToken(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    private static class XblToken {
        public String token;
        public String ush;

        public XblToken(String token, String ush) {
            this.token = token;
            this.ush = ush;
        }
    }

    private static class XstsToken {
        public String token;

        public XstsToken(String token) {
            this.token = token;
        }
    }

    public static class MinecraftToken {
        public String accessToken;

        public MinecraftToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    private static class UrlBuilder {

        private final String url;
        private final Map<String, Object> parameters = new HashMap<>();

        public UrlBuilder(String url) {
            this.url = url;
        }

        public UrlBuilder addParameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }

        public String build() {
            StringBuilder builder = new StringBuilder();
            builder.append(url);
            if (!parameters.isEmpty()) {
                builder.append("?");
            }
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            builder.setLength(builder.length() - 1);
            return builder.toString();
        }
    }

    public class MinecraftProfile {
        public String name;
        public String id;
        public MinecraftToken token;

        public MinecraftProfile(String name, String id, MinecraftToken token) {
            this.name = name;
            this.id = id;
            this.token = token;
        }

        public String str() {
            return "name=" + name + "&" + "id=" + id + "&" + "token=" + token.accessToken;
        }
    }

    public static class BaseMicrosoftLoginException extends Exception {
    }

    public static class NoXboxAccountException extends BaseMicrosoftLoginException {
    }

    public static class BannedCountryException extends BaseMicrosoftLoginException {
    }

    public static class UnderageAccountException extends BaseMicrosoftLoginException {
    }

    public static class NoAccountFoundException extends BaseMicrosoftLoginException {
    }
}
