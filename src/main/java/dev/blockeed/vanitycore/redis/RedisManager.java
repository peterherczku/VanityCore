package dev.blockeed.vanitycore.redis;


import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.server.VanityServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RedisManager {

    private final VanityCoreAPI coreAPI;
    private final String host;
    private final String port;
    private final String password;

    private Jedis subscriber;

    @Getter
    private List<VanityPubSubListener> subChannelListeners=new ArrayList<>();

    public void connect() {
        this.subscriber=new Jedis();
    }

    public void registerListeners(VanityPubSubListener... listeners) {
        for (VanityPubSubListener listener : listeners) {
            subChannelListeners.add(listener);
        }
    }

    public void subscribeToChannel() {
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
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
        };

        CompletableFuture.supplyAsync(() -> {
            subscriber.subscribe(jedisPubSub, "MAIN-CHANNEL");
           return null;
        });
    }

    public void addToList(String key, JSONObject value, Runnable runnable) {
        CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = new Jedis() ){
                jedis.sadd(key, value.toString());
                runnable.run();
            }
            return null;
        });
    }

    public void removeFromList(String key, JSONObject value, Runnable runnable) {
        CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = new Jedis() ){
                jedis.srem(key, value.toString());
                runnable.run();
            }
            return null;
        });
    }

    public void updateList(String key, JSONObject oldValue, JSONObject newValue, Runnable runnable) {
        removeFromList(key, oldValue, () -> {
            addToList(key, newValue, runnable);
        });
    }

    public void getElementFromList(String listName, String fieldName, String value, Consumer<JSONObject> callback) {
        getList(listName, (jsonObjects -> {
            callback.accept(jsonObjects.stream().filter(jsonObject -> jsonObject.getString(fieldName).equals(value)).findAny().get());
        }));
    }

    public void getList(String key, Consumer<List<JSONObject>> callback) {
        CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = new Jedis() ){
                callback.accept(jedis.smembers(key).stream().map(JSONObject::new).collect(Collectors.toList()));
            }
            return null;
        });
    }

    public void query(String key, String fieldName, String value, Consumer<JSONObject> callback) {
        getList(key, (list) -> {
            for (JSONObject jsonObject : list) {
                if (!jsonObject.getString(fieldName).equals(value)) continue;

                callback.accept(jsonObject);
                return;
            }

            callback.accept(null);
        });
    }

    public void publishMessage(String subChannel, VanityServer receiver, JSONObject message, Runnable runnable) {
        CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = new Jedis() ){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("subChannel", subChannel);
                jsonObject.put("sender", coreAPI.getServer().getName());
                if (receiver==null) {
                    jsonObject.put("receiver", "*");
                } else {
                    jsonObject.put("receiver", receiver.getName());
                }
                jsonObject.put("message", message);
                jedis.publish("MAIN-CHANNEL", jsonObject.toString());
                runnable.run();
            }
            return null;
        });
    }

}
