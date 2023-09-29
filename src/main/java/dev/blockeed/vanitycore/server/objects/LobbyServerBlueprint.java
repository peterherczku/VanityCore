package dev.blockeed.vanitycore.server.objects;

import dev.blockeed.vanitycore.server.VanityServer;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

@AllArgsConstructor
public class LobbyServerBlueprint extends VanityServer {

    private String name;
    private boolean inProduction;

    @Override
    public String getName() {
        return this.name;
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
