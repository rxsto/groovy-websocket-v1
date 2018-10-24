package io.groovybot.websocket;

import io.groovybot.websocket.core.Websocket;
import io.groovybot.websocket.io.config.Configuration;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


@Log4j2
public class Launcher implements Closeable {

    private final boolean debug;

    public static void main(String[] args) throws IOException {
        new Launcher(args);
    }

    private Launcher(String[] args) throws IOException {
        initLogger(args);
        debug = String.join(" ", args).contains("debug");
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        log.info("Starting ...");
        File configFile = new File("config.json");
        if (!configFile.exists())
            configFile.createNewFile();
        Configuration configuration = new Configuration("config.json");
        configuration.addDefault("websocket", new JSONObject()
                .put("bind", "0.0.0.0")
                .put("port", 6015));
        configuration.addDefault("db", new JSONObject()
                .put("host", "host")
                .put("port", 1234)
                .put("username", "user")
                .put("database", "database")
                .put("password", "password")
        );
        configuration.addDefault("ssl", new JSONObject()
                .put("storetype", "JKS")
                .put("keystore", "keystore.jks")
                .put("password", "keysotrepw")
                .put("keypassword", "keypw")
        );
        configuration = configuration.init();
        JSONObject sslConfig = configuration.getJSONObject("ssl");
        WebSocketServer server = new Websocket(configuration);

        if (!debug) {
            try {
                KeyStore ks = KeyStore.getInstance(sslConfig.getString("storetype"));
                File keyFile = new File(sslConfig.getString("keystore"));
                ks.load(new FileInputStream(keyFile), sslConfig.getString("password").toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(ks, sslConfig.getString("keypassword").toCharArray());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
                trustManagerFactory.init(ks);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
                server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
                log.fatal("[Launcher] Error while preparing SSL Socket", e);
            }
        }
        server.run();
    }

    private static void initLogger(String[] args) {
        Configurator.setRootLevel(args.length == 0 ? Level.INFO : Level.toLevel(args[0]));
        try {
            Configurator.initialize(ClassLoader.getSystemClassLoader(), new ConfigurationSource(ClassLoader.getSystemResourceAsStream("log4j2.xml")));
        } catch (IOException e) {
            log.fatal("[Launcher] Could not initialize logger. Exiting ...", e);
        }
    }

    @Override
    public void close() {
        try {
            Websocket.getWebsocket().stop();
        } catch (IOException | InterruptedException e) {
            log.fatal("[Launcher] Error while closing websocket", e);
        }
    }
}
