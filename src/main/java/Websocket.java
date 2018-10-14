import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class Websocket extends WebSocketServer {

    private Configuration config = new Configuration("config/config.json").init();
    private Set<WebSocket> trusted = new HashSet<>();
    private Set<WebSocket> connected = new HashSet<>();

    protected Websocket(InetSocketAddress address) {
        super(address);
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info(String.format("[Websocket] WebsocketConnection opened from %s!", webSocket.getRemoteSocketAddress()));
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log.info(String.format("[Websocket] WebsocketConnection closed from %s!", webSocket.getRemoteSocketAddress()));
        trusted.remove(webSocket);
        connected.remove(webSocket);
    }

    public void onMessage(WebSocket webSocket, String message) {
        JSONObject object = new JSONObject(message);

        if (!object.has("type") || !object.has("data"))
            return;

        String type = object.get("type").toString();
        JSONObject data = object.getJSONObject("data");

        if (type.equals("authorization"))
            if (data.get("token").equals(config.getJSONObject("websocket").getString("token"))) {
                trusted.add(webSocket);
                connected.add(webSocket);
            }

        if (!trusted.contains(webSocket))
            webSocket.close();

        if (type.equals("getstats"))
            this.broadcast(parseMessage("botgetstats", new JSONObject().put("stats", "get")).toString());

        if (type.equals("poststats"))
            this.broadcast(object.toString());

        if (type.equals("heartbeat")) {
            connected.add(webSocket);
            log.info("[Websocket] Received new Heartbeat! Currently connected: " + connected.size() + " Clients!");
        }
    }

    public void onError(WebSocket webSocket, Exception e) {
        log.error(String.format("[Websocket] Error on WebsocketConnection from %s!", webSocket.getRemoteSocketAddress()), e);
        if (!webSocket.isClosed())
            webSocket.close();
        trusted.remove(webSocket);
    }

    public void onStart() {
        log.info("[Websocket] Successfully started WebsocketServer!");
    }

    public static void main(String[] args) {
        initLogger(args);
        log.info("[Websocket] Starting ...");

        WebSocketServer server = new Websocket(new InetSocketAddress("127.0.0.1", 6015));
        server.run();
    }

    private static void initLogger(String[] args) {
        Configurator.setRootLevel(args.length == 0 ? Level.INFO : Level.toLevel(args[0]));
    }

    public static JSONObject parseStats(int playing, int guilds, int users) {
        JSONObject object = new JSONObject();
        object.put("playing", playing);
        object.put("guilds", guilds);
        object.put("users", users);

        return object;
    }

    public static JSONObject parseMessage(String type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("data", data);

        return object;
    }
}
