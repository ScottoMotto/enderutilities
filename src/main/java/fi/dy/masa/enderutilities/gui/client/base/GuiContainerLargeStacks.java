package fi.dy.masa.enderutilities.gui.client.base;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.util.EUStringUtils;

public class GuiContainerLargeStacks extends GuiEnderUtilities
{
    protected final List<IItemHandler> scaledStackSizeTextInventories;

    public GuiContainerLargeStacks(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container, xSize, ySize, textureName);

        this.scaledStackSizeTextInventories = new ArrayList<IItemHandler>();
    }

    @Override
    public void drawSlot(Slot slotIn)
    {
        int slotPosX = slotIn.xPos;
        int slotPosY = slotIn.yPos;
        ItemStack stack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == this.clickedSlot && this.draggedStack.isEmpty() == false && this.isRightMouseClick == false;
        ItemStack stackCursor = this.mc.player.inventory.getItemStack();
        String str = null;

        if (slotIn == this.clickedSlot && this.draggedStack.isEmpty() == false && this.isRightMouseClick && stack.isEmpty() == false)
        {
            stack = stack.copy();
            stack.setCount(stack.getCount() / 2);
        }
        else if (this.dragSplitting && this.dragSplittingSlots.contains(slotIn) && stackCursor.isEmpty() == false)
        {
            if (this.dragSplittingSlots.size() == 1)
            {
                return;
            }

            if (Container.canAddItemToSlot(slotIn, stackCursor, true) && this.inventorySlots.canDragIntoSlot(slotIn))
            {
                stack = stackCursor.copy();
                flag = true;
                Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, stack, slotIn.getStack().getCount());

                if (stack.getCount() > stack.getMaxStackSize())
                {
                    str = TextFormatting.YELLOW + "" + stack.getMaxStackSize();
                    stack.setCount(stack.getMaxStackSize());
                }

                if (stack.getCount() > slotIn.getItemStackLimit(stack))
                {
                    str = TextFormatting.YELLOW + "" + slotIn.getItemStackLimit(stack);
                    stack.setCount(slotIn.getItemStackLimit(stack));
                }
            }
            else
            {
                this.dragSplittingSlots.remove(slotIn);
                this.updateDragSplitting();
            }
        }

        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;

        if (stack.isEmpty())
        {
            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();

            if (textureatlassprite != null)
            {
                GlStateManager.disableLighting();
                this.mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
                this.drawTexturedModalRect(slotPosX, slotPosY, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        if (flag1 == false)
        {
            if (flag)
            {
                drawRect(slotPosX, slotPosY, slotPosX + 16, slotPosY + 16, -2130706433);
            }

            GlStateManager.enableDepth();
            this.itemRender.renderItemAndEffectIntoGUI(stack, slotPosX, slotPosY);

            // This slot belongs to a "large stacks" type inventory, render the stack size text scaled to 0.5x
            if (slotIn instanceof SlotItemHandler && this.scaledStackSizeTextInventories.contains(((SlotItemHandler) slotIn).getItemHandler()))
            {
                this.renderLargeStackItemOverlayIntoGUI(this.fontRenderer, stack, slotPosX, slotPosY);
            }
            else
            {
                this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, slotPosX, slotPosY, str);
            }
        }

        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    public void renderLargeStackItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack stack, int xPosition, int yPosition)
    {
        if (stack.isEmpty())
        {
            return;
        }

        if (stack.getCount() != 1)
        {
            String str = EUStringUtils.getStackSizeString(stack, 4);

            if (stack.getCount() < 1)
            {
                str = TextFormatting.RED + str;
            }

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();

            GlStateManager.pushMatrix();
            GlStateManager.translate(xPosition, yPosition, 0.0d);
            GlStateManager.scale(0.5d, 0.5d, 0.5d);

            fontRenderer.drawStringWithShadow(str, (31 - fontRenderer.getStringWidth(str)), 23, 0xFFFFFF);

            GlStateManager.popMatrix();

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }

        if (stack.getItem().showDurabilityBar(stack))
        {
            double health = stack.getItem().getDurabilityForDisplay(stack);
            int j = (int)Math.round(13.0D - health * 13.0D);
            int i = (int)Math.round(255.0D - health * 255.0D);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexBuffer = tessellator.getBuffer();

            this.drawQuad(vertexBuffer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
            this.drawQuad(vertexBuffer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
            this.drawQuad(vertexBuffer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);

            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    public void drawQuad(BufferBuilder vertexBuffer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        vertexBuffer.pos(x +     0, y +      0, 0.0d).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x +     0, y + height, 0.0d).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x + width, y + height, 0.0d).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x + width, y +      0, 0.0d).color(red, green, blue, alpha).endVertex();

        Tessellator.getInstance().draw();
    }
}
