package io.github.fisher2911.schematicpaster.schematic;

import io.github.fisher2911.fisherlib.manager.Manager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SchematicTaskManager implements Manager<SchematicTask, Integer>  {

    public static final String FOLDER_NAME = "schematics";

    private final Map<Integer, SchematicTask> currentTasks;

    public SchematicTaskManager(Map<Integer, SchematicTask> currentTasks) {
        this.currentTasks = currentTasks;
    }

    @Override
    public Optional<SchematicTask> get(Integer id) {
        return Optional.ofNullable(this.currentTasks.get(id));
    }

    @Override
    public SchematicTask forceGet(Integer id) {
        return this.currentTasks.get(id);
    }

    public void add(SchematicTask task) {
        this.currentTasks.put(task.getId(), task);
    }

    public void remove(Integer id) {
        this.currentTasks.remove(id);
    }

    public Collection<SchematicTask> getAll() {
        return this.currentTasks.values();
    }

}
