package dev.blockeed.vanitycore.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

@RequiredArgsConstructor
public class DatabaseManager {

    private MongoClient mongoClient;
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String database;

    public void connect() {
        ConnectionString connString = new ConnectionString("mongodb://localhost:27017");
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .serverApi(serverApi)
                .build();
        this.mongoClient = MongoClients.create(settings);
    }

    public void exists(String collection, String fieldName, Object value, Consumer<Boolean> callback) {
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);

        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            return mongoCollection.countDocuments(eq(fieldName, value))>0;
        });

        completableFuture.thenAccept(callback);

    }

    public void query(String collection, String fieldName, Object value, Consumer<Document> callback) {
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);

        CompletableFuture<Document> completableFuture = CompletableFuture.supplyAsync(() -> {
            return mongoCollection.find(eq(fieldName, value)).first();
        });

        completableFuture.thenAccept(callback);
    }

    public void insert(String collection, Document document, Runnable runnable){
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);

        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
           return mongoCollection.insertOne(document).wasAcknowledged();
        });

        completableFuture.thenAccept((acknowledged) -> {
            if (acknowledged) runnable.run();
            else throw new RuntimeException();
        });
    }

    public void insertMany(String collection, List<Document> document, Runnable runnable) {
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            return mongoCollection.insertMany(document).wasAcknowledged();
        });

        completableFuture.thenAccept((acknowledged) -> {
            if (acknowledged) runnable.run();
            else throw new RuntimeException();
        });
    }

    public void update(String collection, String whereField, Object whereValue, Document newDocument, Runnable runnable) {
        query(collection, whereField, whereValue, (oldDocument) -> {
            MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);
            CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
                return mongoCollection.updateOne(oldDocument, newDocument).wasAcknowledged();
            });
            completableFuture.thenAccept((acknowledged) -> {
                if (acknowledged) runnable.run();
                else throw new RuntimeException();
            });
        });
    }

}
