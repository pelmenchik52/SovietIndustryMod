package com.sovietindustry.tileentities;

import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.sovietindustry.api.IBundledPylon;

import api.hbm.energymk2.Nodespace.PowerNode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Головний блок трансформаторної підстанції 750 кВ (мультиблок 13x13x23, див. Substation750).
 *
 * Майстер-блок стоїть посередині краю з боку входу (на ту сторону, куди дивився гравець при установці).
 *
 * Вхід: портал 750 кВ (ConnectionType.TRIPLE, три фази, кожна розщеплена: 2 гірлянди по 4 дроти). Приймає дроти
 * ТІЛЬКИ від анкерно-кутової опори 750 кВ (TileEntityLep750Angle) і лише один проліт (див. ItemWireCoil).
 *
 * Вихід: чотири кабельні порти на протилежному торці плити. Це повні блоки-dummy (1x1x1); вони є додатковими
 * позиціями вузла NTM, тож мідний кабель NTM просто ставиться впритул до плити ззовні.
 */
public class TileEntitySubstation750kV extends TileEntityLepPylon implements IBundledPylon {

    public static final double MAX_SPAN_750 = Lep750.MAX_SPAN_750;

    // ------------------------------------------------------------ геометрія

    @Override
    protected double[][] getLocalMounts() {
        return Substation750.mounts();
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return Substation750.STRING_LEN;
    }

    @Override
    public double getMaxWireLength() {
        return MAX_SPAN_750;
    }

    // ------------------------------------------------------------ пучок (такий самий, як у анкерної опори 750 кВ)

    @Override
    public int getStringsPerMount() {
        return Lep750.STRINGS;
    }

    @Override
    public double getStringSpacing() {
        return Lep750.STRING_SPACING;
    }

    @Override
    public int getWiresPerString() {
        return Lep750.WIRES_PER_STRING;
    }

    @Override
    public double getWireSpacing() {
        return Lep750.WIRE_SPACING;
    }

    /** Гірлянди анкерні: вісь пучка перпендикулярна до прольоту. */
    @Override
    public double[] getBundleAxis(double spanX, double spanZ) {
        double l = Math.sqrt(spanX * spanX + spanZ * spanZ);
        if (l < 1.0E-6D) return new double[] { 1.0D, 0.0D, 0.0D };
        return new double[] { -spanZ / l, 0.0D, spanX / l };
    }

    // ------------------------------------------------------------ кабельні порти

    /** Світові зміщення трьох кабельних портів {dx, 0, dz} від майстер-блока. */
    public int[][] getCablePortOffsets() {
        return Substation750.portOffsets(angle);
    }

    /** Напрямок назовні від кабельного краю (туди ставиться кабель NTM). */
    public ForgeDirection getCableOut() {
        int[] o = Substation750.outOffset(angle);
        if (o[0] > 0) return ForgeDirection.EAST;
        if (o[0] < 0) return ForgeDirection.WEST;
        if (o[1] > 0) return ForgeDirection.SOUTH;
        if (o[1] < 0) return ForgeDirection.NORTH;
        return ForgeDirection.UNKNOWN;
    }

    /** Чи є клітинка зі зміщенням (dx, dy, dz) від майстра кабельним портом. */
    public boolean isCablePort(int dx, int dy, int dz) {
        if (dy != 0) return false;
        for (int[] p : getCablePortOffsets()) {
            if (p[0] == dx && p[2] == dz) return true;
        }
        return false;
    }

    // ------------------------------------------------------------ вузол мережі NTM

    /**
     * Вузол займає центр + 3 кабельні порти, а до звичайних зв'язків пілона (дроти) додаються входи
     * для кабелів із зовнішнього боку кожного порту.
     */
    @Override
    public PowerNode createNode() {
        int[][] ports = getCablePortOffsets();
        BlockPos[] positions = new BlockPos[ports.length + 1];
        positions[0] = new BlockPos(xCoord, yCoord, zCoord);
        for (int i = 0; i < ports.length; i++) {
            positions[i + 1] = new BlockPos(xCoord + ports[i][0], yCoord, zCoord + ports[i][2]);
        }
        PowerNode node = new PowerNode(positions)
            .setConnections(new DirPos(xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN));

        for (Object o : connected) {
            int[] pos = (int[]) o;
            node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
        }

        ForgeDirection out = getCableOut();
        if (out != ForgeDirection.UNKNOWN) {
            for (int[] port : ports) {
                node.addConnection(new DirPos(
                    xCoord + port[0] + out.offsetX, yCoord, zCoord + port[2] + out.offsetZ, out));
            }
        }
        return node;
    }

    // ------------------------------------------------------------ рамка рендера

    @Override
    protected double renderHeight() {
        return 25.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        double minX = xCoord - 10.0D, minY = yCoord - 1.0D, minZ = zCoord - 10.0D;
        double maxX = xCoord + 11.0D, maxY = yCoord + renderHeight(), maxZ = zCoord + 11.0D;
        for (int[] p : getLinks()) {
            minX = Math.min(minX, p[0] - 40.0D);
            minY = Math.min(minY, p[1] - 1.0D);
            minZ = Math.min(minZ, p[2] - 40.0D);
            maxX = Math.max(maxX, p[0] + 41.0D);
            maxY = Math.max(maxY, p[1] + 36.0D);
            maxZ = Math.max(maxZ, p[2] + 41.0D);
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
