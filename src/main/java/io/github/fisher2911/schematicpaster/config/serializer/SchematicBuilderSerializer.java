package io.github.fisher2911.schematicpaster.config.serializer;

import io.github.fisher2911.fisherlib.config.serializer.ItemSerializer;
import io.github.fisher2911.fisherlib.configurate.ConfigurationNode;
import io.github.fisher2911.fisherlib.configurate.serialize.SerializationException;
import io.github.fisher2911.fisherlib.economy.Price;
import io.github.fisher2911.fisherlib.task.TaskChain;
import io.github.fisher2911.fisherlib.util.builder.BaseItemBuilder;
import io.github.fisher2911.fisherlib.util.function.TriFunction;
import io.github.fisher2911.fisherlib.world.WorldPosition;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilder;
import io.github.fisher2911.schematicpaster.schematic.SchematicTask;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import io.github.fisher2911.schematicpaster.util.TriPredicate;

public class SchematicBuilderSerializer {

    private static final String ITEM_SECTION = "item";
    private static final String ID_SECTION = "id";
    private static final String NAME_SECTION = "name";
    private static final String MONEY_COST_SECTION = "money-cost";
    private static final String PLACE_INTERVAL_SECTION = "place-interval";
    private static final String FILE_NAME_SECTION = "file-name";
    private static final String PERMISSION_SECTION = "permission";

    public static SchematicBuilder deserialize(
            ConfigurationNode source,
            TriFunction<SchematicBuilder, SchematicUser, WorldPosition, TaskChain<Object, SchematicTask>> taskFunction,
            TriPredicate<SchematicBuilder, SchematicUser, WorldPosition> permissionPredicate

    ) throws SerializationException {
        final BaseItemBuilder itemBuilder = ItemSerializer.INSTANCE.deserialize(BaseItemBuilder.class, source.node(ITEM_SECTION));
        final String id = source.node(ID_SECTION).getString();
        final String name = source.node(NAME_SECTION).getString();
        final Price cost = Price.money(source.node(MONEY_COST_SECTION).getDouble());
        final int placeInterval = source.node(PLACE_INTERVAL_SECTION).getInt();
        final String fileName = source.node(FILE_NAME_SECTION).getString();
        final String permission = source.node(PERMISSION_SECTION).getString();

        return new SchematicBuilder(
                id,
                name,
                itemBuilder,
                placeInterval,
                cost,
                fileName,
                permission,
                taskFunction,
                permissionPredicate
        );
    }

}
