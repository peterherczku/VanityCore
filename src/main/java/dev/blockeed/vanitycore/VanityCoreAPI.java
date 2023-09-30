package dev.blockeed.vanitycore;

import dev.blockeed.vanitycore.database.DatabaseManager;
import dev.blockeed.vanitycore.profile.ProfileManager;
import dev.blockeed.vanitycore.redis.RedisManager;
import dev.blockeed.vanitycore.server.ServerManager;
import dev.blockeed.vanitycore.server.VanityServer;
import lombok.Getter;

public class VanityCoreAPI<T extends VanityServer> {

    @Getter
    private RedisManager redisManager;
    @Getter
    private ServerManager serverManager;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private ProfileManager profileManager;

    @Getter
    private T server;

    public VanityCoreAPI() {
        this.redisManager=new RedisManager(this, "127.0.0.1", "6379", "asd123");
        this.serverManager=new ServerManager(this);
        this.databaseManager=new DatabaseManager(this);
        this.profileManager=new ProfileManager(this);

        this.redisManager.connect();
    }

    public void registerServer(T server, Runnable runnable) {
        server.register(this, ()->{
            this.server=server;
            runnable.run();
        });
    }

}
