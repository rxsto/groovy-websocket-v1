package io.groovybot.websocket.core.command;

import io.groovybot.websocket.core.TrustManager;
import io.groovybot.websocket.core.Websocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.groovybot.websocket.util.Helpers.parseMessage;

@Log4j2
@RequiredArgsConstructor
public class CommandManager implements Closeable {

    private final Map<String, Command> commandMap = new HashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final TrustManager trustManager;
    private final Websocket instance;

    public void registerCommands(Command... commands) {
        for (Command command : commands)
            commandMap.put(command.getName(), command);
    }

    public void onMessage(WebSocket webSocket, String message) {
        executor.execute(() -> parseCommands(webSocket, message));
    }

    private void parseCommands(WebSocket webSocket, String message) {
        JSONObject parsedMessage = new JSONObject(message);

        if (!parsedMessage.has("type") || !parsedMessage.has("data") || !parsedMessage.has("client"))
            return;

        String client = parsedMessage.getString("client");
        String type = parsedMessage.getString("type");
        JSONObject data;
        try {
            data = parsedMessage.getJSONObject("data");
        } catch (JSONException e) {
            data = new JSONObject();
        }

        if (!commandMap.containsKey(type))
            return;

        Command command = commandMap.get(type);
        CommandEvent commandEvent = new CommandEvent(webSocket, data, message, client.equals("bot"), instance, trustManager);
        if (command.isNeedsAuthorization() && !trustManager.isTrusted(webSocket)) {
            webSocket.send(parseMessage("server", "error", new JSONObject().put("type", "forbidden").put("text", "This client is not authorized to send packets!")).toString());
            webSocket.close();
            return;
        }

        try {
            command.run(data, commandEvent);
        } catch (Exception e) {
            log.error("[CommandManager] An error occurred while parsing command", e);
        }
        log.info(String.format("[CommandExecutor] Commands %s got executed by %s", command.getClass().getCanonicalName(), client));
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
