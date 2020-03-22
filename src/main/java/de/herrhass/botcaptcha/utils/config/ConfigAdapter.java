package de.herrhass.botcaptcha.utils.config;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigAdapter {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static void setRegistered(UUID uuid, String name) {
        CompletableFuture.runAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                    BotCaptcha.getConfigAdapterYaml().set("players." + uuid.toString() + ".name", name);
                    BotCaptcha.getConfigAdapterYaml().set("players." + uuid.toString() + ".blocked", false);
                    BotCaptcha.saveConfigAdapter();

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                }

            }

        }, getExecutorService()).handle((aVoid, throwable) -> throwable);

    }

    public static void unregister(UUID uuid) {
        CompletableFuture.runAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                    BotCaptcha.getConfigAdapterYaml().set("players." + uuid.toString(), null);
                    BotCaptcha.saveConfigAdapter();

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                }

            }

        }, getExecutorService()).handle((aVoid, throwable) -> throwable);

    }

    public static void setBlocked(UUID uuid) {
        CompletableFuture.runAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                    BotCaptcha.getConfigAdapterYaml().set("players." + uuid.toString() + ".blocked", true);
                    BotCaptcha.saveConfigAdapter();

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                }

            } else {
                Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config storing system isn't activated!");
            }

        }, getExecutorService()).handle((aVoid, throwable) -> throwable);

    }

    public static void unblock(UUID uuid) {
        CompletableFuture.runAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                    BotCaptcha.getConfigAdapterYaml().set("players." + uuid.toString() + ".blocked", false);
                    BotCaptcha.saveConfigAdapter();

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                }

            }

        }, getExecutorService()).handle((aVoid, throwable) -> throwable);

    }

    public static void updateNameByUUID(UUID uuid, String updatedName) {
        CompletableFuture.runAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                    if (BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players." + uuid) != null) {
                        BotCaptcha.getConfigAdapterYaml().set("players." + uuid + ".name", updatedName);
                        BotCaptcha.saveConfigAdapter();
                    }

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                }

            }

        }, getExecutorService()).handle((aVoid, throwable) -> aVoid);

    }

    private static CompletableFuture<Boolean> isRegisteredAsync(UUID uuid) {
        return CompletableFuture.supplyAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (uuid != null) {
                    if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                        try {
                            return BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players." + uuid.toString()) != null;

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                    }

                } else {
                    throw new NullPointerException();
                }

            }

            return false;
        }, getExecutorService()).handle((b, e) -> b != null ? b : false);

    }

    private static CompletableFuture<Boolean> isBlockedAsync(UUID uuid) {
        return CompletableFuture.supplyAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (uuid != null) {
                    if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                        return BotCaptcha.getConfigAdapterYaml().getBoolean("players." + uuid.toString() + ".blocked");

                    } else {
                        Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                    }

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "[Config] The given player doesn't exist!");
                }

            }

            return false;
        }, getExecutorService()).handle((b, e) -> b != null ? b : false);

    }

    private static CompletableFuture<String> getNameFromUUIDAsync(UUID uuid) {
        return CompletableFuture.supplyAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig()) {
                if (Objects.requireNonNull(BotCaptcha.getConfigAdapter()).exists()) {
                    if (BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players." + uuid) != null) {
                        return BotCaptcha.getConfigAdapterYaml().get("players." + uuid + ".name");
                    }

                } else {
                    Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + "The config file doesn't exist!");
                }

            }

            return null;
        }, getExecutorService()).handle((s, e) -> s.toString());

    }

    private static CompletableFuture<Boolean> isInconsistentAsync() {
        return CompletableFuture.supplyAsync( () -> {
            if (BotCaptcha.isActivated() && BotCaptcha.isConfig() && BotCaptcha.getConfigAdapter().exists()) {
                try (PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("SELECT * FROM botcaptcha WHERE UUID = ?")) {
                    int count = 0;

                    for (String s : BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players").getKeys(false)) {
                        preparedStatement.setString(1, s);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (!resultSet.next()) {
                                ++count;
                            }

                        }

                    }
                    return count > 0;

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            return false;
        }, getExecutorService()).handle((b, throwable) -> b != null ? b : false);

    }

    private static ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    public static String getNameFromUUID(UUID uuid) {
        return getNameFromUUIDAsync(uuid).join();
    }

    public static boolean isBlocked(UUID uuid) {
        return isBlockedAsync(uuid).join();
    }

    public static boolean isRegistered(UUID uuid) {
        return isRegisteredAsync(uuid).join();
    }

    public static boolean isInconsistent() {
        return isInconsistentAsync().join();
    }

}
