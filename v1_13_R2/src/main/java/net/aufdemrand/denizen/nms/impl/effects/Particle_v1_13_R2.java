package net.aufdemrand.denizen.nms.impl.effects;

import net.aufdemrand.denizen.nms.interfaces.Particle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Particle_v1_13_R2 implements Particle {

    private org.bukkit.Particle particle;

    public Particle_v1_13_R2(org.bukkit.Particle particle) {
        this.particle = particle;
    }

    @Override
    public Class neededData() {
        Class clazz = particle.getDataType();
        if (clazz == Void.class) {
            return null;
        }
        return clazz;
    }

    @Override
    public void playFor(Player player, Location location, int count, Vector offset, double extra) {
        player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra);
    }

    @Override
    public <T> void playFor(Player player, Location location, int count, Vector offset, double extra, T data) {
        if (data instanceof MaterialData) {
            player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra,
                    CraftBlockData.fromData(CraftMagicNumbers.getBlock((MaterialData) data)));
        }
        else {
            player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data);
        }
    }

    @Override
    public boolean isVisible() {
        return particle != org.bukkit.Particle.SUSPENDED && particle != org.bukkit.Particle.SUSPENDED_DEPTH
                && particle != org.bukkit.Particle.WATER_BUBBLE;
    }

    @Override
    public String getName() {
        return particle.name();
    }
}
