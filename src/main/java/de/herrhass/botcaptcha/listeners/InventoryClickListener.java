package de.herrhass.botcaptcha.listeners;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.captchas.CaptchaType;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) return;

        if (event.getCurrentItem().getType() == Material.AIR) return;


        if (event.getClickedInventory().getTitle().equals("§aCaptcha ->") && CaptchaSystems.getCaptchaInventory().containsKey(player)) {
            event.setCancelled(true);

            if (event.getCurrentItem().hasItemMeta()) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§aCaptcha")) {
                    CaptchaSystems.getCaptchaInventory().remove(player);
                    CaptchaSystems.getUserSession().get(player).cancel();
                    CaptchaSystems.getUserSession().remove(player);

                    BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Inventory captcha passed!");
                    player.closeInventory();

                    if (BotCaptcha.isWritingCaptcha()) {
                        CaptchaSystems.startCaptcha(player, CaptchaType.WRITING);

                    } else {
                        BotCaptcha.finishProcess(player);
                    }

                } else {
                    CaptchaSystems.getCaptchaInventory().remove(player);
                    CaptchaSystems.getUserSession().get(player).cancel();
                    CaptchaSystems.getUserSession().remove(player);

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
                        Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), () -> player.kickPlayer(BotCaptcha.getPrefix() + "§cYou selected the wrong glass pane!\n§cTry added!\n§cGood luck next time!"));
                    }

                }

            }

        }

    }

}
