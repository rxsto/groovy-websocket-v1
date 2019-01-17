package co.groovybot.websocket.commands;

import co.groovybot.websocket.core.command.Command;
import co.groovybot.websocket.core.command.CommandEvent;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

@Log4j2
public class HeartBeatCommand extends Command {

    public HeartBeatCommand() {
        super("heartbeat", true);
    }

    @Override
    protected void run(JSONObject message, CommandEvent event) {
        log.info("[WebSocket] Received heartbeat from bot");
    }
}
