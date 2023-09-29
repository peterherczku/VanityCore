package dev.blockeed.vanitycore.server;

import dev.blockeed.vanitycore.VanityCoreAPI;
import org.json.JSONObject;

public abstract class VanityServer {

    public void register(VanityCoreAPI coreAPI, Runnable runnable) {
        coreAPI.getRedisManager().addToList("servers", getData(), runnable);
    }

    public void unregister(VanityCoreAPI coreAPI, Runnable runnable) {
        coreAPI.getRedisManager().getElementFromList("servers", "name", getName(), (jsonObject -> {
            coreAPI.getRedisManager().removeFromList("servers", jsonObject, runnable);
        }));
    }

    public void update(VanityCoreAPI coreAPI, Runnable runnable) {
        coreAPI.getRedisManager().getElementFromList("servers", "name", getName(), (jsonObject)->{
            coreAPI.getRedisManager().updateList("servers", jsonObject, getData(), runnable);
        });
    }

    public abstract String getName();
    public abstract Type getType();
    public abstract JSONObject getData();

    public enum Type {
        BUNGEE,
        GAME,
        LOBBY
    }

}
