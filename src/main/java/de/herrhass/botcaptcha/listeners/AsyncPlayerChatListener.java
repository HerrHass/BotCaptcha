package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AsyncPlayerChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (CaptchaSystems.getCaptchaWord().containsKey(player)) {
            if (!event.getMessage().equals(CaptchaSystems.getCaptchaWord().get(player))) {
                event.setCancelled(true);

                if (CaptchaSystems.getUserSession().containsKey(player)) {
                    CaptchaSystems.getUserSession().get(player).cancel();
                    CaptchaSystems.getUserSession().remove(player);

                } else {
                    return;
                }
                BotCaptcha.addTry(player);

                if (BotCaptcha.getTries(player) == 3) {
                    if (BotCaptcha.isMySQL()) {
                        MySQL.setRegistered(player.getUniqueId(), player.getName());
                        MySQL.setBlocked(player.getUniqueId());
                        BotCaptcha.addMySQLBlockedBot();
                    }

                    if (BotCaptcha.isConfig()) {
                        ConfigAdapter.setRegistered(player.getUniqueId(), player.getName());
                        ConfigAdapter.setBlocked(player.getUniqueId());
                        BotCaptcha.addConfigBlockedBot();
                    }

                    Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), () -> player.kickPlayer(BotCaptcha.getPrefix() + "§cYou have been blocked\n §cbecause you are suspected of being a bot!\n" +
                            "§cYou aren't a bot? Appeal here: §a" + BotCaptcha.getWebsite()));

                    BotCaptcha.getCaptchaTries().remove(player.getUniqueId());

                } else {
                    final String givenWord = CaptchaSystems.getCaptchaWord().get(player);

                    Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), () ->
                            player.kickPlayer(BotCaptcha.getPrefix() + "§cYour written word didn't match the pattern!\n§c" + event.getMessage() + " §a!= §c" + givenWord + "\n§cTry added!"));
                }
                CaptchaSystems.getCaptchaWord().remove(player);

            } else {
                event.setCancelled(true);
                BotCaptcha.finishProcess(player);
            }

        } else {
            if (BotCaptcha.isActivated()) {
                if (BotCaptcha.isMySQL() && MySQL.isConnected()) {
                    if (!MySQL.isRegistered(player.getUniqueId())) {
                        event.setCancelled(true);
                        return;
                    }

                }

                if (BotCaptcha.isConfig()) {
                    if (!ConfigAdapter.isRegistered(player.getUniqueId())) {
                        event.setCancelled(true);
                        return;
                    }

                }

                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (CaptchaSystems.getUserSession().containsKey(all) || CaptchaSystems.getCaptchaInventory().containsKey(all) || CaptchaSystems.getCaptchaWord().containsKey(all)) {
                        event.getRecipients().remove(all);

                    } else {
                        event.getRecipients().add(player);
                    }

                }

            }

        }

        if (BotCaptcha.getCompareValues().containsKey(player)) {
            if (BotCaptcha.getCompareValues().get(player)) {
                final String message = event.getMessage().toUpperCase();
                event.setCancelled(true);

                switch (message) {

                    case "CONFIRM":
                        if (ConfigAdapter.isInconsistent()) {
                            CompletableFuture.runAsync( () -> {
                                try (PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("SELECT * FROM botcaptcha WHERE 1")) {
                                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                        while (resultSet.next()) {
                                            String UUID = resultSet.getString("UUID");

                                            if (BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players." + UUID) == null) {
                                                String playerName = resultSet.getString("PLAYERNAME");
                                                boolean blocked   = resultSet.getBoolean("BLOCKED");

                                                BotCaptcha.getConfigAdapterYaml().set("players." + UUID + ".name", playerName);
                                                BotCaptcha.getConfigAdapterYaml().set("players." + UUID + ".blocked", blocked);
                                                BotCaptcha.saveConfigAdapter();

                                                BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Player " + playerName + " successfully transferred!");
                                            }

                                        }

                                    }

                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                            }, Executors.newCachedThreadPool())
                                    .handle((aVoid, throwable) -> throwable)
                                    .whenComplete((throwable, throwable2) -> {

                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Transmission of data from the database\n" + BotCaptcha.getPrefix() + "into the config file was successful!");
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The check and the transmission of the data from\n" + BotCaptcha.getPrefix() + "the config file into the database starts!");
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                            });

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "No data from the database needs to be copied into the config!");
                            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Process finished! Thank you for your patience!");
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                        }

                        if (MySQL.isInconsistent()) {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(BotCaptcha.getPlugin(), () -> CompletableFuture.runAsync( () -> {
                                try (PreparedStatement preparedStatement = MySQL.getConnection().prepareStatement("SELECT * FROM botcaptcha WHERE UUID = ?")) {
                                    for (String s : BotCaptcha.getConfigAdapterYaml().getConfigurationSection("players").getKeys(false)) {
                                        preparedStatement.setString(1, s);

                                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                            if (!resultSet.next()) {
                                                String playerName = BotCaptcha.getConfigAdapterYaml().getString("players." + s + ".name");
                                                int blocked;

                                                if (!BotCaptcha.getConfigAdapterYaml().getBoolean("players." + s + ".blocked")) {
                                                    blocked = 0;

                                                } else {
                                                    blocked = 1;
                                                }

                                                MySQL.customInsert(playerName, UUID.fromString(s), blocked);
                                                BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Player " + playerName + " successfully transferred!");
                                            }

                                        }

                                    }

                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                            }, Executors.newCachedThreadPool())
                                    .handle((aVoid, throwable) -> throwable)
                                    .whenComplete((throwable, throwable2) -> {

                                        BotCaptcha.getCompareValues().remove(player);
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Transmission of data from the config file\n" + BotCaptcha.getPrefix() + "into the database was successful!");
                                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Process finished! Thank you for your patience!");
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                            }), 50);

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "No data from the config needs to be copied into the database!");
                            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Process finished! Thank you for your patience!");
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                        }

                        break;

                    case "END":
                        player.sendMessage(BotCaptcha.getPrefix() + "The data will not be compared and copied!");
                        BotCaptcha.getCompareValues().remove(player);
                        break;

                    default:
                        player.sendMessage(BotCaptcha.getPrefix() + "Type confirm | end");
                        break;

                }

            }

        }

    }

}
