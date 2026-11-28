package com.sovietindustry.tileentities;

import com.sovietindustry.blocks.BlockCraneRail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * Контролер портального крана (етап 1: колія, сканування, рух без вантажу).
 *
 * Геометрія. Контролер стоїть на початку колії десь між двома лініями рейок, на рівні рейок, і не обов'язково
 * посередині. Рейки йдуть від контролера в одному з чотирьох напрямів. Контролер шукає найближчу рейку
 * праворуч (railPlus блоків від нього) і ліворуч (railMinus), а кран сам стає по центру між ними. Вільних блоків
 * між рейками тоді railPlus + railMinus - 1 (від 7 до 39), самі рейки в цей підрахунок не входять.
 * За опорами міст має консолі (cantilever), тож гак може працювати і за межами колії.
 *
 * Координати стану: s це положення опор уздовж колії в клітинках від контролера (0 = клітинка контролера),
 * t це положення візка поперек колії від осі крана (центр між рейками), h це довжина троса від низу моста до верху гака.
 *
 * Синхронізація: сервер веде рух, клієнт повторює той самий рух до тих самих цілей (stepMotion), а
 * сервер надсилає повний стан, коли щось змінюється (старт, зупинка, нова ціль, сканування).
 */
public class TileEntityCraneController extends TileEntity {

    /** Відстань між осями рейок (railPlus + railMinus): мінімум і максимум, у блоках. */
    public static final int MIN_SPAN = 8;
    public static final int MAX_SPAN = 40;
    public static final int MIN_LEN = 8;
    public static final int MAX_LEN = 64;

    public static final int MIN_HEIGHT = 10;
    public static final int MAX_HEIGHT = 32;
    public static final int DEFAULT_HEIGHT = 21;

    /** Відступ від основи жорсткої опори до її драбини й візків, у блоках. */
    private static final double FOOT_MARGIN = 0.4D;
    public static final double HOOK_MIN = 1.0D;

    public static final double SPEED_S = 0.10D;
    public static final double SPEED_T = 0.10D;
    public static final double SPEED_H = 0.12D;

    /** Колір за замовчуванням: жовтий. Решту кольорів дають фарби (див. paint). */
    public static final int DEFAULT_COLOR = 0xFAC814;

    /** Кольори за пошкодженням (meta) предмета-фарби: 0 чорний ... 15 білий. Жовта (11) повертає колір за замовчуванням. */
    private static final int[] DYE_COLORS = {
        0x2B2B2B, 0xB3312C, 0x3B7A1A, 0x6B4226, 0x2C3FB5, 0x7B2FBE, 0x2E8FA8, 0xABABAB,
        0x5A5A5A, 0xD88198, 0x41CD34, DEFAULT_COLOR, 0x6689D3, 0xC354CD, 0xEB8844, 0xF0F0F0 };

    private static final int[] DX = { 1, 0, -1, 0 };
    private static final int[] DZ = { 0, 1, 0, -1 };

    // ------------------------------------------------------------ стан (зберігається й синхронізується)

    public boolean assembled = false;
    /** 0 = +X, 1 = +Z, 2 = -X, 3 = -Z. */
    public int dir = 0;
    /** Довжина колії в клітинках (від контролера). */
    public int length = 0;
    /** Відстань від контролера до найближчої рейки праворуч і ліворуч (поперек колії). */
    public int railPlus = MIN_SPAN / 2;
    public int railMinus = MIN_SPAN / 2;
    /** Висота верху моста над низом рейкового блока. */
    public int height = DEFAULT_HEIGHT;

    /** Колір фарби крана, RGB. */
    public int color = DEFAULT_COLOR;

    public double s = 2.0D, t = 0.0D, h = 1.0D;
    public double targetS = 2.0D, targetT = 0.0D, targetH = 1.0D;
    public boolean moving = false;

    /** Попередні значення для плавного рендера між тактами (лише клієнт). */
    public double prevS = 2.0D, prevT = 0.0D, prevH = 1.0D;

