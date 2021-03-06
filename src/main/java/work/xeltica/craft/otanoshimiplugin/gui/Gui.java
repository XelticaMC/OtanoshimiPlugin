package work.xeltica.craft.otanoshimiplugin.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class Gui implements Listener {
    public static Gui getInstance() {
        return instance == null ? (instance = new Gui()) : instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void openMenu(Player player, String title, MenuItem... items) {
        if (isBedrock(player)) {
            openMenuBedrockImpl(player, title, items);
        } else {
            openMenuJavaImpl(player, title, items);
        }
    }

    public void openDialog(Player player, String title, String content) {
        openDialog(player, title, content, null);
    }

    public void openDialog(Player player, String title, String content, Consumer<BookButtonEventArgs> callback) {
        openDialog(player, title, content, callback, null);
    }

    public void openDialog(Player player, String title, String content, Consumer<BookButtonEventArgs> callback, String okButtonText) {
        var okText = okButtonText == null ? "かしこま！" : okButtonText;

        if (isBedrock(player)) {
            openDialogBedrockImpl(player, title, content, callback, okText);
        } else {
            openDialogJavaImpl(player, title, content, callback, okText);
        }
    }

    public void handleCommand(String id) {
        if (!bookHandlersMap.containsKey(id)) return;
        var t = bookHandlersMap.get(id);
        t.handler.accept(t.eventArgs);
        bookHandlersMap.remove(id);
        return;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        var inv = e.getInventory();
        var p = e.getWhoClicked();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;
        e.setCancelled(true);

        var menuItems = invMap.get(inv);
        var id = e.getRawSlot();
    
        if (menuItems.length <= id) return;
        p.closeInventory();
        var handler = menuItems[id].getOnClick();
        if (handler != null) handler.accept(menuItems[id]);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        var inv = e.getInventory();

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return;
        
        // GC
        invMap.remove(inv);
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        if (bookSet.contains(e.getPreviousBookMeta())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("てすてすてすとですとよ");
            Bukkit.getLogger().info("テスト");
        }
    }

    private void openMenuJavaImpl(Player player, String title, MenuItem[] items) {
        var inv = Bukkit.createInventory(null, (1 + items.length / 9) * 9, title);

        Arrays.stream(items).map(i -> {
            var item = new ItemStack(i.getIcon(), i.getCount());
            if (i.isShiny()) {
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            }
            var meta = item.getItemMeta();
            meta.setDisplayName(i.getName());
            item.setItemMeta(meta);

            return item;
        }).forEach(i -> inv.addItem(i));

        invMap.put(inv, items);
        player.openInventory(inv);        
    }

    private void openMenuBedrockImpl(Player player, String title, MenuItem[] items) {
        var builder = SimpleForm.builder()
            .title(title);
        
        for (var item : items) {
            var text = item.getName();
            if (item.isShiny()) {
                text = ChatColor.RED + text;
            }
            builder.button(text);
        }

        builder.responseHandler((form, data) -> {
            var res = form.parseResponse(data);
            if (!res.isCorrect()) {
                return;
            }

            var id = res.getClickedButtonId();
            var callback = items[id].getOnClick();
            if (callback != null) {
                callback.accept(items[id]);
            }
        });
        
        var fPlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        fPlayer.sendForm(builder);
    }

    private void openDialogJavaImpl(Player player, String title, String content, Consumer<BookButtonEventArgs> callback, String okButtonText) {
        var book = new ItemStack(Material.WRITTEN_BOOK);
        var meta = (BookMeta)book.getItemMeta();

        var handleString = UUID.randomUUID().toString().replace("-", "");

        var okButton = new TextComponent(okButtonText);
        okButton.setUnderlined(true);
        okButton.setBold(true);
        okButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/__otanoshimi_gui_event__ " + handleString));
      
        var components = new ComponentBuilder()
            .append(title + "\n\n")
            .bold(true)
            .append(content + "\n\n")
            .reset()
            .append(okButton)
            .create();

        meta.spigot().addPage(components);
        meta.setAuthor("XelticaMC");
        meta.setTitle(title);

        book.setItemMeta(meta);

        bookSet.add(meta);

        player.openBook(book);

        if (callback != null) {
            bookHandlersMap.put(handleString, new HandlerTuple(callback, new BookButtonEventArgs(player), meta));
        }
    }

    private void openDialogBedrockImpl(Player player, String title, String content, 
            Consumer<BookButtonEventArgs> callback, String okButtonText) {
        var api = FloodgateApi.getInstance();
        var form = SimpleForm.builder()
            .title(title)
            .content(content)
            .button(okButtonText)
            .responseHandler((f, r) -> {
                var res = f.parseResponse(r);
                if (!res.isCorrect()) return;
                callback.accept(new BookButtonEventArgs(player));
            });
        api.getPlayer(player.getUniqueId()).sendForm(form);
    }

    private static boolean isBedrock(Player player) {
        return FloodgateApi.getInstance().isFloodgateId(player.getUniqueId());
    }

    private final HashMap<Inventory, MenuItem[]> invMap = new HashMap<>();
    private final HashMap<String, HandlerTuple> bookHandlersMap = new HashMap<>();
    private final HashSet<BookMeta> bookSet = new HashSet<>();
    private static Gui instance;

    class HandlerTuple {
        public Consumer<BookButtonEventArgs> handler;
        public BookMeta meta;
        public BookButtonEventArgs eventArgs;

        public HandlerTuple(Consumer<BookButtonEventArgs> handler, BookButtonEventArgs eventArgs, BookMeta meta) {
            this.handler = handler;
            this.eventArgs = eventArgs;
            this.meta = meta;
        }
    }
}
