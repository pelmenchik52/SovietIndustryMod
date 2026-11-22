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
 * Головний блок трансформаторної підстанції 330 кВ (мультиблок 7x6x8, див. Substation330).
 *
 * Вхід: портал 330 кВ (ConnectionType.TRIPLE, три фази). Приймає дроти ТІЛЬКИ від анкерно-кутової опори
 * 330 кВ (TileEntityLep330Angle) і лише один прольот (див. ItemWireCoil). Кожна фаза подвійна гірлянда,
 * точно як на анкерній опорі 330 кВ, тому дроти двох кінців збігаються.
 *
 * Вихід: три кабельні порти на торці плити, протилежному порталу. Це повні блоки-dummy (1x1x1) на краю плити;
 * вони є додатковими позиціями вузла NTM, тому мідний кабель NTM просто ставиться впритул
 * до плити ззовні (так само, як у підстанції 110 кВ).
 */
public class TileEntitySubstation330kV extends TileEntityLepPylon implements IBundledPylon {

    public static final double MAX_SPAN_330 = 128.0D;

    // ------------------------------------------------------------ геометрія

    @Override
    protected double[][] getLocalMounts() {
        return Substation330.mounts();
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return Substation330.STRING_LEN;
    }

    @Override
    public double getMaxWireLength() {
        return MAX_SPAN_330;
    }

    // ------------------------------------------------------------ пучок (такий самий, як у анкерної опори 330 кВ)

    @Override
    public int getStringsPerMount() {
        return Lep330.A_STRINGS;
    }

    @Override
    public double getStringSpacing() {
        return Lep330.A_STRING_SPACING;
    }

    @Override
    public int getWiresPerString() {
        return Lep330.A_WIRES_PER_STRING;
    }

    @Override
    public double getWireSpacing() {
        return Lep330.A_WIRE_SPACING;
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
        return Substation330.portOffsets(angle);
    }

    /** Напрямок назовні від кабельного краю (туди ставиться кабель NTM). */
    public ForgeDirection getCableOut() {
        int[] o = Substation330.outOffset(angle);
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
        PowerNode node = new PowerNode(
            new BlockPos(xCoord, yCoord, zCoord),
            new BlockPos(xCoord + ports[0][0], yCoord, zCoord + ports[0][2]),
            new BlockPos(xCoord + ports[1][0], yCoord, zCoord + ports[1][2]),
            new BlockPos(xCoord + ports[2][0], yCoord, zCoord + ports[2][2]))
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
        return 10.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        double minX = xCoord - 8.0D, minY = yCoord - 1.0D, minZ = zCoord - 8.0D;
        double maxX = xCoord + 9.0D, maxY = yCoord + renderHeight(), maxZ = zCoord + 9.0D;
        for (int[] p : getLinks()) {
            minX = Math.min(minX, p[0] - 9.0D);
            minY = Math.min(minY, p[1] - 1.0D);
            minZ = Math.min(minZ, p[2] - 9.0D);
            maxX = Math.max(maxX, p[0] + 10.0D);
            maxY = Math.max(maxY, p[1] + 20.0D);
            maxZ = Math.max(maxZ, p[2] + 10.0D);
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
