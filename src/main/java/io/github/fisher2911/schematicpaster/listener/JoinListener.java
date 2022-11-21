package io.github.fisher2911.schematicpaster.listener;

import io.github.fisher2911.fisherlib.listener.CoreListener;
import io.github.fisher2911.fisherlib.task.TaskChain;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.data.DataManager;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilderManager;
import io.github.fisher2911.schematicpaster.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener extends CoreListener {

    private final DataManager dataManager;
    private final UserManager userManager;

    public JoinListener(SchematicPasterPlugin plugin) {
        super(plugin);
        this.userManager = plugin.getUserManager();
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.userManager.add(player);
        this.userManager.get(player.getUniqueId())
                .ifPresent(user -> TaskChain.create(this.plugin)
                        .supplyAsync(() -> this.dataManager.loadUserSchematicTasks(user.getId(), SchematicBuilderManager.TASK_REGION_BI_CONSUMER))
                        .consumeSync(tasks -> tasks.forEach(task -> user.addTask(task.getId()))));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.userManager.remove(event.getPlayer().getUniqueId());
    }
}
