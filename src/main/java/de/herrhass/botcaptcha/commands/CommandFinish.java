package de.herrhass.botcaptcha.commands;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFinish implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (BotCaptcha.isActivated()) {
                if (BotCaptcha.isMySQL()) {
                    if (MySQL.isRegistered(player.getUniqueId())) {
                        BotCaptcha.sendMessageToPlayer(player, "§fUnknown command. Type \"/help\" for help.");
                        return false;
                    }

                } else if (BotCaptcha.isConfig()) {
                    if (ConfigAdapter.isRegistered(player.getUniqueId())) {
                        BotCaptcha.sendMessageToPlayer(player, "§fUnknown command. Type \"/help\" for help.");
                        return false;
                    }

                }

                if (BotCaptcha.isConfig()) {
                    if (!ConfigAdapter.isRegistered(player.getUniqueId())) {
                        ConfigAdapter.setRegistered(player.getUniqueId(), player.getName());
                    }

                }

                if (BotCaptcha.isMySQL()) {
                    if (!MySQL.isRegistered(player.getUniqueId())) {
                        MySQL.setRegistered(player.getUniqueId(), player.getName());
                    }

                }
                player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

                for (int i = 0; i < 200; i++) {
                    player.sendMessage(" ");
                }

                BotCaptcha.sendTitleToPlayer(
                        player,
                        IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + BotCaptcha.getPrefix() + "\"}"),
                        IChatBaseComponent.ChatSerializer.a("{\"text\":\"§aVerification process successfully completed!\"}")
                        , 40, 20, 20);

                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Congratulations!");
                BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "You were successfully verified and registered!");
                BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Have fun on our server!");
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());

                BotCaptcha.getCaptchaTries().remove(player.getUniqueId());
            }

        }

        return false;
    }

}
