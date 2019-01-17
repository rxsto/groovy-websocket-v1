package co.groovybot.websocket.commands;

import co.groovybot.websocket.core.command.Command;
import co.groovybot.websocket.core.command.CommandEvent;
import co.groovybot.websocket.util.Helpers;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Log4j2
public class AuthorizationCommand extends Command {

    public AuthorizationCommand() {
        super("authorization", false);
    }

    @Override
    protected void run(JSONObject data, CommandEvent event) {
        String token = null;
        try (Connection connection = event.getWebsocket().getDatabase().getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM websocket");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                token = rs.getString("token");
        } catch (SQLException e) {
            log.error("Error while processing authentication!", e);
        }

        if (data.get("token").equals(token)) {
            event.getWebsocket().setBot(event.getInvoker());
            event.getTrustManager().trust(event.getInvoker());
            try (Connection connection = event.getWebsocket().getDatabase().getDataSource().getConnection()) {
                connection.prepareStatement("DELETE FROM websocket").execute();
                PreparedStatement resetToken = connection.prepareStatement("INSERT INTO websocket (token) VALUES (?)");
                resetToken.setString(1, Helpers.createToken());
                resetToken.execute();
            } catch (SQLException e) {
                log.error("Error while processing authentication!", e);
            }
            log.info("Successfully authorized from " + event.getInvoker().getRemoteSocketAddress() + "!");
        } else {
            event.getInvoker().send(parseMessage("error", new JSONObject().put("type", "forbidden").put("text", "The given token was invalid!")).toString());
        }
    }
}
