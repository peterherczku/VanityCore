package dev.blockeed.vanitycore.server;

import dev.blockeed.vanitycore.VanityCoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

public class HeartBeatHelper {

    public static void startSender(VanityCoreAPI coreAPI, JavaPlugin javaPlugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(javaPlugin, new Runnable() {
            @Override
            public void run() {
                coreAPI.getServerManager().getServer("bungee", (server) -> {
                    JSONObject message = new JSONObject();
                    message.put("serverName", coreAPI.getServer().getName());
                    coreAPI.getRedisManager().publishMessage("HEART-BEAT", server, message, () -> {

                    });
                });
            }
        }, 0, 20*60);
    }

}
