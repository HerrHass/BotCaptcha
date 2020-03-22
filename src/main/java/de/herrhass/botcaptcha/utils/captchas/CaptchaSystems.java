package de.herrhass.botcaptcha.utils.captchas;

import de.herrhass.botcaptcha.BotCaptcha;
import de.herrhass.botcaptcha.utils.config.ConfigAdapter;
import de.herrhass.botcaptcha.utils.itembuilder.ItemBuilder;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class CaptchaSystems {

    private static final Random RANDOM = new Random();

    private static final ConcurrentHashMap<Player, BukkitTask> USER_SESSION = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, Inventory> CAPTCHA_INVENTORY = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, String> CAPTCHA_WORD = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, Boolean> FINISH_PROCESS = new ConcurrentHashMap<>();

    private static final ItemStack CAPTCHA_PANE = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 5).setName("§aCaptcha").build();

    public static void startCaptcha(Player player, CaptchaType captchaType) {
        if (player != null) {

            switch (captchaType) {

                case WRITING:
                    startWriteCaptcha(player);
                    break;

                case CLICKING:
                    startInventoryCaptcha(player);
                    break;

                case PING:
                    startPingCaptcha(player);
                    break;

            }

        }

    }

    private static void startInventoryCaptcha(Player player) {
        if (!getUserSession().containsKey(player)) {
            final Inventory inventory = Bukkit.createInventory(null, 45, "§aCaptcha ->");

            inventory.setItem(1 + getRandom().nextInt(44), getCaptchaPane());
            ItemBuilder.fillEmptySlots(inventory);

            getCaptchaInventory().put(player, inventory);

            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The inventory captcha starts now!\n" + BotCaptcha.getPrefix() + "Click the green glass pane to pass!");
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "You got §c" + BotCaptcha.getTimeLimit() + " seconds §ato click it!");

            Bukkit.getScheduler().runTaskLaterAsynchronously(BotCaptcha.getPlugin(), () -> player.openInventory(getCaptchaInventory().get(player)), 50L);

            getUserSession().put(player, new BukkitRunnable() {
                int countdown = BotCaptcha.getTimeLimit();

                @Override
                public void run() {
                    if (countdown <= 0) {
                        getCaptchaInventory().remove(player);

                        try {
                            BotCaptcha.addTry(player);

                            if (BotCaptcha.getTries(player) == 3) {
                                if (BotCaptcha.isMySQL()) {
                                    MySQL.setRegistered(player.getUniqueId(), player.getName());
                                    MySQL.setBlocked(player.getUniqueId());
                                }

                                if (BotCaptcha.isConfig()) {
                                    ConfigAdapter.setRegistered(player.getUniqueId(), player.getName());
                                    ConfigAdapter.setBlocked(player.getUniqueId());
                                }

                                Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), () -> player.kickPlayer(BotCaptcha.getPrefix() + "§cYou have been blocked\n §cbecause you are suspected of being a bot!\n" +
                                        "§cYou aren't a bot? Appeal here: §a" + BotCaptcha.getWebsite()));

                                BotCaptcha.getCaptchaTries().remove(player.getUniqueId());

                            } else {
                                Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), () -> player.kickPlayer(BotCaptcha.getPrefix() + "§cYou exceeded the time limit!\n§cTry added!\n§cGood luck next time!"));
                            }

                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }

                        getUserSession().get(player).cancel();
                        getUserSession().remove(player);
                    }

                    --countdown;
                }

            }.runTaskTimerAsynchronously(BotCaptcha.getPlugin(), 0, 20));

        } else {
            Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), ()
                    -> player.openInventory(getCaptchaInventory().get(player)));
        }

    }

    private static void startWriteCaptcha(Player player) {
        if (!getUserSession().containsKey(player) && !getCaptchaWord().containsKey(player)) {
            getCaptchaWord().put(player, generateCaptchaWord(12));

            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "The writing captcha starts now!");
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "You got §c" + BotCaptcha.getTimeLimit() + " seconds §ato\n" + BotCaptcha.getPrefix() + "write the combination in the chat!");

            Bukkit.getScheduler().runTaskLaterAsynchronously(BotCaptcha.getPlugin(), () -> {
                BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "§c" + getCaptchaWord().get(player));

                getUserSession().put(player, new BukkitRunnable() {
                    int countdown = BotCaptcha.getTimeLimit();

                    @Override
                    public void run() {
                        if (countdown <= 0) {
                            try {
                                BotCaptcha.addTry(player);

                                if (BotCaptcha.getTries(player) == 3) {
                                    if (BotCaptcha.isMySQL()) {
                                        MySQL.setRegistered(player.getUniqueId(), player.getName());
                                        MySQL.setBlocked(player.getUniqueId());
                                    }

                                    if (BotCaptcha.isConfig()) {
                                        ConfigAdapter.setRegistered(player.getUniqueId(), player.getName());
                                        ConfigAdapter.setBlocked(player.getUniqueId());
                                    }

                                    Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), ()
                                            -> player.kickPlayer(BotCaptcha.getPrefix() + "§cYou have been blocked\n §cbecause you are suspected of being a bot!\n" +
                                            "§cYou aren't a bot? Appeal here: §a" + BotCaptcha.getWebsite()));

                                    BotCaptcha.getCaptchaTries().remove(player.getUniqueId());

                                } else {
                                    Bukkit.getScheduler().runTask(BotCaptcha.getPlugin(), ()
                                            -> player.kickPlayer(BotCaptcha.getPrefix() + "§cYou exceeded the time limit!\n§cTry added!\n§cGood luck next time!"));
                                }

                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }

                            getUserSession().get(player).cancel();
                            getUserSession().remove(player);
                            getCaptchaWord().remove(player);
                        }

                        --countdown;
                    }

                }.runTaskTimerAsynchronously(BotCaptcha.getPlugin(), 0, 20));

            }, 50L);

        }

    }

    private static void startPingCaptcha(Player player) {
        if (!CaptchaSystems.getUserSession().containsKey(player)) {
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "First ping-check passed!");
            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());

            CaptchaSystems.getUserSession().put(player, new BukkitRunnable() {
                int countdown = 5;
                int average;

                @Override
                public void run() {
                    if (countdown <= 0) {
                        average /= 5;

                        if (average <= BotCaptcha.getMaxPing()) {
                            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Second ping-check passed!");

                        } else {
                            CaptchaSystems.getUserSession().get(player).cancel();
                            CaptchaSystems.getUserSession().remove(player);

                            player.kickPlayer(BotCaptcha.getPrefix() + "§cYou have been disallowed because your ping is too high!\n" +
                                    "§c" + average + "ms > §a" + BotCaptcha.getMaxPing());
                            return;

                        }
                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                        CaptchaSystems.getUserSession().get(player).cancel();
                        CaptchaSystems.getUserSession().remove(player);

                        if (BotCaptcha.isInventoryCaptcha()) {
                            CaptchaSystems.startCaptcha(player, CaptchaType.CLICKING);

                        } else if (BotCaptcha.isWritingCaptcha()) {
                            CaptchaSystems.startCaptcha(player, CaptchaType.WRITING);

                        } else {
                            BotCaptcha.finishProcess(player);
                        }

                        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
                    }
                    int ping = ((CraftPlayer) player).getHandle().ping;
                    average += ping;

                    --countdown;
                }

            }.runTaskTimerAsynchronously(BotCaptcha.getPlugin(), 0, 20));

        }

    }

    private static String generateCaptchaWord(int count) {
        final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-()+#";
        final StringBuilder builder = new StringBuilder();

        while (--count != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());

            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }

        return builder.toString();
    }

    public static ConcurrentHashMap<Player, Boolean> getFinishProcess() {
        return FINISH_PROCESS;
    }

    public static ConcurrentHashMap<Player, BukkitTask> getUserSession() {
        return USER_SESSION;
    }

    public static ConcurrentHashMap<Player, Inventory> getCaptchaInventory() {
        return CAPTCHA_INVENTORY;
    }

    public static ConcurrentHashMap<Player, String> getCaptchaWord() {
        return CAPTCHA_WORD;
    }

    private static Random getRandom() {
        return RANDOM;
    }

    public static ItemStack getCaptchaPane() {
        return CAPTCHA_PANE;
    }

}
