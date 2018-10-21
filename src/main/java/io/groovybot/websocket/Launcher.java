package io.groovybot.websocket;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


@Log4j2
public class Launcher {

    public static void main(String[] args) throws IOException {
        initLogger(args);
        log.info("[io.groovybot.websocket.Websocket] Starting ...");
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
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
        configuration = configuration.init();
        WebSocketServer server = new Websocket(configuration);
        server.run();
    }

    private static void initLogger(String[] args) {
        Configurator.setRootLevel(args.length == 0 ? Level.INFO : Level.toLevel(args[0]));
    }

}
