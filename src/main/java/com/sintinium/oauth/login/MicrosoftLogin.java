package com.sintinium.oauth.login;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sintinium.oauth.util.Lambdas;
import com.sun.net.httpserver.HttpServer;
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

//import org.json.*;

public class MicrosoftLogin {

    private static final String msTokenUrl = "https://login.live.com/oauth20_token.srf";
    private static final String authXbl = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String authXsts = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String minecraftAuth = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String minecraftProfile = "https://api.minecraftservices.com/minecraft/profile";
    private static String clientId = "907a248d-3eb5-4d01-99d2-ff72d79c5eb1";
    private static String redirectDict = "relogin";
    private static String redirect = "http://localhost:26669/" + redirectDict;
    // https://wiki.vg/Microsoft_Authentication_Scheme
    private static final String msAuthUrl = new UrlBuilder("https://login.live.com/oauth20_authorize.srf")
            .addParameter("client_id", clientId)
            .addParameter("response_type", "code")
            .addParameter("redirect_uri", redirect)
            .addParameter("scope", "XboxLive.signin%20offline_access")
            .addParameter("prompt", "select_account")
            .build();
    private RequestConfig config = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).setConnectionRequestTimeout(30 * 1000).build();
    private CloseableHttpClient client;
    private boolean isCancelled = false;
    private boolean isDebug = false;
    private CountDownLatch serverLatch = null;

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

    public MinecraftProfile login(Runnable callback) throws Exception {
        try {
            String authorizeCode = callIfNotCancelled(this::authorizeUser);
            if (authorizeCode != null) {
                printDebug("MS Oauth: " + authorizeCode);
            }
            if (authorizeCode == null) return null;

            MsToken token = callIfNotCancelled(this::getMsToken, authorizeCode);
            if (token != null) {
                printDebug("Ms Token: " + token.accessToken);
            }

            XblToken xblToken = callIfNotCancelled(this::getXblToken, token.accessToken);
            if (xblToken != null) {
                printDebug("XBL Token: " + xblToken.token + " | " + xblToken.ush);
            }

            XstsToken xstsToken = callIfNotCancelled(this::getXstsToken, xblToken);
            if (xstsToken != null) {
                printDebug("Xsts Token: " + xstsToken.token);
            }

            MinecraftToken profile = callIfNotCancelled(() -> getMinecraftToken(xstsToken, xblToken));
            if (profile != null) {
                printDebug("Minecraft Profile Token: " + profile.accessToken);
            }

            MinecraftProfile mcProfile = callIfNotCancelled(this::getMinecraftProflile, profile);
            if (mcProfile != null) {
                printDebug("Username: " + mcProfile.name);
                printDebug("UUID: " + mcProfile.id);
            }

            if (mcProfile != null) {
                LoginUtil.loginMs(mcProfile);
            }

            callback.run();
            return mcProfile;
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printDebug(String text) {
        if (!isDebug) return;
        System.out.println(text);
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

    // 1st
    private String authorizeUser() throws Exception {
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
//        Desktop.getDesktop().browse(new URI(msAuthUrl));

        try {
            Desktop.getDesktop().browse(new URI(msAuthUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.serverLatch.await();
        server.stop(2);
        if (this.isCancelled) return null;

        return msCode.get();
    }

    // 2nd
    private MsToken getMsToken(String authorizeCode) throws Exception {
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

//        System.out.println(EntityUtils.toString(response.getEntity()));
        JsonObject obj = parseObject(EntityUtils.toString(response.getEntity()));
        return new MsToken(obj.get("access_token").getAsString(), obj.get("refresh_token").getAsString());
    }

    // 3
    private XblToken getXblToken(String accessToken) throws Exception {
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
        return new XblToken(responseObj.get("Token").getAsString(), responseObj.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString());
    }

    private XstsToken getXstsToken(XblToken xblToken) throws Exception {
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
        return new XstsToken(parseObject(response).get("Token").getAsString());
    }

    private MinecraftToken getMinecraftToken(XstsToken xstsToken, XblToken xblToken) throws Exception {
        HttpPost post = new HttpPost(minecraftAuth);
        JsonObject obj = new JsonObject();
        obj.addProperty("identityToken", "XBL3.0 x=" + xblToken.ush + ";" + xstsToken.token);
        StringEntity entity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        JsonObject responseObj = parseObject(response);
        return new MinecraftToken(responseObj.get("access_token").getAsString());
    }

    private MinecraftProfile getMinecraftProflile(MinecraftToken minecraftToken) throws Exception {
        HttpGet get = new HttpGet(minecraftProfile);
        get.setHeader("Authorization", "Bearer " + minecraftToken.accessToken);
        HttpResponse response = client.execute(get);
        JsonObject obj = parseObject(response);
        return new MinecraftProfile(obj.get("name").getAsString(), obj.get("id").getAsString(), minecraftToken);
    }

    private JsonObject parseObject(HttpResponse entity) throws IOException {
        return parseObject(EntityUtils.toString(entity.getEntity()));
    }

    private JsonObject parseObject(String str) {
        return new JsonParser().parse(str).getAsJsonObject();
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

        private String url;
        private Map<String, Object> parameters = new HashMap<>();

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
}
