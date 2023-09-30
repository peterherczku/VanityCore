package dev.blockeed.vanitycore.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.List;
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
        Publisher<Long> publisher = mongoCollection.countDocuments(eq(fieldName, value));

        Flux.from(publisher).doOnNext(count -> callback.accept(count>0)).subscribe();
    }

    public void query(String collection, String fieldName, Object value, Consumer<Document> callback) {
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);
        Publisher<Document> publisher = mongoCollection.find(eq(fieldName, value));

        Flux.from(publisher).doOnNext((callback)).subscribe();
    }

    public void insert(String collection, Document document, Runnable runnable){
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);
        Publisher<InsertOneResult> insertOperation = mongoCollection.insertOne(document);

        Flux.from(insertOperation).doOnNext((voidValue) -> runnable.run()).subscribe();
    }

    public void insertMany(String collection, List<Document> document, Runnable runnable) {
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);
        Publisher<InsertManyResult> insertManyOperation = mongoCollection.insertMany(document);

        Flux.from(insertManyOperation).doOnNext((voidValue) -> runnable.run()).subscribe();
    }

    public void update(String collection, String whereField, Object whereValue, Document newDocument, Runnable runnable) {
        query(collection, whereField, whereValue, (oldDocument) -> {
            MongoCollection<Document> mongoCollection = mongoClient.getDatabase(this.database).getCollection(collection);
            Publisher<UpdateResult> updateOperation = mongoCollection.updateOne(oldDocument, newDocument);

            Flux.from(updateOperation).doOnNext((voidValue) -> runnable.run()).subscribe();
        });
    }

}
