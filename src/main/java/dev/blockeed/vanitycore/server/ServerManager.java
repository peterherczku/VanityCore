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

    public void getServer(String name, Consumer<LobbyServer> callback) {
        coreAPI.getRedisManager().getElementFromList("servers", "name", name, (jsonObject -> {
            VanityServer.Type type = VanityServer.Type.valueOf(jsonObject.getString("type").toUpperCase());

            switch (type) {
                case LOBBY:
                    System.out.println(LobbyServer.fromJson(jsonObject).getName());
                    callback.accept(LobbyServer.fromJson(jsonObject));
                    break;
            }
        }));
    }

}
