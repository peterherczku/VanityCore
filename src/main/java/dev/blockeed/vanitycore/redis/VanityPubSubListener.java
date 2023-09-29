package dev.blockeed.vanitycore.redis;

import dev.blockeed.vanitycore.server.VanityServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

@AllArgsConstructor
public abstract class VanityPubSubListener {

    public abstract String getSubChannel();

    public abstract void onMessage(VanityServer sender, JSONObject message);

}
