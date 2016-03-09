package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemStackHandlerBasic implements IItemHandlerModifiable, INBTSerializable<NBTTagCompound>
{
    protected ItemStack[] items;
    protected final int invSize;
    protected final int stackLimit;
    protected final boolean allowCustomStackSizes;
    protected final String tagName;

    public ItemStackHandlerBasic(int invSize)
    {
        this(invSize, 64, false, "Items");
    }

    public ItemStackHandlerBasic(int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName)
    {
        this.invSize = invSize;
        this.stackLimit = stackLimit;
        this.tagName = tagName;
        this.allowCustomStackSizes = allowCustomStackSizes;
        this.items = new ItemStack[invSize];
    }

    public int getInventoryStackLimit()
    {
        return this.stackLimit;
    }

    public int getInventoryStackLimit(ItemStack stack)
    {
        if (this.allowCustomStackSizes == true)
        {
            return this.getInventoryStackLimit();
        }

        return stack != null ? stack.getMaxStackSize() : 64;
    }

    @Override
    public int getSlots()
    {
        if (this.items != null)
        {
            return this.items.length;
        }

        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        /*
        if (slot >= 0 && slot < this.items.length)
        {
            return this.items[slot];
        }

        return null;
        */

        return this.items[slot];
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        /*
        if (slot >= 0 && slot < this.items.length)
        {
            this.items[slot] = stack;
            this.onContentsChanged(slot);
        }
        */

        this.items[slot] = stack;
        this.onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (slot < 0 || slot >= this.items.length || stack == null)
        {
            return stack;
        }

        int existingStackSize = this.items[slot] != null ? this.items[slot].stackSize : 0;
        int max = this.stackLimit;
        if (this.allowCustomStackSizes == false)
        {
            max = Math.min(max, stack.getMaxStackSize());
        }

        // Existing items in the target slot
        if (this.items[slot] != null)
        {
            // The slot is already full, bail out now
            if (this.items[slot].stackSize >= max)
            {
                return stack;
            }

            // Check that the item-to-be-inserted is identical to the existing items
            if (stack.getItem() != this.items[slot].getItem() ||
                stack.getMetadata() != this.items[slot].getMetadata() ||
                ItemStack.areItemStackTagsEqual(stack, this.items[slot]) == false)
            {
                return stack;
            }
        }

        int amount = Math.min(max - existingStackSize, stack.stackSize);

        if (simulate == false)
        {
            if (this.items[slot] != null)
            {
                this.items[slot].stackSize += amount;
            }
            else
            {
                this.items[slot] = stack.copy();
                this.items[slot].stackSize = amount;
            }

            this.onContentsChanged(slot);
        }

        if (amount < stack.stackSize)
        {
            ItemStack stackRemaining = stack.copy();
            stackRemaining.stackSize -= amount;

            return stackRemaining;
        }

        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (slot < 0 || slot >= this.items.length || this.items[slot] == null)
        {
            return null;
        }

        amount = Math.min(amount, this.items[slot].stackSize);
        amount = Math.min(amount, this.items[slot].getMaxStackSize());

        ItemStack stack;

        if (simulate == true)
        {
            stack = this.items[slot].copy();
            stack.stackSize = amount;

            return stack;
        }
        else
        {
            if (amount == this.items[slot].stackSize)
            {
                stack = this.items[slot];
                this.items[slot] = null;
            }
            else
            {
                stack = this.items[slot].splitStack(amount);

                if (this.items[slot].stackSize <= 0)
                {
                    this.items[slot] = null;
                }
            }
        }

        this.onContentsChanged(slot);

        return stack;
    }

    protected void onContentsChanged(int slot) { }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return NBTUtils.writeItemsToTag(new NBTTagCompound(), this.items, this.tagName, true);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTUtils.readStoredItemsFromTag(nbt, this.items, this.tagName);
    }

    /**
     * A raw, no-checks version of getStackInSlot
     */
    public ItemStack getStackInSlotDirect(int slot)
    {
        return this.items[slot];
    }

    /**
     * A raw, no-checks version of setStackInSlot
     */
    public void setStackInSlotDirect(int slot, ItemStack stack)
    {
        this.items[slot] = stack;
        this.onContentsChanged(slot);
    }
}