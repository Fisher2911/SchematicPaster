package io.github.fisher2911.schematicpaster.data;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fisher2911.fisherlib.data.sql.SQLType;
import io.github.fisher2911.fisherlib.data.sql.condition.WhereCondition;
import io.github.fisher2911.fisherlib.data.sql.dialect.SQLDialect;
import io.github.fisher2911.fisherlib.data.sql.dialect.SystemDialect;
import io.github.fisher2911.fisherlib.data.sql.field.SQLField;
import io.github.fisher2911.fisherlib.data.sql.field.SQLIdField;
import io.github.fisher2911.fisherlib.data.sql.field.SQLKeyType;
import io.github.fisher2911.fisherlib.data.sql.statement.DeleteStatement;
import io.github.fisher2911.fisherlib.data.sql.statement.SQLQuery;
import io.github.fisher2911.fisherlib.data.sql.statement.SQLStatement;
import io.github.fisher2911.fisherlib.data.sql.table.SQLTable;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.schematic.SchematicTask;
import io.github.fisher2911.schematicpaster.schematic.SchematicTaskManager;
import io.github.fisher2911.schematicpaster.world.Region;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DataManager {

    // --------------- Kingdom Table ---------------
    private static final String SCHEMATICS_TABLE_NAME = "schematics";
    private static final SQLField SCHEMATIC_ID_COLUMN = new SQLIdField(SCHEMATICS_TABLE_NAME, "id", SQLType.INTEGER, SQLKeyType.PRIMARY_KEY, true);
    private static final SQLField SCHEMATIC_CONFIG_ID_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "config_id", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_START_X_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "start_x", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_START_Y_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "start_y", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_START_Z_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "start_z", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_CURRENT_X_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "current_x", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_CURRENT_Y_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "current_y", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_CURRENT_Z_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "current_z", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_ROTATION_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "rotation", SQLType.INTEGER);
    private static final SQLField SCHEMATIC_WORLD_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "world", SQLType.UUID);
    private static final SQLField SCHEMATIC_USER_ID_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "user_id", SQLType.UUID);
    private static final SQLField SCHEMATIC_CHUNK_KEY_COLUMN = new SQLField(SCHEMATICS_TABLE_NAME, "chunk_key", SQLType.LONG);
    private static final SQLTable SCHEMATICS_TABLE = SQLTable.builder(SCHEMATICS_TABLE_NAME)
            .addFields(
                    SCHEMATIC_ID_COLUMN,
                    SCHEMATIC_CONFIG_ID_COLUMN,
                    SCHEMATIC_START_X_COLUMN,
                    SCHEMATIC_START_Y_COLUMN,
                    SCHEMATIC_START_Z_COLUMN,
                    SCHEMATIC_CURRENT_X_COLUMN,
                    SCHEMATIC_CURRENT_Y_COLUMN,
                    SCHEMATIC_CURRENT_Z_COLUMN,
                    SCHEMATIC_ROTATION_COLUMN,
                    SCHEMATIC_WORLD_COLUMN,
                    SCHEMATIC_USER_ID_COLUMN,
                    SCHEMATIC_CHUNK_KEY_COLUMN
            )
            .build();


    private final SchematicPasterPlugin plugin;
    private final Path databasePath;
    private final Supplier<Connection> dataSource;

    public DataManager(SchematicPasterPlugin plugin) {
        this.plugin = plugin;
        this.databasePath = this.plugin.getDataFolder().toPath().resolve("database").resolve("schematics.db");
        this.dataSource = this.init();
    }

    private Supplier<Connection> init() {
        final File folder = this.databasePath.getParent().toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        final HikariConfig config = new HikariConfig();
        if (SystemDialect.getDialect() == SQLDialect.SQLITE) {
            config.setJdbcUrl("jdbc:sqlite:" + this.databasePath);
        }
        final HikariDataSource dataSource = new HikariDataSource(config);
        return () -> {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Could not get connection", e);
            }
        };
    }

    public void load() {
        this.createTables();
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.get();
    }

    private void createTables() {
        try (final Connection connection = this.getConnection()) {
            SCHEMATICS_TABLE.create(connection);
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<SchematicTask> newSchematicTask(
            String configId,
            BlockVector3 start,
            int rotation,
            UUID world,
            UUID userId,
            long chunkKey,
            BiConsumer<SchematicTask, Region> onComplete
    ) {
        try (final Connection connection = this.getConnection()) {
            final int id = this.createSchematicTask(
                    connection,
                    configId,
                    start,
                    rotation,
                    world,
                    userId,
                    chunkKey
            );
            return this.plugin.getSchematicBuilderManager().get(configId)
                    .map(builder -> new SchematicTask(
                            id,
                            configId,
                            userId,
                            this.plugin,
                            this.plugin.getDataFolder().toPath().resolve(SchematicTaskManager.FOLDER_NAME).resolve(builder.getFileName()).toFile(),
                            BukkitAdapter.adapt(Bukkit.getWorld(world)),
                            start,
                            rotation,
                            builder.getPlaceInterval(),
                            1,
                            onComplete
                    ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void saveSchematicTasks(Collection<SchematicTask> tasks) {
        try (final Connection connection = this.getConnection()) {
            for (SchematicTask task : tasks) {
                if (task.isEnded()) {
                    this.deleteSchematicTask(connection, task.getId());
                }
            }
            for (final SchematicTask task : tasks) {
                this.saveSchematicTask(
                        connection,
                        task
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<SchematicTask> loadSchematicTask(int id, @Nullable Long chunkKey, BiConsumer<SchematicTask, Region> onComplete) {
        try (Connection connection = this.getConnection()) {
            return this.loadSchematicTask(connection, id, chunkKey, onComplete);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<SchematicTask> loadSchematicTask(Connection connection, int id, @Nullable Long chunkKey, BiConsumer<SchematicTask, Region> onComplete) throws SQLException {
        final var queryBuilder = SQLQuery.<Optional<SchematicTask>>select(SCHEMATICS_TABLE_NAME)
                .select(
                        SCHEMATIC_CONFIG_ID_COLUMN,
                        SCHEMATIC_START_X_COLUMN,
                        SCHEMATIC_START_Y_COLUMN,
                        SCHEMATIC_START_Z_COLUMN,
                        SCHEMATIC_CURRENT_X_COLUMN,
                        SCHEMATIC_CURRENT_Y_COLUMN,
                        SCHEMATIC_CURRENT_Z_COLUMN,
                        SCHEMATIC_ROTATION_COLUMN,
                        SCHEMATIC_WORLD_COLUMN,
                        SCHEMATIC_USER_ID_COLUMN,
                        SCHEMATIC_CHUNK_KEY_COLUMN
                )
                .where(WhereCondition.of(SCHEMATIC_ID_COLUMN, () -> id));
        if (chunkKey != null) {
            queryBuilder.where(WhereCondition.of(SCHEMATIC_CHUNK_KEY_COLUMN, () -> chunkKey));
        }
        final SQLQuery<Optional<SchematicTask>> query = queryBuilder.build();

        return query.mapTo(connection, results -> {
            if (!results.next()) return Optional.empty();
            final String configId = results.getString(SCHEMATIC_CONFIG_ID_COLUMN.getName());
            final int startX = results.getInt(SCHEMATIC_START_X_COLUMN.getName());
            final int startY = results.getInt(SCHEMATIC_START_Y_COLUMN.getName());
            final int startZ = results.getInt(SCHEMATIC_START_Z_COLUMN.getName());
            final int currentX = results.getInt(SCHEMATIC_CURRENT_X_COLUMN.getName());
            final int currentY = results.getInt(SCHEMATIC_CURRENT_Y_COLUMN.getName());
            final int currentZ = results.getInt(SCHEMATIC_CURRENT_Z_COLUMN.getName());
            final int rotation = results.getInt(SCHEMATIC_ROTATION_COLUMN.getName());
            final UUID world = UUID.fromString(results.getString(SCHEMATIC_WORLD_COLUMN.getName()));
            final UUID userId = UUID.fromString(results.getString(SCHEMATIC_USER_ID_COLUMN.getName()));
            return this.plugin.getSchematicBuilderManager().get(configId)
                    .map(builder ->
                            new SchematicTask(
                                    id,
                                    configId,
                                    userId,
                                    this.plugin,
                                    this.plugin.getDataFolder().toPath().resolve(SchematicTaskManager.FOLDER_NAME).resolve(builder.getFileName()).toFile(),
                                    BukkitAdapter.adapt(Bukkit.getWorld(world)),
                                    BlockVector3.at(startX, startY, startZ),
                                    rotation,
                                    builder.getPlaceInterval(),
                                    1,
                                    BlockVector3.at(currentX, currentY, currentZ),
                                    onComplete
                            )
                    );
        });
    }

    private void saveSchematicTask(Connection connection, SchematicTask task) throws SQLException {
        final SQLStatement statement = SQLStatement.insert(SCHEMATICS_TABLE_NAME)
                .add(SCHEMATIC_ID_COLUMN)
                .add(SCHEMATIC_CONFIG_ID_COLUMN)
                .add(SCHEMATIC_START_X_COLUMN)
                .add(SCHEMATIC_START_Y_COLUMN)
                .add(SCHEMATIC_START_Z_COLUMN)
                .add(SCHEMATIC_CURRENT_X_COLUMN)
                .add(SCHEMATIC_CURRENT_Y_COLUMN)
                .add(SCHEMATIC_CURRENT_Z_COLUMN)
                .add(SCHEMATIC_ROTATION_COLUMN)
                .add(SCHEMATIC_WORLD_COLUMN)
                .add(SCHEMATIC_USER_ID_COLUMN)
                .add(SCHEMATIC_CHUNK_KEY_COLUMN)
                .build();
        final BlockVector3 start = task.getTo();
        final BlockVector3 current = task.getCurrentPos();
        final List<Object> values = List.of(
                task.getId(),
                task.getConfigId(),
                start.getX(),
                start.getY(),
                start.getZ(),
                current.getX(),
                current.getY(),
                current.getZ(),
                task.getRotation(),
                task.getWorld(),
                task.getUserId(),
                task.getChunkKey()
        );
        statement.insert(connection, List.of(() -> values), 1);
    }

    public void deleteSchematicTask(int id) {
        try (Connection connection = this.getConnection()) {
            this.deleteSchematicTask(connection, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteSchematicTask(Connection connection, int id) throws SQLException {
        DeleteStatement.builder(SCHEMATICS_TABLE_NAME)
                .where(WhereCondition.of(SCHEMATIC_ID_COLUMN, () -> id))
                .build()
                .execute(connection);
    }

    private int createSchematicTask(
            Connection connection,
            String configId,
            BlockVector3 start,
            int rotation,
            UUID world,
            UUID userId,
            long chunkKey
    ) throws SQLException {
        final SQLStatement statement = SQLStatement.insert(SCHEMATICS_TABLE_NAME)
                .add(SCHEMATIC_CONFIG_ID_COLUMN)
                .add(SCHEMATIC_START_X_COLUMN)
                .add(SCHEMATIC_START_Y_COLUMN)
                .add(SCHEMATIC_START_Z_COLUMN)
                .add(SCHEMATIC_CURRENT_X_COLUMN)
                .add(SCHEMATIC_CURRENT_Y_COLUMN)
                .add(SCHEMATIC_CURRENT_Z_COLUMN)
                .add(SCHEMATIC_ROTATION_COLUMN)
                .add(SCHEMATIC_WORLD_COLUMN)
                .add(SCHEMATIC_USER_ID_COLUMN)
                .add(SCHEMATIC_CHUNK_KEY_COLUMN)
                .build();
        final List<Object> values = List.of(
                configId,
                start.getBlockX(),
                start.getBlockY(),
                start.getBlockZ(),
                start.getBlockX(),
                start.getBlockY(),
                start.getBlockZ(),
                rotation,
                world,
                userId,
                chunkKey
        );
        final Integer id = statement.insert(connection, List.of(() -> values), 1, SQLStatement.INTEGER_ID_FINDER);
        if (id == null) {
            throw new IllegalStateException("Could not create schematic");
        }
        return id;
    }

    public Collection<SchematicTask> loadChunkTasks(
            long chunkKey,
            BiConsumer<SchematicTask, Region> onComplete
    ) {
        try (Connection connection = this.getConnection()) {
            final var queryBuilder = SQLQuery.<Collection<SchematicTask>>select(SCHEMATICS_TABLE_NAME)
                    .select(
                            SCHEMATIC_ID_COLUMN
                    )
                    .where(WhereCondition.of(SCHEMATIC_CHUNK_KEY_COLUMN, () -> chunkKey));
            return queryBuilder.build().mapTo(connection, results -> {
                final Set<SchematicTask> tasks = new HashSet<>();
                while (results.next()) {
                    final int id = results.getInt(SCHEMATIC_ID_COLUMN.getName());
                    this.loadSchematicTask(connection, id, chunkKey, onComplete)
                            .ifPresent(tasks::add);
                }
                return tasks;
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Collection<SchematicTask> loadUserSchematicTasks(
            UUID userId,
            BiConsumer<SchematicTask, Region> onComplete
    ) {
        try (Connection connection = this.getConnection()) {
            final var queryBuilder = SQLQuery.<Collection<SchematicTask>>select(SCHEMATICS_TABLE_NAME)
                    .select(
                            SCHEMATIC_ID_COLUMN,
                            SCHEMATIC_CHUNK_KEY_COLUMN
                    )
                    .where(WhereCondition.of(SCHEMATIC_USER_ID_COLUMN, () -> userId));
            return queryBuilder.build().mapTo(connection, results -> {
                final Set<SchematicTask> tasks = new HashSet<>();
                while (results.next()) {
                    final int id = results.getInt(SCHEMATIC_ID_COLUMN.getName());
                    this.loadSchematicTask(connection, id, results.getLong(SCHEMATIC_CHUNK_KEY_COLUMN.getName()), onComplete)
                            .ifPresent(tasks::add);
                }
                return tasks;
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private byte[] uuidToBytes(UUID uuid) {
        return ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits()).array();
    }

    private UUID bytesToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

}
