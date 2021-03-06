package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutEntityMetadata;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_9_R2.DataWatcher;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityMetadata;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class PacketOutEntityMetadata_v1_9_R2 implements PacketOutEntityMetadata {

    private PacketPlayOutEntityMetadata internal;
    private int entityId;
    private List<DataWatcher.Item<?>> metadata;

    public PacketOutEntityMetadata_v1_9_R2(PacketPlayOutEntityMetadata internal) {
        this.internal = internal;
        try {
            entityId = ENTITY_ID.getInt(internal);
            metadata = (List<DataWatcher.Item<?>>) METADATA.get(internal);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean checkForGlow() {
        for (DataWatcher.Item<?> data : metadata) {
            if (data.a().a() == 0) {
                // TODO: Instead of cancelling, casually strip out the 0x40 "Glowing" metadata rather than cancelling entirely?
                return true;
            }
        }
        return false;
    }

    private static final Field ENTITY_ID, METADATA;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutEntityMetadata.class);
        ENTITY_ID = fields.get("a");
        METADATA = fields.get("b");
    }
}
