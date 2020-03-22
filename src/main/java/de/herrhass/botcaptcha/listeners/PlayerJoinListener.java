package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.captchas.CaptchaType;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import de.herrhass.botcaptcha.utils.proxy.ProxyChecker;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        if (BotCaptcha.isActivated()) {
            if (!BotCaptcha.getCaptchaTries().containsKey(player.getUniqueId())) {
                BotCaptcha.getCaptchaTries().put(player.getUniqueId(), new AtomicInteger(0));
            }

            if (!player.hasPermission(BotCaptcha.getAdminPermission())) {
                if (BotCaptcha.isProxyBlocking()) {
                    if (ProxyChecker.isVPN(player.getAddress().getHostName())) {
                        BotCaptcha.addTry(player);
                        player.kickPlayer(BotCaptcha.getPrefix() + "§cYou have been kicked for using a VPN!\n§cPlease deactivate your VPN and rejoin!\n§cTry added!");

                    } else {
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "Ping checked!");
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "No VPN detected!");
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                    }

                }

            } else {
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "Simple ping check ignored (VIP)!");
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "VPN Captcha ignored (VIP)!");
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
            }

            BotCaptcha.sendTitleToPlayer(
                    player,
                    IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + BotCaptcha.getPrefix() + "\"}"),
                    IChatBaseComponent.ChatSerializer.a("{\"text\":\"§aProtects this server!\"}")
                    , 40, 20, 20);

            if (BotCaptcha.isMySQL() || BotCaptcha.isConfig()) {
                if (BotCaptcha.isConfig() && ConfigAdapter.isRegistered(player.getUniqueId())) {
                    if (!ConfigAdapter.getNameFromUUID(player.getUniqueId()).equals(player.getName())) {
                        ConfigAdapter.updateNameByUUID(player.getUniqueId(), player.getName());
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[Config] Your new name was copied into our config!");
                    }

                }

                if (BotCaptcha.isMySQL() && MySQL.isRegistered(player.getUniqueId())) {
                    if (!MySQL.getNameFromUUID(player.getUniqueId()).equals(player.getName())) {
                        MySQL.updateNameByUUID(player.getUniqueId(), player.getName());
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "[MySQL] Your new name was copied into our database!");
                    }

                }

                if (MySQL.isRegistered(player.getUniqueId()) || ConfigAdapter.isRegistered(player.getUniqueId())) {
                    if (player.hasPermission(BotCaptcha.getAdminPermission())) {
                        compareValues(player);
                        return;
                    }

                }

                if ((BotCaptcha.isMySQL() && !MySQL.isRegistered(player.getUniqueId())) || (BotCaptcha.isConfig() && !ConfigAdapter.isRegistered(player.getUniqueId()))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 99999));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999, 99999));

                    BotCaptcha.sendTitleToPlayer(
                            player,
                            IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + BotCaptcha.getPrefix() + "\"}"),
                            IChatBaseComponent.ChatSerializer.a("{\"text\":\"§aVerification process starts now!\"}")
                            , 40, 20, 20);

                    Bukkit.getScheduler().runTaskLaterAsynchronously(BotCaptcha.getPlugin(), () -> {
                        if (BotCaptcha.isPingCheck()) {
                            CaptchaSystems.startCaptcha(player, CaptchaType.PING);

                        } else if (BotCaptcha.isInventoryCaptcha()) {
                            CaptchaSystems.startCaptcha(player, CaptchaType.CLICKING);

                        } else if (BotCaptcha.isWritingCaptcha()) {
                            CaptchaSystems.startCaptcha(player, CaptchaType.WRITING);

                        } else {
                            BotCaptcha.finishProcess(player);
                        }

                    }, 60L);

                }

            }

        }

    }

    private void compareValues(Player player) {
        if (BotCaptcha.isCompareValues()) {
            if (BotCaptcha.isMySQL() && BotCaptcha.isConfig()) {
                if (BotCaptcha.getCompareValues().size() < 1) {
                    if (!MySQL.isInconsistent()) {
                        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "There were no inconsistencies found!");
                        return;
                    }
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "§cThere were inconsistencies found!");
                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Do you want to compare the data and copy it?");
                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Confirm with confirm or end the process with end!");

                    BotCaptcha.getCompareValues().put(player, true);

                } else {
                    BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "Somebody else currently checks the data!");
                }

            } else {
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "Both the MySQL and the Config storing system\n" + BotCaptcha.getPrefix() + "need to be activated to compare and copy!");
            }

        } else {
            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "The compare values function is currently deactivated!");
            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix() + "Activate it with /botcaptcha comparevalues!");
        }

    }

}
