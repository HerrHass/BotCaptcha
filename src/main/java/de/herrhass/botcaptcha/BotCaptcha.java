package de.herrhass.botcaptcha;

import de.herrhass.botcaptcha.commands.CommandBotCaptcha;
import de.herrhass.botcaptcha.commands.CommandFinish;
import de.herrhass.botcaptcha.listeners.*;
import de.herrhass.botcaptcha.utils.captchas.CaptchaSystems;
import de.herrhass.botcaptcha.utils.mysql.MySQL;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BotCaptcha extends JavaPlugin {

    private static final ConcurrentHashMap<UUID, AtomicInteger> CAPTCHA_TRIES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, Boolean> COMPARE_VALUES = new ConcurrentHashMap<>();

    private final PluginManager pluginManager = Bukkit.getPluginManager();

    // Singleton only used for schedulers
    private static BotCaptcha plugin;

    private static File baseConfig;
    private static FileConfiguration baseConfigYaml;

    private static File configAdapter;
    private static FileConfiguration configAdapterYaml;

    @Override
    public void onEnable() {
        plugin = this;
        loadBaseConfig();

        if (!isMySQL() && !isConfig()) {
            Bukkit.getConsoleSender().sendMessage("§c[BotCaptcha] You need to select (or activate both) the kind of storing the data (check the base config file)!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        loadConfigAdapter();
        loadMySQLConfig();
        init();

        if (isMySQL() && isActivated()) {
            MySQL.connect();

            if(MySQL.isConnected()) {
                MySQL.update("CREATE TABLE IF NOT EXISTS botcaptcha (PLAYERNAME VARCHAR(16),UUID VARCHAR(36),BLOCKED BOOLEAN)");
            }

        }

        Bukkit.getConsoleSender().sendMessage("§b§m----------------------§7| §cBotCaptcha §7|§b§m----------------------");
        Bukkit.getConsoleSender().sendMessage("§b§m§ §cPlugin by " + BotCaptcha.getPlugin().getDescription().getAuthors().toString());
        Bukkit.getConsoleSender().sendMessage("§b§m§ §cVersion: " + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§b§m§ §cInitialized listeners and commands...");
        Bukkit.getConsoleSender().sendMessage("§b§m§ §cInitialized base config file...");
        if (isConfig() && isActivated()) {
            Bukkit.getConsoleSender().sendMessage("§b§m§ §cInitialized config storing system...");
        }

        if (isMySQL() && isActivated()) {
            if (MySQL.isConnected()) {
                Bukkit.getConsoleSender().sendMessage("§b§m§ §cInitialized MySQL storing system...");
            }

        }

        //for (int i = 0; i < 10; i++) {
        //            UUID uuid = UUID.randomUUID();
        //            String playerName = "abc-" + i;
        //
        //            MySQL.setRegistered(uuid, playerName);
        //        }
        //
        //        for (int i = 0; i < 10; i++) {
        //            UUID uuid = UUID.randomUUID();
        //            String playerName = "def-" + i;
        //
        //            ConfigAdapter.setRegistered(uuid, playerName);
        //        }

        Bukkit.getConsoleSender().sendMessage("§b§m----------------------§7| §cBotCaptcha §7|§b§m----------------------");
    }

    @Override
    public void onDisable() {
        if (isMySQL() && isActivated()) {
            if (MySQL.isConnected()) {
                MySQL.disconnect();
            }

        }

        Bukkit.getConsoleSender().sendMessage("§b§m----------------------§7| §cBotCaptcha §7|§b§m----------------------");
        Bukkit.getConsoleSender().sendMessage("§b§m§ §cShutting down systems...");
        Bukkit.getConsoleSender().sendMessage("§b§m----------------------§7| §cBotCaptcha §7|§b§m----------------------");
    }

    public static void loadConfigAdapter() {
        configAdapter = new File("plugins//BotCaptcha//players.yml");

        if (isConfig() && isActivated()) {
            if (!configAdapter.exists()) {
                configAdapter.getParentFile().mkdirs();
                configAdapterYaml = new YamlConfiguration();

                try {
                    configAdapterYaml.save(configAdapter);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            configAdapterYaml = YamlConfiguration.loadConfiguration(configAdapter);

        } else {
            Bukkit.getConsoleSender().sendMessage(getPrefix() + "The config storing system is deactivated and/or the plugin is disabled!");
        }

    }

    public static void loadMySQLConfig() {
        File mySQLConfig = new File("plugins//BotCaptcha//mysql.yml");

        if (isMySQL() && isActivated()) {
            FileConfiguration mySQLConfigYaml;

            if (!mySQLConfig.exists()) {
                mySQLConfig.getParentFile().mkdirs();
                mySQLConfigYaml = new YamlConfiguration();

                mySQLConfigYaml.set("host", "localhost");
                mySQLConfigYaml.set("port", "3306");
                mySQLConfigYaml.set("database", "botcaptcha");
                mySQLConfigYaml.set("username", "root");
                mySQLConfigYaml.set("password", "");

                try {
                    mySQLConfigYaml.save(mySQLConfig);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            mySQLConfigYaml = YamlConfiguration.loadConfiguration(mySQLConfig);

            MySQL.setHost(mySQLConfigYaml.getString("host"));
            MySQL.setPort(mySQLConfigYaml.getString("port"));
            MySQL.setDatabase(mySQLConfigYaml.getString("database"));
            MySQL.setUsername(mySQLConfigYaml.getString("username"));
            MySQL.setPassword(mySQLConfigYaml.getString("password"));

        } else {
            Bukkit.getConsoleSender().sendMessage(getPrefix() + "The MySQL storing system is deactivated and/or the plugin is disabled!");
        }

    }

    public static void saveBaseConfig() {
        try {
            getBaseConfigYaml().save(getBaseConfig());
            getBaseConfigYaml().load(getBaseConfig());

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public static void saveConfigAdapter() {
        try {
            if (isConfig() && Objects.requireNonNull(getConfigAdapter()).exists()) {
                getConfigAdapterYaml().save(getConfigAdapter());
                getConfigAdapterYaml().load(getConfigAdapter());
            }

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public static void sendMessageToPlayer(Player player, String message) {
        player.spigot().sendMessage(TextComponent.fromLegacyText(message));
    }

    public static void sendTitleToPlayer(Player player, IChatBaseComponent mainTitle, IChatBaseComponent subTitle, int i, int i1, int i2) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TITLE,
                mainTitle,
                i,
                i1,
                i2
        ));

        playerConnection.sendPacket(new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                subTitle,
                i,
                i1,
                i2
        ));

    }

    public static void finishProcess(Player player) {
        CaptchaSystems.getCaptchaWord().remove(player);

        if (CaptchaSystems.getUserSession().containsKey(player)) {
            CaptchaSystems.getUserSession().get(player).cancel();
            CaptchaSystems.getUserSession().remove(player);
        }

        TextComponent website = new TextComponent();
        website.setText(BotCaptcha.getPrefix() + "Please click here and read carefully!");
        website.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, BotCaptcha.getWebsite()));
        website.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(BotCaptcha.getWebsite()).create()));

        TextComponent accept = new TextComponent();
        accept.setText("§7[§bACCEPT§7]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/finish"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accept the terms of service!").create()));

        if (isWritingCaptcha()) {
            BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
            BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Word captcha passed!");
        }

        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());
        BotCaptcha.sendMessageToPlayer(player,BotCaptcha.getPrefix() + "Now please read the Terms of Service\n" + BotCaptcha.getPrefix() + "carefully and accept them!");
        BotCaptcha.sendMessageToPlayer(player, BotCaptcha.getPrefix());

        player.spigot().sendMessage(website);
        player.spigot().sendMessage(accept);
    }

    public static void addConfigBlockedBot() {
        if (getBaseConfigYaml().get("blocked-bots.config") != null) {
            getBaseConfigYaml().set("blocked-bots.config", Integer.parseInt(getConfigBlockedBots()) + 1);

        } else {
            getBaseConfigYaml().set("blocked-bots.config", 1);
        }

        saveBaseConfig();
    }

    public static void removeConfigBlockedBot() {
        if (getBaseConfigYaml().get("blocked-bots.config") != null) {
            if (getBaseConfigYaml().getInt("blocked-bots.config") > 0) {
                getBaseConfigYaml().set("blocked-bots.config", Integer.parseInt(getConfigBlockedBots()) - 1);
            }

        } else {
            getBaseConfigYaml().set("blocked-bots.config", 0);
        }

        saveBaseConfig();
    }

    public static void addMySQLBlockedBot() {
        if (getBaseConfigYaml().get("blocked-bots.mysql") != null) {
            getBaseConfigYaml().set("blocked-bots.mysql", Integer.parseInt(getMySQLBlockedBots()) + 1);

        } else {
            getBaseConfigYaml().set("blocked-bots.mysql", 1);
        }

        saveBaseConfig();
    }

    public static void removeMySQLBlockedBot() {
        if (getBaseConfigYaml().get("blocked-bots.mysql") != null) {
            if (getBaseConfigYaml().getInt("blocked-bots.mysql") > 0) {
                getBaseConfigYaml().set("blocked-bots.mysql", Integer.parseInt(getMySQLBlockedBots()) - 1);
            }

        } else {
            getBaseConfigYaml().set("blocked-bots.mysql", 0);
        }

        saveBaseConfig();
    }

    public static void addTry(Player player) {
        if (getCaptchaTries().containsKey(player.getUniqueId())) {
            getCaptchaTries().get(player.getUniqueId()).incrementAndGet();
        }

    }

    private void init() {
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new InventoryCloseListener(), this);
        pluginManager.registerEvents(new BlockBreakListener(), this);
        pluginManager.registerEvents(new BlockPlaceListener(), this);
        pluginManager.registerEvents(new InventoryClickListener(), this);
        pluginManager.registerEvents(new PlayerLeaveListener(), this);
        pluginManager.registerEvents(new PlayerLoginListener(), this);
        pluginManager.registerEvents(new AsyncPlayerChatListener(), this);

        getCommand("botcaptcha").setExecutor(new CommandBotCaptcha());
        getCommand("finish").setExecutor(new CommandFinish());
    }

    private void loadBaseConfig() {
        baseConfig = new File(getDataFolder().getAbsolutePath() + "/config.yml");
        baseConfigYaml = getConfig();

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public static BotCaptcha getPlugin() {
        return plugin;
    }

    public static ConcurrentHashMap<UUID, AtomicInteger> getCaptchaTries() {
        synchronized (CAPTCHA_TRIES) {
            return CAPTCHA_TRIES;
        }

    }

    public static ConcurrentHashMap<Player, Boolean> getCompareValues() {
        return COMPARE_VALUES;
    }

    public static File getBaseConfig() {
        return baseConfig;
    }

    public static File getConfigAdapter() {
        return configAdapter;
    }

    public static FileConfiguration getConfigAdapterYaml() {
        return configAdapterYaml;
    }

    public static FileConfiguration getBaseConfigYaml() {
        return baseConfigYaml;
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', getBaseConfigYaml().getString("prefix"));
    }

    public static String getNoPlayer() {
        return ChatColor.translateAlternateColorCodes('&', getBaseConfigYaml().getString("messages.no-player"));
    }

    public static String getNoPerms() {
        return ChatColor.translateAlternateColorCodes('&', getBaseConfigYaml().getString("messages.no-perms"));
    }

    public static String getWrongArgs() {
        return ChatColor.translateAlternateColorCodes('&', getBaseConfigYaml().getString("messages.wrong-args"));
    }

    public static String getAdminPermission() {
        return ChatColor.translateAlternateColorCodes('&', getBaseConfigYaml().getString("permissions.admin"));
    }

    public static String getBypassPermission() {
        return ChatColor.translateAlternateColorCodes('&', getBaseConfigYaml().getString("permissions.bypass"));
    }

    public static boolean isActivated() {
        return baseConfigYaml.getBoolean("enabled");
    }

    public static void setActivated(boolean activate) {
        getBaseConfigYaml().set("enabled", activate);
    }

    public static boolean isMySQL() {
        return getBaseConfigYaml().getBoolean("mysql");
    }

    public static void setMySQL(boolean mySQL) {
        getBaseConfigYaml().set("mysql", mySQL);
    }

    public static boolean isConfig() {
        return getBaseConfigYaml().getBoolean("config");
    }

    public static void setConfig(boolean config) {
        getBaseConfigYaml().set("config", config);
    }

    public static int getMaxPing() {
        return getBaseConfigYaml().getInt("max-ping");
    }

    public static int getTimeLimit() {
        return getBaseConfigYaml().getInt("time-limit");
    }

    public static void setTimeLimit(int timeLimit) {
        getBaseConfigYaml().set("time-limit", timeLimit);
    }

    public static void setMaxPing(int maxPing) {
        getBaseConfigYaml().set("max-ping", maxPing);
    }

    public static boolean isProxyBlocking() {
        return getBaseConfigYaml().getBoolean("block-proxies");
    }

    public static void setProxyBlock(boolean block) {
        getBaseConfigYaml().set("block-proxies", block);
    }

    public static boolean isPingCheck() {
        return getBaseConfigYaml().getBoolean("check-ping");
    }

    public static void setCheckPing(boolean checkPing) {
        getBaseConfigYaml().set("check-ping", checkPing);
    }

    public static boolean isInventoryCaptcha() {
        return getBaseConfigYaml().getBoolean("inventory-captcha");
    }

    public static void setInventoryCaptcha(boolean inventoryCaptcha) {
        getBaseConfigYaml().set("inventory-captcha", inventoryCaptcha);
    }

    public static boolean isWritingCaptcha() {
        return getBaseConfigYaml().getBoolean("writing-captcha");
    }

    public static void setWritingCaptcha(boolean writingCaptcha) {
        getBaseConfigYaml().set("writing-captcha", writingCaptcha);
    }

    public static boolean isCompareValues() {
        return getBaseConfigYaml().getBoolean("compare-database-configuration");
    }

    public static void setCompareValues(boolean compareValues) {
        getBaseConfigYaml().set("compare-database-configuration", compareValues);
    }

    public static String getWebsite() {
        return getBaseConfigYaml().getString("url");
    }

    public static String getMySQLBlockedBots() {
        if (getBaseConfigYaml().get("blocked-bots.mysql") != null) {
            return "" + getBaseConfigYaml().getInt("blocked-bots.mysql");
        }

        return "0";
    }

    public static String getConfigBlockedBots() {
        if (getBaseConfigYaml().get("blocked-bots.config") != null) {
            return "" + getBaseConfigYaml().getInt("blocked-bots.config");
        }

        return "0";
    }

    public static int getTries(Player player) {
        if (getCaptchaTries().containsKey(player.getUniqueId())) {
            return getCaptchaTries().get(player.getUniqueId()).get();
        }

        return -1;
    }

}
