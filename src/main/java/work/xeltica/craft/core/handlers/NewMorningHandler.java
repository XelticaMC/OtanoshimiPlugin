package work.xeltica.craft.core.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import work.xeltica.craft.core.events.NewMorningEvent;
import work.xeltica.craft.core.stores.OmikujiStore;

public class NewMorningHandler implements Listener {
    @EventHandler
    public void onNewMorning(NewMorningEvent e) {
        OmikujiStore.getInstance().reset();
    }
}
