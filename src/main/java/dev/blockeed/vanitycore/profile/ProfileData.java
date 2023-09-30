package dev.blockeed.vanitycore.profile;

import dev.blockeed.vanitycore.database.DatabaseManager;
import dev.blockeed.vanitycore.utils.Int;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.json.JSONObject;

import java.util.UUID;

@Getter @RequiredArgsConstructor
public class ProfileData {

    private final UUID uuid;
    private final String name;
    private Int tokenBalance=new Int(0);

    public void load(DatabaseManager databaseManager, Runnable runnable) {
        databaseManager.exists("profile", "uuid", uuid.toString(), (exists) -> {
            if (!exists) {
                databaseManager.insert("profile", new Document("uuid", uuid.toString()).append("name", name).append("tokenBalance", tokenBalance.getAmount()), runnable);
                return;
            }

            databaseManager.query("profile", "uuid", uuid.toString(), (document) -> {
                JSONObject jsonObject = new JSONObject(document.toJson());
                this.tokenBalance.setAmount(jsonObject.getInt("tokenBalance"));
            });
        });
    }

    public void save(DatabaseManager databaseManager, Runnable runnable) {
        databaseManager.update("profile", "uuid", uuid.toString(), new Document("uuid", uuid.toString()).append("name", name).append("tokenBalance", tokenBalance.getAmount()), runnable);
    }

}
