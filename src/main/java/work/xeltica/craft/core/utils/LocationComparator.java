package work.xeltica.craft.core.utils;

import org.bukkit.Location;

public class LocationComparator {
    public static boolean equals(Location l1, Location l2) {
        if (!l1.getWorld().equals(l2.getWorld())) return false;

        var x1 = l1.getX();
        var y1 = l1.getY();
        var z1 = l1.getZ();

        var x2 = l2.getX();
        var y2 = l2.getY();
        var z2 = l2.getZ();
        return x1 == x2 && y1 == y2 && z1 == z2;
    }
}
