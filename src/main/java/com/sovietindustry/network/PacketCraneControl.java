package com.sovietindustry.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Команда пульта на сервер: контролер, у якого крутимо кран, напрямки по трьох осях і одноразова дія.
 * ax: кран уздовж колії (+1 від контролера, -1 до нього), at: візок (+1 праворуч, -1 ліворуч),
 * lift: гак (+1 вгору, -1 вниз), action: 1 = захопити/відпустити вантаж. Нуль по осі означає «стоп».
 */
public class PacketCraneControl implements IMessage {

    public int x, y, z;
    public int ax, at, lift, action;

    public PacketCraneControl() {
    }

    public PacketCraneControl(int x, int y, int z, int ax, int at, int lift, int action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ax = ax;
        this.at = at;
        this.lift = lift;
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        ax = clampDir(buf.readByte());
        at = clampDir(buf.readByte());
        lift = clampDir(buf.readByte());
        action = buf.readByte() == 1 ? 1 : 0;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(ax);
        buf.writeByte(at);
        buf.writeByte(lift);
        buf.writeByte(action);
    }

    private static int clampDir(int v) {
        return v > 0 ? 1 : (v < 0 ? -1 : 0);
    }

    public static class Handler implements IMessageHandler<PacketCraneControl, IMessage> {
        @Override
        public IMessage onMessage(PacketCraneControl message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            // Обробник працює в мережевому потоці, а зі світом можна працювати лише в серверному: кладемо в чергу.
            if (player != null) CraneCommandQueue.enqueue(player, message);
            return null;
        }
    }
}
