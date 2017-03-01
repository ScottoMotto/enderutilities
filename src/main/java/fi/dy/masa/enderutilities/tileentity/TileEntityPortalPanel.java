package fi.dy.masa.enderutilities.tileentity;

import org.apache.commons.lang3.StringUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiPortalPanel;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerPortalPanel;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.PortalFormer;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortalPanel extends TileEntityEnderUtilitiesInventory
{
    private final ItemHandlerWrapper inventoryWrapper;
    private byte activeTargetId;
    private byte portalTargetId;
    private String displayName = EUStringUtils.EMPTY;
    private String[] targetDisplayNames = new String[9];
    private int[] colors = new int[9];

    public TileEntityPortalPanel()
    {
        super(ReferenceNames.NAME_TILE_PORTAL_PANEL);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 16, 1, false, "Items", this);
        this.inventoryWrapper = new ItemHandlerWrapper(this.itemHandlerBase);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerBase, this.inventoryWrapper);
    }

    public int getActiveTargetId()
    {
        return this.activeTargetId;
    }

    private TargetData getActiveTarget()
    {
        int slot = this.getActiveTargetId();
        ItemStack stack = this.itemHandlerBase.getStackInSlot(slot);

        if (stack != null)
        {
            return TargetData.getTargetFromItem(stack);
        }

        return null;
    }

    private OwnerData getOwner()
    {
        int slot = this.getActiveTargetId();
        ItemStack stack = this.itemHandlerBase.getStackInSlot(slot);

        if (stack != null)
        {
            return OwnerData.getOwnerDataFromItem(stack);
        }

        return null;
    }

    public void setActiveTargetId(int target)
    {
        this.activeTargetId = (byte)MathHelper.clamp(target, 0, 7);
    }

    public int getActiveColor()
    {
        return this.getColorFromItems(8);
    }

    private int getColorFromItems(int target)
    {
        // The large button in the center will take the color of the active target
        if (target == 8)
        {
            target = this.activeTargetId;
        }

        if (target >= 0 && target < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(target + 8);

            if (stack != null && stack.getItem() == Items.DYE)
            {
                return EnumDyeColor.byDyeDamage(stack.getMetadata()).getMapColor().colorValue;
            }
        }

        return 0xFFFFFF;
    }

    public int getColor(int target)
    {
        target = MathHelper.clamp(target, 0, 8);
        return this.colors[target];
    }

    private String getActiveName()
    {
        return this.getTargetName(this.activeTargetId);
    }

    private String getTargetName(int targetId)
    {
        if (targetId >= 0 && targetId <= 7)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(targetId);

            if (stack != null)
            {
                return stack.getDisplayName();
            }
        }

        return EUStringUtils.EMPTY;
    }

    public String getPanelDisplayName()
    {
        return this.displayName;
    }

    public String getTargetDisplayName(int targetId)
    {
        if (targetId >= 0 && targetId <= 7)
        {
            String name = this.targetDisplayNames[targetId];
            return name != null ? name : EUStringUtils.EMPTY;
        }

        return EUStringUtils.EMPTY;
    }

    public void setTargetName(String name)
    {
        if (this.activeTargetId >= 0 && this.activeTargetId < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(this.activeTargetId);

            if (stack != null)
            {
                if (StringUtils.isBlank(name))
                {
                    stack.clearCustomName();
                }
                else
                {
                    stack.setStackDisplayName(name);
                }

                this.itemHandlerBase.setStackInSlot(this.activeTargetId, stack);
            }
        }
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setActiveTargetId(nbt.getByte("SelectedTarget"));
        this.portalTargetId = nbt.getByte("PortalTarget");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("SelectedTarget", this.activeTargetId);
        nbt.setByte("PortalTarget", this.portalTargetId);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("s", this.activeTargetId);
        String name = this.getActiveName();
        if (StringUtils.isBlank(name) == false)
        {
            nbt.setString("n", name);
        }

        for (int i = 0; i < 9; i++)
        {
            nbt.setInteger("c" + i, this.getColorFromItems(i));
        }

        for (int i = 0; i < 8; i++)
        {
            nbt.setString("n" + i, this.getTargetName(i));
        }

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.activeTargetId = tag.getByte("s");
        this.displayName = tag.getString("n");

        for (int i = 0; i < 9; i++)
        {
            this.colors[i] = tag.getInteger("c" + i);

            if (this.colors[i] == 0)
            {
                this.colors[i] = 0xFFFFFF;
            }
        }

        for (int i = 0; i < 8; i++)
        {
            if (tag.hasKey("n" + i, Constants.NBT.TAG_STRING))
            {
                this.targetDisplayNames[i] = tag.getString("n" + i);
            }
        }

        super.handleUpdateTag(tag);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 0 && element >= 0 && element < 8)
        {
            this.setActiveTargetId(element);

            IBlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
        }
    }

    private class ItemHandlerWrapper extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapper(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return false;
            }

            if (slot < 8)
            {
                return stack.getItem() == EnderUtilitiesItems.linkCrystal &&
                        ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
            }

            return stack.getItem() == Items.DYE;
        }
    }

    public void tryTogglePortal()
    {
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockEnderUtilities blockPanel = EnderUtilitiesBlocks.blockPortalPanel;
        BlockPos posFrame = posPanel.offset(world.getBlockState(posPanel).getValue(blockPanel.propFacing).getOpposite());

        PortalFormer portalFormer = new PortalFormer(world, posFrame,
                EnderUtilitiesBlocks.blockPortalFrame, EnderUtilitiesBlocks.blockPortal);
        portalFormer.setPortalData(this.getActiveTarget(), this.getOwner(), this.getActiveColor());
        portalFormer.analyzePortal();
        boolean state = portalFormer.getPortalState();
        boolean recreate = this.activeTargetId != this.portalTargetId;

        // Portal was inactive
        if (state == false)
        {
            if (portalFormer.togglePortalState(false))
            {
                this.portalTargetId = this.activeTargetId;
                world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 0.5f, 1.0f);
            }
        }
        // Portal was active
        else if (portalFormer.togglePortalState(recreate))
        {
            // Portal was active but the target id has changed, so it was just updated
            if (recreate)
            {
                this.portalTargetId = this.activeTargetId;
                world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 0.5f, 1.0f);
            }
            // Portal was active and the target id hasn't changed, so it was shut down
            else
            {
                world.playSound(null, posPanel, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.4f, 0.85f);
            }
        }
    }

    @Override
    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return new ContainerPortalPanel(player, this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiPortalPanel(this.getContainer(player), this);
    }
}
