package co.groovybot.websocket.core.command;

import co.groovybot.websocket.commands.AuthorizationCommand;
import co.groovybot.websocket.commands.GetStatsCommand;
import co.groovybot.websocket.commands.HeartBeatCommand;
import co.groovybot.websocket.commands.PostStatsCommand;

public class CommandRegistry {

    public CommandRegistry(CommandManager commandManager) {
        commandManager.registerCommands(
                new AuthorizationCommand(),
                new GetStatsCommand(),
                new HeartBeatCommand(),
                new PostStatsCommand()
        );
    }
}
