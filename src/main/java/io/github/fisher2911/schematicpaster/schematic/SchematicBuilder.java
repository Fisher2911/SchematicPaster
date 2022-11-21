package io.github.fisher2911.schematicpaster.schematic;

import io.github.fisher2911.fisherlib.economy.Price;
import io.github.fisher2911.fisherlib.task.TaskChain;
import io.github.fisher2911.fisherlib.util.builder.BaseItemBuilder;
import io.github.fisher2911.fisherlib.util.function.TriFunction;
import io.github.fisher2911.fisherlib.world.WorldPosition;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import io.github.fisher2911.schematicpaster.util.PDCUtil;
import io.github.fisher2911.schematicpaster.util.TriPredicate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SchematicBuilder {

    private final String id;
    private final String name;
    private final BaseItemBuilder placeItem;
    private final int placeInterval;
    private final Price price;
    private final String fileName;
    @Nullable
    private final String permission;
    private final TriFunction<SchematicBuilder, SchematicUser, WorldPosition, TaskChain<Object, SchematicTask>> taskFunction;
    private final TriPredicate<SchematicBuilder, SchematicUser, WorldPosition> permissionPredicate;

    public SchematicBuilder(
            String id,
            String name,
            BaseItemBuilder placeItem,
            int placeInterval,
            Price price,
            String fileName,
            @Nullable String permission,
            TriFunction<SchematicBuilder, SchematicUser, WorldPosition, TaskChain<Object, SchematicTask>> taskFunction,
            TriPredicate<SchematicBuilder, SchematicUser, WorldPosition> permissionPredicate
    ) {
        this.id = id;
        this.name = name;
        this.placeItem = placeItem;
        this.placeInterval = placeInterval;
        this.price = price;
        this.fileName = fileName;
        this.permission = permission;
        this.taskFunction = taskFunction;
        this.permissionPredicate = permissionPredicate;
    }

    public TaskChain<Object, SchematicTask> startBuilding(SchematicUser user, WorldPosition position) {
        if (this.permissionPredicate.test(this, user, position)) {
            return this.taskFunction.apply(this, user, position);
        }
        return TaskChain.create(SchematicPasterPlugin.getPlugin(SchematicPasterPlugin.class));
    }

    public String getId() {
        return id;
    }

    public ItemStack getPlaceItem() {
        final ItemStack itemStack = this.placeItem.copy().build();
        PDCUtil.setSchematicBuilderId(itemStack, this.id);
        return itemStack;
    }

    public String getName() {
        return name;
    }

    public int getPlaceInterval() {
        return placeInterval;
    }

    public Price getPrice() {
        return price;
    }

    public String getFileName() {
        return fileName;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    public TriFunction<SchematicBuilder, SchematicUser, WorldPosition, TaskChain<Object, SchematicTask>> getTaskFunction() {
        return taskFunction;
    }

    public TriPredicate<SchematicBuilder, SchematicUser, WorldPosition> getPermissionPredicate() {
        return permissionPredicate;
    }

}
