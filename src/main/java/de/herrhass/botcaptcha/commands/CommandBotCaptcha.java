package de.herrhass.botcaptcha.commands;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import de.herrhass.botcaptcha.utils.uuid.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandBotCaptcha implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(BotCaptcha.getPrefix() + BotCaptcha.getNoPlayer());
            return false;
        }
        Player player = (Player) commandSender;

        if (!player.hasPermission(BotCaptcha.getAdminPermission())) {
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + BotCaptcha.getNoPerms());
            return false;
        }

        if (args.length > 2) {
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + BotCaptcha.getWrongArgs());
            return false;
        }

        if (args.length == 0) {
            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Plugin by: §c" + BotCaptcha.getPlugin().getDescription().getAuthors().toString());
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Version: §c" + BotCaptcha.getPlugin().getDescription().getVersion());
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "MySQL Blocked bots: §c" + BotCaptcha.getMySQLBlockedBots());
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Config Blocked bots: §c" + BotCaptcha.getConfigBlockedBots());
            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());

        } else if (args.length == 1) {

            switch (args[0].toUpperCase()) {

                case "ENABLE":
                    if (BotCaptcha.isActivated()) {
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The functions are activated already!");

                    } else {
                        BotCaptcha.setActivated(true);

                        if (BotCaptcha.isMySQL()) {
                            MySQL.connect();
                        }

                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The functions were successfully activated!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "DISABLE":
                    if (!BotCaptcha.isActivated()) {
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The functions are deactivated already!");

                    } else {
                        BotCaptcha.setActivated(false);

                        if (BotCaptcha.isMySQL()) {
                            MySQL.disconnect();
                        }

                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The functions were successfully deactivated!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "CONFIG":
                    if (BotCaptcha.isConfig()) {
                        if (BotCaptcha.isMySQL()) {
                            BotCaptcha.setConfig(false);
                            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The config storing system was successfully deactivated!");

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cOne storing system must be activated!");
                            return false;
                        }

                    } else {
                        BotCaptcha.setConfig(true);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The config storing system was successfully activated!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "MYSQL":
                    if (BotCaptcha.isMySQL()) {
                        if (BotCaptcha.isConfig()) {
                            BotCaptcha.setMySQL(false);

                            MySQL.disconnect();

                            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "[MySQL] Connection closed!");
                            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The MySQL storing system was successfully deactivated");

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cOne storing system must be activated!");
                            return false;
                        }

                    } else {
                        BotCaptcha.setMySQL(true);

                        BotCaptcha.loadMySQLConfig();
                        MySQL.connect();

                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "[MySQL] Connection opened!");
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The MySQL storing system was successfully activated!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "BLOCKPROXY":
                    if (!BotCaptcha.isProxyBlocking()) {
                        BotCaptcha.setProxyBlock(true);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Proxy blocking was successfully enabled!");

                    } else {
                        BotCaptcha.setProxyBlock(false);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Proxy blocking was successfully disabled!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "PINGCAPTCHA":
                    if (!BotCaptcha.isPingCheck()) {
                        BotCaptcha.setCheckPing(true);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Ping checking was successfully enabled!");

                    } else {
                        BotCaptcha.setCheckPing(false);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Ping checking was successfully disabled!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "COMPAREVALUES":
                    if (!BotCaptcha.isCompareValues()) {
                        BotCaptcha.setCompareValues(true);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Compare values was successfully enabled!");

                    } else {
                        BotCaptcha.setCompareValues(false);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Compare values was successfully disabled!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "INVCAPTCHA":
                    if (!BotCaptcha.isInventoryCaptcha()) {
                        BotCaptcha.setInventoryCaptcha(true);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Inventory captcha was successfully enabled!");

                    } else {
                        BotCaptcha.setInventoryCaptcha(false);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Inventory captcha was successfully disabled!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "WRITECAPTCHA":
                    if (!BotCaptcha.isWritingCaptcha()) {
                        BotCaptcha.setWritingCaptcha(true);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Writing captcha was successfully enabled!");

                    } else {
                        BotCaptcha.setWritingCaptcha(false);
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Writing captcha was successfully disabled!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "HELP":
                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha | Information");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha help | All commands");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha enable | Enable all functions");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha disable | Disable all functions");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha mysql | Enable/Disable MySQL");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha config | Enable/Disable config");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha register <player> | Register player");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha unregister <player> | Unregister player");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha block <player> | Block player");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha unblock <player> | Unblock player");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha blockproxy | Enable/Disable proxy-check");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha pingcaptcha | Enable/Disable ping captcha!");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha invcaptcha | Enable/Disable inventory captcha");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha writecaptcha | Enable/Disable writing captcha");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha setping | Max ping allowed");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha settimelimit | Time limit for inv + writing captcha");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "/botcaptcha comparealues | Compare + copy database + config");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Tip: you can activate both the MySQL and config storing system");
                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                    break;

                default:
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + BotCaptcha.getWrongArgs());
                    break;

            }

        } else {
            if (!BotCaptcha.isActivated()) {
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cBotCaptcha's functions are disabled! Enable them with /botcaptcha enable!");
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "Tip: only major configuration tasks can be executed when BotCaptcha's main functions are disabled!");
                return false;
            }
            UUID uuid = UUIDFetcher.getUUID(args[1], player);

            switch (args[0].toUpperCase()) {

                case "REGISTER":
                    if (uuid != null) {
                        OfflinePlayer finalTarget = Bukkit.getOfflinePlayer(UUIDFetcher.getUUID(args[1], player));

                        if (BotCaptcha.isMySQL()) {
                            if (MySQL.isRegistered(finalTarget.getUniqueId())) {
                                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The player was already registered!");

                            } else {
                                if (!CaptchaSystems.getUserSession().containsKey(player) && !CaptchaSystems.getCaptchaInventory().containsKey(player) && !CaptchaSystems.getCaptchaWord().containsKey(player)) {
                                    MySQL.setRegistered(finalTarget.getUniqueId(), args[1]);
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[MySQL] The player was successfully registered!");

                                } else {
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The player gets verified in this moment! Please wait until the verification is over!");
                                }

                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The MySQL storing system is currently deactivated!");
                        }

                        if (BotCaptcha.isConfig()) {
                            if (ConfigAdapter.isRegistered(finalTarget.getUniqueId())) {
                                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The player was already registered!");

                            } else {
                                if (!CaptchaSystems.getUserSession().containsKey(player) && !CaptchaSystems.getCaptchaInventory().containsKey(player) && !CaptchaSystems.getCaptchaWord().containsKey(player)) {
                                    ConfigAdapter.setRegistered(finalTarget.getUniqueId(), args[1]);
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[Config] The player was successfully registered!");

                                } else {
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The player gets verified in this moment! Please wait until the verification is over!");
                                }

                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The config storing system is currently deactivated!");
                        }

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The given user doesn't exist!");
                    }
                    break;

                case "UNREGISTER":
                    if (uuid != null) {
                        OfflinePlayer finalTarget = Bukkit.getOfflinePlayer(UUIDFetcher.getUUID(args[1], player));

                        if (BotCaptcha.isMySQL()) {
                            if (!MySQL.isRegistered(finalTarget.getUniqueId())) {
                                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The player was already unregistered!");

                            } else {
                                MySQL.unregister(finalTarget.getUniqueId());
                                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[MySQL] The player was successfully unregistered!");
                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The MySQL storing system is currently deactivated!");
                        }

                        if (BotCaptcha.isConfig()) {
                            if (!ConfigAdapter.isRegistered(finalTarget.getUniqueId())) {
                                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The player was already unregistered!");

                            } else {
                                ConfigAdapter.unregister(finalTarget.getUniqueId());
                                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[Config] The player was successfully unregistered!");
                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The config storing system is currently deactivated!");
                        }

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The given user doesn't exist!");
                    }
                    break;

                case "BLOCK":
                    if (uuid != null) {
                        OfflinePlayer finalTarget = Bukkit.getOfflinePlayer(UUIDFetcher.getUUID(args[1], player));

                        if (BotCaptcha.isMySQL()) {
                            if (finalTarget != null) {
                                if (MySQL.isRegistered(finalTarget.getUniqueId())) {
                                    if (MySQL.isBlocked(finalTarget.getUniqueId())) {
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The player was already blocked!");

                                    } else {
                                        MySQL.setBlocked(finalTarget.getUniqueId());
                                        BotCaptcha.addMySQLBlockedBot();
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[MySQL] The player was successfully blocked!");
                                    }

                                } else {
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cThe given player isn't registered!");
                                }

                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The MySQL storing system is currently deactivated!");
                        }

                        if (BotCaptcha.isConfig()) {
                            if (finalTarget != null) {
                                if (ConfigAdapter.isRegistered(finalTarget.getUniqueId())) {
                                    if (ConfigAdapter.isBlocked(finalTarget.getUniqueId())) {
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The player was already blocked!");

                                    } else {
                                        ConfigAdapter.setBlocked(finalTarget.getUniqueId());
                                        BotCaptcha.addConfigBlockedBot();
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[Config] The player was successfully blocked!");
                                    }

                                } else {
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cThe given player isn't registered!");
                                }

                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The config storing system is currently deactivated!");
                        }

                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null && target.isOnline() && !target.isOp()) {
                            if (MySQL.isBlocked(target.getUniqueId()) || ConfigAdapter.isBlocked(target.getUniqueId())) {
                                Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), () -> target.kickPlayer(BotCaptcha.getPrefix() + "§cYou have been blocked\n §cbecause you are suspected of being a bot!\n" +
                                        "§cYou aren't a bot? Appeal here: §a" + BotCaptcha.getWebsite()));
                            }

                        }

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The given user doesn't exist!");
                    }
                    break;

                case "UNBLOCK":
                    if (uuid != null) {
                        OfflinePlayer finalTarget = Bukkit.getOfflinePlayer(UUIDFetcher.getUUID(args[1], player));

                        if (BotCaptcha.isMySQL()) {
                            if (finalTarget != null) {
                                if (MySQL.isRegistered(finalTarget.getUniqueId())) {
                                    if (!MySQL.isBlocked(finalTarget.getUniqueId())) {
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The player was already unblocked!");

                                    } else {
                                        MySQL.unblock(finalTarget.getUniqueId());
                                        BotCaptcha.removeMySQLBlockedBot();
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[MySQL] The player was successfully unblocked!");
                                    }

                                } else {
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cThe given player isn't registered!");
                                }

                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[MySQL] The MySQL storing system is currently deactivated!");
                        }

                        if (BotCaptcha.isConfig()) {
                            if (finalTarget != null) {
                                if (ConfigAdapter.isRegistered(finalTarget.getUniqueId())) {
                                    if (!ConfigAdapter.isBlocked(finalTarget.getUniqueId())) {
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The player was already unblocked!");

                                    } else {
                                        ConfigAdapter.unblock(finalTarget.getUniqueId());
                                        BotCaptcha.removeConfigBlockedBot();
                                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[Config] The player was successfully unblocked!");
                                    }

                                } else {
                                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§cThe given player isn't registered!");
                                }

                            }

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "§c[Config] The config storing system is currently deactivated!");
                        }

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The given user doesn't exist!");
                    }
                    break;

                case "SETPING":
                    int ping = Integer.parseInt(args[1]);

                    if (BotCaptcha.isPingCheck()) {
                        if (ping <= 1000 && ping >= 20) {
                            BotCaptcha.setMaxPing(ping);
                            BotCaptcha.saveBaseConfig();
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The max ping was successfully set to §c" + args[1] + "ms§a!");

                        } else {
                            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The max ping needs to be smaller than §a1000ms §cand bigger or equal to §a20ms§a!");
                        }

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "You need to activate check ping first (/botcaptcha help)!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                case "SETTIMELIMIT":
                    int timeLimit = Integer.parseInt(args[1]);

                    if (timeLimit < 60 && timeLimit > 10) {
                        BotCaptcha.setTimeLimit(timeLimit);
                        BotCaptcha.saveBaseConfig();
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The time limit was successfully set to §c" + args[1] + " seconds§a!");

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The time limit needs to be shorter or equal to §a60 seconds §cand longer or equal to §a10 seconds§c!");
                    }

                    BotCaptcha.saveBaseConfig();
                    break;

                default:
                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + BotCaptcha.getWrongArgs());
                    break;

            }

        }

        return false;
    }

}
