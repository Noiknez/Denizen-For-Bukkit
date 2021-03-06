package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lockable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemLock implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((dItem) item).getItemStack().getItemMeta()).getBlockState() instanceof Lockable;
    }

    public static ItemLock getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLock((dItem) _item);
        }
    }

    private String getItemLock() {
        return ((Lockable) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).getLock();
    }

    private boolean isLocked() {
        return ((Lockable) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).isLocked();
    }

    private ItemLock(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.lock>
        // @returns Element
        // @mechanism dItem.lock
        // @group properties
        // @description
        // Returns the lock password of this item.
        // -->
        if (attribute.startsWith("lock")) {
            return new Element(isLocked() ? getItemLock() : null).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.is_locked>
        // @returns Element(Boolean)
        // @mechanism dItem.lock
        // @group properties
        // @description
        // Returns whether this item has a lock password.
        // -->
        if (attribute.startsWith("is_locked")) {
            return new Element(isLocked()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        return isLocked() ? getItemLock() : null;
    }

    @Override
    public String getPropertyId() {
        return "lock";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name lock
        // @input Element
        // @description
        // Sets the item's lock password.
        // Locked blocks can only be opened while holding an item with the name of the lock.
        // @tags
        // <i@item.lock>
        // <i@item.is_locked>
        // <i@item.is_lockable>
        // -->
        if (mechanism.matches("lock")) {
            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            Lockable lockable = (Lockable) bsm.getBlockState();

            lockable.setLock(mechanism.hasValue() ? mechanism.getValue().asString() : null);
            bsm.setBlockState((BlockState) lockable);
            itemStack.setItemMeta(bsm);
        }
    }
}
