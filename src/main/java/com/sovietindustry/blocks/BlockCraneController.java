package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.tileentities.TileEntityCraneController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * Контролер портального крана. Ставиться на початку колії, посередині між двома лініями рейок,
 * на тому ж рівні, що й рейки. Тут зберігається стан крана, а сам кран малює TESR.
 *
 * ПКМ порожньою рукою: пересканувати колію. ПКМ блоком Crane Rail: змінити висоту моста (по колу).
 * Shift + ПКМ порожньою рукою: тестовий рух крана (поки немає пульта).
 */
public class BlockCraneController extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockCraneController() {
        super(Material.iron);
        setHardness(5.0F);
        setResistance(30.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCraneController();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityCraneController)) return false;
        TileEntityCraneController crane = (TileEntityCraneController) te;
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == Item.getItemFromBlock(ModBlocks.craneRail)) {
            crane.cycleHeight(player);
        } else if (player.isSneaking()) {
            crane.debugSweep(player);
        } else {
            crane.rescan(player);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return side < 2 ? iconTop : iconSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        iconTop = reg.registerIcon(SovietIndustryMod.MODID + ":crane_controller_top");
        iconSide = reg.registerIcon(SovietIndustryMod.MODID + ":crane_controller_side");
        blockIcon = iconSide;
    }
}
