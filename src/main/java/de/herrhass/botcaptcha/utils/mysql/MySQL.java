package de.herrhass.botcaptcha.utils.mysql;

import de.herrhass.botcaptcha.BotCaptcha;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQL {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static String host;
    private static String port;
    private static String database;
    private static String username;
    private static String password;
    private static Connection connection;

    public static void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            Bukkit.getConsoleSender().sendMessage("§a[MySQL] Connection opened!");

        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("§c[MySQL] Failed to open MySQL connection!");
        }

    }

    public static void disconnect() {
        if(isConnected()) {
            try {
                getConnection().close();
                Bukkit.getConsoleSender().sendMessage("§c[MySQL] Connection closed!");

            } catch (SQLException e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("§c[MySQL] Failed to close MySQL connection!");
            }

        }

    }

    public static void update(final String update) {
        CompletableFuture.runAsync( () -> {
            if (isConnected() && BotCaptcha.isMySQL() && BotCaptcha.isActivated()) {
                try (PreparedStatement preparedStatement = getConnection().prepareStatement(update);) {
                    preparedStatement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }, getExecutorService()).handle((aVoid, throwable) -> throwable);

    }

    public static void setRegistered(UUID uuid, String username) {
        MySQL.update("INSERT INTO `botcaptcha` (PLAYERNAME, UUID, BLOCKED) VALUES (" + '"' + username + '"' + "," + '"' + uuid.toString() + '"' + "," + '"' + "0" + '"' + ")");
    }

    public static void setBlocked(UUID uuid) {
        MySQL.update("UPDATE botcaptcha SET BLOCKED = 1 WHERE UUID = " + '"' + uuid.toString() + '"' + "");
    }

    public static void unregister(UUID uuid) {
        MySQL.update("DELETE FROM `botcaptcha` WHERE UUID = " + '"' + uuid.toString() + '"' + "");
    }

    public static void unblock(UUID uuid) {
        MySQL.update("UPDATE botcaptcha SET BLOCKED = 0 WHERE UUID = " + '"' + uuid.toString() + '"' + "");
    }

    public static void customInsert(String username, UUID uuid, int blocked) {
        MySQL.update("INSERT INTO `botcaptcha` (PLAYERNAME, UUID, BLOCKED) VALUES (" + '"' + username + '"' + "," + '"' + uuid.toString() + '"' + "," + '"' + blocked + '"' + ")");
    }

    public static void updateNameByUUID(UUID uuid, String updatedName) {
        MySQL.update("UPDATE botcaptcha SET PLAYERNAME = " + '"' + updatedName + '"' + " WHERE UUID = " + '"' + uuid.toString() + '"' + "");
    }

    private static CompletableFuture<Boolean> isRegisteredAsync(UUID uuid) {
        return CompletableFuture.supplyAsync( () -> {
            if (isConnected() && BotCaptcha.isActivated() && BotCaptcha.isMySQL()) {

                try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM botcaptcha WHERE UUID = " + '"' + uuid.toString() + '"' + "")){
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        return resultSet.next();

                    }

                } catch (SQLException e) {
                    e.printStackTrace();

                }

            }

            return false;
        }, getExecutorService()).handle((b, e) -> b != null ? b : false);

    }

    private static CompletableFuture<Boolean> isBlockedAsync(UUID uuid) {
        return CompletableFuture.supplyAsync( () -> {
            if (isConnected() && BotCaptcha.isActivated() && BotCaptcha.isMySQL()) {

                try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM botcaptcha WHERE UUID = " + '"' + uuid.toString() + '"' + "")) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getBoolean("BLOCKED");
                        }

                    }

                } catch (SQLException e) {
                    e.printStackTrace();

                }

            }

            return false;
        }, getExecutorService()).handle((b, e) -> b != null ? b : false);

    }

    private static CompletableFuture<String> getNameFromUUIDAsync(UUID uuid) {
        return CompletableFuture.supplyAsync( () -> {
            if (isConnected() && BotCaptcha.isActivated() && BotCaptcha.isMySQL()) {

                try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT PLAYERNAME FROM botcaptcha WHERE UUID = " + '"' + uuid.toString() + '"' + "")) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getString("PLAYERNAME");
                        }

                    }

                } catch (SQLException e) {
                    e.printStackTrace();

                }

            }

            return null;
        }, getExecutorService()).handle((s, e) -> s);

    }

    private static CompletableFuture<Integer> getEntriesNumberAsync() {
        return CompletableFuture.supplyAsync( () -> {
            if (isConnected() && BotCaptcha.isActivated() && BotCaptcha.isMySQL()) {

                try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM botcaptcha")) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        int count = 0;

                        while (resultSet.next()) {
                            ++count;
                        }

                        return count;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();

                }

            }

            return -1;
        }, getExecutorService()).handle((i, e) -> i != null ? i : -1);

    }

    private static CompletableFuture<Boolean> isInconsistentAsync() {
        return CompletableFuture.supplyAsync( () -> {
            if (isConnected() && BotCaptcha.isActivated() && BotCaptcha.isMySQL()) {

                try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM botcaptcha")){
                    try (ResultSet resultSet = preparedStatement.executeQuery()){
                        int count = 0;

                        while (resultSet.next()) {
                            if (BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players." + resultSet.getString("UUID")) == null) {
                                ++count;
                            }

                        }

                        return count > 0;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();

                }

            }

            return false;
        }, getExecutorService()).handle((b, throwable) -> b != null ? b : false);

    }

    public static boolean isInconsistent() {
        return isInconsistentAsync().join();
    }

    public static boolean isRegistered(UUID uuid) {
        return isRegisteredAsync(uuid).join();
    }

    public static boolean isBlocked(UUID uuid) {
        return isBlockedAsync(uuid).join();
    }

    public static String getNameFromUUID(UUID uuid) {
        return getNameFromUUIDAsync(uuid).join();
    }

    public static int getEntriesNumber() {
        return getEntriesNumberAsync().join();
    }

    public static boolean isConnected() {
        return (connection != null);
    }

    public static Connection getConnection() {
        return connection;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        MySQL.password = password;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        MySQL.username = username;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        MySQL.host = host;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        MySQL.port = port;
    }

    public static String getDatabase() {
        return database;
    }

    public static void setDatabase(String database) {
        MySQL.database = database;
    }

    public static ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

}
