package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.setup.Registry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

@SuppressWarnings("deprecation")
public class ItemEnderBag extends ItemLocationBoundModular implements IChunkLoadingItem, IKeyBound
{
    public static final int ENDER_CHARGE_COST = 200;

    public ItemEnderBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BAG);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == true || stack == null || stack.getTagCompound() == null)
        {
            return stack;
        }

        NBTTagCompound bagNbt = stack.getTagCompound();
        NBTHelperTarget targetData = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (targetData == null || targetData.blockName == null)
        {
            return stack;
        }

        // Access is allowed for everyone to a vanilla Ender Chest
        if (targetData.blockName.equals("minecraft:ender_chest") == true)
        {
            if (UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST, true) == false)
            {
                return stack;
            }

            bagNbt.setBoolean("IsOpen", true);
            player.displayGUIChest(player.getInventoryEnderChest());
            return stack;
        }

        // For other targets, access is only allowed if the mode is set to public, or if the player is the owner
        if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return stack;
        }

        // Target block is not whitelisted, so it is known to not work unless within the client's loaded range
        // FIXME: How should we properly check if the player is within range?
        if (isTargetBlockWhitelisted(targetData.blockName, targetData.blockMeta) == false && targetOutsideOfPlayerRange(stack, player) == true)
        {
            player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("enderutilities.chat.message.enderbag.outofrange")));
            return stack;
        }

        // The target block has changed since binding the bag, remove the bind (not for vanilla Ender Chests)
        if (targetData.isTargetBlockUnchanged() == false)
        {
            NBTHelperTarget.removeTargetTagFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            bagNbt.removeTag("ChunkLoadingRequired");
            bagNbt.removeTag("IsOpen");

            player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.enderbag.blockchanged"));

            return stack;
        }

        // Check that we have sufficient charge left to use the bag.
        if (UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST, false) == false)
        {
            return stack;
        }

        // Only open the GUI if the chunk loading succeeds. 60 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 60) == true)
        {
            MinecraftServer server = MinecraftServer.getServer();
            if (server == null)
            {
                return stack;
            }

            World targetWorld = server.worldServerForDimension(targetData.dimension);
            if (targetWorld == null)
            {
                return stack;
            }

            // Actually use the charge. This _shouldn't_ be able to fail due to the above simulation...
            if (UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST, true) == false)
            {
                // Remove the chunk loading delay FIXME this doesn't take into account possible overlapping chunk loads...
                //ChunkLoading.getInstance().refreshChunkTimeout(targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 0, false);
                return stack;
            }

            bagNbt.setBoolean("ChunkLoadingRequired", true);
            bagNbt.setBoolean("IsOpen", true);

            float hx = (float)targetData.dPosX - targetData.posX;
            float hy = (float)targetData.dPosY - targetData.posY;
            float hz = (float)targetData.dPosZ - targetData.posZ;

            IBlockState iBlockState = world.getBlockState(targetData.pos);
            Block block = iBlockState.getBlock();
            // Access is allowed in onPlayerOpenContainer(PlayerOpenContainerEvent event) in PlayerEventHandler
            block.onBlockActivated(targetWorld, targetData.pos, iBlockState, player, targetData.facing, hx, hy, hz);
        }

        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true || player.isSneaking() == false)
        {
            return true;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te != null && (te instanceof IInventory || te.getClass() == TileEntityEnderChest.class))
        {
            /*if (this.isTargetBlockWhitelisted(Block.blockRegistry.getNameForObject(block), meta) == false)
            {
                player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("enderutilities.chat.message.enderbag.blocknotwhitelisted")
                        + " '" + Block.blockRegistry.getNameForObject(block) + ":" + meta + "'"));
                return true;
            }*/

            return super.onItemUse(stack, player, world, pos, face, hitX, hitY, hitZ);
        }

        return true;
    }

    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Only allow the block/inventory type Link Crystals
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == false || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
        {
            return this.getMaxModules(toolStack, moduleType);
        }

        return 0;
    }

    public static boolean targetNeedsToBeLoadedOnClient(ItemStack stack)
    {
        NBTHelperTarget targetData = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (targetData == null || targetData.blockName == null)
        {
            return false;
        }

        // Player's location doesn't matter with Ender Chests
        if (targetData.blockName.equals("minecraft:ender_chest") == true
            || isTargetBlockWhitelisted(targetData.blockName, targetData.blockMeta) == true)
        {
            return false;
        }

        return true;
    }

    public static boolean targetOutsideOfPlayerRange(ItemStack stack, EntityPlayer player)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (target == null)
        {
            return true;
        }

        // We allow a max range of 64 blocks, to hopefully be on the safer side
        return target.dimension != player.dimension || player.getDistanceSq(target.posX, target.posY, target.posZ) >= 4096.0d;
    }

    public static boolean isTargetBlockWhitelisted(String name, int meta)
    {
        List<String> list;

        // FIXME add metadata handling
        // Black list
        if (Configs.enderBagListType.getString().equalsIgnoreCase("blacklist") == true)
        {
            list = Registry.getEnderbagBlacklist();
            if (list.contains(name) == true)
            {
                return false;
            }

            return true;
        }
        // White list
        else
        {
            list = Registry.getEnderbagWhitelist();
            if (list.contains(name) == true)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (target != null)
        {
            ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);

            if (targetStack != null && targetStack.getItem() != null)
            {
                String pre = EnumChatFormatting.GREEN.toString();
                String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();
                return StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim() + " " + pre + targetStack.getDisplayName() + rst;
            }
        }

        return StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (target != null)
        {
            if ("minecraft:ender_chest".equals(target.blockName))
            {
                ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);
                String targetName = (targetStack != null && targetStack.getItem() != null ? targetStack.getDisplayName() : "");

                String textPre = EnumChatFormatting.DARK_GREEN.toString();
                String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.target") + ": " + textPre + targetName + rst);
                return;
            }
        }

        super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player)
    {
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean isCurrent)
    {
        // Ugly workaround to get the bag closing tag update to sync to the client
        // For some reason it won't sync if set directly in the PlayerOpenContainerEvent
        if (stack != null && stack.getTagCompound() != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt.hasKey("IsOpenDummy") == true)
            {
                nbt.removeTag("IsOpenDummy");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerTextures(TextureMap textureMap)
    {
        this.textures = new TextureAtlasSprite[4];
        this.texture_names = new String[this.textures.length];

        // TODO add locked textures
        this.registerTexture(0, this.name + ".regular.closed", textureMap);
        this.registerTexture(1, this.name + ".regular.open", textureMap);
        this.registerTexture(2, this.name + ".enderchest.closed", textureMap);
        this.registerTexture(3, this.name + ".enderchest.open", textureMap);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBakedModel getItemModel(ItemStack stack)
    {
        int index = 0;
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (target != null)
        {
            // Bag currently open
            if (stack.getTagCompound().getBoolean("IsOpen") == true)
            {
                index += 1;
            }

            // Currently linked to a vanilla Ender Chest
            if ("minecraft:ender_chest".equals(target.blockName))
            {
                index += 2;
            }
        }

        /*
        NBTHelperPlayer playerData = NBTHelperPlayer.getPlayerDataFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (playerData != null && playerData.isPublic == false)
        {
            index += 4;
        }
        */

        return this.models[index < this.textures.length ? index : 0];
    }
}
