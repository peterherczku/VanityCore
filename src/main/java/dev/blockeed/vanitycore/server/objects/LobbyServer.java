package dev.blockeed.vanitycore.server.objects;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.bungee.BungeeServer;
import dev.blockeed.vanitycore.server.VanityServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

public class LobbyServer extends VanityServer {

    private String name;
    private boolean inProduction;

    public LobbyServer(String name, boolean inProduction) {
        this.name=name;
        this.inProduction=inProduction;
    }

    public LobbyServer(VanityCoreAPI coreAPI, ConfigurationSection section, JavaPlugin javaPlugin) {
        this.name=section.getString("serverName");
        this.inProduction=section.getBoolean("inProduction");

        Bukkit.getScheduler().runTaskTimerAsynchronously(javaPlugin, new Runnable() {
            @Override
            public void run() {
                sendHeartBeat(coreAPI);
            }
        }, 0, 20*60);
    }

    public void sendHeartBeat(VanityCoreAPI coreAPI) {
        coreAPI.getServerManager().getServer("bungee", (server) -> {
            JSONObject message = new JSONObject();
            message.put("serverName", coreAPI.getServer().getName());
            coreAPI.getRedisManager().publishMessage("HEART-BEAT", server, message, () -> {

            });
        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return Type.LOBBY;
    }

    @Override
    public JSONObject getData() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType().name());
        jsonObject.put("name", name);
        jsonObject.put("inProduction", inProduction);
        return jsonObject;
    }

    public static LobbyServer fromJson(JSONObject jsonObject) {
        return new LobbyServer(jsonObject.getString("name"), jsonObject.getBoolean("inProduction"));
    }
}
