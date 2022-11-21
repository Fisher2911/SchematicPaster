package io.github.fisher2911.schematicpaster.gui;

import io.github.fisher2911.fisherlib.config.serializer.GuiItemSerializer;
import io.github.fisher2911.fisherlib.gui.AbstractGuiManager;
import io.github.fisher2911.fisherlib.gui.Gui;
import io.github.fisher2911.fisherlib.gui.GuiOpener;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.user.SchematicUser;

import java.util.List;

public class SchematicGuiManager extends AbstractGuiManager<SchematicUser, SchematicPasterPlugin> {

    protected static final List<String> DEFAULT_FILES = List.of(
//            "gui.yml"
    );

    public SchematicGuiManager(SchematicPasterPlugin plugin) {
        super(plugin, DEFAULT_FILES, null); // todo
    }

    @Override
    protected void openHandler(GuiOpener<SchematicUser> guiOpener, Gui.Builder builder, SchematicUser user) {
        guiOpener.open(user);
    }

}
