package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (CaptchaSystems.getFinishProcess().containsKey(player)) {
            if (!CaptchaSystems.getFinishProcess().get(player)) {
                event.setCancelled(true);
            }

        }

    }

}
