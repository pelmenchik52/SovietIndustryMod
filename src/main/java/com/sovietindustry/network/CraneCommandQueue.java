package com.sovietindustry.network;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.sovietindustry.items.ItemCraneRemote;
import com.sovietindustry.tileentities.TileEntityCraneController;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

/**
 * Черга команд пульта. Пакети приходять у мережевому потоці, тому тут їх лише збираємо, а виконуємо на початку
 * такту сервера, де безпечно чіпати світ і TileEntity. Підписку див. у SovietIndustryMod.
 */
public class CraneCommandQueue {

    /** Дальність пульта в блоках. */
    public static final double REMOTE_RANGE = 96.0D;

    private static final class Pending {
        final EntityPlayerMP player;
        final PacketCraneControl msg;

        Pending(EntityPlayerMP player, PacketCraneControl msg) {
            this.player = player;
            this.msg = msg;
        }
    }

    private static final ConcurrentLinkedQueue<Pending> QUEUE = new ConcurrentLinkedQueue<Pending>();

    public static void enqueue(EntityPlayerMP player, PacketCraneControl msg) {
        QUEUE.add(new Pending(player, msg));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Pending p;
        while ((p = QUEUE.poll()) != null) process(p.player, p.msg);
    }

    private static void process(EntityPlayerMP player, PacketCraneControl m) {
        if (player.isDead || player.worldObj == null) return;
        World world = player.worldObj;
        if (!world.blockExists(m.x, m.y, m.z)) return;
        TileEntity te = world.getTileEntity(m.x, m.y, m.z);
        if (!(te instanceof TileEntityCraneController)) return;
        TileEntityCraneController crane = (TileEntityCraneController) te;

        // Керувати можна лише тим краном, до якого прив'язаний пульт у інвентарі гравця.
        if (!ItemCraneRemote.hasBoundRemote(player, m.x, m.y, m.z, world.provider.dimensionId)) return;

        if (player.getDistanceSq(m.x + 0.5D, m.y + 0.5D, m.z + 0.5D) > REMOTE_RANGE * REMOTE_RANGE) {
            crane.remoteCommand(player, 0, 0, 0, 0);
            if (player.ticksExisted % 40 == 0) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Out of remote range"));
            }
            return;
        }
        crane.remoteCommand(player, m.ax, m.at, m.lift, m.action);
    }
}
