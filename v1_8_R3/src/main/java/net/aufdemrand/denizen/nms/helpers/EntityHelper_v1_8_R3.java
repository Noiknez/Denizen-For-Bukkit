package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_8_R3;
import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.aufdemrand.denizen.nms.util.Utilities;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntityHelper_v1_8_R3 implements EntityHelper {

    /*
        General Entity Methods
     */

    @Override
    public void forceInteraction(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block.getById(location.getBlock().getType().getId())
                .interact(((CraftWorld) location.getWorld()).getHandle(), pos,
                        ((CraftWorld) location.getWorld()).getHandle().getType(pos),
                        craftPlayer != null ? craftPlayer.getHandle() : null, EnumDirection.NORTH, 0f, 0f, 0f);
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftWorld) world).getHandle().getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override
    public boolean isBreeding(Animals entity) {
        return ((CraftAnimals) entity).getHandle().isInLove();
    }

    @Override
    public void setBreeding(Animals entity, boolean breeding) {
        if (breeding) {
            ((CraftAnimals) entity).getHandle().c((EntityHuman) null);
        }
        else {
            ((CraftAnimals) entity).getHandle().cs();
        }
    }

    @Override
    public void setTarget(Creature entity, LivingEntity target) {
        EntityLiving nmsTarget = target != null ? ((CraftLivingEntity) target).getHandle() : null;
        ((CraftCreature) entity).getHandle().setGoalTarget(nmsTarget, EntityTargetEvent.TargetReason.CUSTOM, true);
        entity.setTarget(target);
    }

    @Override
    public CompoundTag getNbtData(Entity entity) {
        NBTTagCompound compound = new NBTTagCompound();
        ((CraftEntity) entity).getHandle().c(compound);
        return CompoundTag_v1_8_R3.fromNMSTag(compound);
    }

    @Override
    public void setNbtData(Entity entity, CompoundTag compoundTag) {
        ((CraftEntity) entity).getHandle().f(((CompoundTag_v1_8_R3) compoundTag).toNMSTag());
    }

    @Override
    public void setSilent(Entity entity, boolean silent) {
        ((CraftEntity) entity).getHandle().b(silent);
    }

    @Override
    public boolean isSilent(Entity entity) {
        return ((CraftEntity) entity).getHandle().R();
    }

    @Override
    public ItemStack getItemInHand(LivingEntity entity) {
        return entity.getEquipment().getItemInHand();
    }

    @Override
    public void setItemInHand(LivingEntity entity, ItemStack itemStack) {
        entity.getEquipment().setItemInHand(itemStack);
    }

    @Override
    public ItemStack getItemInOffHand(LivingEntity entity) {
        return new ItemStack(Material.AIR);
    }

    @Override
    public void setItemInOffHand(LivingEntity entity, ItemStack itemStack) {
        // nope
    }

    /*
        Entity Movement
     */

    private final static Map<UUID, BukkitTask> followTasks = new HashMap<UUID, BukkitTask>();

    @Override
    public void stopFollowing(Entity follower) {
        if (follower == null) {
            return;
        }
        UUID uuid = follower.getUniqueId();
        if (followTasks.containsKey(uuid)) {
            followTasks.get(uuid).cancel();
        }
    }

    @Override
    public void stopWalking(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).getNavigation().n();
    }

    @Override
    public void toggleAI(Entity entity, boolean hasAI) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return;
        }
        ((EntityInsentient) nmsEntity).k(!hasAI);
    }

    @Override
    public boolean isAIDisabled(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient)) {
            return true;
        }
        return ((EntityInsentient) nmsEntity).ce();
    }

    @Override
    public double getSpeed(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return 0.0;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        return nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).b();
    }

    @Override
    public void setSpeed(Entity entity, double speed) {
        net.minecraft.server.v1_8_R3.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
    }

    @Override
    public void follow(final Entity target, final Entity follower, final double speed, final double lead,
                              final double maxRange, final boolean allowWander) {
        if (target == null || follower == null) {
            return;
        }

        final net.minecraft.server.v1_8_R3.Entity nmsEntityFollower = ((CraftEntity) follower).getHandle();
        if (!(nmsEntityFollower instanceof EntityInsentient)) {
            return;
        }
        final EntityInsentient nmsFollower = (EntityInsentient) nmsEntityFollower;
        final NavigationAbstract followerNavigation = nmsFollower.getNavigation();

        UUID uuid = follower.getUniqueId();

        if (followTasks.containsKey(uuid)) {
            followTasks.get(uuid).cancel();
        }

        final int locationNearInt = (int) Math.floor(lead);
        final boolean hasMax = maxRange > lead;

        followTasks.put(follower.getUniqueId(), new BukkitRunnable() {

            private boolean inRadius = false;

            public void run() {
                if (!target.isValid() || !follower.isValid()) {
                    this.cancel();
                }
                followerNavigation.a(2F);
                Location targetLocation = target.getLocation();
                PathEntity path;

                if (hasMax && !Utilities.checkLocation(targetLocation, follower.getLocation(), maxRange)
                        && !target.isDead() && target.isOnGround()) {
                    if (!inRadius) {
                        follower.teleport(Utilities.getWalkableLocationNear(targetLocation, locationNearInt));
                    }
                    else {
                        inRadius = false;
                        path = followerNavigation.a(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
                        if (path != null) {
                            followerNavigation.a(path, 1D);
                            followerNavigation.a(2D);
                        }
                    }
                }
                else if (!inRadius && !Utilities.checkLocation(targetLocation, follower.getLocation(), lead)) {
                    path = followerNavigation.a(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
                    if (path != null) {
                        followerNavigation.a(path, 1D);
                        followerNavigation.a(2D);
                    }
                }
                else {
                    inRadius = true;
                }
                if (inRadius && !allowWander) {
                    followerNavigation.n();
                }
                nmsFollower.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            }
        }.runTaskTimer(NMSHandler.getJavaPlugin(), 0, 10));
    }

    @Override
    public void walkTo(final Entity entity, Location location, double speed, final Runnable callback) {
        if (entity == null || location == null) {
            return;
        }

        net.minecraft.server.v1_8_R3.Entity nmsEntityEntity = ((CraftEntity) entity).getHandle();
        if (!(nmsEntityEntity instanceof EntityInsentient)) {
            return;
        }
        final EntityInsentient nmsEntity = (EntityInsentient) nmsEntityEntity;
        final NavigationAbstract entityNavigation = nmsEntity.getNavigation();

        final PathEntity path;
        final boolean aiDisabled = isAIDisabled(entity);
        if (aiDisabled) {
            toggleAI(entity, true);
            nmsEntity.onGround = true;
        }
        path = entityNavigation.a(location.getX(), location.getY(), location.getZ());
        if (path != null) {
            entityNavigation.a(path, 1D);
            entityNavigation.a(2D);
            final double oldSpeed = nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).b();
            nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (entityNavigation.m() || path.b()) {
                        if (callback != null) {
                            callback.run();
                        }
                        nmsEntity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(oldSpeed);
                        if (aiDisabled) {
                            toggleAI(entity, false);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(NMSHandler.getJavaPlugin(), 1, 1);
        }
        //if (!Utilities.checkLocation(location, entity.getLocation(), 20)) {
        // TODO: generate waypoints to the target location?
        else {
            entity.teleport(location);
        }
    }

    /*
        Hide Entity
     */

    public static Map<UUID, Set<UUID>> hiddenEntities = new HashMap<UUID, Set<UUID>>();

    @Override
    public void hideEntity(Player player, Entity entity, boolean keepInTabList) {
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        UUID playerUUID = player.getUniqueId();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity)) {
            if (!hiddenEntities.containsKey(playerUUID)) {
                hiddenEntities.put(playerUUID, new HashSet<UUID>());
            }
            Set hidden = hiddenEntities.get(playerUUID);
            UUID entityUUID = entity.getUniqueId();
            if (!hidden.contains(entityUUID)) {
                hidden.add(entityUUID);
                EntityTracker tracker = ((WorldServer)craftPlayer.getHandle().world).tracker;
                net.minecraft.server.v1_8_R3.Entity other = ((CraftEntity)entity).getHandle();
                EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
                if (entry != null) {
                    entry.clear(entityPlayer);
                }
                if (entity instanceof Player && !entity.hasMetadata("NPC") && !keepInTabList) {
                    entityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) other));
                }
            }
        }
    }

    @Override
    public void unhideEntity(Player player, Entity entity) {
        CraftPlayer craftPlayer = (CraftPlayer)player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        UUID playerUUID = player.getUniqueId();
        if (entityPlayer.playerConnection != null && !craftPlayer.equals(entity) && hiddenEntities.containsKey(playerUUID)) {
            Set hidden = hiddenEntities.get(playerUUID);
            UUID entityUUID = entity.getUniqueId();
            if (hidden.contains(entityUUID)) {
                hidden.remove(entityUUID);
                EntityTracker tracker = ((WorldServer)craftPlayer.getHandle().world).tracker;
                net.minecraft.server.v1_8_R3.Entity other = ((CraftEntity)entity).getHandle();
                if (entity instanceof Player && !entity.hasMetadata("NPC")) {
                    entityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) other));
                }
                EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
                if(entry != null && !entry.trackedPlayers.contains(entityPlayer)) {
                    entry.updatePlayer(entityPlayer);
                }
            }
        }
    }

    @Override
    public boolean isHidden(Player player, UUID entity) {
        if (entity == null) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        return hiddenEntities.containsKey(uuid) && hiddenEntities.get(uuid).contains(entity);
    }

    @Override
    public void rotate(Entity entity, float yaw, float pitch) {
        // If this entity is a real player instead of a player type NPC,
        // it will appear to be online
        if (entity instanceof Player && ((Player) entity).isOnline()) {
            Location location = entity.getLocation();
            location.setYaw(yaw);
            location.setPitch(pitch);
            entity.teleport(location);
        }
        else if (entity instanceof LivingEntity) {
            if (entity instanceof EnderDragon) {
                yaw = normalizeYaw(yaw - 180);
            }
            look(entity, yaw, pitch);
        }
        else {
            net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
            handle.yaw = yaw;
            handle.pitch = pitch;
        }
    }

    @Override
    public void look(Entity entity, float yaw, float pitch) {
        net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
        if (handle != null) {
            handle.yaw = yaw;
            if (handle instanceof EntityLiving) {
                EntityLiving livingHandle = (EntityLiving) handle;
                while (yaw < -180.0F) {
                    yaw += 360.0F;
                }
                while (yaw >= 180.0F) {
                    yaw -= 360.0F;
                }
                livingHandle.aK = yaw;
                if (!(handle instanceof EntityHuman)) {
                    livingHandle.aI = yaw;
                }
                livingHandle.aL = yaw;
            }
            handle.pitch = pitch;
        }
    }

    private static MovingObjectPosition rayTrace(World world, Vector start, Vector end) {
        return ((CraftWorld) world).getHandle().rayTrace(new Vec3D(start.getX(), start.getY(), start.getZ()),
                new Vec3D(end.getX(), end.getY(), end.getZ()));
    }

    @Override
    public boolean canTrace(World world, Vector start, Vector end) {
        return rayTrace(world, start, end) == null;
    }

    @Override
    public MapTraceResult mapTrace(LivingEntity from, double range) {
        Location start = from.getEyeLocation();
        Vector startVec = start.toVector();
        double xzLen = Math.cos((start.getPitch() % 360) * (Math.PI / 180));
        double nx = xzLen * Math.sin(-start.getYaw() * (Math.PI / 180));
        double ny = Math.sin(start.getPitch() * (Math.PI / 180));
        double nz = xzLen * Math.cos(start.getYaw() * (Math.PI / 180));
        Vector endVec = startVec.clone().add(new Vector(nx, -ny, nz).multiply(range));
        MovingObjectPosition l = rayTrace(start.getWorld(), startVec, endVec);
        if (l == null || l.pos == null) {
            return null;
        }
        Vector finalVec = new Vector(l.pos.a, l.pos.b, l.pos.c);
        MapTraceResult mtr = new MapTraceResult();
        switch (l.direction) {
            case NORTH:
                mtr.angle = BlockFace.NORTH;
                break;
            case SOUTH:
                mtr.angle = BlockFace.SOUTH;
                break;
            case EAST:
                mtr.angle = BlockFace.EAST;
                break;
            case WEST:
                mtr.angle = BlockFace.WEST;
                break;
        }
        // wallPosition - ((end - start).normalize() * 0.072)
        Vector hit = finalVec.clone().subtract((endVec.clone().subtract(startVec)).normalize().multiply(0.072));
        mtr.hitLocation = new Location(start.getWorld(), hit.getX(), hit.getY(), hit.getZ());
        return mtr;
    }

    @Override
    public Location rayTrace(Location start, Vector direction, double range) {
        Vector startVec = start.toVector();
        MovingObjectPosition l = rayTrace(start.getWorld(), startVec, startVec.clone().add(direction.multiply(range)));
        if (l != null && l.pos != null) {
            return new Location(start.getWorld(), l.pos.a, l.pos.b, l.pos.c);
        }
        return null;
    }

    @Override
    public Location getImpactNormal(Location start, Vector direction, double range) {
        Vector startVec = start.toVector();
        MovingObjectPosition l = rayTrace(start.getWorld(), startVec, startVec.clone().add(direction.multiply(range)));
        if (l != null && l.direction != null) {
            return new Location(start.getWorld(), l.direction.getAdjacentX(), l.direction.getAdjacentY(), l.direction.getAdjacentZ());
        }
        return null;
    }

    @Override
    public Location eyeTrace(LivingEntity from, double range) {
        Location start = from.getEyeLocation();
        double xzLen = Math.cos((start.getPitch() % 360) * (Math.PI / 180));
        double nx = xzLen * Math.sin(-start.getYaw() * (Math.PI / 180));
        double ny = Math.sin(start.getPitch() * (Math.PI / 180));
        double nz = xzLen * Math.cos(start.getYaw() * (Math.PI / 180));
        return rayTrace(start, new Vector(nx, -ny, nz), range);
    }

    @Override
    public Location faceLocation(Location from, Location at) {
        Vector direction = from.toVector().subtract(at.toVector()).normalize();
        Location newLocation = from.clone();
        newLocation.setYaw(180 - (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())));
        newLocation.setPitch(90 - (float) Math.toDegrees(Math.acos(direction.getY())));
        return newLocation;
    }

    @Override
    public void faceLocation(Entity from, Location at) {
        if (from.getWorld() != at.getWorld()) {
            return;
        }
        Location origin = from instanceof LivingEntity ? ((LivingEntity) from).getEyeLocation()
                : from.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
        Location rotated = faceLocation(origin, at);
        rotate(from, rotated.getYaw(), rotated.getPitch());
    }

    @Override
    public void faceEntity(Entity entity, Entity target) {
        faceLocation(entity, target.getLocation());
    }

    @Override
    public boolean isFacingLocation(Location from, Location at, float degreeLimit) {
        double currentYaw = normalizeYaw(from.getYaw());
        double requiredYaw = normalizeYaw(getYaw(at.toVector().subtract(
                from.toVector()).normalize()));
        return (Math.abs(requiredYaw - currentYaw) < degreeLimit ||
                Math.abs(requiredYaw + 360 - currentYaw) < degreeLimit ||
                Math.abs(currentYaw + 360 - requiredYaw) < degreeLimit);
    }

    @Override
    public boolean isFacingLocation(Entity from, Location at, float degreeLimit) {
        return isFacingLocation(from.getLocation(), at, degreeLimit);
    }

    @Override
    public boolean isFacingEntity(Entity from, Entity at, float degreeLimit) {
        return isFacingLocation(from.getLocation(), at.getLocation(), degreeLimit);
    }

    @Override
    public float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) {
            yaw += 360.0;
        }
        return yaw;
    }

    @Override
    public float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            }
            else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        }
        else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI);
    }

    @Override
    public String getCardinal(float yaw) {
        yaw = normalizeYaw(yaw);
        // Compare yaws, return closest direction.
        if (0 <= yaw && yaw < 22.5) {
            return "south";
        }
        else if (22.5 <= yaw && yaw < 67.5) {
            return "southwest";
        }
        else if (67.5 <= yaw && yaw < 112.5) {
            return "west";
        }
        else if (112.5 <= yaw && yaw < 157.5) {
            return "northwest";
        }
        else if (157.5 <= yaw && yaw < 202.5) {
            return "north";
        }
        else if (202.5 <= yaw && yaw < 247.5) {
            return "northeast";
        }
        else if (247.5 <= yaw && yaw < 292.5) {
            return "east";
        }
        else if (292.5 <= yaw && yaw < 337.5) {
            return "southeast";
        }
        else if (337.5 <= yaw && yaw < 360.0) {
            return "south";
        }
        else {
            return null;
        }
    }

    @Override
    public void move(Entity entity, Vector vector) {
        ((CraftEntity) entity).getHandle().move(vector.getX(), vector.getY(), vector.getZ());
    }
}