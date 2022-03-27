package me.elijuh.soup.events.arena;

import me.elijuh.soup.data.Cuboid;
import org.bukkit.Location;

public interface EventArena {
    String getId();
    Cuboid getRegion();
    Location getSpawn();
}
