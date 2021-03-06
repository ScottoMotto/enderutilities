package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public abstract class BlockEnderUtilitiesInventory extends BlockEnderUtilitiesTileEntity
{
    public BlockEnderUtilitiesInventory(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntityEnderUtilitiesInventory te = getTileEntitySafely(world, pos, TileEntityEnderUtilitiesInventory.class);

        if (te != null && te.getBaseItemHandler() != null)
        {
            InventoryUtils.dropInventoryContentsInWorld(world, pos, te.getBaseItemHandler());
            world.updateComparatorOutputLevel(pos, this);
        }

        world.removeTileEntity(pos);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
    {
        TileEntityEnderUtilities te = getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP))
        {
            IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

            return inv != null ? InventoryUtils.calcRedstoneFromInventory(inv) : 0;
        }

        return 0;
    }
}
