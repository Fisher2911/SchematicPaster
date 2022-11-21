package io.github.fisher2911.schematicpaster.world;

import io.github.fisher2911.fisherlib.world.ChunkPos;
import io.github.fisher2911.fisherlib.world.Position;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RectangularRegion implements Region {

    private final UUID world;
    private final Position min;
    private final Position max;

    public RectangularRegion(UUID world, Position min, Position max) {
        this.world = world;
        this.min = min;
        this.max = max;
    }

    @Override
    public UUID getWorld() {
        return this.world;
    }

    @Override
    public Position getMin() {
        return this.min;
    }

    @Override
    public Position getMax() {
        return this.max;
    }

    @Override
    public boolean contains(Position position) {
        return this.contains(position.x(), position.y(), position.z());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return x >= this.min.x() && x <= this.max.x() &&
                y >= this.min.y() && y <= this.max.y() &&
                z >= this.min.z() && z <= this.max.z();
    }

    @Override
    public boolean isOnBorder(Position position) {
        return this.isOnBorder(position.x(), position.y(), position.z());
    }

    @Override
    public boolean isOnBorder(double x, double y, double z) {
        return (int) x == this.min.getBlockX() || (int) x == this.max.getBlockX() ||
                (int) y == this.min.getBlockY() || (int) y == this.max.getBlockY() ||
                (int) z == this.min.getBlockZ() || (int) z == this.max.getBlockZ();
    }

    @Override
    public Collection<ChunkPos> getChunks() {
        final Set<ChunkPos> chunks = new HashSet<>();
        for (int x = this.min.getBlockX(); x <= this.max.getBlockX(); x += 16) {
            for (int z = this.min.getBlockZ(); z <= this.max.getBlockZ(); z += 16) {
                chunks.add(new ChunkPos(this.world, x >> 4, z >> 4));
            }
        }
        return chunks;
    }

    @Override
    public boolean intersects(Region region) {
        return this.min.x() <= region.getMax().x() && this.max.x() >= region.getMin().x() &&
                this.min.y() <= region.getMax().y() && this.max.y() >= region.getMin().y() &&
                this.min.z() <= region.getMax().z() && this.max.z() >= region.getMin().z();
    }

}
