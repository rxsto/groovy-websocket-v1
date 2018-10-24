package io.groovybot.websocket.core.command;

import io.groovybot.websocket.util.Helpers;
import lombok.Getter;
import org.json.JSONObject;

@Getter
public abstract class Command {

    private final String name;
    private final boolean needsAuthorization;

    /**
     * Constructs a new command
     * @param name the name of the command
     * @param needsAuthorization whether the command needs an authorization or not
     */
    public Command(String name, boolean needsAuthorization) {
        this.name = name;
        this.needsAuthorization = needsAuthorization;
    }

    protected abstract void run(JSONObject message, CommandEvent event);

    protected JSONObject parseMessage(String type, JSONObject data) {
        return Helpers.parseMessage("server", type, data);
    }
}
