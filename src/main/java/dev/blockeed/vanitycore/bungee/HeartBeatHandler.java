package dev.blockeed.vanitycore.bungee;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.redis.MainChannelListener;
import dev.blockeed.vanitycore.server.VanityServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HeartBeatHandler extends MainChannelListener {

    private Map<String, Long> lastHeartBeat;

    public HeartBeatHandler(VanityCoreAPI coreAPI, Plugin plugin) {
        super(coreAPI, "HEART-BEAT");
        this.lastHeartBeat=new HashMap<>();

        ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                lastHeartBeat.forEach((serverName, lastTime) -> {
                    if (System.currentTimeMillis()-lastTime>120) {
                        System.out.println(serverName+" skipped 2 heart beats, unregistering server...");
                        coreAPI.getServerManager().getServer(serverName, (server) -> {
                            coreAPI.getRedisManager().removeFromList("servers", server.getData(), () -> {
                                System.out.println("Successfully unregistered "+serverName);
                            });
                        });
                        lastHeartBeat.remove(serverName);
                    }
                });
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

}
