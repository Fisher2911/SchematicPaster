package io.github.fisher2911.schematicpaster.listener;

import com.sk89q.worldedit.WorldEditException;
import io.github.fisher2911.fisherlib.FishPlugin;
import io.github.fisher2911.fisherlib.listener.CoreListener;
import io.github.fisher2911.fisherlib.task.TaskChain;
import io.github.fisher2911.fisherlib.world.ChunkPos;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilderManager;
import io.github.fisher2911.schematicpaster.schematic.SchematicTask;
import io.github.fisher2911.schematicpaster.schematic.SchematicTaskManager;
import io.github.fisher2911.schematicpaster.util.PDCUtil;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChunkListener extends CoreListener {

    public ChunkListener(FishPlugin<?, ?> plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        this.loadChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        final SchematicPasterPlugin plugin = (SchematicPasterPlugin) this.plugin;
        final SchematicTaskManager taskManager = plugin.getSchematicTaskManager();
        final int[] currentTasks = PDCUtil.getChunksPastingSchematics(chunk.getPersistentDataContainer());
        final List<SchematicTask> toSave = new ArrayList<>();
        for (int taskID : currentTasks) {
            taskManager.get(taskID).ifPresent(task -> {
                task.stop();
                taskManager.remove(taskID);
                toSave.add(task);
            });
        }

        TaskChain.create(plugin)
                .runAsync(() -> plugin.getDataManager().saveSchematicTasks(toSave))
                .execute();
    }

    public void loadChunk(Chunk chunk) {
        final long chunkKey = ChunkPos.chunkKeyAt(chunk.getX(), chunk.getZ());
        final SchematicPasterPlugin plugin = (SchematicPasterPlugin) this.plugin;
        final int[] currentTasks = PDCUtil.getChunksPastingSchematics(chunk.getPersistentDataContainer());
        if (currentTasks.length == 0) return;
        TaskChain.create(plugin)
                .supplyAsync(() -> plugin.getDataManager().loadChunkTasks(chunkKey, SchematicBuilderManager.TASK_REGION_BI_CONSUMER))
                .consumeSyncLater(tasks -> {
                    tasks.forEach(task -> {
                        plugin.getSchematicTaskManager().add(task);
                        plugin.getUserManager().get(task.getUserId()).ifPresent(user -> user.addTask(task.getId()));
                        try {
                            task.start();
                        } catch (IOException | WorldEditException e) {
                            e.printStackTrace();
                        }
                    });
                }, 1)
                .execute();
    }
}
