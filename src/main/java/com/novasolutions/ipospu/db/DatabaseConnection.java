package com.novasolutions.ipospu.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central database connection factory for Aiven MySQL.
 * <p>
 * Three databases are in use:
 *   ipos_pu — PU-owned tables (members, commercial_applications, etc.)
 *   ipos_ca — CA-owned tables (product catalogue, sales, payments)
 *   ipos_sa — SA-owned tables (system administration)
 * <p>
 * Credentials are loaded from resources/db.properties. Never hardcode secrets.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private final String puUrl;
    private final String puUsername;
    private final String puPassword;

    private final String caUrl;
    private final String caUsername;
    private final String caPassword;

    private final String saUrl;
    private final String saUsername;
    private final String saPassword;

    private DatabaseConnection() {
        try {
            Properties props = new Properties();

            InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("db.properties");

            if (input == null) {
                throw new RuntimeException("db.properties not found in resources");
            }

            props.load(input);

            this.puUrl      = props.getProperty("pu.url");
            this.puUsername = props.getProperty("pu.username");
            this.puPassword = props.getProperty("pu.password");

            this.caUrl      = props.getProperty("ca.url");
            this.caUsername = props.getProperty("ca.username");
            this.caPassword = props.getProperty("ca.password");

            this.saUrl      = props.getProperty("sa.url");
            this.saUsername = props.getProperty("sa.username");
            this.saPassword = props.getProperty("sa.password");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config from db.properties", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Connection to ipos_pu — use for PU-owned tables. */
    public Connection getPuConnection() throws SQLException {
        return DriverManager.getConnection(puUrl, puUsername, puPassword);
    }

    /** Connection to ipos_ca — use for product catalogue, sales, and payment tables. */
    public Connection getCaConnection() throws SQLException {
        return DriverManager.getConnection(caUrl, caUsername, caPassword);
    }

    /** Connection to ipos_sa — use for system administration tables. */
    public Connection getSaConnection() throws SQLException {
        return DriverManager.getConnection(saUrl, saUsername, saPassword);
    }
}
