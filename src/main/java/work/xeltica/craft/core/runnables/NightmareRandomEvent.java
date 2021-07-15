package work.xeltica.craft.core.runnables;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import work.xeltica.craft.core.XCorePlugin;

public class NightmareRandomEvent extends BukkitRunnable {
    public NightmareRandomEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        final var nightmare = plugin.getServer().getWorld("nightmare2");
        final var players = nightmare.getPlayers();
        nightmare.setStorm(true);
        nightmare.setThundering(true);

        for (var player : players) {
            final var l = player.getLocation();

            // プレイヤー周辺でランダムに雷を発生させる
            final var z = Math.sin(random.nextDouble() * 2 * Math.PI);
            final var x = Math.cos(random.nextDouble() * 2 * Math.PI);
            final var sl = l;
            sl.add(x * 64, 0, z * 64);
            sl.setY(sl.getWorld().getHighestBlockYAt(sl.getBlockX(), sl.getBlockZ()));
            sl.getWorld().strikeLightning(sl);

            final var dice = random.nextInt(8);

            final var MODE_BLAZE = 0;
            final var MODE_PIGLIN = 1;
            final var MODE_ILLAGER = 2;
            final var MODE_BEES = 4;
            final var MODE_WOLVES = 5;
            final var MODE_LIGHTNING = 6;
            final var MODE_CREEPER = 7;

            final var pivot = l.add(new Vector(random.nextInt(10) + 5, 0, random.nextInt(10) + 5));
            pivot.setY(l.getWorld().getHighestBlockYAt(pivot.getBlockX(), pivot.getBlockZ()));
            final var amount = random.nextInt(3) + 1;

            final var scheduler = Bukkit.getScheduler();
            final var plugin = XCorePlugin.getInstance();

            switch (dice) {
                case MODE_BLAZE: {
                    for (var i = 0; i < amount; i++) {
                        var loc = pivot.add(new Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
                        loc.setY(l.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
                        nightmare.spawnEntity(loc, EntityType.BLAZE);
                    }
                    break;
                }
                case MODE_PIGLIN: {
                    for (var i = 0; i < amount; i++) {
                        final var loc = pivot.add(new Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
                        loc.setY(l.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);

                        final var piglin = (Piglin) nightmare.spawnEntity(loc, EntityType.PIGLIN);

                        // ゾンビ化しない
                        piglin.setImmuneToZombification(true);

                        // 50:50で武器が決まる
                        final var equip = new ItemStack(random.nextBoolean() ? Material.CROSSBOW : Material.GOLDEN_SWORD);
                        piglin.getEquipment().setItemInMainHand(equip);

                        // 5%の確率でスピードバフ
                        if (random.nextInt(100) < 5) {
                            piglin.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30000, 2));
                        }

                        // 5%の確率で子供
                        if (random.nextInt(100) < 5) {
                            piglin.setBaby();
                        }
                    }
                    break;
                }
                case MODE_ILLAGER: {
                    for (var i = 0; i < amount; i++) {
                        var loc = pivot.add(new Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
                        loc.setY(l.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
                        nightmare.spawnEntity(loc, illigers[random.nextInt(illigers.length)]);
                    }
                    break;
                }
                case MODE_BEES: {
                    pivot.add(0, 5, 0);

                    for (var i = 0; i < amount; i++) {
                        var loc = pivot.add(new Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
                        loc.setY(l.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ() + 5));
                        var bee = (Bee)nightmare.spawnEntity(loc, EntityType.BEE);
                        // 30分おいかり
                        bee.setAnger(20 * 60 * 30);
                        bee.setTarget(player);
                    }
                    break;
                }
                case MODE_WOLVES: {
                    for (var i = 0; i < amount; i++) {
                        var loc = pivot.add(new Vector(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
                        loc.setY(l.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
                        var wolf = (Wolf)nightmare.spawnEntity(loc, EntityType.WOLF);
                        // 30分おいかり
                        wolf.setAngry(true);
                        wolf.setTarget(player);
                    }
                    break;
                }
                case MODE_LIGHTNING: {
                    Runnable strike = () -> {
                        l.getWorld().strikeLightning(l);
                    };
                    // 今いる場所に、5分後に雷を3つ落とす
                    scheduler.runTaskLater(plugin, () -> {
                        scheduler.runTask(plugin, strike);
                        scheduler.runTaskLater(plugin, strike, 10);
                        scheduler.runTaskLater(plugin, strike, 20);
                    }, 20 * 60 * 5);
                    break;
                }
                case MODE_CREEPER: {
                    var creeper = (Creeper)l.getWorld().spawnEntity(l, EntityType.CREEPER);
                    creeper.setPowered(true);
                    scheduler.runTaskLater(plugin, () -> {
                        creeper.ignite();
                    }, 20 * 60 * 5);
                    break;
                }
            }
        }
    }

    private Plugin plugin;
    private final Random random = new Random();
    private final EntityType[] illigers = {
        EntityType.PILLAGER,
        EntityType.VINDICATOR,
        EntityType.RAVAGER,
        EntityType.WITCH,
        EntityType.EVOKER,
    };
}
