package io.groovybot.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class Websocket extends WebSocketServer {

    @Getter
    private final Configuration config;
    @Getter
    private static Websocket websocket;
    private Connection connection;
    private WebSocket bot;
    private Set<WebSocket> trusted = new HashSet<>();

    private Websocket(InetSocketAddress address, Configuration configuration) {
        super(address);
        websocket = this;
        this.config = configuration;
        PostgreSQL db = new PostgreSQL();
        this.connection = db.getConnection();
    }

    public Websocket(Configuration configuration) {
        this(new InetSocketAddress(configuration.getJSONObject("websocket").getString("bind"), configuration.getJSONObject("websocket").getInt("port")), configuration);
    }


    @SuppressWarnings("unused")
    public static JSONObject parseStats(int playing, int guilds, int users) {
        JSONObject object = new JSONObject();
        object.put("playing", playing);
        object.put("guilds", guilds);
        object.put("users", users);
        return object;
    }

    public static JSONObject parseMessage(String client, String type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put("client", client);
        object.put("type", type);
        object.put("data", data);
        return object;
    }

    public static String createToken() {
        return RandomStringUtils.randomAlphanumeric(64);
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info(String.format("[io.groovybot.websocket.Websocket] WebsocketConnection opened from %s!", webSocket.getRemoteSocketAddress()));
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log.info(String.format("[io.groovybot.websocket.Websocket] WebsocketConnection closed from %s!", webSocket.getRemoteSocketAddress()));
        if (webSocket.equals(bot))
            this.broadcast(parseMessage("server", "error", new JSONObject().put("text", "Client bot disconnected!")).toString());
        trusted.remove(webSocket);
    }

    public void onMessage(WebSocket webSocket, String message) {
        JSONObject object = new JSONObject(message);

        if (!object.has("type") || !object.has("data"))
            return;

        String client = object.get("client").toString();
        String type = object.get("type").toString();
        JSONObject data = object.getJSONObject("data");

        if (type.equals("authorization")) {
            PreparedStatement ps;
            ResultSet rs;
            String token = null;

            try {
                ps = connection.prepareStatement("SELECT * FROM websocket");
                rs = ps.executeQuery();
                while (rs.next()) {
                    token = rs.getString("token");
                }
            } catch (SQLException e) {
                log.error("[io.groovybot.websocket.Websocket] Error while processing authentication!", e);
            }

            if (data.get("token").equals(token)) {
                trusted.add(webSocket);
                if (client.equals("bot"))
                    this.bot = webSocket;
                try {
                    connection.prepareStatement("DELETE FROM websocket").execute();
                    PreparedStatement resetToken = connection.prepareStatement("INSERT INTO websocket (token) VALUES (?)");
                    resetToken.setString(1, createToken());
                    resetToken.execute();
                } catch (SQLException e) {
                    log.error("[io.groovybot.websocket.Websocket] Error while processing authentication!", e);
                }
                log.info("[io.groovybot.websocket.Websocket] Successfully authorized from " + webSocket.getRemoteSocketAddress() + "!");
            } else {
                webSocket.send(parseMessage("server", "error", new JSONObject().put("type", "forbidden").put("text", "The given token was invalid!")).toString());
                return;
            }
        }

        if (!trusted.contains(webSocket)) {
            webSocket.send(parseMessage("server", "error", new JSONObject().put("type", "forbidden").put("text", "This client is not authorized to send packets!!")).toString());
            webSocket.close();
            return;
        }

        if (type.equals("getstats"))
            this.broadcast(parseMessage("server", "botgetstats", new JSONObject().put("stats", "get")).toString());

        if (type.equals("poststats"))
            this.broadcast(object.toString());

        if (type.equals("heartbeat")) {
            log.info("[io.groovybot.websocket.Websocket] Received heartbeat from bot!");
        }
    }

    public void onError(WebSocket webSocket, Exception e) {
        log.error(String.format("[io.groovybot.websocket.Websocket] Error on WebsocketConnection from %s!", webSocket.getRemoteSocketAddress()), e);
        if (webSocket.equals(bot))
            this.broadcast(parseMessage("server", "error", new JSONObject().put("text", "Client bot disconnected!")).toString());
        if (!webSocket.isClosed())
            webSocket.close();
        trusted.remove(webSocket);
    }

    public void onStart() {
        log.info("[io.groovybot.websocket.Websocket] Successfully started WebsocketServer!");

        try {
            this.connection.prepareStatement("DELETE FROM websocket");
            PreparedStatement setToken = connection.prepareStatement("INSERT INTO websocket (token) VALUES (?)");
            setToken.setString(1, createToken());
            setToken.execute();
            log.info("[io.groovybot.websocket.Websocket] Generated Token!");
        } catch (SQLException e) {
            log.error("[io.groovybot.websocket.Websocket] Error while generating first token!", e);
        }
    }
}
