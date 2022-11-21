package io.github.fisher2911.schematicpaster.config;

import io.github.fisher2911.fisherlib.FishPlugin;
import io.github.fisher2911.fisherlib.config.BaseSettings;
import io.github.fisher2911.fisherlib.configurate.yaml.YamlConfigurationLoader;
import io.github.fisher2911.schematicpaster.command.SchematicPermissions;

import java.io.IOException;

public class SchematicSettings extends BaseSettings {

    public SchematicSettings(FishPlugin<?, ?> plugin) {
        super(plugin, "config.yml");
    }

    private static final String COMMANDS_PATH = "commands";
    private static final String COMMANDS_PER_HELP_PAGE_PATH = "commands-per-help-page";
    private static final String USER_PATH = "user";
    private static final String MAX_TASKS_PATH = "max-tasks";

    private int commandsPerHelpPage;
    private int maxTasks;

    public void load() {
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(this.path)
                .build();

        try {
            final var source = loader.load();
            this.commandsPerHelpPage = source.node(COMMANDS_PATH, COMMANDS_PER_HELP_PAGE_PATH).getInt(5);
            this.maxTasks = source.node(USER_PATH, MAX_TASKS_PATH).getInt(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCommandsPerHelpPage() {
        return this.commandsPerHelpPage;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    @Override
    public String getAdminHelpPermission() {
        return SchematicPermissions.ADMIN_HELP_PERMISSION;
    }

}
