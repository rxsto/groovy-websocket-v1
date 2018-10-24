package io.groovybot.websocket.commands;

import io.groovybot.websocket.core.StatisticHolder;
import io.groovybot.websocket.core.command.Command;
import io.groovybot.websocket.core.command.CommandEvent;
import org.json.JSONObject;

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
        event.getWebsocket().broadcast(event.getRawMessage());
    }
}
