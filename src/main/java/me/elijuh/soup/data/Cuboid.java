package me.elijuh.soup.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@Getter
@AllArgsConstructor
public class Cuboid {
    private final int minX, minY, minZ, maxX, maxY, maxZ;

    public boolean contains(Location loc) {
        return contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean contains(int x, int y, int z) {
        if (x < minX || x > maxX) {
            return false;
        }
        if (y < minY || y > maxY) {
            return false;
        }
        return z >= minZ && z <= maxZ;
    }
}
