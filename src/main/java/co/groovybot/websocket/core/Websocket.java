package co.groovybot.websocket.core;

import co.groovybot.websocket.core.command.CommandManager;
import co.groovybot.websocket.core.command.CommandRegistry;
import co.groovybot.websocket.io.config.Configuration;
import co.groovybot.websocket.io.database.PostgreSQL;
import co.groovybot.websocket.util.Helpers;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Log4j2
public class Websocket extends WebSocketServer {

    @Getter
    private static Websocket websocket;
    @Getter
    private final Configuration config;
    @Getter
    private final PostgreSQL database;
    @Getter
    private final TrustManager trustManager;
    private final CommandManager commandManager;
    @Getter
    private final StatisticHolder statisticHolder;
    @Setter
    private WebSocket bot;

    private Websocket(InetSocketAddress address, Configuration configuration) {
        super(address);
        websocket = this;
        this.config = configuration;
        this.database = new PostgreSQL();
        this.trustManager = new TrustManager();
        this.commandManager = new CommandManager(trustManager, this);
        //Register commands
        new CommandRegistry(commandManager);
        this.statisticHolder = new StatisticHolder();
        log.info(String.format("[Websocket] Initializing Websocket with address %s ...", address.toString()));
    }

    private Websocket(String bind, int port, Configuration configuration) {
        this(new InetSocketAddress(bind, port), configuration);
    }

    public Websocket(Configuration configuration) {
        this(configuration.getJSONObject("websocket").getString("bind"), configuration.getJSONObject("websocket").getInt("port"), configuration);
    }


    @SuppressWarnings("unused")
    private JSONObject parseStats(int playing, int guilds, int users) {
        JSONObject object = new JSONObject();
        object.put("playing", playing);
        object.put("guilds", guilds);
        object.put("users", users);
        return object;
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info(String.format("WebsocketConnection opened from %s!", webSocket.getRemoteSocketAddress()));
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log.info(String.format("WebsocketConnection closed from %s!", webSocket.getRemoteSocketAddress()));
        if (webSocket.equals(bot))
            this.broadcast(Helpers.parseMessage("server", "error", new JSONObject().put("text", "Client bot disconnected!")).toString());
        trustManager.unTrust(webSocket);
    }

    public void onMessage(WebSocket webSocket, String message) {
        commandManager.onMessage(webSocket, message);
    }

    public void onError(WebSocket webSocket, Exception e) {
        log.error(String.format(" Error on WebsocketConnection from %s!", webSocket.getRemoteSocketAddress()), e);
        if (webSocket.equals(bot))
            this.broadcast(Helpers.parseMessage("server", "error", new JSONObject().put("text", "Client bot disconnected!")).toString());
        if (!webSocket.isClosed())
            webSocket.close();
        trustManager.unTrust(webSocket);
    }

    public void onStart() {
        log.info(" Successfully started WebsocketServer!");

        try (Connection connection = database.getDataSource().getConnection()) {
            connection.prepareStatement("DELETE FROM websocket");
            PreparedStatement setToken = connection.prepareStatement("INSERT INTO websocket (token) VALUES (?)");
            setToken.setString(1, Helpers.createToken());
            setToken.execute();
            log.info(" Generated Token!");
        } catch (SQLException e) {
            log.error(" Error while generating first token!", e);
        }
    }
}
