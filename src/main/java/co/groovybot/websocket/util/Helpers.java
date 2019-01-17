package co.groovybot.websocket.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

public class Helpers {

    public static JSONObject parseMessage(String client, String type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put("client", client);
        object.put("type", type);
        object.put("data", data);
        return object;
    }

    public static String createToken() {
        return RandomStringUtils.randomAlphanumeric(64);
    }


}
