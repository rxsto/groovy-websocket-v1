import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Arrays;

@Log4j2
public class Websocket extends WebSocketServer {

    private Configuration config = new Configuration("config/config.json").init();

    protected Websocket(InetSocketAddress address) {
        super(address);
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info(String.format("[Websocket] WebsocketConnection opened from %s!", webSocket.getRemoteSocketAddress()));
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log.info(String.format("[Websocket] WebsocketConnection closed from %s!", webSocket.getRemoteSocketAddress()));
    }

    public void onMessage(WebSocket webSocket, String message) {
        if (message.equals("getstats")) {
            broadcast("botgetstats");
            return;
        }

        String token = message.split("-")[0];
        if (!token.equals(config.getJSONObject("websocket").getString("token"))) {
            webSocket.close();
            return;
        }

        message = message.split("-")[1];

        if (message.startsWith("poststats")) {
            String[] stats = message.split(":");
            broadcast(String.format("stats:%s:%s:%s", stats[1], stats[2], stats[3]));
        }
    }

    public void onError(WebSocket webSocket, Exception e) {
        log.error(String.format("[Websocket] Error on WebsocketConnection from %s!", webSocket.getRemoteSocketAddress()), e);
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
}
