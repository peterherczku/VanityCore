package dev.blockeed.vanitycore.profile;

import com.google.common.collect.Lists;
import com.mongodb.client.model.Updates;
import dev.blockeed.vanitycore.database.DatabaseManager;
import dev.blockeed.vanitycore.utils.Int;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class ProfileData {

    private volatile UUID uuid;
    private volatile Int tokenBalance=new Int(0);

    public ProfileData(UUID uuid) {
        this.uuid=uuid;
    }

    public void load(DatabaseManager databaseManager) {
        databaseManager.exists("profile", "uuid", uuid.toString()).thenAcceptAsync((exists) -> {
            if (!exists) {
                create(databaseManager).thenRunAsync(() -> {
                    load(databaseManager);
                });
                return;
            }

            databaseManager.query("profile", "uuid", uuid.toString()).thenAcceptAsync((document) -> {
                JSONObject jsonObject = new JSONObject(document.toJson());
                tokenBalance.setAmount(jsonObject.getInt("tokenBalance"));
            });

        });
    }

    public void save(DatabaseManager databaseManager, Runnable runnable) {
        List<Bson> updateFields = Lists.newArrayList(
                Updates.set("tokenBalance", tokenBalance.getAmount())
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
        );
    }

}
