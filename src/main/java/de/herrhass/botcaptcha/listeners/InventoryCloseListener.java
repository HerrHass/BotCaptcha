package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.captchas.CaptchaType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (event.getInventory().getTitle().equals("§aCaptcha ->")) {
            if (CaptchaSystems.getUserSession().containsKey(player) && CaptchaSystems.getCaptchaInventory().containsKey(player)) {
                CaptchaSystems.startCaptcha(player, CaptchaType.CLICKING);
            }

        }

    }

}
