package io.github.fisher2911.schematicpaster.placeholder;

import io.github.fisher2911.fisherlib.placeholder.Placeholder;
import io.github.fisher2911.fisherlib.placeholder.Placeholders;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilder;
import io.github.fisher2911.schematicpaster.user.SchematicUser;

import static io.github.fisher2911.fisherlib.placeholder.Placeholder.fromString;

public class SchematicPlaceholders extends Placeholders {

    public static final Placeholder BUILDER_ID = fromString("builder_id");
    public static final Placeholder BUILDER_NAME = fromString("builder_name");

    public SchematicPlaceholders() {
        super.load();
        this.put(SchematicBuilder.class, BUILDER_ID, u -> castAndParse(SchematicBuilder.class, u, SchematicBuilder::getId));
        this.put(SchematicBuilder.class, BUILDER_NAME, u -> castAndParse(SchematicBuilder.class, u, SchematicBuilder::getName));
        this.put(SchematicUser.class, Placeholder.USER_NAME, u -> castAndParse(SchematicUser.class, u, SchematicUser::getName));
    }

}
