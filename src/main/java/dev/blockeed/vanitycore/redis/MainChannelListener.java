package dev.blockeed.vanitycore.redis;

import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.server.VanityServer;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.Optional;

@RequiredArgsConstructor
public class MainChannelListener implements RedisPubSubListener<String, String>{

    private final VanityCoreAPI coreAPI;

    @Override
    public void message(String channel, String message) {
        System.out.println("asd");
        if (!channel.equals("MAIN-CHANNEL")) return;
        JSONObject json = new JSONObject(message);
        if (!json.has("sender")) return;
        if (!json.has("receiver")) return;
        if (!json.has("subChannel")) return;
        if (!json.has("message")) return;

        String senderServerName = json.getString("sender");
        String receiverServerName = json.getString("receiver");
        String subChannelName = json.getString("subChannel");
        if (!receiverServerName.equals(coreAPI.getServer().getName()) && !receiverServerName.equals("*")) return;

        if (!coreAPI.getRedisManager().getSubChannelListeners().stream().filter(listener -> listener.getSubChannel().equals(subChannelName)).findAny().isPresent()) {
            System.out.println("Catched message without sub channel handler, ignoring message...");
            return;
        }

        VanityPubSubListener vanityPubSubListener = coreAPI.getRedisManager().getSubChannelListeners().stream().filter(listener -> listener.getSubChannel().equals(subChannelName)).findAny().get();
        System.out.println(vanityPubSubListener.getSubChannel());
        JSONObject messageJson = json.getJSONObject("message");

        coreAPI.getServerManager().getServer(senderServerName, (server) -> {
            vanityPubSubListener.onMessage(server, messageJson);
        });
    }

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