    private int tickCounter = 0;
    private boolean debugForward = true;

    // ------------------------------------------------------------ форма моста (спільна для логіки й моделі)

    /**
     * Скільки блоків міст виступає за опору з кожного боку (span це відстань між осями рейок). Консоль має
     * півтори секції повної висоти, далі нижній пояс піднімається під 45° до висоти 0.6 і закінчується майданчиком.
     */
    public static int cantilever(int span) {
        return (int) Math.ceil(2.5D * bridgeDepth(span) - 0.6D);
    }

    /** Висота моста разом з балкою (від низу балки до верху ферми). Ширший кран має вищу ферму: від 2.5 до 4 блоків. */
    public static double bridgeDepth(int span) {
        return Math.min(4.0D, Math.max(2.5D, 1.9D + span * 0.05D));
    }

    /** Відстань між осями рейок. */
    public int span() {
        return railPlus + railMinus;
    }

    /** Зсув осі крана (центру між рейками) від контролера поперек колії, у блоках; плюс це бік railPlus. */
    public double centerOffset() {
        return (railPlus - railMinus) * 0.5D;
    }

    /** Півдовжина основи жорсткої опори вздовж колії: чим вище кран, тим ширша піраміда. Спільна з моделлю. */
    public static double rigidFootX(int height) {
        return Math.min(5.5D, Math.max(3.1D, 0.2D * height + 0.8D));
    }

    /** Півширина верху жорсткої опори впоперек колії: опора вгорі ширша, а до коліс звужується. Спільна з моделлю. */
    public static double rigidTopZ(int height) {
        return Math.min(2.4D, Math.max(1.0D, 0.09D * height));
    }

    /** На скільки клітинок убік від рейки опора заходить (верх опори розходиться), для перевірки вільного місця. */
    public int legReach() {
        return (int) Math.ceil(rigidTopZ(height) - 0.2D);
    }

    /** Скільки кран займає вздовж колії від своєї осі в один бік (з драбиною). */
    public double halfFoot() {
        return rigidFootX(height) + FOOT_MARGIN;
    }

    // ------------------------------------------------------------ межі руху

    public double minS() {
        return Math.ceil(halfFoot() - 0.5D);
    }

    public double maxS() {
        return Math.max(minS(), Math.floor(length - 0.5D - halfFoot()));
    }

    public double trolleyRange() {
        // візок їздить по поясах повної висоти: від опори ще півтори секції
        return span() * 0.5D + 1.5D * bridgeDepth(span()) - 0.8D;
    }

