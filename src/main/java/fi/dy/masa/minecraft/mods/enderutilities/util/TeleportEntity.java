package fi.dy.masa.minecraft.mods.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;

public class TeleportEntity
{
	public static boolean transferEntityToDimension(EntityLiving entity, int dim)
	{
		TeleportEntity.transferEntityToDimension(entity, dim, entity.posX, entity.posY, entity.posZ);
		return true;
	}

	public static boolean transferEntityToDimension(EntityLiving entitySrc, int dimDst, double x, double y, double z)
	{
		if (entitySrc != null && entitySrc.worldObj.isRemote == false && entitySrc.isDead == false)
		{
			int dimSrc = entitySrc.dimension;
			entitySrc.worldObj.theProfiler.startSection("changeDimension");

			MinecraftServer minecraftserver = MinecraftServer.getServer();
			WorldServer worldServerSrc = minecraftserver.worldServerForDimension(dimSrc);
			WorldServer worldServerDst = minecraftserver.worldServerForDimension(dimDst);
			entitySrc.dimension = dimDst;

			entitySrc.worldObj.removeEntity(entitySrc);
			entitySrc.isDead = false;
			entitySrc.worldObj.theProfiler.startSection("reposition");

			entitySrc.worldObj.theProfiler.endStartSection("reloading");
			Entity entityDst = EntityList.createEntityByName(EntityList.getEntityString(entitySrc), worldServerDst);

			if (entityDst != null && entityDst.isEntityAlive() == true)
			{
				x = (double)MathHelper.clamp_int((int)x, -29999872, 29999872);
				z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);

				entityDst.copyDataFrom(entitySrc, true);
				entityDst.setLocationAndAngles(x + 0.5d, y, z + 0.5d, entitySrc.rotationYaw, entitySrc.rotationPitch);
				worldServerDst.spawnEntityInWorld(entityDst);
				worldServerDst.updateEntityWithOptionalForce(entityDst, false);
				entityDst.setWorld(worldServerDst);
			}

			// FIXME debug: this actually kills the original entity, commenting it will make clones
			entitySrc.isDead = true;

			entitySrc.worldObj.theProfiler.endSection();
			worldServerSrc.resetUpdateEntityTick();
			worldServerDst.resetUpdateEntityTick();
			entitySrc.worldObj.theProfiler.endSection();

			return true;
		}

		return false;
	}
}