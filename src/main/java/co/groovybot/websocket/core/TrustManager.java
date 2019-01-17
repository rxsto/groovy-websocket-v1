package co.groovybot.websocket.core;


import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;

public class TrustManager {

    private final Set<WebSocket> trustedClients = new HashSet<>();

    public void trust(WebSocket websocket) {
        trustedClients.add(websocket);
    }

    public void unTrust(WebSocket webSocket) {
        trustedClients.remove(webSocket);
    }

    public boolean isTrusted(WebSocket webSocket) {
        return trustedClients.contains(webSocket);
    }
}
