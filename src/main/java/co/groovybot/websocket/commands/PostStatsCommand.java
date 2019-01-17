package co.groovybot.websocket.commands;

import co.groovybot.websocket.core.StatisticHolder;
import co.groovybot.websocket.core.command.Command;
import co.groovybot.websocket.core.command.CommandEvent;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

@Log4j2
public class PostStatsCommand extends Command {

    public PostStatsCommand() {
        super("poststats", true);
    }

    @Override
    protected void run(JSONObject message, CommandEvent event) {
        StatisticHolder holder = event.getWebsocket().getStatisticHolder();
        holder.setPlayingCount(message.getInt("playing"));
        holder.setServersCount(message.getInt("guilds"));
        holder.setUsersCount(message.getInt("users"));
        log.info("Statistics got updated " + holder);
        event.getWebsocket().broadcast(event.getRawMessage());
    }
}
