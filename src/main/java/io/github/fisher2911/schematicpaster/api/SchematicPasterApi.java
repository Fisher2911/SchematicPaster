package io.github.fisher2911.schematicpaster.api;

import com.sk89q.worldedit.math.BlockVector3;
import io.github.fisher2911.fisherlib.economy.Price;
import io.github.fisher2911.fisherlib.util.builder.BaseItemBuilder;
import io.github.fisher2911.fisherlib.world.ChunkPos;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.data.DataManager;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilder;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilderManager;
import io.github.fisher2911.schematicpaster.schematic.SchematicTask;
import io.github.fisher2911.schematicpaster.world.Region;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SchematicPasterApi {

    private static final SchematicPasterPlugin PLUGIN = SchematicPasterPlugin.getPlugin(SchematicPasterPlugin.class);

    public static Optional<SchematicTask> createSchematicTask(
            String configId,
            UUID userId,
            SchematicPasterPlugin plugin,
            World world,
            BlockVector3 to,
            int rotation,
            BiConsumer<SchematicTask, Region> onComplete
    ) {
        final DataManager dataManager = plugin.getDataManager();
        final long chunkKey = ChunkPos.chunkKeyAt(to.getX() >> 4, to.getZ() >> 4);
        return dataManager.newSchematicTask(configId, to, rotation, world.getUID(), userId, chunkKey, (task, region) -> {
            SchematicBuilderManager.TASK_REGION_BI_CONSUMER.accept(task, region);
            onComplete.accept(task, region);
        });
    }

    public static SchematicBuilder registerSchematicBuilder(
            String id,
            String name,
            BaseItemBuilder placeItem,
            int placeInterval,
            Price price,
            String fileName,
            @Nullable String permission
    ) {
        final SchematicBuilder builder = new SchematicBuilder(
                id,
                name,
                placeItem,
                placeInterval,
                price,
                fileName,
                permission,
                SchematicBuilderManager.TASK_FUNCTION,
                SchematicBuilderManager.PERMISSION_PREDICATE
        );
        PLUGIN.getSchematicBuilderManager().register(builder);
        return builder;
    }

}
