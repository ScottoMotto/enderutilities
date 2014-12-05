package fi.dy.masa.enderutilities.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.item.ItemEnderArrow;
import fi.dy.masa.enderutilities.item.ItemEnderBag;
import fi.dy.masa.enderutilities.item.ItemEnderBow;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemEnderPearlReusable;
import fi.dy.masa.enderutilities.item.ItemEnderPorter;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class EnderUtilitiesItems
{
    public static final ItemEnderUtilities enderPart = new ItemEnderPart();
    public static final ItemEnderUtilities enderCapacitor = new ItemEnderCapacitor();
    public static final ItemEnderUtilities linkCrystal = new ItemLinkCrystal();

    public static final ItemEnderUtilities enderArrow = new ItemEnderArrow();
    public static final ItemEnderUtilities enderBag = new ItemEnderBag();
    public static final ItemEnderUtilities enderBow = new ItemEnderBow();
    public static final Item enderBucket = new ItemEnderBucket();
    public static final ItemEnderUtilities enderLasso = new ItemEnderLasso();
    public static final ItemEnderUtilities enderPearlReusable = new ItemEnderPearlReusable();
    public static final ItemEnderUtilities enderPorter = new ItemEnderPorter();
    public static final Item enderSword = new ItemEnderSword();
    public static final Item enderTool = new ItemEnderTool();
    public static final ItemEnderUtilities mobHarness = new ItemMobHarness();

    public static void init()
    {
        if (EUConfigs.disableItemCraftingPart.getBoolean(false) == false) {
            GameRegistry.registerItem(enderPart, ReferenceBlocksItems.NAME_ITEM_ENDERPART);
        }
        if (EUConfigs.disableItemEnderCapacitor.getBoolean(false) == false) {
            GameRegistry.registerItem(enderCapacitor, ReferenceBlocksItems.NAME_ITEM_ENDERPART_ENDERCAPACITOR);
        }
        if (EUConfigs.disableItemLinkCrystal.getBoolean(false) == false) {
            GameRegistry.registerItem(linkCrystal, ReferenceBlocksItems.NAME_ITEM_ENDERPART_LINKCRYSTAL);
        }
        if (EUConfigs.disableItemEnderArrow.getBoolean(false) == false) {
            GameRegistry.registerItem(enderArrow, ReferenceBlocksItems.NAME_ITEM_ENDER_ARROW);
        }
        if (EUConfigs.disableItemEnderBag.getBoolean(false) == false) {
            GameRegistry.registerItem(enderBag, ReferenceBlocksItems.NAME_ITEM_ENDER_BAG);
        }
        if (EUConfigs.disableItemEnderBow.getBoolean(false) == false) {
            GameRegistry.registerItem(enderBow, ReferenceBlocksItems.NAME_ITEM_ENDER_BOW);
        }
        if (EUConfigs.disableItemEnderBucket.getBoolean(false) == false) {
            GameRegistry.registerItem(enderBucket, ReferenceBlocksItems.NAME_ITEM_ENDER_BUCKET);
        }
        if (EUConfigs.disableItemEnderLasso.getBoolean(false) == false) {
            GameRegistry.registerItem(enderLasso, ReferenceBlocksItems.NAME_ITEM_ENDER_LASSO);
        }
        if (EUConfigs.disableItemEnderPearl.getBoolean(false) == false) {
            GameRegistry.registerItem(enderPearlReusable, ReferenceBlocksItems.NAME_ITEM_ENDER_PEARL_REUSABLE);
        }
        if (EUConfigs.disableItemEnderPorterBasic.getBoolean(false) == false ||
            EUConfigs.disableItemEnderPorterAdvanced.getBoolean(false) == false) {
            GameRegistry.registerItem(enderPorter, ReferenceBlocksItems.NAME_ITEM_ENDER_PORTER);
        }
        if (EUConfigs.disableItemEnderSword.getBoolean(false) == false) {
            GameRegistry.registerItem(enderSword, ReferenceBlocksItems.NAME_ITEM_ENDER_SWORD);
        }
        if (EUConfigs.disableItemEnderTool.getBoolean(false) == false) {
            GameRegistry.registerItem(enderTool, ReferenceBlocksItems.NAME_ITEM_ENDERTOOL);
        }
        if (EUConfigs.disableItemMobHarness.getBoolean(false) == false) {
            GameRegistry.registerItem(mobHarness, ReferenceBlocksItems.NAME_ITEM_MOB_HARNESS);
        }

        ItemStack arrow = new ItemStack(Items.arrow);
        ItemStack bow = new ItemStack(Items.bow);
        ItemStack bucket = new ItemStack(Items.bucket);
        ItemStack diamond = new ItemStack(Items.diamond);
        ItemStack eye = new ItemStack(Items.ender_eye);
        ItemStack gold = new ItemStack(Items.gold_ingot);
        ItemStack goldnugget = new ItemStack(Items.gold_nugget);
        ItemStack leather = new ItemStack(Items.leather);
        ItemStack pearl = new ItemStack(Items.ender_pearl);
        //ItemStack powder = new ItemStack(Items.blaze_powder);
        ItemStack rsblock = new ItemStack(Blocks.redstone_block);
        ItemStack string = new ItemStack(Items.string);

        if (EUConfigs.disableRecipeEnderArrow.getBoolean(false) == false && EUConfigs.disableItemEnderArrow.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(enderArrow), " NP", " AN", "E  ", 'N', goldnugget, 'P', pearl, 'A', arrow, 'E', eye);
        }
        if (EUConfigs.disableRecipeEnderBag.getBoolean(false) == false && EUConfigs.disableItemEnderBag.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(enderBag), "LDL", "DED", "LDL", 'L', leather, 'D', diamond, 'E', eye);
        }
        if (EUConfigs.disableRecipeEnderBow.getBoolean(false) == false && EUConfigs.disableItemEnderBow.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(enderBow), "PDP", "DBD", "PDP", 'P', pearl, 'D', diamond, 'B', bow);
        }
        if (EUConfigs.disableRecipeEnderBucket.getBoolean(false) == false && EUConfigs.disableItemEnderBucket.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(enderBucket), "EGE", "DBD", "EGE", 'E', eye, 'G', gold, 'D', diamond, 'B', bucket);
        }
        if (EUConfigs.disableRecipeEnderLasso.getBoolean(false) == false && EUConfigs.disableItemEnderLasso.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(enderLasso), "DGD", "GPG", "DSD", 'D', diamond, 'G', gold, 'E', eye, 'P', pearl, 'S', string);
        }
        if (EUConfigs.disableRecipeEnderPearl.getBoolean(false) == false && EUConfigs.disableItemEnderPearl.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable), "PEP", "ERE", "PEP", 'P', pearl, 'E', eye, 'R', rsblock); // regular pearl
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable, 1, 1), " D ", "DPD", " D ", 'D', diamond, 'P', new ItemStack(enderPearlReusable, 1, 0)); // Elite pearl
        }
        if (EUConfigs.disableRecipeEnderPorterBasic.getBoolean(false) == false &&
            (EUConfigs.disableItemEnderPorterBasic.getBoolean(false) == false ||
            EUConfigs.disableItemEnderPorterAdvanced.getBoolean(false) == false)) {
            GameRegistry.addRecipe(new ItemStack(enderPorter), "PNP", "NRN", "PNP", 'P', pearl, 'N', goldnugget, 'R', rsblock);
        }
        if (EUConfigs.disableRecipeEnderPorterAdvanced.getBoolean(false) == false &&
                (EUConfigs.disableItemEnderPorterBasic.getBoolean(false) == false ||
                EUConfigs.disableItemEnderPorterAdvanced.getBoolean(false) == false)) {
            GameRegistry.addRecipe(new ItemStack(enderPorter, 1, 1), "GDG", "DPD", "GDG", 'G', gold, 'D', diamond, 'P', new ItemStack(enderPorter, 1, 0)); // Ender Porter (Advanced)
        }
        if (EUConfigs.disableRecipeMobHarness.getBoolean(false) == false && EUConfigs.disableItemMobHarness.getBoolean(false) == false) {
            GameRegistry.addRecipe(new ItemStack(mobHarness), "LEL", "LDL", "LEL", 'L', leather, 'E', eye, 'D', diamond);
        }
    }
}
