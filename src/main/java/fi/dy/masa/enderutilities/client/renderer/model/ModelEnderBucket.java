package fi.dy.masa.enderutilities.client.renderer.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class ModelEnderBucket implements IModel
{
    public static final IModel MODEL = new ModelEnderBucket();
    private final ResourceLocation resourceMain;
    private final ResourceLocation resourceInsideTop;
    private final ResourceLocation resourceInsideBottom;
    private final ResourceLocation resourceModeIcon;
    private final Fluid fluid;
    private final boolean flipGas;
    private final int amount;
    private final int capacity;

    public ModelEnderBucket()
    {
        this(null, null, null, null, null, 0, 0, false);
    }

    public ModelEnderBucket(ResourceLocation main, ResourceLocation insideTop, ResourceLocation insideBottom,
                            ResourceLocation mode, Fluid fluid, int amount, int capacity, boolean flipGas)
    {
        this.resourceMain = main;
        this.resourceInsideTop = insideTop;
        this.resourceInsideBottom = insideBottom;
        this.resourceModeIcon = mode;
        this.fluid = fluid;
        this.flipGas = flipGas;
        this.amount = amount;
        this.capacity = capacity;
    }

    @Override
    public IModelState getDefaultState()
    {
        return TRSRTransformation.identity();
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures()
    {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_normal_main"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_normal_insidetop"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_normal_insidebottom"));

        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_linked_main"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_linked_insidetop"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_linked_insidebottom"));

        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_mode_drain"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_mode_fill"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket_32_mode_bind"));

        return builder.build();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        String fluidName = customData.get("fluid");
        Fluid fluid = fluidName != null ? FluidRegistry.getFluid(fluidName) : null;
        boolean flip = this.flipGas;
        String tmp = customData.get("linked");
        boolean isLinked = (tmp != null && tmp.equals("true"));
        int amount = 0;
        int capacity = 1000;
        String mode = null;

        if (customData.containsKey("mode"))
        {
            tmp = customData.get("mode");
            if (tmp != null && (tmp.equals("drain") || tmp.equals("fill") || tmp.equals("bind")))
            {
                mode = tmp;
            }
        }

        if (customData.containsKey("amount"))
        {
            try
            {
                amount = Integer.valueOf(customData.get("amount"));
            }
            catch (NumberFormatException e) {}
        }

        if (customData.containsKey("capacity"))
        {
            try
            {
                capacity = Integer.valueOf(customData.get("capacity"));
            }
            catch (NumberFormatException e) {}
        }

        String rlBase = Reference.MOD_ID + ":items/enderbucket_32_";
        ResourceLocation main = new ResourceLocation(rlBase + (isLinked ? "linked_" : "normal_") + "main");
        ResourceLocation inTop = new ResourceLocation(rlBase + (isLinked ? "linked_" : "normal_") + "insidetop");
        ResourceLocation inBot = new ResourceLocation(rlBase + (isLinked ? "linked_" : "normal_") + "insidebottom");
        ResourceLocation rlMode = mode != null ? new ResourceLocation(rlBase + "mode_" + mode) : null;

        return new ModelEnderBucket(main, inTop, inBot, rlMode, fluid, amount, capacity, flip);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
                                    Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);
        TRSRTransformation transform = state.apply(Optional.empty()).orElse(TRSRTransformation.identity());
        TextureAtlasSprite mainSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        if (this.resourceMain != null)
        {
            mainSprite = bakedTextureGetter.apply(this.resourceMain);
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceMain))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceInsideTop != null)
        {
            // Inset the inside part a little, the fluid model will be on top of it
            IModelState stateTmp = this.getTransformedModelState(state, 0f, 0.95f);
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceInsideTop))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceInsideBottom != null)
        {
            // Inset the inside part a little, the fluid model will be on top of it
            IModelState stateTmp = this.getTransformedModelState(state, 0f, 0.95f);
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceInsideBottom))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceModeIcon != null)
        {
            // Offset the mode icons a bit, so that they stick out slightly
            IModelState stateTmp = this.getTransformedModelState(state, 0.0125f, 1f);
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceModeIcon))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.fluid != null)
        {
            TextureAtlasSprite fluidTex = bakedTextureGetter.apply(this.fluid.getStill());
            int color = fluid.getColor();
            float capacity = this.capacity > 0 ? this.capacity : 1000;
            float height = (float)this.amount / capacity;
            // top x: 4 .. 12 ; y: 3 .. 7
            // bottom: x: 6.5 .. 10 ; y: 9 .. 13
            float yt = 7 - height * 4;
            float yb = 13 - height * 4;
            // Top part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform,   4f, yt, 12f,  7f, 0.469f, fluidTex, EnumFacing.NORTH, color));
            // Bottom part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform, 6.5f, yb, 10f, 13f, 0.469f, fluidTex, EnumFacing.NORTH, color));

            // Top part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform,   4f, yt, 12f,  7f, 0.531f, fluidTex, EnumFacing.SOUTH, color));
            // Bottom part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform, 6.5f, yb, 10f, 13f, 0.531f, fluidTex, EnumFacing.SOUTH, color));
        }

        return new BakedEnderBucket(this, builder.build(), mainSprite, format, Maps.immutableEnumMap(transformMap), Maps.<String, IBakedModel>newHashMap());
    }

    private IModelState getTransformedModelState(IModelState state, float offZ, float scaleZ)
    {
        TRSRTransformation tr = new TRSRTransformation(new Vector3f(0f, 0f, offZ), null, new Vector3f(1f, 1f, scaleZ), null);
        return new ModelStateComposition(state, TRSRTransformation.blockCenterToCorner(tr));
    }

    private static final class BakedEnderBucketOverrideHandler extends ItemOverrideList
    {
        public static final BakedEnderBucketOverrideHandler INSTANCE = new BakedEnderBucketOverrideHandler();

        private BakedEnderBucketOverrideHandler()
        {
            super(ImmutableList.<ItemOverride>of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModelIn, ItemStack stack, World world, EntityLivingBase entity)
        {
            if ((stack.getItem() instanceof ItemEnderBucket) == false)
            {
                return originalModelIn;
            }

            BakedEnderBucket originalModel = (BakedEnderBucket) originalModelIn;
            ItemEnderBucket item = (ItemEnderBucket)stack.getItem();
            String linked = ItemEnderBucket.LinkMode.fromStack(stack) == ItemEnderBucket.LinkMode.ENABLED ? "true" : "false";
            int capacity = item.getCapacityCached(stack, null);
            ItemEnderBucket.BucketMode mode = ItemEnderBucket.BucketMode.fromStack(stack);
            String modeStr = "none";
            int amount = 0;

            FluidStack fluidStack = item.getFluidCached(stack);
            Fluid fluid = null;

            if (fluidStack != null)
            {
                amount = fluidStack.amount;
                fluid = fluidStack.getFluid();
            }

            if (mode == ItemEnderBucket.BucketMode.DRAIN) { modeStr = "drain"; }
            else if (mode == ItemEnderBucket.BucketMode.FILL) { modeStr = "fill"; }
            else if (mode == ItemEnderBucket.BucketMode.BIND) { modeStr = "bind"; }

            String key = linked + "_" + modeStr + "_" + fluid + "_" + amount + "_" + capacity;

            if (originalModel.cache.containsKey(key) == false)
            {
                ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
                if (fluid != null)
                {
                    map.put("fluid", fluid.getName());
                }
                map.put("linked", linked);
                map.put("mode", modeStr);
                map.put("amount", String.valueOf(amount));
                map.put("capacity", String.valueOf(capacity));

                IModel parent = originalModel.parent.process(map.build());

                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
                {
                    public TextureAtlasSprite apply(ResourceLocation location)
                    {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                    }
                };

                IBakedModel bakedModel = parent.bake(new SimpleModelState(originalModel.transforms), originalModel.format, textureGetter);
                originalModel.cache.put(key, bakedModel);

                return bakedModel;
            }

            return originalModel.cache.get(key);
        }
    }

    //protected static class BakedEnderBucket extends ItemLayerModel.BakedModel implements ISmartItemModel, IPerspectiveAwareModel
    protected static class BakedEnderBucket implements IBakedModel
    {
        private final ModelEnderBucket parent;
        private final Map<String, IBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;
        private final ImmutableList<BakedQuad> quads;
        private final TextureAtlasSprite particle;
        private final VertexFormat format;

        public BakedEnderBucket(ModelEnderBucket parent, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format,
                ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, Map<String, IBakedModel> cache)
        {
            this.quads = quads;
            this.particle = particle;
            this.format = format;
            this.parent = parent;
            this.transforms = transforms;
            this.cache = cache;
        }

        @Override
        public ItemOverrideList getOverrides()
        {
            return BakedEnderBucketOverrideHandler.INSTANCE;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return PerspectiveMapWrapper.handlePerspective(this, this.transforms, cameraTransformType);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
        {
            if(side == null) return quads;
            return ImmutableList.of();
        }

        public boolean isAmbientOcclusion() { return true;  }
        public boolean isGui3d() { return false; }
        public boolean isBuiltInRenderer() { return false; }
        public TextureAtlasSprite getParticleTexture() { return particle; }
        public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    }

    public enum LoaderEnderBucket implements ICustomModelLoader
    {
        instance;

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.getResourceDomain().equals(Reference.MOD_ID) && modelLocation.getResourcePath().contains("generated_model_enderbucket");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws IOException
        {
            return MODEL;
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            // no need to clear cache since we create a new model instance
        }
    }
}
