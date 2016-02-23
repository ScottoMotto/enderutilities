package fi.dy.masa.enderutilities.proxy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityEnderPearl;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.client.renderer.model.ItemMeshDefinitionWrapper;
import fi.dy.masa.enderutilities.client.renderer.model.ModelEnderBucket;
import fi.dy.masa.enderutilities.client.renderer.model.ModelEnderTools;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.event.GuiEventHandler;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.ConfigReader;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.Keybindings;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class ClientProxy extends CommonProxy
{
    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case CLIENT:
                return FMLClientHandler.instance().getClientPlayerEntity();
            case SERVER:
                return ctx.getServerHandler().playerEntity;
            default:
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @Override
    public void registerEventHandlers()
    {
        super.registerEventHandlers();

        MinecraftForge.EVENT_BUS.register(new ConfigReader());
        MinecraftForge.EVENT_BUS.register(new InputEventHandler());
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
    }

    @Override
    public void registerKeyBindings()
    {
        Keybindings.keyToggleMode = new KeyBinding(ReferenceKeys.KEYBIND_NAME_TOGGLE_MODE,
                                                   ReferenceKeys.DEFAULT_KEYBIND_TOGGLE_MODE,
                                                   ReferenceKeys.KEYBIND_CAREGORY_ENDERUTILITIES);

        ClientRegistry.registerKeyBinding(Keybindings.keyToggleMode);
    }

    @Override
    public void registerRenderers()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class,
                new IRenderFactory<EntityEnderArrow>() {
                    @Override public Render<? super EntityEnderArrow> createRenderFor (RenderManager manager) {
                        return new RenderEnderArrow(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class,
                new IRenderFactory<EntityEnderPearlReusable>() {
                    @Override public Render<? super EntityEnderPearlReusable> createRenderFor (RenderManager manager) {
                        return new RenderEntityEnderPearl(manager, EnderUtilitiesItems.enderPearlReusable, Minecraft.getMinecraft().getRenderItem());
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityEndermanFighter.class,
                new IRenderFactory<EntityEndermanFighter>() {
                    @Override public Render<? super EntityEndermanFighter> createRenderFor (RenderManager manager) {
                        return new RenderEndermanFighter(manager);
                    }
                });

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyBridge.class, new TileEntityRendererEnergyBridge());
        MinecraftForge.EVENT_BUS.register(new BuildersWandRenderer());
        MinecraftForge.EVENT_BUS.register(new RulerRenderer());
    }

    @Override
    public boolean isShiftKeyDown()
    {
        return GuiScreen.isShiftKeyDown();
    }

    @Override
    public boolean isControlKeyDown()
    {
        return GuiScreen.isCtrlKeyDown();
    }

    @Override
    public boolean isAltKeyDown()
    {
        return GuiScreen.isAltKeyDown();
    }

    @Override
    public void registerModels()
    {
        this.registerItemBlockModels();
        this.registerAllItemModels();
    }

    public void registerAllItemModels()
    {
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderCapacitor);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPart);
        this.registerItemModelWithVariants(EnderUtilitiesItems.linkCrystal);

        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.buildersWand);
        this.registerItemModel(EnderUtilitiesItems.enderArrow);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBag);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBow);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBucket);
        this.registerItemModel(EnderUtilitiesItems.enderLasso);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPearlReusable);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderPorter);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderSword);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderTool);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.handyBag);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.inventorySwapper);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.livingManipulator);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.mobHarness);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.pickupManager);
        this.registerItemModel(EnderUtilitiesItems.portalScaler);
        this.registerItemModel(EnderUtilitiesItems.ruler);

        ModelLoaderRegistry.registerLoader(ModelEnderBucket.LoaderEnderBucket.instance);
        ModelLoaderRegistry.registerLoader(ModelEnderTools.LoaderEnderTools.instance);
    }

    private void registerItemModel(ItemEnderUtilities item)
    {
        this.registerItemModel(item, 0);
    }

    private void registerItemModel(ItemEnderUtilities item, int meta)
    {
        ResourceLocation rl = Item.itemRegistry.getNameForObject(item);
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(rl, "inventory"));
    }

    private void registerItemModelWithVariants(ItemEnderUtilities item)
    {
        ResourceLocation[] variants = item.getItemVariants();
        List<ItemStack> items = new ArrayList<ItemStack>();
        item.getSubItems(item, item.getCreativeTab(), items);

        int i = 0;
        for (ItemStack stack : items)
        {
            ModelResourceLocation mrl = (variants[i] instanceof ModelResourceLocation) ?
                                        (ModelResourceLocation)variants[i] : new ModelResourceLocation(variants[i], "inventory");
            ModelLoader.setCustomModelResourceLocation(stack.getItem(), stack.getItemDamage(), mrl);
            i++;
        }
    }

    private void registerItemModelWithVariantsAndMeshDefinition(ItemEnderUtilities item)
    {
        ModelLoader.registerItemVariants(item, item.getItemVariants());
        ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
    }

    /*private void registerSmartItemModelWrapper(ItemEnderUtilities item)
    {
        ModelLoader.registerItemVariants(item, item.getItemVariants());
        ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
    }*/

    private void registerItemBlockModels()
    {
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockMachine_0, 0,  "facing=north,mode=off");

        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockMachine_1,    "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockEnergyBridge, "active=false,facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockStorage_0,    "facing=north,type=", "");
    }

    private void registerItemBlockModel(BlockEnderUtilities blockIn, int meta, String fullVariant)
    {
        ItemStack stack = new ItemStack(blockIn, 1, meta);
        Item item = stack.getItem();
        if (item == null)
        {
            return;
        }

        ModelResourceLocation mrl = new ModelResourceLocation(Item.itemRegistry.getNameForObject(item), fullVariant);
        ModelLoader.setCustomModelResourceLocation(item, stack.getItemDamage(), mrl);
    }

    private void registerAllItemBlockModels(BlockEnderUtilities blockIn, String variantPre, String variantPost)
    {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        blockIn.getSubBlocks(Item.getItemFromBlock(blockIn), blockIn.getCreativeTabToDisplayOn(), stacks);
        String[] names = blockIn.getUnlocalizedNames();

        for (ItemStack stack : stacks)
        {
            Item item = stack.getItem();
            int damage = stack.getItemDamage();
            ModelResourceLocation mrl = new ModelResourceLocation(Item.itemRegistry.getNameForObject(item), variantPre + names[damage] + variantPost);
            ModelLoader.setCustomModelResourceLocation(item, damage, mrl);
        }
    }
}
