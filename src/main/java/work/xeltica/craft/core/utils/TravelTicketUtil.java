package work.xeltica.craft.core.utils;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.travels.TicketType;

public class TravelTicketUtil {

    public static ItemStack GenerateTravelTicket(String ticketType) {
        return GenerateTravelTicket(ticketType);
    }

    public static ItemStack GenerateTravelTicket(int amount, TicketType ticketType) {
        var ticket = new ItemStack(Material.WRITTEN_BOOK, amount);
        var meta = ticket.getItemMeta();
        meta.displayName(Component.text(String.format(TRAVEL_TICKET_NAME, ticketType.getDisplayName())));
        var list = new ArrayList<String>();
        list.add(TRAVEL_TICKET_LORE1);
        list.add(TRAVEL_TICKET_LORE2);
        list.add(TRAVEL_TICKET_LORE3);
        list.add("");
        list.add(ticketType.toString());
        meta.lore(list.stream().map(l -> Component.text(l).asComponent()).toList());
        ticket.setItemMeta(meta);
        return ticket;
    }

    public static boolean isTravelTicket(ItemStack i) {
        if (i == null) return false;
        if (i.getType() != Material.WRITTEN_BOOK) return false;
        var meta = i.getItemMeta();

        var lores = meta.lore().stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c)).toList();
        if (lores == null || lores.size() != 5) return false;
        if (!lores.get(0).equals(TRAVEL_TICKET_LORE1)) return false;
        if (!lores.get(1).equals(TRAVEL_TICKET_LORE2)) return false;
        if (!lores.get(2).equals(TRAVEL_TICKET_LORE3)) return false;

        return true;
    }

    public static TicketType getTicketType(ItemStack i) {
        var meta = i.getItemMeta();
        var lores = meta.lore();

        return TicketType.valueOf(PlainTextComponentSerializer.plainText().serialize(lores.get(4)));
    }

    private static final String TRAVEL_TICKET_NAME = ChatColor.AQUA + "%s行き旅行券";
    private static final String TRAVEL_TICKET_LORE1 = "手に持って右クリックまたはタップすると、";
    private static final String TRAVEL_TICKET_LORE2 = "遠く離れた土地にひとっ飛び。";
    private static final String TRAVEL_TICKET_LORE3 = "使う前に必ず準備をすること。";
}
