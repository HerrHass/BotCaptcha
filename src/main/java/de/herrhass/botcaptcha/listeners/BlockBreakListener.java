package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (BotCaptcha.isActivated()) {
            if ((BotCaptcha.isMySQL() && !MySQL.isRegistered(player.getUniqueId())) || (BotCaptcha.isConfig() && !ConfigAdapter.isRegistered(player.getUniqueId()))) {
                event.setCancelled(true);
            }

            event.setCancelled(false);
        }

    }

}
