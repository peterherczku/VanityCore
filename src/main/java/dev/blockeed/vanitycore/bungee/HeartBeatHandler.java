package dev.blockeed.vanitycore.bungee;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.redis.VanityPubSubListener;
import dev.blockeed.vanitycore.server.VanityServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HeartBeatHandler extends VanityPubSubListener {

    private Map<String, Long> lastHeartBeat;

    public HeartBeatHandler(VanityCoreAPI coreAPI, Plugin plugin) {
        this.lastHeartBeat=new HashMap<>();

        ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
            Iterator<Map.Entry<String, Long>> iterator = lastHeartBeat.entrySet().iterator();

            @Override
            public void run() {
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> entry = iterator.next();
                    String serverName = entry.getKey();
                    Long lastTime = entry.getValue();

                    if (System.currentTimeMillis()-lastTime>120) {
                        System.out.println(serverName+" skipped 2 heart beats, unregistering server...");
                        coreAPI.getServerManager().getServer(serverName, (server) -> {
                            coreAPI.getRedisManager().removeFromList("servers", server.getData(), () -> {
                                System.out.println("Successfully unregistered "+serverName);
                            });
                        });
                        iterator.remove();
                    }
                }

            }
        }, 0, 120, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(VanityServer sender, JSONObject message) {
        if (!lastHeartBeat.containsKey(sender.getName())) {
            lastHeartBeat.put(sender.getName(), System.currentTimeMillis());
            return;
        }

         lastHeartBeat.replace(sender.getName(), System.currentTimeMillis());
    }

    @Override
    public String getSubChannel() {
        return "HEART-BEAT";
    }

}
