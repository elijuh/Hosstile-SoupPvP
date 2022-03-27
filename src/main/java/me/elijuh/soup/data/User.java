package me.elijuh.soup.data;

import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.elijuh.soup.Core;
import me.elijuh.soup.data.perks.Perk;
import me.elijuh.soup.events.Event;
import me.elijuh.soup.scoreboard.DefaultScoreboard;
import me.elijuh.soup.scoreboard.IScoreboard;
import me.elijuh.soup.tasks.SpawnTask;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ForceFieldUtil;
import me.elijuh.soup.util.ItemBuilder;
import me.elijuh.soup.util.ObjectUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
@Setter
public class User {
    private final Player player;
    private final Map<String, Object> data = new HashMap<>();
    private final Document perks;
    private final List<String> kits;
    private IScoreboard scoreboard;
    private SpawnTask spawnTask;
    private HealType healType;
    private int combat, bounty;
    private boolean spawn;

    public User(Player player) {
        this.player = player;

        Document doc = Core.i().getMongoManager().getData(UUID.fromString(uuid()));
        if (doc == null) {
            doc = new Document("unique", Core.i().getMongoManager().getUserdata().countDocuments() + 1)
                    .append("uuid", uuid())
                    .append("name", name())
                    .append("kills", 0)
                    .append("deaths", 0)
                    .append("streak", 0)
                    .append("coins", 100)
                    .append("bounty", 0)
                    .append("last_kit", "default")
                    .append("heal_type", "SOUP")
                    .append("kits", Lists.newArrayList("default", "archer"))
                    .append("perks", new Document());
            Core.i().getMongoManager().getUserdata().insertOne(doc);
        } else {
            Core.i().getMongoManager().update(this);
        }

        perks = doc.get("perks", Document.class);
        kits = ObjectUtil.nullOrDef(doc.getList("kits", String.class), Lists.newArrayList("default", "archer"));

        data.put("kills", doc.getInteger("kills"));
        data.put("deaths", doc.getInteger("deaths"));
        data.put("streak", doc.getInteger("streak"));
        data.put("coins", doc.getInteger("coins"));
        data.put("last_kit", doc.getString("last_kit"));

        bounty = doc.getInteger("bounty");

        healType = HealType.valueOf(doc.getString("heal_type"));

        if (Core.i().getSpawnRegion().contains(loc())) {
            spawn(false);
        }

        scoreboard = new DefaultScoreboard(player);
    }

    public void save() {
        Document update = new Document("kills", data.get("kills"))
                .append("deaths", data.get("deaths"))
                .append("streak", data.get("streak"))
                .append("coins", data.get("coins"))
                .append("bounty", bounty)
                .append("last_kit", data.get("last_kit"))
                .append("heal_type", healType.toString())
                .append("kits", kits)
                .append("perks", perks);

        Core.i().getMongoManager().getUserdata().updateOne(Filters.eq("uuid", uuid()), new Document("$set", update));
    }

    public void unload() {
        refreshCombatTagForceField(false);
        save();

        if (Event.getCurrent() != null && Event.getCurrent().getUsers().contains(this)) {
            Event.getCurrent().remove(this);
        }
    }

    public void spawn() {
        spawn(true);
    }

    public void spawn(boolean teleport) {
        spawn = true;
        if (teleport) {
            player.teleport(Core.i().getSpawn());
        }

        if (player.hasMetadata("staffmode")) return;

        if (!(scoreboard instanceof DefaultScoreboard)) {
            scoreboard = new DefaultScoreboard(player);
        }

        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.SURVIVAL);

        combat = 0;

        clearEffects();
        setContents(new ItemStack[40]);
        ItemBuilder events = new ItemBuilder(Material.BOOK).setName("&eEvents");
        ItemBuilder perks = new ItemBuilder(Material.ENDER_CHEST).setName("&ePerks");
        ItemBuilder selector = new ItemBuilder(Material.CHEST).setName("&eKit Selector");
        ItemBuilder cycleHeal = new ItemBuilder(Material.MUSHROOM_SOUP).setName("&eCycle Healing: &d" + ChatUtil.upperFirst(healType.name()));
        ItemBuilder leaderboards = new ItemBuilder(Material.NETHER_STAR).setName("&eLeaderboards &a&lNEW!");
        if (healType == HealType.POTS) {
            cycleHeal.setMaterial(Material.POTION).setDura(16421);
        }
        player.getInventory().setItem(0, events.build());
        player.getInventory().setItem(3, perks.build());
        player.getInventory().setItem(4, selector.build());
        player.getInventory().setItem(5, cycleHeal.build());
        player.getInventory().setItem(8, leaderboards.build());
        player.getInventory().setHeldItemSlot(4);
    }

    public void clearEffects() {
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type != null && player.hasPotionEffect(type)) {
                player.removePotionEffect(type);
            }
        }
    }

    public void pay(int coins) {
        data.put("coins", (int) data.get("coins") + coins);
    }

    public boolean charge(int coins) {
        if ((int) get("coins") >= coins) {
            data.put("coins", (int) data.get("coins") - coins);
            return true;
        }
        return false;
    }

    public void refreshCombatTagForceField(boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(Core.i(), ()-> {
                Set<Location> changedBlocks = ForceFieldUtil.getChangedBlocks(player);

                Set<Location> removeBlocks = data.containsKey("previous-forcefield") ? get("previous-forcefield") : new HashSet<>();

                for (Location location : changedBlocks) {
                    player.sendBlockChange(location, Material.STAINED_GLASS, (byte)14);
                    removeBlocks.remove(location);
                }

                for (Location location : removeBlocks) {
                    Block block = location.getBlock();
                    player.sendBlockChange(location, block.getType(), block.getData());
                }

                data.put("previous-forcefield", changedBlocks);
            });
        } else {
            Set<Location> changedBlocks = ForceFieldUtil.getChangedBlocks(player);

            Set<Location> removeBlocks = data.containsKey("previous-forcefield") ? get("previous-forcefield") : new HashSet<>();

            for (Location location : changedBlocks) {
                player.sendBlockChange(location, Material.STAINED_GLASS, (byte)14);
                removeBlocks.remove(location);
            }

            for (Location location : removeBlocks) {
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getType(), block.getData());
            }

            data.put("previous-forcefield", changedBlocks);
        }
    }

    public int getPerkLevel(Perk perk) {
        if (perks.containsKey(perk.name())) {
            return perks.getInteger(perk.name());
        }
        return 0;
    }

    public Location loc() {
        return player.getLocation();
    }

    public Player p() {
        return player;
    }

    public String coloredName() {
        return PlaceholderAPI.setPlaceholders(player, "%vault_prefix_color%") + player.getName();
    }

    public String uuid() {
        return player.getUniqueId().toString();
    }

    public String name() {
        return player.getName();
    }

    public void msg(String s) {
        player.sendMessage(ChatUtil.color(s));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public void setContents(ItemStack[] contents) {
        for (int i = 0; i < Math.min(contents.length, 40); i++) {
            player.getInventory().setItem(i, contents[i]);
        }
        player.updateInventory();
    }
}
