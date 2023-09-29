package dev.blockeed.vanitycore.redis;


import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.server.VanityServer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RedisManager {

    private final VanityCoreAPI coreAPI;
    private final String host;
    private final String port;
    private final String password;

    private RedisClient redisClient;

    @Getter
    private Map<String, VanityPubSubListener> subChannelListeners=new HashMap<>();

    public void connect() {
        this.redisClient=RedisClient.create("redis://"+host+":"+port+"/");
    }

    public void registerListeners(VanityPubSubListener... listeners) {
        for (VanityPubSubListener listener : listeners) {
            subChannelListeners.put(listener.getSubChannel(), listener);
        }
    }

    public void subscribeToChannel() {
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
        connection.addListener(new MainChannelListener(coreAPI));
        connection.async().subscribe("MAIN-CHANNEL");
    }

    public void addToList(String key, JSONObject value, Runnable runnable) {
        if (redisClient==null) return;

        redisClient.connect().async().sadd(key, value.toString()).thenRunAsync(runnable);
    }

    public void removeFromList(String key, JSONObject value, Runnable runnable) {
        if (redisClient==null) return;

        redisClient.connect().async().srem(key, value.toString()).thenRunAsync(runnable);;
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
        if (redisClient==null) return;

        try {
            redisClient.connect().async().smembers(key).thenAcceptAsync((set) -> {
                callback.accept(set.stream().map(JSONObject::new).collect(Collectors.toList()));
            });
        } catch (Exception ex) {
            System.out.println("Error retrieving List "+ key +" from Redis: "+ex.getMessage());
        }
    }

    public void query(String key, String fieldName, String value, Consumer<JSONObject> callback) {
        if (redisClient==null) return;

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
        StatefulRedisPubSubConnection<String, String> connection
                = redisClient.connectPubSub();

        RedisPubSubAsyncCommands<String, String> async
                = connection.async();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("subChannel", subChannel);
        jsonObject.put("sender", coreAPI.getServer().getName());
        if (receiver==null) {
            jsonObject.put("receiver", "*");
        } else {
            jsonObject.put("receiver", receiver.getName());
        }
        jsonObject.put("message", message);

        async.publish("MAIN-CHANNEL", jsonObject.toString()).thenRunAsync(runnable);
    }

}
