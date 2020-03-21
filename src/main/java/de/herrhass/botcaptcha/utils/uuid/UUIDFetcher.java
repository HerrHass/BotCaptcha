package de.herrhass.botcaptcha.utils.uuid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mojang.util.UUIDTypeAdapter;
import de.herrhass.botcaptcha.BotCaptcha;
import org.bukkit.entity.Player;

public class UUIDFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static final Map<String, UUID> UUID_CACHE = new HashMap<>();

    private String name;
    private UUID id;

    public static UUID getUUID(String name, Player executor) {
        return getUUIDAt(name, System.currentTimeMillis(), executor).join();
    }

    public static CompletableFuture<UUID> getUUIDAt(final String name, long timestamp, Player executor) {
        return CompletableFuture.supplyAsync( () -> {
            if (name.length() > 2 && name.length() <= 16) {
                String lowerCaseName = name.toLowerCase();

                if (getUuidCache().containsKey(lowerCaseName)) {
                    return getUuidCache().get(lowerCaseName);
                }

                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(String.format(getUuidUrl(), lowerCaseName, timestamp/1000)).openConnection();
                    connection.setReadTimeout(5000);
                    UUIDFetcher data = getGson().fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);

                    if (data != null) {
                        getUuidCache().put(lowerCaseName, data.id);

                        return data.id;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                BotCaptcha.sendMessageToPlayer(executor,BotCaptcha.getPrefix() + "The length of the given username must be longer than two chars and shorter or equal to 16 chars!");
                return null;
            }

            return null;
        }, Executors.newCachedThreadPool()).handle((u, throwable) -> u);

    }

    private static Gson getGson() {
        return GSON;
    }

    private static Map<String, UUID> getUuidCache() {
        return UUID_CACHE;
    }

    private static String getUuidUrl() {
        return UUID_URL;
    }

}