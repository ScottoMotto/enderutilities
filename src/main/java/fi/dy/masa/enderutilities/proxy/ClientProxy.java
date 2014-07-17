package fi.dy.masa.enderutilities.proxy;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.item.ItemRendererEnderBucket;
import fi.dy.masa.enderutilities.client.renderer.item.RenderEnderBow;
import fi.dy.masa.enderutilities.client.settings.Keybindings;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.reference.key.ReferenceKeys;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerEventHandlers()
	{
		super.registerEventHandlers();
		FMLCommonHandler.instance().bus().register(new InputEventHandler());
	}

	@Override
	public void registerKeyBindings()
	{
		Keybindings.keyToggleMode = new KeyBinding(ReferenceKeys.KEYBIND_TOGGLE_MODE, ReferenceKeys.KEYBIND_DEFAULT_TOGGLE_MODE, ReferenceKeys.KEYBIND_CAREGORY_ENDERUTILITIES);

		ClientRegistry.registerKeyBinding(Keybindings.keyToggleMode);
	}

	@Override
	public void registerRenderers()
	{
		// FIXME
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class, new RenderEnderArrow());
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, new RenderSnowball(EnderUtilitiesItems.enderPearlReusable));

		MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBow, new RenderEnderBow());
		MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBucket, new ItemRendererEnderBucket());
	}

	@Override
	public boolean isShiftKeyDown()
	{
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}
}