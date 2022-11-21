package io.github.fisher2911.schematicpaster.schematic;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.github.fisher2911.fisherlib.task.TaskChain;
import io.github.fisher2911.fisherlib.world.ChunkPos;
import io.github.fisher2911.fisherlib.world.Position;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.util.PDCUtil;
import io.github.fisher2911.schematicpaster.world.RectangularRegion;
import io.github.fisher2911.schematicpaster.world.Region;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SchematicTask {

    private final int id;
    private final String configId;
    private final UUID userId;
    private final SchematicPasterPlugin plugin;
    private final File file;
    private final World world;
    private final BlockVector3 to;
    private final int rotation;
    private final int placeInterval;
    // for the future
    private final int blocksPerPlace;
    private final BiConsumer<SchematicTask, Region> onComplete;

    private Clipboard clipboard;
    @Nullable
    private Region region;

    private boolean started;
    private boolean ended;
    private int currentX;
    private int currentY;
    private int currentZ;

    public SchematicTask(
            int id,
            String configId,
            UUID userId,
            SchematicPasterPlugin plugin,
            File file,
            World world,
            BlockVector3 to,
            int rotation,
            int placeInterval,
            int blocksPerPlace,
            BiConsumer<SchematicTask, Region> onComplete
    ) {
        this.id = id;
        this.configId = configId;
        this.userId = userId;
        this.plugin = plugin;
        this.file = file;
        this.world = world;
        this.to = to;
        this.rotation = rotation;
        this.placeInterval = placeInterval;
        this.blocksPerPlace = blocksPerPlace;
        this.onComplete = onComplete;
    }

    public SchematicTask(
            int id,
            String configId,
            UUID userId,
            SchematicPasterPlugin plugin,
            File file,
            World world,
            BlockVector3 to,
            int rotation,
            int placeInterval,
            int blocksPerPlace,
            BlockVector3 current,
            BiConsumer<SchematicTask, Region> onComplete
    ) {
        this(id, configId, userId, plugin, file, world, to, rotation, placeInterval, blocksPerPlace, onComplete);
        this.currentX = current.getBlockX();
        this.currentY = current.getBlockY();
        this.currentZ = current.getBlockZ();
        this.started = true;
    }

    private BukkitTask task;

    public void start() throws IOException, WorldEditException {
        final Chunk chunk = BukkitAdapter.adapt(world).getChunkAt(this.to.getBlockX() >> 4, this.to.getBlockZ() >> 4);
        PDCUtil.addChunkPastingSchematic(chunk.getPersistentDataContainer(), this.id);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                if (this.clipboard == null) {
                    ClipboardFormat format = ClipboardFormats.findByFile(file);
                    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                        this.clipboard = reader.read();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                try {
                    if (!this.started) {
                        this.setAir();
                    }
                    if (this.ended) {
                        this.task.cancel();
                        try {
                            this.onComplete.accept(this, this.getRegion());
                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    this.doTask();
                } catch (IOException | WorldEditException e) {
                    e.printStackTrace();
                }
            }, this.placeInterval, this.placeInterval);
        });
    }

    public void stop() {
        if (this.task == null || this.task.isCancelled()) {
            return;
        }
        this.task.cancel();
    }

    private void doTask() throws IOException, WorldEditException {
        final BlockVector3 min = clipboard.getMinimumPoint();
        final BlockVector3 max = clipboard.getMaximumPoint();

        if (!this.started) {
            this.currentX = min.getBlockX();
            this.currentY = min.getBlockY();
            this.currentZ = min.getBlockZ();
            this.started = true;
            TaskChain.create(this.plugin)
                    .runAsync(() -> this.plugin.getDataManager().saveSchematicTasks(List.of(this)))
                    .execute();
        }
        final BlockTransformExtent extent = new BlockTransformExtent(clipboard, new AffineTransform().rotateY(-rotation));
        final BlockVector3 pasteAt = BlockVector3.at(this.currentX, this.currentY, this.currentZ);
        final CuboidRegion pasteRegion = new CuboidRegion(pasteAt, pasteAt);
        final ForwardExtentCopy copy = new ForwardExtentCopy(extent, pasteRegion, clipboard.getOrigin(), world, to);
        copy.setTransform(new AffineTransform().rotateY(-rotation));
        copy.setCopyingEntities(true);
        copy.setCopyingBiomes(clipboard.hasBiomes());
        var blockAt = extent.getBlock(pasteAt);
        do {
            this.nextBlock(min, max);
            blockAt = extent.getBlock(BlockVector3.at(this.currentX, this.currentY, this.currentZ));
        } while (blockAt.getBlockType() == BlockTypes.AIR && !this.ended);
        if (extent.getBlock(pasteAt).getBlockType().equals(BlockTypes.AIR)) return;
        Operations.complete(copy);
    }

    private void nextBlock(BlockVector3 min, BlockVector3 max) {
        if (this.currentZ != max.getBlockZ()) {
            this.currentZ++;
            return;
        } else if (this.currentX != max.getBlockX()) {
            this.currentX++;
            this.currentZ = min.getBlockZ();
            return;
        }
        if (this.currentX == max.getBlockX() && this.currentZ == max.getBlockZ() && this.currentY != max.getBlockY()) {
            this.currentY++;
            this.currentX = min.getBlockX();
            this.currentZ = min.getBlockZ();
            return;
        }
        final Chunk chunk = BukkitAdapter.adapt(world).getChunkAt(this.to.getBlockX() >> 4, this.to.getBlockZ() >> 4);
        PDCUtil.removeChunkPastingSchematics(chunk.getPersistentDataContainer(), this.id);
        this.ended = true;
    }

    private void setAir() throws WorldEditException {
        final BlockVector3 min = this.clipboard.getMinimumPoint();
        final BlockVector3 max = this.clipboard.getMaximumPoint();
        final BlockArrayClipboard clipboard = new BlockArrayClipboard(this.clipboard.getRegion());
        clipboard.setOrigin(this.clipboard.getOrigin());
        for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y < max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                    clipboard.setBlock(BlockVector3.at(x, y, z), BlockTypes.AIR.getDefaultState());
                }
            }
        }
        final BlockTransformExtent extent = new BlockTransformExtent(clipboard, new AffineTransform().rotateY(-rotation));
        final ForwardExtentCopy copy = new ForwardExtentCopy(extent, clipboard.getRegion(), clipboard.getOrigin(), world, to);
        copy.setTransform(new AffineTransform().rotateY(-rotation));
        Operations.complete(copy);
    }

    public Region getRegion() throws WorldEditException {
        if (this.region != null) return this.region;
        if (this.clipboard == null) return null;
        final BlockVector3 min = this.clipboard.getMinimumPoint();
        final BlockVector3 max = this.clipboard.getMaximumPoint();
        final BlockArrayClipboard clipboard = new BlockArrayClipboard(this.clipboard.getRegion());
        clipboard.setOrigin(this.clipboard.getOrigin());
        for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y < max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                    clipboard.setBlock(BlockVector3.at(x, y, z), BlockTypes.AIR.getDefaultState());
                }
            }
        }

        final Transform transform = new AffineTransform().rotateY(-rotation);

        BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
        BlockVector3 minInWorld = this.to.toVector3().add(transform.apply(clipboardOffset.toVector3())).toBlockPoint();
        BlockVector3 maxInWorld = minInWorld.add(
                transform.apply(
                        clipboard.getRegion().getMaximumPoint().subtract(clipboard.getRegion().getMinimumPoint()).toVector3()
                ).toBlockPoint()
        );
        final org.bukkit.World bukkitWorld = BukkitAdapter.adapt(this.world);
        this.region = new RectangularRegion(
                bukkitWorld.getUID(),
                Position.at(minInWorld.getBlockX(), minInWorld.getBlockY(), minInWorld.getBlockZ()),
                Position.at(maxInWorld.getBlockX(), maxInWorld.getBlockY(), maxInWorld.getBlockZ())
        );
        return this.region;
    }

    public int getId() {
        return id;
    }

    public String getConfigId() {
        return configId;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isEnded() {
        return ended;
    }

    public BlockVector3 getTo() {
        return to;
    }

    public BlockVector3 getCurrentPos() {
        return BlockVector3.at(this.currentX, this.currentY, this.currentZ);
    }

    public long getChunkKey() {
        return ChunkPos.chunkKeyAt(this.to.getBlockX() >> 4, this.to.getBlockZ() >> 4);
    }

    public UUID getWorld() {
        return BukkitAdapter.adapt(world).getUID();
    }

    public int getRotation() {
        return rotation;
    }

}
