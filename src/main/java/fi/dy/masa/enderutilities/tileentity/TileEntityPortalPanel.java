package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
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
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.PortalFormer;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortalPanel extends TileEntityEnderUtilitiesInventory
{
    private final ItemHandlerWrapper inventoryWrapper;
    private byte activeTargetId;
    private byte portalTargetId;
    private boolean active;
    private String displayName;
    private int[] colors = new int[9];

    public TileEntityPortalPanel()
    {
        super(ReferenceNames.NAME_TILE_PORTAL_PANEL);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 16, 1, false, "Items", this);
        this.inventoryWrapper = new ItemHandlerWrapper(this.itemHandlerBase);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
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
        this.activeTargetId = (byte)MathHelper.clamp_int(target, 0, 7);
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
        target = MathHelper.clamp_int(target, 0, 8);
        return this.colors[target];
    }

    private String getActiveName()
    {
        if (this.activeTargetId >= 0 && this.activeTargetId < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(this.activeTargetId);

            if (stack != null && stack.hasDisplayName())
            {
                return stack.getDisplayName();
            }
        }

        return null;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setActiveTargetId(nbt.getByte("SelectedTarget"));
        this.portalTargetId = nbt.getByte("PortalTarget");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("SelectedTarget", this.activeTargetId);
        nbt.setByte("PortalTarget", this.portalTargetId);
        nbt.setBoolean("Active", this.active);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("s", this.activeTargetId);
        String name = this.getActiveName();
        if (name != null)
        {
            nbt.setString("n", name);
        }

        for (int i = 0; i < 9; i++)
        {
            nbt.setInteger("c" + i, this.getColorFromItems(i));
        }

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        this.activeTargetId = nbt.getByte("s");
        this.displayName = nbt.getString("n");

        for (int i = 0; i < 9; i++)
        {
            this.colors[i] = nbt.getInteger("c" + i);

            if (this.colors[i] == 0)
            {
                this.colors[i] = 0xFFFFFF;
            }
        }

        super.onDataPacket(net, packet);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        super.inventoryChanged(inventoryId, slot);

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

    @Override
    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return new ContainerPortalPanel(player, this);
    }

    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiPortalPanel(this.getContainer(player), this);
    }

    public void tryTogglePortal()
    {
        if (this.active == false)
        {
            this.tryActivatePortal();
        }
        else if (this.activeTargetId != this.portalTargetId)
        {
            this.tryUpdatePortal();
        }
        else
        {
            this.tryDisablePortal();
        }
    }

    private boolean tryActivatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockPortalFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());
        boolean success = false;
        TargetData destination = this.getActiveTarget();

        if (destination == null || world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return false;
        }

        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame, blockPortal);
        portalFormer.setLimits(500, 1000, 4000);
        portalFormer.setTarget(destination).setOwner(this.getOwner()).setColor(this.getActiveColor());
        portalFormer.analyzePortalFrame();
        portalFormer.validatePortalAreas();
        success = portalFormer.formPortals();

        if (success)
        {
            this.active = true;
            this.portalTargetId = this.activeTargetId;
            world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.MASTER, 0.5f, 1.0f);
        }

        return success;
    }

    private void tryDisablePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockPortalFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posFrame = this.getPos().offset(this.getFacing().getOpposite());
        boolean success = false;

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return;
        }

        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame, blockPortal);
        portalFormer.setLimits(500, 1000, 4000);
        portalFormer.analyzePortalFrame();
        success = portalFormer.destroyPortals();

        if (success)
        {
            this.active = false;
            world.playSound(null, this.getPos(), SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.MASTER, 0.5f, 0.85f);
        }
    }

    private void tryUpdatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockPortalFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posFrame = this.getPos().offset(this.getFacing().getOpposite());
        boolean success = false;
        TargetData destination = this.getActiveTarget();

        if (destination == null || world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return;
        }

        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame, blockPortal);
        portalFormer.setLimits(500, 1000, 4000);
        portalFormer.setTarget(destination).setOwner(this.getOwner()).setColor(this.getActiveColor());
        portalFormer.analyzePortalFrame();
        success = portalFormer.destroyPortals();
        portalFormer.validatePortalAreas();
        success &= portalFormer.formPortals();

        if (success)
        {
            this.active = true;
            this.portalTargetId = this.activeTargetId;
        }
    }
}
