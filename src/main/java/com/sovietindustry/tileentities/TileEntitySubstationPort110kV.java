package com.sovietindustry.tileentities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;

/**
 * 3-фазний вихідний портал 110 кВ на підстанції (ConnectionType.TRIPLE).
 * Розміщується на краю плити підстанції з протилежного боку від порталу NTM.
 */
public class TileEntitySubstationPort110kV extends TileEntityLepPylon {

    /** 3 точки кріплення на вихідній траверсі 110 кВ (відносно самого блока порталу). */
    private static final double[][] PORT_110_MOUNTS = {
        { -1.55D, 4.45D, 0.15D },
        {  0.00D, 4.45D, 0.15D },
        {  1.55D, 4.45D, 0.15D }
    };

    @Override
    protected double[][] getLocalMounts() {
        return PORT_110_MOUNTS;
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return 0.95D;
    }

    @Override
    public List<int[]> getLinks() {
        List<int[]> out = new ArrayList<int[]>();
        for (Object o : connected) {
            int[] p = (int[]) o;
            if (worldObj != null) {
                TileEntity te = worldObj.getTileEntity(p[0], p[1], p[2]);
                if (te instanceof TileEntitySubstation110kV) continue;
            }
            out.add(p);
        }
        return out;
    }
}