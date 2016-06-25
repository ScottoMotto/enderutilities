package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import fi.dy.masa.enderutilities.item.ItemRuler;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;

@SideOnly(Side.CLIENT)
public class RulerRenderer
{
    public static final int[] COLORS = new int[] { 0x70FFFF, 0xFF70FF, 0xFFFF70, 0xA401CD, 0x3C3CC9, 0xD9850C, 0x13A43C, 0xED2235};
    protected final Minecraft mc;
    protected final Map<Integer, List<BlockPosEU>> positions;
    public float partialTicksLast;
    public String modeStrDimensions;
    public String modeStrDifference;

    public RulerRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new HashMap<Integer, List<BlockPosEU>>();
        this.modeStrDimensions = I18n.format("enderutilities.tooltip.item.ruler.dimensions");
        this.modeStrDifference = I18n.format("enderutilities.tooltip.item.ruler.difference");
    }

    public void renderHud()
    {
        EntityPlayer player = this.mc.thePlayer;
        if (player == null)
        {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.ruler)
        {
            stack = InventoryUtils.getFirstMatchingItem(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), EnderUtilitiesItems.ruler);
            if (stack == null || ((ItemRuler)stack.getItem()).getRenderWhenUnselected(stack) == false)
            {
                return;
            }
        }

        ItemRuler item = (ItemRuler)stack.getItem();
        int selected = item.getLocationSelection(stack);

        BlockPosEU posStart = item.getPosition(stack, selected, ItemRuler.POS_START);
        BlockPosEU posEnd = item.getPosition(stack, selected, ItemRuler.POS_END);

        if (posStart == null && posEnd == null)
        {
            return;
        }

        if (posStart == null)
        {
            posStart = posEnd;
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY - 1.6d), (int)player.posZ, player.dimension, EnumFacing.UP.getIndex());
        }
        else if (posEnd == null)
        {
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY - 1.6d), (int)player.posZ, player.dimension, EnumFacing.UP.getIndex());
        }

        if ((posStart != null && posStart.dimension != player.dimension) || (posEnd != null && posEnd.dimension != player.dimension))
        {
            return;
        }

        int lenX = Math.abs(posStart.posX - posEnd.posX);
        int lenY = Math.abs(posStart.posY - posEnd.posY);
        int lenZ = Math.abs(posStart.posZ - posEnd.posZ);
        String modeStr = this.modeStrDifference;

        if (item.getDistanceMode(stack) == ItemRuler.DISTANCE_MODE_DIMENSIONS)
        {
            lenX += 1;
            lenY += 1;
            lenZ += 1;
            modeStr = this.modeStrDimensions;
        }

        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        int scaledY = scaledResolution.getScaledHeight();
        int x = 0;
        int y = scaledY - 16;

        //System.out.println("scX: " + scaledX + " scY: " + scaledY);
        this.mc.fontRendererObj.drawString(modeStr + " X: " + lenX + ", Y: " + lenY + ", Z: " + lenZ, x + 10, y, 0xFF70FFFF, true);
    }

    public void renderAllPositionPairs(EntityPlayer usingPlayer, EntityPlayer clientPlayer, float partialTicks)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(usingPlayer, EnderUtilitiesItems.ruler);

        if (stack == null)
        {
            stack = InventoryUtils.getFirstMatchingItem(usingPlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), EnderUtilitiesItems.ruler);

            if (stack == null || ((ItemRuler)stack.getItem()).getRenderWhenUnselected(stack) == false)
            {
                return;
            }
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disablePolygonOffset(); //GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();

        ItemRuler item = (ItemRuler)stack.getItem();
        int selected = item.getLocationSelection(stack);

        if (item.getRenderAllLocations(stack) == true)
        {
            int count = item.getLocationCount(stack);

            for (int i = 0; i < count; i++)
            {
                int color = i < COLORS.length ? COLORS[i] : 0x70FFFF;

                // We render the selected location pair last
                if (i != selected && item.getAlwaysRenderLocation(stack, i) == true)
                {
                    BlockPosEU posStart = item.getPosition(stack, i, ItemRuler.POS_START);
                    BlockPosEU posEnd = item.getPosition(stack, i, ItemRuler.POS_END);
                    this.renderPointPair(usingPlayer, posStart, posEnd, color, clientPlayer, partialTicks);
                }
            }
        }

        // Render the currently selected point pair in white
        BlockPosEU posStart = item.getPosition(stack, selected, ItemRuler.POS_START);
        BlockPosEU posEnd = item.getPosition(stack, selected, ItemRuler.POS_END);
        this.renderPointPair(usingPlayer, posStart, posEnd, 0xFFFFFF, clientPlayer, partialTicks);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);

        this.partialTicksLast = partialTicks;
    }

    private void renderPointPair(EntityPlayer usingPlayer, BlockPosEU posStart, BlockPosEU posEnd,
            int color, EntityPlayer clientPlayer, float partialTicks)
    {
        if ((posStart != null && posStart.dimension != usingPlayer.dimension) || (posEnd != null && posEnd.dimension != usingPlayer.dimension))
        {
            return;
        }

        // Only update the positions once per game tick
        //if (partialTicks < this.partialTicksLast)
        {
            this.updatePositions(usingPlayer, posStart, posEnd);
        }

        this.renderPositions(clientPlayer, posStart, posEnd, color, partialTicks);
        this.renderStartAndEndPositions(clientPlayer, posStart, posEnd, partialTicks);
    }

    private void renderPositions(EntityPlayer clientPlayer, BlockPosEU posStart, BlockPosEU posEnd, int color, float partialTicks)
    {
        GL11.glLineWidth(2.0f);
        for (int a = 0; a < 3; a++)
        {
            List<BlockPosEU> column = this.positions.get(a);
            if (column == null)
            {
                continue;
            }

            for (int i = 0; i < column.size(); i++)
            {
                BlockPosEU pos = column.get(i);
                //if (pos.equals(posStart) == false && (posEnd == null || posEnd.equals(pos) == false))
                {
                    AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(pos.posX, pos.posY, pos.posZ, 0, partialTicks, clientPlayer);
                    RenderGlobal.drawSelectionBoundingBox(aabb, ((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1.0f);
                }
            }
        }
    }

    private void renderStartAndEndPositions(EntityPlayer clientPlayer, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        if (posStart != null)
        {
            // Render the start position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(posStart.posX, posStart.posY, posStart.posZ, 0, partialTicks, clientPlayer);
            RenderGlobal.drawSelectionBoundingBox(aabb, 1.0f, 0x11 / 255f, 0x11 / 255f, 1.0f);
        }

        if (posEnd != null)
        {
            // Render the end position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(posEnd.posX, posEnd.posY, posEnd.posZ, 0, partialTicks, clientPlayer);
            RenderGlobal.drawSelectionBoundingBox(aabb, 0x11 / 255f, 0x11 / 255f, 1.0f, 1.0f);
        }
    }

    private void updatePositions(EntityPlayer usingPlayer, BlockPosEU posStart, BlockPosEU posEnd)
    {
        if (posStart == null && posEnd == null)
        {
            for (int i = 0; i < 3; i++)
            {
                this.positions.remove(i);
            }
            return;
        }

        if (posStart == null)
        {
            posStart = posEnd;
            posEnd = new BlockPosEU((int)usingPlayer.posX, (int)(usingPlayer.posY), (int)usingPlayer.posZ, usingPlayer.dimension, EnumFacing.UP.getIndex());
        }
        else if (posEnd == null)
        {
            posEnd = new BlockPosEU((int)usingPlayer.posX, (int)(usingPlayer.posY), (int)usingPlayer.posZ, usingPlayer.dimension, EnumFacing.UP.getIndex());
        }

        BlockPosEU[] pos = new BlockPosEU[] { posStart, posEnd };
        int[] done = new int[] { 0, 0, 0 };

        for (int i = 0; i < 3; i++)
        {
            BlockPosAligner aligner = new BlockPosAligner(pos[0], pos[1], usingPlayer);
            BlockPosEU aligned = aligner.getAlignedPointAlongLongestAxis();
            int furthest = aligner.furthestPoint;

            if (aligner.axisLength > 0)
            {
                // We don't want to include duplicate positions
                boolean includeStart = aligned.equals(pos[0]) == false && aligned.equals(pos[1]) == false;
                this.positions.put(aligner.longestAxis, this.getColumn(aligned, pos[furthest], aligner.longestAxis, includeStart, false));
                done[aligner.longestAxis] = 1;
            }

            pos[furthest] = aligned;
        }

        for (int i = 0; i < 3; i++)
        {
            if (done[i] == 0)
            {
                this.positions.remove(i);
            }
        }
    }

    private List<BlockPosEU> getColumn(BlockPosEU posNear, BlockPosEU posFar, int axis, boolean includeStart, boolean includeEnd)
    {
        List<BlockPosEU> list = new ArrayList<BlockPosEU>();

        int[] p1 = new int[] { posNear.posX, posNear.posY, posNear.posZ };
        int[] p2 = new int[] { posFar.posX, posFar.posY, posFar.posZ };
        int inc = p1[axis] < p2[axis] ? 1 : -1;

        if (includeStart == false)
        {
            p1[axis] += inc;
        }

        if (includeEnd == false)
        {
            p2[axis] -= inc;
        }

        int maxLength = 160;

        if (p1[axis] <= p2[axis])
        {
            for (int i = 0; i < maxLength && p1[axis] <= p2[axis]; i++)
            {
                list.add(new BlockPosEU(p1[0], p1[1], p1[2], posNear.dimension, posNear.face));
                p1[axis] += 1;
            }
        }
        else if (p1[axis] > p2[axis])
        {
            for (int i = 0; i < maxLength && p1[axis] >= p2[axis]; i++)
            {
                list.add(new BlockPosEU(p1[0], p1[1], p1[2], posNear.dimension, posNear.face));
                p1[axis] -= 1;
            }
        }

        return list;
    }

    private class BlockPosAligner
    {
        public final double[] playerPos;
        public int longestAxis;
        public int axisLength;
        public int furthestPoint;
        public int[][] points;

        public BlockPosAligner(BlockPosEU p1, BlockPosEU p2, EntityPlayer player)
        {
            this.playerPos = new double[] { player.posX, player.posY - 1.0d, player.posZ };
            this.points = new int[][] {
                { p1.posX, p1.posY, p1.posZ },
                { p2.posX, p2.posY, p2.posZ }
            };
        }

        /*public int getLongestAxisLength()
        {
            this.getLongestAxis();
            return this.axisLength;
        }*/

        public int getLongestAxis()
        {
            int longest = 0;
            int length = Math.abs(this.points[0][0] - this.points[1][0]);

            for (int i = 1; i < 3; i++)
            {
                int tmp = Math.abs(this.points[0][i] - this.points[1][i]);
                if (tmp > length)
                {
                    longest = i;
                    length = tmp;
                }
            }

            this.longestAxis = longest;
            this.axisLength = length;

            return longest;
        }

        public int getFurthestPointIndexOnLongestAxis()
        {
            int axisId = this.getLongestAxis();
            double len0 = Math.abs(this.playerPos[axisId] - (this.points[0][axisId] + 0.5d));
            double len1 = Math.abs(this.playerPos[axisId] - (this.points[1][axisId] + 0.5d));
            this.furthestPoint = len0 > len1 ? 0 : 1;
            return this.furthestPoint;
        }

        public BlockPosEU getAlignedPointAlongLongestAxis()
        {
            int far = this.getFurthestPointIndexOnLongestAxis();
            int near = far ^ 0x1;
            int[] p = new int[] { this.points[far][0], this.points[far][1], this.points[far][2] };
            p[this.longestAxis] = this.points[near][this.longestAxis];

            return new BlockPosEU(p[0], p[1], p[2]); 
        }
    }
}
