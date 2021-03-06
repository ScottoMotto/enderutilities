package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerTileLargeStacks;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;

public class ContainerBarrel extends ContainerTileLargeStacks
{
    private final IItemHandler upgradeInv;

    public ContainerBarrel(EntityPlayer player, TileEntityBarrel te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);

        this.upgradeInv = te.getUpgradeInventory();
        this.inventoryNonWrapped = te.getInventoryBarrel();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 93);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 1);

        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 80, 23));

        // Upgrade slots
        for (int slot = 0; slot < this.upgradeInv.getSlots(); slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.upgradeInv, slot, 44 + slot * 18, 59));
        }
    }

    public IItemHandler getUpgradeInventory()
    {
        return this.upgradeInv;
    }
}
