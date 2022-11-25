package io.github.fisher2911.schematicpaster.schematic;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import io.github.fisher2911.fisherlib.config.Config;
import io.github.fisher2911.fisherlib.configurate.yaml.YamlConfigurationLoader;
import io.github.fisher2911.fisherlib.manager.Manager;
import io.github.fisher2911.fisherlib.message.MessageHandler;
import io.github.fisher2911.fisherlib.task.TaskChain;
import io.github.fisher2911.fisherlib.util.function.TriFunction;
import io.github.fisher2911.fisherlib.world.ChunkPos;
import io.github.fisher2911.fisherlib.world.WorldPosition;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.config.serializer.SchematicBuilderSerializer;
import io.github.fisher2911.schematicpaster.data.DataManager;
import io.github.fisher2911.schematicpaster.message.SchematicMessages;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import io.github.fisher2911.schematicpaster.user.UserManager;
import io.github.fisher2911.schematicpaster.util.DirectionUtil;
import io.github.fisher2911.schematicpaster.util.PDCUtil;
import io.github.fisher2911.schematicpaster.util.TriPredicate;
import io.github.fisher2911.schematicpaster.world.Region;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SchematicBuilderManager extends Config implements Manager<SchematicBuilder, String> {

    private final SchematicPasterPlugin plugin;
    private final Map<String, SchematicBuilder> builders;
    private final MessageHandler messageHandler;

    public SchematicBuilderManager(SchematicPasterPlugin plugin, Map<String, SchematicBuilder> builders) {
        super(plugin, "builder-items.yml");
        this.plugin = plugin;
        this.builders = builders;
        this.messageHandler = plugin.getMessageHandler();
    }

    @Override
    public Optional<SchematicBuilder> get(String id) {
        return Optional.ofNullable(this.builders.get(id));
    }

    @Override
    public SchematicBuilder forceGet(String id) {
        return this.builders.get(id);
    }

    public Optional<SchematicBuilder> getByItem(ItemStack itemStack) {
        return Optional.ofNullable(this.builders.get(PDCUtil.getSchematicBuilderId(itemStack)));
    }

    public void giveItem(@Nullable SchematicUser giver, SchematicUser user, String id, int amount) {
        this.get(id).ifPresentOrElse(builder -> {
            final ItemStack itemStack = builder.getPlaceItem();
            final Player player = user.getPlayer();
            if (player == null) {
                if (giver != null) {
                    this.messageHandler.sendMessage(
                            giver,
                            SchematicMessages.PLAYER_NOT_FOUND
                    );
                }
                return;
            }
            itemStack.setAmount(amount);
            user.getPlayer().getInventory().addItem(itemStack);
            this.messageHandler.sendMessage(
                    user,
                    SchematicMessages.GIVEN_ITEM,
                    builder
            );
            if (giver != null) {
                this.messageHandler.sendMessage(
                        giver,
                        SchematicMessages.GAVE_ITEM,
                        user,
                        builder
                );
            }
        }, () -> {
            if (giver == null) return;
            this.messageHandler.sendMessage(giver, SchematicMessages.SCHEMATIC_ITEM_NOT_FOUND);
        });
    }

    public void place(ItemStack itemStack, SchematicUser user, WorldPosition position, Consumer<PlaceResult> result) {
        this.getByItem(itemStack).ifPresent(builder -> {
            final TaskChain<Object, SchematicTask> task = builder.startBuilding(user, position);
            task.consumeSync(schematicTask -> {
                if (schematicTask == null) {
                    result.accept(PlaceResult.FAILED);
                    return;
                }
                result.accept(PlaceResult.SUCCESS);
            }).execute();
        });
    }

    public Collection<String> getBuilderIds() {
        return this.builders.keySet();
    }

    public static final BiConsumer<SchematicTask, Region> TASK_REGION_BI_CONSUMER = (schematicTask, region) -> {
        final SchematicPasterPlugin plugin = SchematicPasterPlugin.getPlugin(SchematicPasterPlugin.class);
        final UserManager userManager = plugin.getUserManager();
        final Optional<SchematicUser> optionalUser = userManager.get(schematicTask.getUserId());
        optionalUser.ifPresent(user -> {
            final SchematicBuilder builder = plugin.getSchematicBuilderManager().forceGet(schematicTask.getConfigId());
            if (user.isOnline() && builder != null) {
                plugin.getMessageHandler().sendMessage(
                        user,
                        SchematicMessages.SCHEMATIC_BUILD_COMPLETE,
                        builder,
                        region
                );
            }
            user.getCurrentTasks().remove(schematicTask.getId());
            plugin.getSchematicTaskManager().remove(schematicTask.getId());
            TaskChain.create(plugin)
                    .runAsync(() -> plugin.getDataManager().deleteSchematicTask(schematicTask.getId()))
                    .execute();
        });
    };

    public static final TriFunction<SchematicBuilder, SchematicUser, WorldPosition, TaskChain<Object, SchematicTask>> TASK_FUNCTION = (builder, user, position) -> {
        final SchematicPasterPlugin plugin = SchematicPasterPlugin.getPlugin(SchematicPasterPlugin.class);
        final DataManager dataManager = plugin.getDataManager();
        final int x = position.position().getBlockX();
        final int y = position.position().getBlockY();
        final int z = position.position().getBlockZ();
        if (!user.isOnline()) return null;
        return TaskChain.create(plugin)
                .supplyAsync(() -> dataManager.newSchematicTask(
                        builder.getId(),
                        BlockVector3.at(x, y, z),
                        DirectionUtil.getFacing(user.getPlayer()),
                        position.world(),
                        user.getId(),
                        ChunkPos.chunkKeyAt(position.position().getChunkX(), position.position().getChunkZ()),
                        TASK_REGION_BI_CONSUMER
                ))
                .sync(optional -> optional.map(task -> {
                                    user.getCurrentTasks().add(task.getId());
                                    try {
                                        task.start();
                                    } catch (IOException | WorldEditException e) {
                                        e.printStackTrace();
                                    }
                                    return task;
                                }
                        ).orElse(null)

                );
//                    try {
//                        return builder.getSchematic().paste(
//                                BukkitAdapter.adapt(user.getPlayer().getWorld()),
//                                BlockVector3.at(x, y, z),
//                                false,
//                                false
//                        );
//                    } catch (WorldEditException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                })
//        final SchematicTask task = new SchematicTask(
//                id.getAndIncrement(),
//                plugin,
//                plugin.getDataFolder().toPath().resolve(SchematicTaskManager.FOLDER_NAME).resolve(builder.getFileName()).toFile(),
//                BukkitAdapter.adapt(Bukkit.getWorld(position.world())),
//                BlockVector3.at(x, y, z),
//                DirectionUtil.getFacing(user.getPlayer()),
//                builder.getPlaceInterval(),
//                (schematicTask, region) -> {
//                    plugin.getMessageHandler().sendMessage(
//                            user,
//                            SchematicMessages.SCHEMATIC_BUILD_COMPLETE,
//                            builder,
//                            region
//                    );
//                    user.getCurrentTasks().remove(schematicTask.getId());
//                }
//        );
//        try {
//            task.start();
//        } catch (IOException | WorldEditException e) {
//            e.printStackTrace();
//        }
    };

    public static final TriPredicate<SchematicBuilder, SchematicUser, WorldPosition> PERMISSION_PREDICATE = (builder, user, position) -> {
        final SchematicPasterPlugin plugin = SchematicPasterPlugin.getPlugin(SchematicPasterPlugin.class);
        if (builder.getPermission() != null && !user.hasPermission(builder.getPermission())) {
            plugin.getMessageHandler().sendMessage(
                    user,
                    SchematicMessages.NO_PERMISSION_TO_PLACE_SCHEMATIC,
                    builder
            );
            return false;
        }
        if (user.getCurrentTasks().size() >= plugin.getSchematicSettings().getMaxTasks()) {
            plugin.getMessageHandler().sendMessage(user.getPlayer(), SchematicMessages.MAX_TASKS);
            return false;
        }
        return true;
    };

    public void register(SchematicBuilder builder) {
        this.builders.put(builder.getId(), builder);
    }

    private static final String BUILDER_ITEMS_PATH = "builder-items";

    public void load() {
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(this.path)
                .build();

        try {
            final var source = loader.load();
            final var node = source.node(BUILDER_ITEMS_PATH);
            for (var child : node.childrenMap().values()) {
                final var builder = SchematicBuilderSerializer.deserialize(child, TASK_FUNCTION, PERMISSION_PREDICATE);
                this.register(builder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
