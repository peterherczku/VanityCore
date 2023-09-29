package dev.blockeed.vanitycore.bungee;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.server.VanityServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.json.JSONObject;

public class BungeeServer extends VanityServer {

    private Configuration section;
    private final String name;

    public BungeeServer(VanityCoreAPI coreAPI, Configuration section, Plugin plugin) {
        this.section=section;
        this.name=this.section.getString("serverName");

        coreAPI.getRedisManager().registerListeners(new HeartBeatHandler(coreAPI, plugin));
        coreAPI.getRedisManager().subscribeToChannel();
    }

    public BungeeServer(String name) {
        this.name=name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return Type.BUNGEE;
    }

    @Override
    public JSONObject getData() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType().name());
        jsonObject.put("name", this.name);
        return jsonObject;
    }

    public static BungeeServer fromJson(JSONObject jsonObject) {
        return new BungeeServer(jsonObject.getString("name"));
    }

}
