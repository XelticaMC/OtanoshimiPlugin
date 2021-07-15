package work.xeltica.craft.core.handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import work.xeltica.craft.core.stores.VehicleStore;

public class VehicleHandler implements Listener {
    @EventHandler
    public void onEnter(VehicleEnterEvent e) {
        var entity = e.getEntered();
        if (!(entity instanceof Player)) return;
        VehicleStore.getInstance().unregisterVehicle(e.getVehicle());
    }

    @EventHandler
    public void onExit(VehicleExitEvent e) {
        VehicleStore.getInstance().registerVehicle(e.getVehicle());
    }

    @EventHandler
    public void onCreated(VehicleCreateEvent e) {
        VehicleStore.getInstance().registerVehicle(e.getVehicle());
    }

    @EventHandler
    public void onDestroyed(VehicleDestroyEvent e) {
        var v = e.getVehicle();
        if (VehicleStore.getInstance().isValidVehicle(v)) {
            e.setCancelled(true);
            e.getVehicle().remove();
        }
        VehicleStore.getInstance().unregisterVehicle(v);
    }
}
