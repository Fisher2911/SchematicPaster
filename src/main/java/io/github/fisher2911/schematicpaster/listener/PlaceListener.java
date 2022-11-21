package io.github.fisher2911.schematicpaster.listener;

import io.github.fisher2911.fisherlib.listener.CoreListener;
import io.github.fisher2911.fisherlib.world.WorldPosition;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.schematic.PlaceResult;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilderManager;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import io.github.fisher2911.schematicpaster.user.UserManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceListener extends CoreListener {

    private final UserManager userManager;
    private final SchematicBuilderManager schematicBuilderManager;

    public PlaceListener(SchematicPasterPlugin plugin) {
        super(plugin);
        this.userManager = plugin.getUserManager();
        this.schematicBuilderManager = plugin.getSchematicBuilderManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        final ItemStack itemStack = event.getItemInHand();
        final Player player = event.getPlayer();
        final SchematicUser user = this.userManager.forceGet(player);
        if (user == null) return;
        if (this.schematicBuilderManager.getByItem(itemStack).isEmpty()) {
            return;
        }
        event.setCancelled(true);
        this.schematicBuilderManager.place(
                itemStack,
                user, WorldPosition.fromLocation(event.getBlock().getLocation()),
                result -> {
                    if (result == PlaceResult.FAILED) {
                        return;
                    }
                    if (result == PlaceResult.SUCCESS) {
                        event.setCancelled(true);
                        if (player.getGameMode() == GameMode.CREATIVE) return;
                        final ItemStack setItem = itemStack.clone();
                        setItem.setAmount(itemStack.getAmount() - 1);
                        player.getInventory().setItemInMainHand(setItem);
                    }
                }
        );

    }

}
