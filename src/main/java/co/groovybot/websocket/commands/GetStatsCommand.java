package co.groovybot.websocket.commands;

import co.groovybot.websocket.core.StatisticHolder;
import co.groovybot.websocket.core.command.Command;
import co.groovybot.websocket.core.command.CommandEvent;
import org.json.JSONObject;

public class GetStatsCommand extends Command {

    public GetStatsCommand() {
        super("getstats", false);
    }

    @Override
    protected void run(JSONObject message, CommandEvent event) {
        StatisticHolder statisticHolder = event.getWebsocket().getStatisticHolder();
        event.getInvoker().send(parseMessage("poststats",
                new JSONObject()
                        .put("playing", statisticHolder.getPlayingCount())
                        .put("guilds", statisticHolder.getServersCount())
                        .put("users", statisticHolder.getUsersCount()
                        ))
                .toString());
    }
}