    public double maxH() {
        // верх гака не нижче, ніж на висоту гака (1 блок) над рейкою
        return Math.max(HOOK_MIN, height - bridgeDepth(span()) - 1.3D);
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    // ------------------------------------------------------------ тик

    @Override
    public void updateEntity() {
        if (worldObj == null) return;

        if (worldObj.isRemote) {
            prevS = s;
            prevT = t;
            prevH = h;
            if (assembled && moving) stepMotion();
            return;
        }

        if (!assembled) return;

        if (moving) {
            if (!travelClear()) {
                targetS = s; // уперлися в перешкоду, їзду колією зупиняємо
            }
            if (!stepMotion()) {
                moving = false;
                sync();
            }
        } else if (++tickCounter % 40 == 0) {
            // тиха перевірка, що колія досі на місці
            doScan(false);
        }
    }

    /** Один крок руху до цілей. Повертає true, якщо щось зрушило. */
    private boolean stepMotion() {
        double ns = approach(s, targetS, SPEED_S);
        double nt = approach(t, targetT, SPEED_T);
        double nh = approach(h, targetH, SPEED_H);
        boolean any = ns != s || nt != t || nh != h;
        s = ns;
        t = nt;
        h = nh;
        return any;
    }

    private static double approach(double cur, double target, double step) {
        double d = target - cur;
        if (Math.abs(d) <= step) return target;
        return cur + (d > 0 ? step : -step);
    }

    /** Перевіряє клітинку, у яку заходить передній край крана на цьому кроці. */
    private boolean travelClear() {
        double ds = targetS - s;
        if (Math.abs(ds) < 1.0E-6D) return true;
        int sign = ds > 0 ? 1 : -1;
        double next = s + sign * Math.min(Math.abs(ds), SPEED_S);
        int cell = (int) Math.floor(next + sign * halfFoot() + 0.5D);
        if (cell < 0 || cell >= length) return false;
        return findObstacle(cell) == null;
    }

    // ------------------------------------------------------------ команди (сервер)

    /** Задає цілі, обмежуючи їх межами. Для пульта на наступних етапах. */
    public void setTargets(double ns, double nt, double nh) {
        if (!assembled) return;
        double r = trolleyRange();
        targetS = clamp(ns, minS(), maxS());
        targetT = clamp(nt, -r, r);
        targetH = clamp(nh, HOOK_MIN, maxH());
        moving = true;
        sync();
    }

    /** Тестовий рух: туди-назад із візком і гаком, щоб побачити модель у русі. */
    public void debugSweep(EntityPlayer player) {
        if (!assembled) {
            msg(player, EnumChatFormatting.RED, "Crane track is not assembled. Right click the controller to scan it first");
            return;
        }
        double r = trolleyRange() - 0.5D;
        if (debugForward) {
            setTargets(maxS(), r, maxH());
        } else {
            setTargets(minS(), -r, HOOK_MIN + 1.0D);
        }
        debugForward = !debugForward;
    }

    /** Перефарбовує кран за пошкодженням предмета-фарби (meta 0-15). */
    public void paint(int dyeDamage) {
        color = DYE_COLORS[dyeDamage & 15];
        sync();
    }

    /** Тимчасова зміна висоти (поки немає GUI): +1 за клік, після максимуму знову мінімум. */
    public void cycleHeight(EntityPlayer player) {
        height = height >= MAX_HEIGHT ? MIN_HEIGHT : height + 1;
        rescan(player);
    }

    // ------------------------------------------------------------ сканування колії

    /** Повне сканування за запитом гравця: перевіряє колію й вільне місце, вирівнює рейки, пише в чат. */
    public void rescan(EntityPlayer player) {
        String err = doScan(true);
        if (err != null) {
            msg(player, EnumChatFormatting.RED, err);
        } else {
            msg(player, EnumChatFormatting.GREEN, "Crane track found: " + (span() - 1) + " free blocks between the rails, "
                + length + " blocks long, bridge height " + height + ". Right click with a Crane Rail to change the height, "
                + "with a dye to repaint, sneak + right click with an empty hand to test movement");
        }
    }

    private static void msg(EntityPlayer p, EnumChatFormatting color, String text) {
        if (p != null) p.addChatMessage(new ChatComponentText(color + text));
    }

    /** Повертає null, якщо колія в порядку, інакше текст помилки (і кран вимикається). */
    private String doScan(boolean full) {
        int bestDir = -1;
        int bestLen = 0;
        int bestPlus = 0;
        int bestMinus = 0;
        for (int d = 0; d < 4; d++) {
            int[] pm = findRails(d);
            if (pm == null) continue;
            int len = trackLength(d, pm[0], pm[1]);
            if (len > bestLen) {
                bestLen = len;
                bestDir = d;
                bestPlus = pm[0];
                bestMinus = pm[1];
            }
        }
        if (bestLen < MIN_LEN) {
            disassemble();
            return "No crane track. Put this block anywhere between two lines of crane rails (it does not have to be in the "
                + "middle), at the same level as the rails, with " + (MIN_SPAN - 1) + " to " + (MAX_SPAN - 1)
                + " free blocks between the rails (the rails themselves are not counted), at least " + MIN_LEN + " blocks long";
        }

        height = (int) clamp(height, MIN_HEIGHT, MAX_HEIGHT);
        int need = Math.max(MIN_LEN, (int) Math.ceil(2.0D * halfFoot() + 1.0D));
        if (bestLen < need) {
            disassemble();
            return "The track is too short for this crane height: need at least " + need + " blocks of track, found " + bestLen;
        }

        int oldDir = dir;
        int oldLen = length;
        int oldPlus = railPlus;
        int oldMinus = railMinus;
        boolean wasAssembled = assembled;
        dir = bestDir;
        length = bestLen;
        railPlus = bestPlus;
        railMinus = bestMinus;

        if (full) {
            for (int k = 0; k < length; k++) {
                int[] bad = findObstacle(k);
                if (bad != null) {
                    disassemble();
                    return "Crane is blocked at " + bad[0] + ", " + bad[1] + ", " + bad[2]
                        + ": keep the space above the rails and the bridge row free up to height " + height;
                }
            }
            alignRails();
        }

        assembled = true;
        boolean changed = !wasAssembled || oldDir != dir || oldLen != length || oldPlus != railPlus || oldMinus != railMinus;
        double r = trolleyRange();
        s = clamp(s, minS(), maxS());
        t = clamp(t, -r, r);
        h = clamp(h, HOOK_MIN, maxH());
        targetS = clamp(targetS, minS(), maxS());
        targetT = clamp(targetT, -r, r);
        targetH = clamp(targetH, HOOK_MIN, maxH());
        if (changed || full) sync();
        return null;
    }

    private void disassemble() {
        if (!assembled && !moving) return;
        assembled = false;
        moving = false;
        sync();
    }

    /**
     * Найближчі рейки праворуч і ліворуч від контролера в клітинці його ряду для напряму d.
     * Повертає {railPlus, railMinus} або null, якщо рейок немає або відстань між ними поза межами.
     */
    private int[] findRails(int d) {
        int lx = -DZ[d];
        int lz = DX[d];
        int plus = -1;
        int minus = -1;
        for (int g = 1; g < MAX_SPAN; g++) {
            if (plus < 0 && isRail(xCoord + lx * g, yCoord, zCoord + lz * g)) plus = g;
            if (minus < 0 && isRail(xCoord - lx * g, yCoord, zCoord - lz * g)) minus = g;
            if (plus >= 0 && minus >= 0) break;
        }
        if (plus < 0 || minus < 0) return null;
        int sp = plus + minus;
        if (sp < MIN_SPAN || sp > MAX_SPAN) return null;
        return new int[] { plus, minus };
    }

    /** Скільки клітинок колії підряд має рейки з обох боків у напрямі d. */
    private int trackLength(int d, int plus, int minus) {
        int lx = -DZ[d];
        int lz = DX[d];
        int len = 0;
        for (int k = 0; k < MAX_LEN; k++) {
            int bx = xCoord + DX[d] * k;
            int bz = zCoord + DZ[d] * k;
            if (!isRail(bx + lx * plus, yCoord, bz + lz * plus)) break;
            if (!isRail(bx - lx * minus, yCoord, bz - lz * minus)) break;
            len++;
        }
        return len;
    }

    private boolean isRail(int x, int y, int z) {
        return worldObj.blockExists(x, y, z) && worldObj.getBlock(x, y, z) instanceof BlockCraneRail;
    }

    /** Усі рейки отримують вісь, що збігається з напрямом колії. */
    private void alignRails() {
        int axis = (dir & 1) == 0 ? 0 : 1;
        int lx = -DZ[dir];
        int lz = DX[dir];
        for (int k = 0; k < length; k++) {
            int bx = xCoord + DX[dir] * k;
            int bz = zCoord + DZ[dir] * k;
            for (int side = -1; side <= 1; side += 2) {
                int lat = side > 0 ? railPlus : -railMinus;
                int rx = bx + lx * lat;
                int rz = bz + lz * lat;
                if (worldObj.getBlockMetadata(rx, yCoord, rz) != axis) {
                    worldObj.setBlockMetadataWithNotify(rx, yCoord, rz, axis, 2);
                }
            }
        }
    }

    /**
     * Перевіряє «переріз» крана в клітинці k: стовпи над обома рейками до верху моста й увесь ряд моста
     * разом із консолями. Повертає координати першої перешкоди або null. Незавантажений чанк вважається
     * перешкодою, щоб кран не змушував сервер вантажити чанки.
     */
    private int[] findObstacle(int k) {
        int lx = -DZ[dir];
        int lz = DX[dir];
        int bx = xCoord + DX[dir] * k;
        int bz = zCoord + DZ[dir] * k;
        int topCell = yCoord + height - 1;
        int depthCells = (int) Math.ceil(bridgeDepth(span()));
        int bottomCell = Math.max(yCoord + 1, yCoord + height - depthCells);
        int cant = cantilever(span());

        for (int side = -1; side <= 1; side += 2) {
            int lat = side > 0 ? railPlus : -railMinus;
            for (int off = -legReach(); off <= legReach(); off++) {
                int rx = bx + lx * (lat + off);
                int rz = bz + lz * (lat + off);
                for (int y = yCoord + 1; y <= topCell; y++) {
                    if (blocksAt(rx, y, rz)) return new int[] { rx, y, rz };
                }
            }
        }
        for (int off = -(railMinus + cant); off <= railPlus + cant; off++) {
            int rx = bx + lx * off;
            int rz = bz + lz * off;
            for (int y = bottomCell; y <= topCell; y++) {
                if (blocksAt(rx, y, rz)) return new int[] { rx, y, rz };
            }
        }
        return null;
    }

    private boolean blocksAt(int x, int y, int z) {
        if (!worldObj.blockExists(x, y, z)) return true;
        Block b = worldObj.getBlock(x, y, z);
        return b.getMaterial().blocksMovement();
    }

    // ------------------------------------------------------------ синхронізація

    private void sync() {
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("assembled", assembled);
        tag.setInteger("dir", dir);
        tag.setInteger("length", length);
        tag.setInteger("railPlus", railPlus);
        tag.setInteger("railMinus", railMinus);
        tag.setInteger("height", height);
        tag.setInteger("color", color);
        tag.setDouble("s", s);
        tag.setDouble("t", t);
        tag.setDouble("h", h);
        tag.setDouble("targetS", targetS);
        tag.setDouble("targetT", targetT);
        tag.setDouble("targetH", targetH);
        tag.setBoolean("moving", moving);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        assembled = tag.getBoolean("assembled");
        dir = tag.getInteger("dir") & 3;
        length = tag.getInteger("length");
        railPlus = (int) clamp(tag.hasKey("railPlus") ? tag.getInteger("railPlus") : MIN_SPAN / 2, 1, MAX_SPAN - 1);
        railMinus = (int) clamp(tag.hasKey("railMinus") ? tag.getInteger("railMinus") : MIN_SPAN / 2, 1, MAX_SPAN - 1);
        height = (int) clamp(tag.hasKey("height") ? tag.getInteger("height") : DEFAULT_HEIGHT, MIN_HEIGHT, MAX_HEIGHT);
        color = tag.hasKey("color") ? tag.getInteger("color") : DEFAULT_COLOR;
        s = tag.getDouble("s");
        t = tag.getDouble("t");
        h = tag.getDouble("h");
        targetS = tag.getDouble("targetS");
        targetT = tag.getDouble("targetT");
        targetH = tag.getDouble("targetH");
        moving = tag.getBoolean("moving");
        prevS = s;
        prevT = t;
        prevH = h;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    // ------------------------------------------------------------ рендер

    /** Рамка для рендера: уся колія з запасом по ширині й висоті, щоб кран не зникав, коли контролер поза кадром. */
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (!assembled) {
            return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
        }
        int ex = xCoord + DX[dir] * (length - 1);
        int ez = zCoord + DZ[dir] * (length - 1);
        int pad = span() + cantilever(span()) + 4;
        return AxisAlignedBB.getBoundingBox(
            Math.min(xCoord, ex) - pad, yCoord, Math.min(zCoord, ez) - pad,
            Math.max(xCoord, ex) + pad + 1, yCoord + height + 4, Math.max(zCoord, ez) + pad + 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
