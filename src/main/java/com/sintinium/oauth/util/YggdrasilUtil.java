package com.sintinium.oauth.util;

import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

public class YggdrasilUtil {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String joinUrl = "https://sessionserver.mojang.com/session/minecraft/join";
    private static final RequestConfig config = RequestConfig.custom().setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).setConnectionRequestTimeout(30 * 1000).build();
    private static CloseableHttpClient client;

    private static void openClient() {
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

    public static boolean joinServer(String token, String uuid, String hash) {
        try {
            openClient();
            HttpPost post = new HttpPost(joinUrl);

            JsonObject obj = new JsonObject();
            obj.addProperty("accessToken", token);
            obj.addProperty("selectedProfile", uuid);
            obj.addProperty("serverId", hash);

            StringEntity requestEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);

            HttpResponse response = client.execute(post);
            if (response != null) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private static JsonObject parseObject(HttpResponse entity) throws IOException {
        String responseString = EntityUtils.toString(entity.getEntity());

        if (entity.getStatusLine().getStatusCode() < 200 || entity.getStatusLine().getStatusCode() >= 300) {
            LOGGER.error("Received error code: " + entity.getStatusLine().getStatusCode());
            LOGGER.error("Response: " + responseString);
        }
        return AgnosticUtils.parseJson(responseString).getAsJsonObject();
    }

}
