package dev.blockeed.vanitycore.bungee;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.redis.ServerType;
import dev.blockeed.vanitycore.redis.VanityPubSubListener;
import dev.blockeed.vanitycore.server.VanityServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HeartBeatHandler extends VanityPubSubListener {

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
