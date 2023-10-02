package dev.blockeed.vanitycore.profile;

import com.google.common.collect.Lists;
import com.mongodb.client.model.Updates;
import dev.blockeed.vanitycore.database.DatabaseManager;
import dev.blockeed.vanitycore.utils.Int;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class ProfileData {

    private volatile UUID uuid;
    private volatile Int tokenBalance=new Int(0);
    private volatile Int exp = new Int(0);
    private volatile Int networkLevel = new Int(1);

    private volatile Int bedwarsWins = new Int(0);
    private volatile Int bedwarsLosses = new Int(0);
    private volatile Int bedwarsKills = new Int(0);

    public ProfileData(UUID uuid) {
        this.uuid=uuid;
    }

    public int requiredExpForNextLevel() {
        return networkLevel.getAmount()*networkLevel.getAmount()*1000-exp.getAmount();
    }

    public void load(JavaPlugin plugin, DatabaseManager databaseManager) {
        databaseManager.exists("profile", "uuid", uuid.toString()).thenAcceptAsync((exists) -> {
            if (!exists) {
                create(databaseManager).thenRunAsync(() -> {
                    load(plugin, databaseManager);
                });
                return;
            }

            databaseManager.query("profile", "uuid", uuid.toString()).thenAcceptAsync((document) -> {
                JSONObject jsonObject = new JSONObject(document.toJson());
                tokenBalance.setAmount(jsonObject.getInt("tokenBalance"));
                exp.setAmount(jsonObject.getInt("exp"));
                networkLevel.setAmount(jsonObject.getInt("networkLevel"));
                bedwarsWins.setAmount(jsonObject.getInt("bedwarsWins"));
                bedwarsLosses.setAmount(jsonObject.getInt("bedwarsLosses"));
                bedwarsKills.setAmount(jsonObject.getInt("bedwarskills"));

                if (exp.getAmount()>networkLevel.getAmount()*networkLevel.getAmount()*1000) {
                    networkLevel.increase(1);

                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                                Player player = Bukkit.getPlayer(uuid);
                                player.sendMessage("§7§lVANITY §8| §fGratulálunk, szintet léptél! Szinted: §d"+networkLevel.getAmount());
                                player.sendTitle("§d§lSzintlépés!", "§7Szintet léptél! Szinted: "+networkLevel.getAmount());
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            }
                        }
                    });
                }

            });

        });
    }

    public void save(DatabaseManager databaseManager, Runnable runnable) {
        List<Bson> updateFields = Lists.newArrayList(
                Updates.set("tokenBalance", tokenBalance.getAmount()),
                Updates.set("exp", exp.getAmount()),
                Updates.set("networkLevel", networkLevel.getAmount()),
                Updates.set("bedwarsWins", bedwarsWins.getAmount()),
                Updates.set("bedwarsLosses", bedwarsLosses.getAmount()),
                Updates.set("bedwarsKills", bedwarsKills.getAmount())
        );

        databaseManager.update(
                "profile",
                "uuid",
                uuid.toString(),
                updateFields
        );
    }

    private CompletableFuture<Void> create(DatabaseManager databaseManager) {
        return databaseManager.insert("profile",
                new Document()
                        .append("uuid", uuid.toString())
                        .append("tokenBalance", 0)
                        .append("exp", 0)
                        .append("networkLevel", 1)
                        .append("bedwarsWins", 0)
                        .append("bedwarsLoses", 0)
                        .append("bedwarsKills", 0)
        );
    }

}
