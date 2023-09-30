package dev.blockeed.vanitycore.profile;

import dev.blockeed.vanitycore.VanityCoreAPI;

import java.util.*;

public class ProfileManager {

    private VanityCoreAPI coreAPI;
    private Map<UUID, ProfileData> profiles;

    public ProfileManager(VanityCoreAPI coreAPI) {
        this.coreAPI=coreAPI;
        this.profiles=new HashMap<>();
    }

    public void loadProfile(UUID uuid, String name, Runnable runnable) {
        if (!this.profiles.containsKey(uuid)) {
            this.profiles.put(uuid, new ProfileData(uuid, name));
        }

        getProfile(uuid).load(coreAPI.getDatabaseManager(), runnable);
    }

    public ProfileData getProfile(UUID uuid) {
        return this.profiles.get(uuid);
    }

}
