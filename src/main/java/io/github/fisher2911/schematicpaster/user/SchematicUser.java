package io.github.fisher2911.schematicpaster.user;

import io.github.fisher2911.fisherlib.user.CoreUser;

import java.util.Set;

public interface SchematicUser extends CoreUser {

    Set<Integer> getCurrentTasks();

    void addTask(int id);

    void removeTask(int id);

}
