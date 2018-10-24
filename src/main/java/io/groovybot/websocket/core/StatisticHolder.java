package io.groovybot.websocket.core;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StatisticHolder {

    private int playingCount = 0;
    private int serversCount = 0;
    private int usersCount = 0;

}
