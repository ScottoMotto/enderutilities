package fi.dy.masa.enderutilities.setup;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.reference.Reference;

public class ConfigReader
{
    public static final int CURRENT_CONFIG_VERSION = 40;
    public static int confVersion = 0;

    public static void loadConfigsAll(File baseConfigDir)
    {
        // minecraft/config/enderutilities/something.cfg
        File configDir = new File(baseConfigDir.getAbsolutePath().concat("/").concat(Reference.MOD_ID));
        configDir.mkdirs();

        EnderUtilities.logger.info("Loading configuration...");
        ConfigReader.loadConfigGeneric(new File(configDir, Reference.MOD_ID + "_main.cfg"));
        ConfigReader.loadConfigItemControl(new File(configDir, Reference.MOD_ID + "_itemcontrol.cfg"));
        ConfigReader.loadConfigLists(new File(configDir, Reference.MOD_ID + "_lists.cfg"));
    }

    public static void loadConfigGeneric(File configFile)
    {
        String category;
        Configuration conf = new Configuration(configFile);
        conf.load();

        category = "Generic";
        Configs.enderBowAllowPlayers = conf.get(category, "EnderBowAllowPlayers", false).setRequiresMcRestart(false);
        Configs.enderBowAllowPlayers.comment = "Is the Ender Bow allowed to teleport players (directly or in a 'stack' riding something)";

        Configs.enderBowAllowSelfTP = conf.get(category, "EnderBowAllowSelfTP", true).setRequiresMcRestart(false);
        Configs.enderBowAllowSelfTP.comment = "Can the Ender Bow be used in the 'TP Self' mode";

        Configs.enderBucketCapacity = conf.get(category, "EnderBucketCapacity", ItemEnderBucket.ENDER_BUCKET_MAX_AMOUNT).setRequiresMcRestart(false);
        Configs.enderBucketCapacity.comment = "Maximum amount the Ender Bucket can hold, in millibuckets. Default: 16000 mB (= 16 buckets).";

        Configs.enderLassoAllowPlayers = conf.get(category, "EnderLassoAllowPlayers", false).setRequiresMcRestart(false);
        Configs.enderLassoAllowPlayers.comment = "Is the Ender Lasso allowed to teleport players (directly or in a 'stack' riding something)";

        category = "Version";
        // 0.3.1 was the version where the configs were first added, use that as the default (note that the version number itself was added later in 0.3.2)
        Configs.configFileVersion = conf.get(category, "ConfigFileVersion", 31).setRequiresMcRestart(true);
        Configs.configFileVersion.comment = "Internal config file version tracking. DO NOT CHANGE!!";
        confVersion = Configs.configFileVersion.getInt();

        // Update the version in the config to the current version
        Configs.configFileVersion.setValue(CURRENT_CONFIG_VERSION);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    public static void loadConfigLists(File configFile)
    {
        String category;
        Configuration conf = new Configuration(configFile);
        conf.load();

        category = "EnderBag";
        Configs.enderBagListType = conf.get(category, "ListType", "whitelist").setRequiresMcRestart(true);
        Configs.enderBagListType.comment = "Target control list type used for Ender Bag. Allowed values: blacklist, whitelist.";

        Configs.enderBagBlacklist = conf.get(category, "BlackList", new String[] {}).setRequiresMcRestart(true);
        Configs.enderBagBlacklist.comment = "Block types the Ender Bag is NOT allowed to (= doesn't properly) work with.";

        Configs.enderBagWhitelist = conf.get(category, "WhiteList", new String[] {"minecraft:chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:ender_chest", "minecraft:furnace", "minecraft:hopper", "minecraft:trapped_chest"}).setRequiresMcRestart(true);
        Configs.enderBagWhitelist.comment = "Block types the Ender Bag is allowed to (= should properly) work with.";

        category = "Teleporting";
        Configs.teleportBlacklist = conf.get(category, "EntityBlackList", new String[] {"EntityDragon", "EntityDragonPart", "EntityEnderCrystal", "EntityWither"}).setRequiresMcRestart(true);
        Configs.teleportBlacklist.comment = "Entities that are not allowed to be teleported using any methods";

        updateConfigLists(conf);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    public static void updateConfigLists(Configuration conf)
    {
        boolean found = false;
        int i = 0;

        // 0.3.2: Add EntityEnderCrystal to teleport blacklist
        if (confVersion < 32)
        {
            EnderUtilities.logger.info("Updating configuration lists to 32");

            String[] strs = Configs.teleportBlacklist.getStringList();
            String[] strsNew = new String[strs.length + 1];
            for (i = 0; i < strs.length; ++i)
            {
                strsNew[i] = strs[i];
                if (strs[i].equals("EntityEnderCrystal") == true)
                {
                    found = true;
                }
            }

            if (found == false)
            {
                strsNew[i] = "EntityEnderCrystal";
                Configs.teleportBlacklist.setValues(strsNew);
            }
        }
    }

    public static void loadConfigItemControl(File configFile)
    {
        String category;
        Configuration conf = new Configuration(configFile);
        conf.load();

        category = "DisableBlocks";
        conf.addCustomCategoryComment(category, "Note that machines are grouped together and identified by the meta value. You can't disable just a specific meta value.");

        // Block disable
        Configs.disableBlockMachine_0             = conf.get(category, "DisableBlockMachine_0", false).setRequiresMcRestart(true);

        category = "DisableItems";
        conf.addCustomCategoryComment(category, "Note that some items are grouped together using the damage value to identify them. You can't completely disable a specific damage value (so that existing items would vanish).");

        // Item disable
        Configs.disableItemCraftingPart           = conf.get(category, "DisableItemCraftingPart", false).setRequiresMcRestart(true);
        Configs.disableItemEnderCapacitor         = conf.get(category, "DisableItemEnderCapacitor", false).setRequiresMcRestart(true);
        Configs.disableItemLinkCrystal            = conf.get(category, "DisableItemLinkCrystal", false).setRequiresMcRestart(true);

        Configs.disableItemEnderArrow             = conf.get(category, "DisableItemEnderArrow", false).setRequiresMcRestart(true);
        Configs.disableItemEnderBag               = conf.get(category, "DisableItemEnderBag", false).setRequiresMcRestart(true);
        Configs.disableItemEnderBow               = conf.get(category, "DisableItemEnderBow", false).setRequiresMcRestart(true);
        Configs.disableItemEnderBucket            = conf.get(category, "DisableItemEnderBucket", false).setRequiresMcRestart(true);
        Configs.disableItemEnderLasso             = conf.get(category, "DisableItemEnderLasso", false).setRequiresMcRestart(true);
        Configs.disableItemEnderPearl             = conf.get(category, "DisableItemEnderPearl", false).setRequiresMcRestart(true);
        Configs.disableItemEnderPorterBasic       = conf.get(category, "DisableItemEnderPorterBasic", false).setRequiresMcRestart(true);
        Configs.disableItemEnderPorterAdvanced    = conf.get(category, "DisableItemEnderPorterAdvanced", false).setRequiresMcRestart(true);
        Configs.disableItemEnderSword             = conf.get(category, "DisableItemEnderSword", false).setRequiresMcRestart(true);
        Configs.disableItemEnderTool              = conf.get(category, "DisableItemEnderTool", false).setRequiresMcRestart(true);
        Configs.disableItemMobHarness             = conf.get(category, "DisableItemMobHarness", false).setRequiresMcRestart(true);

        category = "DisableRecipies";
        // Recipe disable
        // Blocks
        Configs.disableRecipeEnderFurnace         = conf.get(category, "DisableRecipeEnderFurnace", false).setRequiresMcRestart(true);

        // Items
        Configs.disableRecipeEnderArrow           = conf.get(category, "DisableRecipeEnderArrow", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderBag             = conf.get(category, "DisableRecipeEnderBag", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderBow             = conf.get(category, "DisableRecipeEnderBow", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderBucket          = conf.get(category, "DisableRecipeEnderBucket", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderLasso           = conf.get(category, "DisableRecipeEnderLasso", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPearl           = conf.get(category, "DisableRecipeEnderPearl", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPorterBasic     = conf.get(category, "DisableRecipeEnderPorterBasic", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPorterAdvanced  = conf.get(category, "DisableRecipeEnderPorterAdvanced", false).setRequiresMcRestart(true);
        Configs.disableRecipeMobHarness           = conf.get(category, "DisableRecipeMobHarness", false).setRequiresMcRestart(true);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }
}
