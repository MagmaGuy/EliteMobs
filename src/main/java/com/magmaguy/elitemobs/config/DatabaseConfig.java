package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class DatabaseConfig extends ConfigurationFile {
    @Getter
    public static boolean useMySQL;
    @Getter
    public static String mysqlHost;
    @Getter
    public static String mysqlPort;
    @Getter
    public static String mysqlDatabaseName;
    @Getter
    public static String mysqlUsername;
    @Getter
    public static String mysqlPassword;
    @Getter
    public static boolean useSSL;

    public DatabaseConfig() {
        super("Database.yml");
    }

    @Override
    public void initializeValues() {
        useMySQL = ConfigurationEngine.setBoolean(List.of("Sets whether MySQL will be used. By default EliteMobs uses SQLite. If you wish to use MySQL you will need a valid MySQL configuration."), fileConfiguration, "useMySQL", false);
        mysqlHost = ConfigurationEngine.setString(List.of("The host of your MySQL database"), file, fileConfiguration, "mysqlHost", "localhost", false);
        mysqlPort = ConfigurationEngine.setString(List.of("The port of your MySQL database"), file, fileConfiguration, "mysqlPort", "3306", false);
        mysqlDatabaseName = ConfigurationEngine.setString(List.of("The name of the database"), file, fileConfiguration, "mysqlDatabaseName", "elitemobs", false);
        mysqlUsername = ConfigurationEngine.setString(List.of("The username for MySQl"), file, fileConfiguration, "mysqlUsername", "your_username_mysql_here", false);
        mysqlPassword = ConfigurationEngine.setString(List.of("The password for your MysSQL database"), file, fileConfiguration, "mysqlPassword", "your_mysql_password_here", false);
        useSSL = ConfigurationEngine.setBoolean(List.of("Whether to use SSL"), fileConfiguration, "useSSL", true);
    }
}
