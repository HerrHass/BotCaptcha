package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        if (BotCaptcha.isActivated()) {
            BotCaptcha.getCompareValues().remove(player);

            if (CaptchaSystems.getCaptchaInventory().containsKey(player) || CaptchaSystems.getCaptchaWord().containsKey(player) || CaptchaSystems.getFinishProcess().containsKey(player)) {
                if (CaptchaSystems.getUserSession().containsKey(player)) {
                    CaptchaSystems.getUserSession().get(player).cancel();
                    CaptchaSystems.getUserSession().remove(player);
                }

                CaptchaSystems.getCaptchaInventory().remove(player);
                CaptchaSystems.getCaptchaWord().remove(player);
                CaptchaSystems.getFinishProcess().remove(player);

                Bukkit.getScheduler().runTaskLaterAsynchronously(BotCaptcha.getPlugin(), () -> {
                    if (BotCaptcha.getTries(player) == 3) {
                        if (BotCaptcha.isMySQL()) {
                            if (!MySQL.isRegistered(player.getUniqueId())) {
                                MySQL.setRegistered(player.getUniqueId(), player.getName());
                                MySQL.setBlocked(player.getUniqueId());
                                BotCaptcha.addMySQLBlockedBot();
                            }

                        }

                        if (BotCaptcha.isConfig()) {
                            if (!ConfigAdapter.isRegistered(player.getUniqueId())) {
                                ConfigAdapter.setRegistered(player.getUniqueId(), player.getName());
                                ConfigAdapter.setBlocked(player.getUniqueId());
                                BotCaptcha.addConfigBlockedBot();
                            }

                        }

                        BotCaptcha.getCaptchaTries().remove(player.getUniqueId());
                    }

                }, 20L);

            }

        }

    }

}