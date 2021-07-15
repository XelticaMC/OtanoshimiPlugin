package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;

import work.xeltica.craft.core.utils.Config;

public class VehicleStore {
    public VehicleStore() {
        VehicleStore.instance = this;
        logger = Bukkit.getLogger();
        this.cm = new Config("vehicles");
    }

    public static VehicleStore getInstance() {
        return VehicleStore.instance;
    }

    public void registerVehicle(Vehicle vehicle) {
        if (!isValidVehicle(vehicle)) {
            return;
        }
        var id = vehicle.getUniqueId().toString();

        // 初期値を登録
        cm.getConf().set(id, 20 * 60 * 5);
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Registered vehicle ID:" + id);
    }

    public void unregisterVehicle(Vehicle vehicle) {
        if (!isValidVehicle(vehicle)) {
            return;
        }

        var id = vehicle.getUniqueId().toString();

        // 削除
        unregisterVehicle(id);
        logger.info("Unregistered vehicle ID:" + id);
    }

    public void tick(int tickCount) {
        var c = this.cm.getConf();
        var ids = c.getKeys(false);
        for (var id : ids) {
            var val = c.getInt(id);
            val -= tickCount;
            c.set(id, val);
            if (val <= 0) {
                var e = Bukkit.getEntity(UUID.fromString(id));
                if (e == null) {
                    logger.warning("A vehicle ID:" + id + " is not found on the server, so skipped to despawn.");
                } else {
                    logger.info("A vehicle ID:" + id + "(" + e.getType() + ") has been destroyed");
                    e.remove();
                }
                unregisterVehicle(id);
            }
        }
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidVehicle(Vehicle v) {
        return v instanceof Boat || v instanceof RideableMinecart;
    }

    public void reloadStore() {
        this.cm.reload();
    }

    public void writeStore() throws IOException {
        this.cm.save();
    }

    private void unregisterVehicle(String id) {
        // 削除
        cm.getConf().set(id, null);
        try {
            writeStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static VehicleStore instance;
    private Config cm;
    private Logger logger;
}
