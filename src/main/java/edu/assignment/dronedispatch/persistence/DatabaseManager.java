package edu.assignment.dronedispatch.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Owns the SQLite file and creates the tables used by independent repositories. */
public final class DatabaseManager {
    private final String jdbcUrl;

    public DatabaseManager(Path databasePath) {
        try {
            Path parent = databasePath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create database directory", e);
        }
        this.jdbcUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
        initializeSchema();
    }

    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    private void initializeSchema() {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS route_plans (id TEXT PRIMARY KEY, destination TEXT NOT NULL, distance_km REAL NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS drone_reservations (id TEXT PRIMARY KEY, workflow_id TEXT NOT NULL, payload_kg REAL NOT NULL, battery_percent INTEGER NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS delivery_records (id TEXT PRIMARY KEY, workflow_id TEXT NOT NULL, route_id TEXT NOT NULL, drone_reservation_id TEXT NOT NULL, landing_pad TEXT NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS dispatch_process (id TEXT PRIMARY KEY, destination TEXT NOT NULL, payload_kg REAL NOT NULL, status TEXT NOT NULL, route_id TEXT, drone_reservation_id TEXT, delivery_id TEXT, last_error TEXT, updated_at TEXT NOT NULL)"
        };
        try (Connection c = openConnection(); Statement s = c.createStatement()) {
            for (String sql : ddl) s.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot initialize SQLite schema", e);
        }
    }
}
