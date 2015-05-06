package fi.dy.masa.enderutilities.util;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPos
{
    public int posX;
    public int posY;
    public int posZ;

    public BlockPos(int x, int y, int z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.clampCoords();
    }

    public BlockPos(BlockPos old)
    {
        this.posX = old.posX;
        this.posY = old.posY;
        this.posZ = old.posZ;
    }

    public BlockPos(BlockPos old, ForgeDirection dir, int distance)
    {
        this(old);
        this.offset(dir, distance);
    }

    public void add(int x, int y, int z)
    {
        this.posX += x;
        this.posY += y;
        this.posZ += z;

        this.clampCoords();
    }

    public void offset(ForgeDirection dir, int distance)
    {
        this.posX += dir.offsetX * distance;
        this.posY += dir.offsetY * distance;
        this.posZ += dir.offsetZ * distance;
    }

    public void clampCoords()
    {
        this.posX = MathHelper.clamp_int(this.posX, -30000000, 30000000);
        this.posY = MathHelper.clamp_int(this.posY, 0, 255);
        this.posZ = MathHelper.clamp_int(this.posZ, -30000000, 30000000);
    }
}
