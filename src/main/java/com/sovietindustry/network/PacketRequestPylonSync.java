package com.sovietindustry.network;

import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketRequestPylonSync implements IMessage {
    private int x, y, z;

    public PacketRequestPylonSync() {
    }

    public PacketRequestPylonSync(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketRequestPylonSync, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestPylonSync message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;
            World world = player.worldObj;
            if (world != null && world.blockExists(message.x, message.y, message.z)) {
                TileEntity te = world.getTileEntity(message.x, message.y, message.z);
                if (te instanceof TileEntityLepPylon) {
                    world.markBlockForUpdate(message.x, message.y, message.z);
                }
            }
            return null;
        }
    }
}