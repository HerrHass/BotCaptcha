package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (BotCaptcha.isActivated()) {
            if (BotCaptcha.isPingCheck()) {
                if (!player.hasPermission(BotCaptcha.getBypassPermission())) {
                    if (MySQL.isBlocked(player.getUniqueId()) || ConfigAdapter.isBlocked(player.getUniqueId())) {
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, BotCaptcha.getPrefix() + "§cYou have been blocked\n §cbecause you are suspected of being a bot!\n" +
                                "§cYou aren't a bot? Appeal here: §a" + BotCaptcha.getWebsite());
                        return;
                    }

                    if (((CraftPlayer) player).getHandle().ping <= BotCaptcha.getMaxPing()) {
                        event.allow();

                    } else {
                        if (!player.hasPermission(BotCaptcha.getBypassPermission())) {
                            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                                    BotCaptcha.getPrefix() + "§cYou have been disallowed because your ping is too high!\n" +
                                            "§c" + ((CraftPlayer) player).getHandle().ping + "ms > §a" + BotCaptcha.getMaxPing());

                        }

                    }

                }

            }

        }

    }

}
