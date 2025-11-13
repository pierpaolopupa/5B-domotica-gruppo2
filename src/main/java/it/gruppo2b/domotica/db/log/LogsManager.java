package it.gruppo2b.domotica.db.log;

import it.gruppo2b.domotica.db.DatabasePool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class LogsManager {

    private static final Logger log = LogManager.getLogger(LogsManager.class);

    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS logs (
            id SERIAL PRIMARY KEY,
            client_id TEXT NOT NULL,
            nome TEXT,
            tipo TEXT NOT NULL,
            valore TEXT NOT NULL,
            data DATE NOT NULL,
            ora TIME NOT NULL
        );
    """;

    private static final String INSERT_LOG_SQL = """
        INSERT INTO logs (client_id, nome, tipo, valore, data, ora)
        VALUES (?, ?, ?, ?, ?, ?);
    """;

    public LogsManager() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE_SQL)) {

            stmt.execute();
            log.info("Tabella 'logs' creata");

        } catch (SQLException e) {
            log.error("Errore nella creazione della tabella logs", e);
        }
    }

    public void salvaLog(String clientId, String nome, String tipo, String valore) {
        LocalDate data = LocalDate.now();
        LocalTime ora = LocalTime.now();

        try (Connection conn = DatabasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LOG_SQL)) {

            stmt.setString(1, clientId);
            stmt.setString(2, nome);
            stmt.setString(3, tipo);
            stmt.setString(4, valore);
            stmt.setDate(5, java.sql.Date.valueOf(data));
            stmt.setTime(6, java.sql.Time.valueOf(ora));

            stmt.executeUpdate();
            log.debug(" Log creato: {} - {} ({}) = {}", clientId, nome, tipo, valore);

        } catch (SQLException e) {
            log.error("Errore nel salvataggio del log", e);
        }
    }
}
