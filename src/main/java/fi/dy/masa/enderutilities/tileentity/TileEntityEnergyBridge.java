package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;

public class TileEntityEnergyBridge extends TileEntityEnderUtilities implements ITickable
{
    public boolean isActive;
    public boolean isPowered;
    public int timer;

    @SideOnly(Side.CLIENT)
    public int beamYMin;
    @SideOnly(Side.CLIENT)
    public int beamYMax;
    @SideOnly(Side.CLIENT)
    AxisAlignedBB renderBB;

    public TileEntityEnergyBridge()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENERGY_BRIDGE);
        this.timer = 0;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        byte f = nbt.getByte("Flags");
        this.isActive = (f & 0x01) == 0x01;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("Flags", (byte)(this.isActive ? 0x01 : 0x00));
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("f", (byte)((this.isPowered ? 0x02 : 0x00) | (this.isActive ? 0x01 : 0x00)));

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();
        byte f = nbt.getByte("f");

        this.isActive = ((f & 0x01) == 0x01);
        this.isPowered = ((f & 0x02) == 0x02);
        this.getBeamEndPoints();

        super.onDataPacket(net, packet);
    }

    public void setState(boolean state)
    {
        this.isActive = state;
        this.worldObj.markBlockForUpdate(this.getPos());
    }

    public void setPowered(boolean value)
    {
        if (this.isPowered != value)
        {
            this.isPowered = value;
            this.worldObj.markBlockForUpdate(this.getPos());
        }
    }

    @Override
    public void update()
    {
        // Master blocks (Transmitter or Receiver) re-validate the multiblock every 2 seconds
        if (this.worldObj.isRemote == false && this.getBlockMetadata() < 2 && ++this.timer >= 40)
        {
            this.tryAssembleMultiBlock(this.worldObj, this.getPos());
            this.timer = 0;
        }
    }

    public void tryAssembleMultiBlock(World worldIn, BlockPos pos)
    {
        // The End has the transmitter, and in a slightly different position than the receivers are
        if (worldIn.provider.getDimensionId() == 1)
        {
            this.tryAssembleMultiBlock(worldIn, pos, 4, 0, true);
        }
        else
        {
            this.tryAssembleMultiBlock(worldIn, pos, 1, 1, false);
        }
    }

    public void disassembleMultiblock(World worldIn, BlockPos pos, int oldMeta)
    {
        // The End has the transmitter, and in a slightly different position than the receivers are
        if (worldIn.provider.getDimensionId() == 1)
        {
            this.disassembleMultiblock(worldIn, pos, 4, 0, oldMeta);
        }
        else
        {
            this.disassembleMultiblock(worldIn, pos, 1, 1, oldMeta);
        }
    }

    public void tryAssembleMultiBlock(World worldIn, BlockPos pos, int height, int masterMeta, boolean requireEnderCrystal)
    {
        List<BlockPosEU> positions = new ArrayList<BlockPosEU>();
        if (this.getBlockPositions(worldIn, pos, height, masterMeta, positions) == false || positions.size() != 6)
        {
            return;
        }

        boolean isValid = this.isStructureValid(worldIn, pos, height, masterMeta, requireEnderCrystal, positions);

        if (isValid == true)
        {
            if (this.isActive == false)
            {
                this.activateMultiBlock(worldIn, positions);
                EnergyBridgeTracker.addBridgeLocation(positions.get(0));
            }

            this.updatePoweredState(worldIn, positions);
        }
        // This gets called from the periodic validation via updateEntity()
        else if (this.isActive == true)
        {
            this.disassembleMultiblock(worldIn, pos, worldIn.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
        }
    }

    public void activateMultiBlock(World world, List<BlockPosEU> blockPositions)
    {
        for (int i = 0; i < 5; i++)
        {
            this.setState(world, blockPositions.get(i), true);
        }
    }

    public boolean getBlockPositions(World world, BlockPos pos, int height, int masterMeta, List<BlockPosEU> blockPositions)
    {
        blockPositions.clear();

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);

        if (block != EnderUtilitiesBlocks.machine_1 || (meta != masterMeta && meta != 2) || (te instanceof TileEntityEnergyBridge) == false)
        {
            return false;
        }

        BlockPosEU posMaster = new BlockPosEU(x, y, z, this.worldObj.provider.getDimensionId(), 1);

        // position of the middle block in the y-plane of the resonators
        BlockPosEU posResonatorBase = new BlockPosEU(x, y, z, this.worldObj.provider.getDimensionId(), 1);
        ForgeDirection dir = ForgeDirection.getOrientation(((TileEntityEnergyBridge)te).getRotation());

        // The given location is a resonator, not the master block; get the master block's location
        if (meta != masterMeta)
        {
            posMaster = posMaster.add(0, height - 1, 0).offset(dir, 3);
            posResonatorBase = posResonatorBase.offset(dir, 3);
        }
        else
        {
            posResonatorBase = posResonatorBase.add(0, -(height - 1), 0);
        }

        blockPositions.add(posMaster);
        blockPositions.add(posResonatorBase.offset(ForgeDirection.NORTH, 3));
        blockPositions.add(posResonatorBase.offset(ForgeDirection.SOUTH, 3));
        blockPositions.add(posResonatorBase.offset(ForgeDirection.EAST, 3));
        blockPositions.add(posResonatorBase.offset(ForgeDirection.WEST, 3));
        blockPositions.add(posResonatorBase);

        return true;
    }

    public boolean isStructureValid(World world, BlockPos pos, int height, int masterMeta, boolean requireEnderCrystal, List<BlockPosEU> blockPositions)
    {
        Block blockEb = EnderUtilitiesBlocks.machine_1;
        Class<TileEntityEnergyBridge> classTEEB = TileEntityEnergyBridge.class;
        boolean isValid = false;

        if (BlockUtils.blockMatches(world, blockPositions.get(0), blockEb, masterMeta, classTEEB, ForgeDirection.UNKNOWN) &&
            BlockUtils.blockMatches(world, blockPositions.get(1), blockEb, 2, classTEEB, ForgeDirection.SOUTH) &&
            BlockUtils.blockMatches(world, blockPositions.get(2), blockEb, 2, classTEEB, ForgeDirection.NORTH) &&
            BlockUtils.blockMatches(world, blockPositions.get(3), blockEb, 2, classTEEB, ForgeDirection.WEST) &&
            BlockUtils.blockMatches(world, blockPositions.get(4), blockEb, 2, classTEEB, ForgeDirection.EAST))
        {
            if (requireEnderCrystal == false)
            {
                isValid = true;
            }
            else
            {
                double xd = blockPositions.get(5).posX;
                double yd = blockPositions.get(5).posY;
                double zd = blockPositions.get(5).posZ;
                double d = 0.0d;
                List<Entity> list = world.getEntitiesWithinAABB(EntityEnderCrystal.class, AxisAlignedBB.getBoundingBox(xd - d, yd - d, zd - d, xd + d, yd + d, zd + d));

                if (list.size() == 1)
                {
                    isValid = true;
                }
            }
        }

        // Our machine blocks are all in the right configuration, now just check that there are no other obstructing blocks in the area
        if (isValid == true)
        {
            return this.isObstructed(world, blockEb, height, masterMeta, blockPositions) == false;
        }

        return false;
    }

    public boolean isObstructedQuadrant(World world, BlockPosEU basePosition, ForgeDirection dir, BlockPosEU ... positions)
    {
        ForgeDirection dirNext = dir.getRotation(ForgeDirection.UP); // the direction 90 degrees clock wise

        for (BlockPosEU pos : positions)
        {
            int x = pos.posX * dir.offsetX + pos.posZ * dir.offsetZ;
            int y = pos.posY;
            int z = pos.posX * dirNext.offsetX + pos.posZ * dirNext.offsetZ;

            if (basePosition != null)
            {
                x += basePosition.posX;
                y += basePosition.posY;
                z += basePosition.posZ;
            }

            if (world.getBlock(x, y, z).isAir(world, x, y, z) == false)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isObstructed(World world, Block blockEb, int height, int masterMeta, List<BlockPosEU> blockPositions)
    {
        if (blockPositions.size() != 6)
        {
            return true;
        }

        BlockPosEU posMaster = blockPositions.get(0);
        BlockPosEU posResonatorMiddle = blockPositions.get(5);

        // Block positions in one quadrant of the area that needs to be clear for the resonators, relative to the middle block
        BlockPosEU positionsToCheck[] = new BlockPosEU[] {
                                                        new BlockPosEU(1, 0, 0),
                                                        new BlockPosEU(2, 0, 0),
                                                        new BlockPosEU(1, 0, 3),
                                                        new BlockPosEU(1, 0, 2),
                                                        new BlockPosEU(2, 0, 2),
                                                        new BlockPosEU(2, 0, 1),
                                                        new BlockPosEU(3, 0, 1)
                                                    };

        if (this.isObstructedQuadrant(world, posResonatorMiddle, ForgeDirection.EAST, positionsToCheck) == true ||
            this.isObstructedQuadrant(world, posResonatorMiddle, ForgeDirection.SOUTH, positionsToCheck) == true ||
            this.isObstructedQuadrant(world, posResonatorMiddle, ForgeDirection.WEST, positionsToCheck) == true ||
            this.isObstructedQuadrant(world, posResonatorMiddle, ForgeDirection.NORTH, positionsToCheck) == true)
        {
            return true;
        }

        // Transmitter
        if (masterMeta == 0)
        {
            // Check the two blocks below the transmitter
            if (this.isObstructedQuadrant(world, posMaster, ForgeDirection.EAST, new BlockPosEU[] {new BlockPosEU(0, -1, 0), new BlockPosEU(0, -2, 0)}) == true)
            {
                return true;
            }
        }
        // Receiver: check the column below the Receiver down to bedrock
        else
        {
            for (int y = posMaster.posY - 1; y >= 0; --y)
            {
                Block block = world.getBlock(posMaster.posX, y, posMaster.posZ);
                if (block.isAir(world, posMaster.posX, y, posMaster.posZ) == false)
                {
                    if (block.getLightOpacity(world, posMaster.posX, y, posMaster.posZ) > 3)
                    {
                        if (block != Blocks.bedrock)
                        {
                            return true;
                        }

                        break;
                    }
                }
            }
        }

        // Check the column above the master block up to world height or first bedrock block
        for (int y = posMaster.posY + 1; y <= world.getActualHeight(); ++y)
        {
            Block block = world.getBlock(posMaster.posX, y, posMaster.posZ);
            if (block.isAir(world, posMaster.posX, y, posMaster.posZ) == false)
            {
                if (block.getLightOpacity(world, posMaster.posX, y, posMaster.posZ) > 3)
                {
                    if (block != Blocks.bedrock)
                    {
                        return true;
                    }

                    break;
                }
            }
        }

        /*if (world.canBlockSeeTheSky(posMaster.posX, posMaster.posY, posMaster.posZ) == false)
        {
            return true;
        }*/

        return false;
    }

    public void disassembleMultiblock(World world, BlockPos pos, int height, int masterMeta, int oldMeta)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te == null || (te instanceof TileEntityEnergyBridge) == false)
        {
            return;
        }

        BlockPosEU posMaster = new BlockPosEU(pos); // position of the master block (the transmitter or the receiver)

        // The given location is a resonator, not the master block; get the master block's location
        if (oldMeta == 2)
        {
            EnumFacing dir = EnumFacing.getFront(((TileEntityEnergyBridge)te).getRotation());
            posMaster = posMaster.add(0, height - 1, 0).offset(dir, 3);
        }

        // Get the block position list from the master block
        List<BlockPosEU> positions = new ArrayList<BlockPosEU>();
        if (this.getBlockPositions(world, x, y, z, height, masterMeta, positions) == false)
        {
            return;
        }

        this.disableMultiBlock(world, masterMeta, positions);
    }

    public void disableMultiBlock(World world, int masterMeta, List<BlockPos> blockPositions)
    {
        if (blockPositions == null || blockPositions.size() != 6)
        {
            return;
        }

        Block blockEb = EnderUtilitiesBlocks.machine_1;
        Class<TileEntityEnergyBridge> classTEEB = TileEntityEnergyBridge.class;

        this.setStateWithCheck(world, blockPositions.get(0), blockEb, masterMeta, classTEEB, null, false);
        this.setStateWithCheck(world, blockPositions.get(1), blockEb, 2, classTEEB, EnumFacing.SOUTH, false);
        this.setStateWithCheck(world, blockPositions.get(2), blockEb, 2, classTEEB, EnumFacing.NORTH, false);
        this.setStateWithCheck(world, blockPositions.get(3), blockEb, 2, classTEEB, EnumFacing.WEST, false);
        this.setStateWithCheck(world, blockPositions.get(4), blockEb, 2, classTEEB, EnumFacing.EAST, false);

        EnergyBridgeTracker.removeBridgeLocation(blockPositions.get(0));
    }

    public void setState(World world, BlockPos pos, boolean state)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setState(state);
        }
    }

    public void setStateWithCheck(World worldIn, BlockPos pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass,
            EnumFacing requiredDirection, boolean state)
    {
        if (BlockUtils.blockMatches(worldIn, pos, requiredBlock, requiredMeta, TEClass, requiredDirection) == true)
        {
            ((TileEntityEnergyBridge)worldIn.getTileEntity(pos)).setState(state);
        }
    }

    public void updatePoweredState(World world, List<BlockPos> positions)
    {
        if (positions == null || positions.size() != 6)
        {
            return;
        }

        int dim = world.provider.getDimensionId();
        boolean powered = EnergyBridgeTracker.dimensionHasEnergyBridge(dim) == true && (dim == 1 || EnergyBridgeTracker.dimensionHasEnergyBridge(1) == true);

        for (int i = 0; i < 5; ++i)
        {
            this.updatePoweredState(world, positions.get(i), powered);
        }
    }

    public void updatePoweredState(World world, BlockPos pos, boolean value)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setPowered(value);
        }
    }

    @SideOnly(Side.CLIENT)
    public void getBeamEndPoints()
    {
        int posX = this.getPos().getX();
        int posY = this.getPos().getY();
        int posZ = this.getPos().getZ();
        int y = posY;

        // Energy Bridge Transmitter
        if (this.getBlockMetadata() == 0)
        {
            this.beamYMin = posY - 2;
        }
        // Energy Bridge Receiver
        else if (this.getBlockMetadata() == 1)
        {
            for (y = posY; y >= 0; y--)
            {
                if (this.worldObj.getBlockState(new BlockPos(posX, y, posZ)).getBlock() == Blocks.bedrock)
                {
                    break;
                }
            }

            this.beamYMin = y + 1;
        }

        for (y = posY; y < this.worldObj.getHeight(); y++)
        {
            if (this.worldObj.getBlockState(new BlockPos(posX, y, posZ)).getBlock() == Blocks.bedrock)
            {
                break;
            }
        }

        this.beamYMax = y;
        this.renderBB = AxisAlignedBB.fromBounds(posX - 4d, this.beamYMin - 4d, posZ - 4d, posX + 4d, this.beamYMax + 4d, posZ + 4d);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0d;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return this.renderBB != null ? this.renderBB : INFINITE_EXTENT_AABB;
    }
}
