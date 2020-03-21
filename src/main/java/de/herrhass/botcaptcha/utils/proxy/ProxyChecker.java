package de.herrhass.botcaptcha.utils.proxy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class ProxyChecker {

    private static final HashMap<String, Boolean> IP_CACHE = new HashMap<>();

    private static CompletableFuture<Boolean> isVPNAsync(String ip) {
        return CompletableFuture.supplyAsync( () -> {
            try {
                if (getIpCache().containsKey(ip)) {
                    return getIpCache().get(ip);
                }

                Map<String, Object> stringObjectMap = new Gson().fromJson(
                        IOUtils.toString(new URL("https://api.iplegit.com/info?ip=" + ip), StandardCharsets.UTF_8), new TypeToken<HashMap<String, Object>>() {}.getType()
                );

                if (Boolean.parseBoolean(stringObjectMap.get("bad").toString())) {
                    getIpCache().put(ip, true);
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            getIpCache().put(ip, false);
            return false;
        }, Executors.newCachedThreadPool()).handle((u, throwable) -> u);

    }

    private static HashMap<String, Boolean> getIpCache() {
        return IP_CACHE;
    }

    public static boolean isVPN(String ip) {
        return isVPNAsync(ip).join();
    }

}
