package me.elijuh.soup.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import me.elijuh.soup.Core;
import me.elijuh.soup.data.User;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class MongoManager {
    private final Map<String, LinkedHashMap<String, Integer>> leaderboards = new HashMap<>();
    private final MongoClient client;
    private final MongoCollection<Document> userdata;

    public MongoManager() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        client = MongoClients.create(Core.i().getConfig().getString("mongodb.connection-string"));
        MongoDatabase database = client.getDatabase("soup");
        userdata = database.getCollection("userdata");

        Bukkit.getScheduler().runTaskTimerAsynchronously(Core.i(), ()-> {
            for (String type : new String[]{"kills", "deaths", "streak", "coins"}) {
                LinkedHashMap<String, Integer> leaderboard = new LinkedHashMap<>();
                for (Document data : userdata.find().sort(new Document(type, -1)).limit(10)) {
                    leaderboard.put(data.getString("name"), data.getInteger(type));
                }
                leaderboards.put(type, leaderboard);
            }
            Core.i().getLeaderboardGUI().setItems();
        }, 0L, 6000L);
    }

    public void update(User user) {
        Document update = new Document("name", user.name());

        userdata.updateOne(Filters.eq("uuid", user.uuid()), new Document("$set", update));
    }

    public Document getData(String name) {
        return userdata.find(new Document("name", name)).collation(Collation.builder().locale("en").collationStrength(CollationStrength.PRIMARY).build()).first();
    }

    public Document getData(UUID uuid) {
        return userdata.find(new Document("uuid", uuid.toString())).first();
    }
}
