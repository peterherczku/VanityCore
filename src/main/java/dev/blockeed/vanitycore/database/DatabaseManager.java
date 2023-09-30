package dev.blockeed.vanitycore.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.sun.org.apache.xpath.internal.operations.Bool;
import dev.blockeed.vanitycore.VanityCoreAPI;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseManager {

    private VanityCoreAPI coreAPI;
    private MongoClient mongoClient;

    public DatabaseManager(VanityCoreAPI coreAPI) {
        this.coreAPI=coreAPI;
        this.mongoClient=this.connectToDatabase();
    }

    public MongoClient connectToDatabase() {
        String uri = "mongodb://localhost:27017";

        ConnectionString connectionString = new ConnectionString(uri);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return mongoClient;
    }

    public void closeConnection() {
        mongoClient.close();
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public CompletableFuture<Document> query(String collection, String databaseObject, Object compare) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document document = getMongoClient().getDatabase("vanity").getCollection(collection).find(eq(databaseObject, compare)).first();
                return document;
            } catch (Exception e) {
                return null;
            }

        });
    }

    public CompletableFuture<Void> updateSingleValue(String collection, String queryDatabaseObject, String compare, String databaseObject, Object updatedValue) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document query = new Document().append(queryDatabaseObject, compare);
                Bson updates = Updates.combine(
                        Updates.set(databaseObject, updatedValue)
                );
                UpdateOptions options = new UpdateOptions().upsert(true);

                getMongoClient().getDatabase("vanity").getCollection(collection).updateOne(query, updates, options);

                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public CompletableFuture<Void> update(String collection, String queryDatabaseObject, Object compare, List<Bson> updateFields) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document query = new Document().append(queryDatabaseObject, compare);
                UpdateOptions options = new UpdateOptions().upsert(true);

                getMongoClient().getDatabase("vanity").getCollection(collection).updateOne(query, Updates.combine(updateFields), options);
                return null;
            } catch (Exception e) {
                return null;
            }
        });

    }

    public CompletableFuture<Boolean> exists(String collection, String queryDatabaseObject, Object compare) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document document = new Document(queryDatabaseObject, compare);
                long userCount = getMongoClient().getDatabase("vanity").getCollection(collection).countDocuments(document);
                return (userCount>0);
            } catch (Exception e) {
                return false;
            }
        });

    }

    public CompletableFuture<Void> insert(String collection, Document document) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                getMongoClient().getDatabase("minedark").getCollection(collection).insertOne(document);
                return null;
            } catch (Exception e) {
                return null;
            }
        });

    }

    public void shutdown() {
        mongoClient.close();
    }

}
