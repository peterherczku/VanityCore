package dev.blockeed.vanitycore.redis;


import dev.blockeed.vanitycore.VanityCoreAPI;
import dev.blockeed.vanitycore.server.VanityServer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
    private List<VanityPubSubListener> subChannelListeners=new ArrayList<>();

    public void connect() {
        this.redisClient=RedisClient.create("redis://"+host+":"+port+"/");
    }

    public void registerListeners(VanityPubSubListener... listeners) {
        for (VanityPubSubListener listener : listeners) {
            subChannelListeners.add(listener);
        }
    }

    public void subscribeToChannel() {
        AtomicReference<StatefulRedisPubSubConnection<String, String>> connection = new AtomicReference<>(redisClient.connectPubSub());
        connection.get().addListener(new MainChannelListener(coreAPI));

        // Implement a loop to continuously check the connection status
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        // Schedule a task to check the connection status every 1 second
        executorService.scheduleAtFixedRate(() -> {
            // Check if the connection is valid
            if (!connection.get().isOpen()) {
                // Connection is lost, re-establish it
                connection.set(redisClient.connectPubSub());
                connection.get().addListener(new MainChannelListener(coreAPI));
            }
        }, 0, 1, TimeUnit.SECONDS);
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
