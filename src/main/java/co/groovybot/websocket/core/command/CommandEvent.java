package co.groovybot.websocket.core.command;

import co.groovybot.websocket.core.TrustManager;
import co.groovybot.websocket.core.Websocket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

@RequiredArgsConstructor
@Getter
public class CommandEvent {

    private final WebSocket invoker;
    private final JSONObject message;
    private final String rawMessage;
    private final boolean isBot;
    private final Websocket websocket;
    private final TrustManager trustManager;
}
