package io.groovybot.websocket.core.command;

import io.groovybot.websocket.commands.AuthorizationCommand;
import io.groovybot.websocket.commands.GetStatsCommand;
import io.groovybot.websocket.commands.HeartBeatCommand;
import io.groovybot.websocket.commands.PostStatsCommand;

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
