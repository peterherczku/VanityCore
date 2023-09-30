package dev.blockeed.vanitycore.server;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.bungee.BungeeServer;
import dev.blockeed.vanitycore.server.objects.GameServer;
import dev.blockeed.vanitycore.server.objects.LobbyServer;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class ServerManager {

    private VanityCoreAPI coreAPI;

    public void getServer(String name, Consumer<VanityServer> callback) {
        coreAPI.getRedisManager().getElementFromList("servers", "name", name, (jsonObject -> {
            VanityServer.Type type = VanityServer.Type.valueOf(jsonObject.getString("type").toUpperCase());

            switch (type) {
                case BUNGEE:
                    callback.accept(BungeeServer.fromJson(jsonObject));
                    break;
                case LOBBY:
                    callback.accept(LobbyServer.fromJson(jsonObject));
                    break;
                case GAME:
                    callback.accept(GameServer.fromJson(jsonObject));
                    break;
            }
        }));
    }

    public void getServers(Consumer<List<VanityServer>> callback) {
        coreAPI.getRedisManager().getList("servers", (jsonObjects -> {

            List<VanityServer> returnList = new ArrayList<>();

            jsonObjects.forEach(jsonObject -> {
                VanityServer.Type type = VanityServer.Type.valueOf(jsonObject.getString("type").toUpperCase());

                switch (type) {
                    case BUNGEE:
                        returnList.add(BungeeServer.fromJson(jsonObject));
                        break;
                    case LOBBY:
                        returnList.add(LobbyServer.fromJson(jsonObject));
                        break;
                    case GAME:
                        returnList.add(GameServer.fromJson(jsonObject));
                        break;
                }
            });

            callback.accept(returnList);

        }));
    }

}
