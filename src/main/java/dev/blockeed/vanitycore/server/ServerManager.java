package dev.blockeed.vanitycore.server;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.bungee.BungeeServer;
import dev.blockeed.vanitycore.server.objects.GameServer;
import dev.blockeed.vanitycore.server.objects.LobbyServer;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class ServerManager {

    private VanityCoreAPI coreAPI;

    public void getServer(String name, Consumer<VanityServer> callback) {
        coreAPI.getRedisManager().getElementFromList("servers", "name", name, (jsonObject -> {
            VanityServer.Type type = VanityServer.Type.valueOf(jsonObject.getString("type").toUpperCase());

            switch (type) {
                case GAME:
                    callback.accept(GameServer.fromJson(jsonObject));
                    break;
                case BUNGEE:
                    callback.accept(BungeeServer.fromJson(jsonObject));
                    break;
                case LOBBY:
                    System.out.println(jsonObject);
                    callback.accept(LobbyServer.fromJson(jsonObject));
                    break;
            }
        }));
    }

}
