package work.xeltica.craft.core.plugins;

import org.bukkit.entity.Player;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import work.xeltica.craft.core.stores.PlayerFlagsStore;

public class CitizenTimerCalculator implements ContextCalculator<Player> {

    @Override
    public void calculate(Player target, ContextConsumer contextConsumer) {
        contextConsumer.accept(KEY, PlayerFlagsStore.getInstance().isNewcomer(target) ? "false" : "true");
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add(KEY, "false");
        builder.add(KEY, "true");
        return builder.build();
    }

    private static final String KEY = "otanoshimi:citizentimerelapsed";

}