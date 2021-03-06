package fi.dy.masa.enderutilities.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;

public class ItemEnderPearlReusable extends ItemEnderUtilities
{
    public ItemEnderPearlReusable(String name)
    {
        super(name);

        this.setMaxStackSize(4);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // damage 1: Elite Ender Pearl
        if (stack.getMetadata() == 1)
        {
            return super.getUnlocalizedName() + "_elite";
        }

        return super.getUnlocalizedName();
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote)
        {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        // Damage 1: "Elite version" of the pearl, makes the thrower fly with it. Idea by xisumavoid in episode Hermitcraft III 303 :)

        EntityEnderPearlReusable pearl = new EntityEnderPearlReusable(world, player, stack.getMetadata() == 1);
        float velocity = stack.getMetadata() == 1 ? 2.5f : 1.9f;
        pearl.shoot(player, player.rotationPitch, player.rotationYaw, 0.0f, velocity, 0.8f);
        world.spawnEntity(pearl);

        if (stack.getMetadata() == 1)
        {
            Entity bottomEntity = player.getLowestRidingEntity();

            // Dismount the previous pearl if we are already riding one
            // (by selecting the entity riding that pearl to be the one mounted to the new pearl)
            if (bottomEntity instanceof EntityEnderPearlReusable)
            {
                bottomEntity = bottomEntity.getPassengers().get(0);
            }

            bottomEntity.startRiding(pearl);
        }

        stack.shrink(1);

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW,
                SoundCategory.MASTER, 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f));

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void getSubItemsCustom(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Regular
        list.add(new ItemStack(this, 1, 1)); // Elite
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "elite=false"),
                new ModelResourceLocation(rl, "elite=true")
        };
    }
}
