package it.gruppo2b.domotica.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabasePool {

    private static final Logger log = LogManager.getLogger(DatabasePool.class);
    private static HikariDataSource ds;

    private static final String DB_NAME = "serenya_db";
    private static final String DB_USER = "";
    private static final String DB_PASS = "";
    private static final String HOST = "localhost";
    private static final int PORT = 5432;

    public static void init() {
        if (ds != null) return;

        ensureDatabaseExists();

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME);
        cfg.setUsername(DB_USER);
        cfg.setPassword(DB_PASS);
        cfg.setMaximumPoolSize(10);
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(cfg);
        log.info("DatabasePool inizializzato su " + DB_NAME);

        createTablesIfNotExist();
    }

    private static void ensureDatabaseExists() {
        String url = "jdbc:postgresql://" + HOST + ":" + PORT + "/postgres";
        try (Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'";
            var rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
                log.info("Creato database: " + DB_NAME);
            } else {
                log.info("Database già esistente: " + DB_NAME);
            }
        } catch (SQLException e) {
            log.error("Errore nella creazione/verifica del database", e);
        }
    }

    private static void createTablesIfNotExist() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS measurements (
                    id SERIAL PRIMARY KEY,
                    client_id TEXT,
                    payload JSONB,
                    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
                );
            """;
            stmt.executeUpdate(createTableSQL);
            log.info("Tabella 'measurements' pronta all'uso");
        } catch (SQLException e) {
            log.error("Errore nella creazione della tabella measurements", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null)
            throw new IllegalStateException("DatabasePool non inizializzato");
        return ds.getConnection();
    }

    public static void close() {
        if (ds != null) {
            ds.close();
            log.info("DatabasePool chiuso");
        }
    }

}
