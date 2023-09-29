package dev.blockeed.vanitycore.redis;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.server.VanityServer;
import dev.blockeed.vanitycore.server.objects.LobbyServer;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

@RequiredArgsConstructor
public abstract class VanityPubSubListener implements RedisPubSubListener<String, String>{

    private final VanityCoreAPI coreAPI;
    private final String subChannel;

    @Override
    public void message(String channel, String message) {
        System.out.println("asd");
        System.out.println(message);
        System.out.println(channel);
        if (!channel.equals("MAIN-CHANNEL")) return;
        JSONObject json = new JSONObject(message);
        if (!json.has("sender")) return;
        if (!json.has("receiver")) return;
        if (!json.has("subChannel")) return;
        if (!json.has("message")) return;

        String senderServerName = json.getString("sender");
        String receiverServerName = json.getString("receiver");
        String subChannelName = json.getString("subChannel");
        if (!subChannelName.equals(subChannel)) return;
        System.out.println(coreAPI.getServer().getName());
        if (!receiverServerName.equals(coreAPI.getServer().getName()) && !receiverServerName.equals("*")) return;

        JSONObject messageJson = json.getJSONObject("message");

        coreAPI.getServerManager().getServer(senderServerName, (server) -> {
            onMessage(server, messageJson);
        });
    }

    public abstract void onMessage(VanityServer server, JSONObject message);

    @Override
    public void message(String string, String k1, String string2) {

    }

    @Override
    public void subscribed(String string, long l) {

    }

    @Override
    public void psubscribed(String string, long l) {

    }

    @Override
    public void unsubscribed(String string, long l) {

    }

    @Override
    public void punsubscribed(String string, long l) {

    }
}
