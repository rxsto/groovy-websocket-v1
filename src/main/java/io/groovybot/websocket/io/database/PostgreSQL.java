package io.groovybot.websocket.io.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import io.groovybot.websocket.core.Websocket;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class PostgreSQL implements Closeable {

    @Getter
    private HikariDataSource dataSource;

    public PostgreSQL() {
        log.info("[Database] Connecting ...");
        JSONObject configuration = Websocket.getWebsocket().getConfig().getJSONObject("db");
        HikariConfig hikariConfig = new HikariConfig();
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.error("[Database] Error while connecting to database!", e);
        }
        hikariConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", configuration.getString("host"), configuration.getInt("port"), configuration.getString("database")));
        hikariConfig.setUsername(configuration.getString("username"));
        hikariConfig.setPassword(configuration.getString("password"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("serverTimezone", "Europe/Berlin");
        hikariConfig.addDataSourceProperty("useSSL", "false");

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            log.error("[Database] Error while connecting to database!", e);
        }

        if (dataSource != null)
            log.info("[Database] Connected!");
    }

    @Override
    public void close() {
        dataSource.close();
    }

}
