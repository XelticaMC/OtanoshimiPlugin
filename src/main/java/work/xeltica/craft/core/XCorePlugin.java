package work.xeltica.craft.core;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.luckperms.api.LuckPerms;
import work.xeltica.craft.core.commands.CommandBase;
import work.xeltica.craft.core.commands.CommandBoat;
import work.xeltica.craft.core.commands.CommandCart;
import work.xeltica.craft.core.commands.CommandCat;
import work.xeltica.craft.core.commands.CommandDebug;
import work.xeltica.craft.core.commands.CommandGiveTravelTicket;
import work.xeltica.craft.core.commands.CommandHub;
import work.xeltica.craft.core.commands.CommandLocalTime;
import work.xeltica.craft.core.commands.CommandOmikuji;
import work.xeltica.craft.core.commands.CommandXCoreGuiEvent;
import work.xeltica.craft.core.commands.CommandPromo;
import work.xeltica.craft.core.commands.CommandPvp;
import work.xeltica.craft.core.commands.CommandReport;
import work.xeltica.craft.core.commands.CommandRespawn;
import work.xeltica.craft.core.commands.CommandSignEdit;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.handlers.EntityHandler;
import work.xeltica.craft.core.handlers.HubHandler;
import work.xeltica.craft.core.handlers.NewMorningHandler;
import work.xeltica.craft.core.handlers.PlayerHandler;
import work.xeltica.craft.core.handlers.VehicleHandler;
import work.xeltica.craft.core.handlers.WakabaHandler;
import work.xeltica.craft.core.handlers.WorldHandler;
import work.xeltica.craft.core.plugins.CitizenTimerCalculator;
import work.xeltica.craft.core.plugins.VaultPlugin;
import work.xeltica.craft.core.runnables.DaylightObserver;
import work.xeltica.craft.core.runnables.NightmareRandomEvent;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.OmikujiStore;
import work.xeltica.craft.core.stores.PlayerFlagsStore;
import work.xeltica.craft.core.stores.VehicleStore;
import work.xeltica.craft.core.stores.WorldStore;

public class XCorePlugin extends JavaPlugin {
    public static XCorePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        loadStores();
        loadCommands();
        loadHandlers();
        loadPlugins();

        // 1秒に1回
        new DaylightObserver(this).runTaskTimer(this, 0, 20);
        // 1分に1回
        new NightmareRandomEvent(this).runTaskTimer(this, 0, 20 * 60);
        // 4tickに1回
        // new FlyingObserver().runTaskTimer(this, 0, 4);
        // 10tickに1回
        new BukkitRunnable(){
            @Override
            public void run() {
                VehicleStore.getInstance().tick(10);
                PlayerFlagsStore.getInstance().tickNewcomers(10);
            }
        }.runTaskTimer(this, 0, 10);


        calculator = new CitizenTimerCalculator();
        var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        var luckPerms = provider.getProvider();
        luckPerms.getContextManager().registerCalculator(calculator);

        logger.info("Booted X-Core Plugin.");
    }

    @Override
    public void onDisable() {
        commands.clear();
        Gui.resetInstance();
        unloadPlugins();
        var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        var luckPerms = provider.getProvider();
        luckPerms.getContextManager().unregisterCalculator(calculator);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var name = command.getName().toLowerCase();

        var com = commands.get(name);
        if (com == null) return false;

        return com.execute(sender, command, label, args);
    }

    private void loadStores() {
        new OmikujiStore();
        logger.info("Loaded Omikuji Store");
        new VehicleStore();
        logger.info("Loaded Vehicle Store");
        new PlayerFlagsStore();
        logger.info("Loaded Player Flags Store");
        new HubStore();
        logger.info("Loaded Hub Store");
        new WorldStore();
        logger.info("Loaded World Store");
    }

    private void loadCommands() {
        commands.clear();
        commands.put("omikuji", new CommandOmikuji());
        logger.info("Loaded /omikuji command");
        commands.put("respawn", new CommandRespawn());
        logger.info("Loaded /respawn command");
        commands.put("pvp", new CommandPvp());
        logger.info("Loaded /pvp command");
        commands.put("signedit", new CommandSignEdit());
        logger.info("Loaded /signedit command");
        commands.put("givetravelticket", new CommandGiveTravelTicket());
        logger.info("Loaded /givetravelticket command");
        commands.put("report", new CommandReport());
        logger.info("Loaded /report command");
        commands.put("localtime", new CommandLocalTime());
        logger.info("Loaded /localtime command");
        commands.put("boat", new CommandBoat());
        logger.info("Loaded /boat command");
        commands.put("cart", new CommandCart());
        logger.info("Loaded /cart command");
        commands.put("promo", new CommandPromo());
        logger.info("Loaded /promo command");
        commands.put("cat", new CommandCat());
        logger.info("Loaded /cat command");
        commands.put("hub", new CommandHub());
        logger.info("Loaded /hub command");
        commands.put("debug", new CommandDebug());
        logger.info("Loaded /debug command");
        commands.put("__otanoshimi_gui_event__", new CommandXCoreGuiEvent());
        logger.info("Loaded /__otanoshimi_gui_event__ command");
    }

    private void loadHandlers() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new NewMorningHandler(), this);
        logger.info("Loaded NewMorningHandler");
        pm.registerEvents(new PlayerHandler(this), this);
        logger.info("Loaded PlayerHandler");
        pm.registerEvents(new EntityHandler(), this);
        logger.info("Loaded EntityHandler");
        pm.registerEvents(new VehicleHandler(), this);
        logger.info("Loaded VehicleHandler");
        pm.registerEvents(new WakabaHandler(), this);
        logger.info("Loaded WakabaHandler");
        pm.registerEvents(new HubHandler(), this);
        logger.info("Loaded HubHandler");
        pm.registerEvents(new WorldHandler(), this);
        logger.info("Loaded WorldHandler");
        
        pm.registerEvents(Gui.getInstance(), this);
        logger.info("Loaded Gui EventHandler");
    }

    private void loadPlugins() {
        VaultPlugin.getInstance().onEnable(this);
    }

    private void unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this);
    }

    private Logger logger;
    private final HashMap<String, CommandBase> commands = new HashMap<>();

    private CitizenTimerCalculator calculator;

    private static XCorePlugin instance;
}