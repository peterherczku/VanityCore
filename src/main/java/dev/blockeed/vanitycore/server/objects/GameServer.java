package dev.blockeed.vanitycore.server.objects;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.bungee.BungeeServer;
import dev.blockeed.vanitycore.server.VanityServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

@AllArgsConstructor
public class GameServer extends VanityServer {

    private ConfigurationSection section;
    private final String name;
    @Setter
    private boolean inProduction;
    @Setter
    private boolean waitingForGameData;

    public GameServer(VanityCoreAPI coreAPI, ConfigurationSection section, JavaPlugin javaPlugin) {
        this.section=section;
        this.name=this.section.getString("serverName");
        this.inProduction=this.section.getBoolean("inProduction");
        this.waitingForGameData=this.section.getBoolean("waitingForGameData");

        Bukkit.getScheduler().runTaskTimerAsynchronously(javaPlugin, new Runnable() {
            @Override
            public void run() {
                sendHeartBeat(coreAPI);
            }
        }, 0, 20*60);

    }

    public GameServer(String name, boolean inProduction, boolean waitingForGameData) {
        this.name=name;
        this.inProduction=inProduction;
        this.waitingForGameData=waitingForGameData;
    }

    public void sendHeartBeat(VanityCoreAPI coreAPI) {
        coreAPI.getServerManager().getServer("bungee", (jsonObject) -> {
            BungeeServer bungeeServer = new BungeeServer(jsonObject.getString("name"));
            JSONObject message = new JSONObject();
            message.put("serverName", coreAPI.getServer().getName());
            coreAPI.getRedisManager().publishMessage("HEART-BEAT", bungeeServer, message, () -> {

            });
        });
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public VanityServer.Type getType() {
        return Type.GAME;
    }

    @Override
    public JSONObject getData() {
        JSONObject returnObject = new JSONObject();
        returnObject.put("type", getType().name());
        returnObject.put("name", getName());
        returnObject.put("inProduction", inProduction);
        returnObject.put("waitingForGameData", waitingForGameData);
        return returnObject;
    }

    public static GameServer fromJson(JSONObject jsonObject) {
        return new GameServer(jsonObject.getString("name"), jsonObject.getBoolean("inProduction"), jsonObject.getBoolean("waitingForGameData"));
    }

}
