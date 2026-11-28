package com.sovietindustry.tileentities;

import com.sovietindustry.blocks.BlockCraneRail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

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
    /** Мінімальна довжина троса від низу моста до верху гака: під рейкою візка висить ще лебідка. */
    public static final double HOOK_MIN = 1.6D;

    /** Скільки від верху гака (низ троса) до його нижньої точки: саме тут вантаж чіпляється під гак. */
    public static final double HOOK_TIP_DROP = 1.05D;
    /** Скільки тактів без команд від пульта, після яких кран сам зупиняється (пульт шле команду кожні 10 тактів). */
    private static final int REMOTE_TIMEOUT = 30;
    /** Допуск при перевірці зіткнень, щоб вантаж, що стоїть рівно на землі, не вважався зануреним у неї. */
    private static final double EPS = 0.001D;

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

    /** Вантаж на гаку: один блок. Зберігається разом зі станом крана. */
    public boolean carrying = false;
    public String carryName = "";
    public int carryMeta = 0;

    private int tickCounter = 0;
    private boolean debugForward = true;

    /** Стан керування з пульта (лише сервер, не зберігається). */
    private boolean remoteActive = false;
    private int lastAx = 0, lastAt = 0, lastLift = 0;
    private long lastCmdTick = 0L;

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

    /** Наскільки візок може відійти від осі крана вздовж моста (положення t). */
    public double trolleyRange() {
        return trolleyRangeFor(span());
    }

    /**
     * Той самий хід візка за відстанню між рейками. Візок їздить по поясах повної висоти: від опори ще півтори
     * секції. Модель малює рейку візка саме на цю довжину плюс запас, тому тут змінювати разом із моделлю.
     */
    public static double trolleyRangeFor(int span) {
        return span * 0.5D + 1.5D * bridgeDepth(span) - 0.8D;
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

        if (remoteActive && worldObj.getTotalWorldTime() - lastCmdTick > REMOTE_TIMEOUT) {
            applyAxes(0, 0, 0); // пульт мовчить (гравець відійшов, закрив екран, відключився): зупиняємо все
        }

        if (moving) {
            if (!travelClear()) {
                targetS = s; // уперлися в перешкоду, їзду колією зупиняємо
            }
            if (!serverStep()) {
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

    /**
     * Один крок руху на сервері: так само, як stepMotion, але кожна вісь перевіряється на зіткнення гака (або вантажу
     * на ньому) з блоками. Якщо крок упирається в блок, пробуємо коротші кроки, щоб підійти майже впритул, і
     * зупиняємо цю вісь (ціль = поточне значення). Повертає true, якщо щось зрушило.
     */
    private boolean serverStep() {
        boolean any = false;
        boolean stopped = false;
        for (int axis = 0; axis < 3; axis++) {
            double cur = axis == 0 ? s : (axis == 1 ? t : h);
            double target = axis == 0 ? targetS : (axis == 1 ? targetT : targetH);
            double speed = axis == 0 ? SPEED_S : (axis == 1 ? SPEED_T : SPEED_H);
            double next = approach(cur, target, speed);
            if (next == cur) continue;
            if (hookBlockedAxis(axis, next) && !hookBlockedAxis(axis, cur)) {
                double ok = cur;
                double step = next - cur;
                for (int i = 0; i < 6; i++) {
                    step *= 0.5D;
                    if (!hookBlockedAxis(axis, cur + step)) {
                        ok = cur + step;
                        break;
                    }
                }
                next = ok;
                if (axis == 0) targetS = ok; else if (axis == 1) targetT = ok; else targetH = ok;
                stopped = true;
            }
            if (axis == 0) s = next; else if (axis == 1) t = next; else h = next;
            if (next != cur) any = true;
        }
        if (stopped) sync(); // клієнт повторює рух без перевірки блоків, тож виправляємо його точним станом
        return any;
    }

    private boolean hookBlockedAxis(int axis, double value) {
        return hookBlocked(axis == 0 ? value : s, axis == 1 ? value : t, axis == 2 ? value : h);
    }

    // ------------------------------------------------------------ положення гака у світі

    /** Світова X-координата осі гака (центр клітинки колії має цілу координату + 0.5). */
    private double hookWorldX(double ts, double tt) {
        return xCoord + 0.5D + DX[dir] * ts + (-DZ[dir]) * (centerOffset() + tt);
    }

    private double hookWorldZ(double ts, double tt) {
        return zCoord + 0.5D + DZ[dir] * ts + DX[dir] * (centerOffset() + tt);
    }

    /** Висота нижньої точки гака над рівнем рейок (локально, від yCoord) при довжині троса th. Спільна з рендером. */
    public double hookTipLocal(double th) {
        return height - bridgeDepth(span()) - th - HOOK_TIP_DROP;
    }

    /**
     * Чи входить гак (або блок на ньому: куб 1x1x1 під гаком) у тверді блоки при заданому положенні.
     * Без вантажу перевіряється лише клітинка під самою точкою гака. Незавантажений чанк вважається перешкодою.
     */
    private boolean hookBlocked(double ts, double tt, double th) {
        double wx = hookWorldX(ts, tt);
        double wz = hookWorldZ(ts, tt);
        double tip = yCoord + hookTipLocal(th);
        double half = carrying ? 0.5D : 0.1D;
        double bottom = carrying ? tip - 1.0D : tip;
        int x0 = (int) Math.floor(wx - half + EPS);
        int x1 = (int) Math.floor(wx + half - EPS);
        int z0 = (int) Math.floor(wz - half + EPS);
        int z1 = (int) Math.floor(wz + half - EPS);
        int y0 = (int) Math.floor(bottom + EPS);
        int y1 = carrying ? (int) Math.floor(bottom + 1.0D - EPS) : y0;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = y0; y <= y1; y++) {
                    if (blocksAt(x, y, z)) return true;
                }
            }
        }
        return false;
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

    // ------------------------------------------------------------ пульт керування

    /**
     * Команда з пульта (викликається на сервері). ax: кран уздовж колії (+1 від контролера, -1 до нього),
     * at: візок поперек колії (+1 у бік railPlus, тобто праворуч, якщо дивитися вздовж колії від контролера),
     * lift: гак (+1 вгору, -1 вниз). Значення 0 по осі зупиняє її. action: 1 = захопити/відпустити вантаж.
     */
    public void remoteCommand(EntityPlayer player, int ax, int at, int lift, int action) {
        if (worldObj == null || worldObj.isRemote) return;
        if (!assembled) {
            msg(player, EnumChatFormatting.RED, "Crane track is not assembled. Right click the controller to scan it first");
            return;
        }
        lastCmdTick = worldObj.getTotalWorldTime();
        if (action == 1) {
            if (carrying) release(player); else grab(player);
        }
        if (ax != lastAx || at != lastAt || lift != lastLift || action == 1) {
            lastAx = ax;
            lastAt = at;
            lastLift = lift;
            applyAxes(ax, at, lift);
        }
    }

    private void applyAxes(int ax, int at, int lift) {
        remoteActive = ax != 0 || at != 0 || lift != 0;
        lastAx = ax;
        lastAt = at;
        lastLift = lift;
        double r = trolleyRange();
        double ns = ax > 0 ? maxS() : (ax < 0 ? minS() : s);
        double nt = at > 0 ? r : (at < 0 ? -r : t);
        double nh = lift > 0 ? HOOK_MIN : (lift < 0 ? maxH() : h); // більша довжина троса = гак нижче
        targetS = ns;
        targetT = nt;
        targetH = nh;
        moving = ns != s || nt != t || nh != h;
        sync();
    }

    // ------------------------------------------------------------ вантаж

    public Block getCarriedBlock() {
        if (!carrying) return null;
        Block b = Block.getBlockFromName(carryName);
        return (b == null || b == Blocks.air) ? null : b;
    }

    /** Клітинка, у якій зараз висить вантаж, або клітинка під гаком: {x, z}. */
    private int[] hookCell() {
        return new int[] { (int) Math.floor(hookWorldX(s, t)), (int) Math.floor(hookWorldZ(s, t)) };
    }

    /** Чи можна цей блок піднімати краном. Повертає null, якщо можна, інакше причину. */
    private String liftProblem(World w, EntityPlayer player, int x, int y, int z) {
        Block b = w.getBlock(x, y, z);
        int meta = w.getBlockMetadata(x, y, z);
        if (b.isAir(w, x, y, z) || b.getMaterial().isLiquid()) return "There is nothing to lift under the hook";
        if (b instanceof BlockCraneRail) return "Crane rails cannot be lifted";
        if (b.getBlockHardness(w, x, y, z) < 0.0F) return "This block is unbreakable";
        if (b.hasTileEntity(meta) || w.getTileEntity(x, y, z) != null) return "Blocks with an inventory or other tile data cannot be lifted";
        if (Block.getIdFromBlock(b) == 0 || Block.blockRegistry.getNameForObject(b) == null) return "This block cannot be lifted";
        if (!w.canMineBlock(player, x, y, z)) return "You cannot lift blocks here (protected area)";
        return null;
    }

    /** Захоплення блока, що лежить просто під гаком. Блок знімається зі світу й чіпляється на гак. */
    private void grab(EntityPlayer player) {
        double tip = yCoord + hookTipLocal(h);
        int[] cell = hookCell();
        int top = (int) Math.floor(tip - EPS);
        int found = Integer.MIN_VALUE;
        for (int y = top; y >= top - 1; y--) {
            Block b = worldObj.getBlock(cell[0], y, cell[1]);
            if (!b.isAir(worldObj, cell[0], y, cell[1])) {
                found = y;
                break;
            }
        }
        if (found == Integer.MIN_VALUE) {
            msg(player, EnumChatFormatting.YELLOW, "Nothing to lift: lower the hook onto a block");
            return;
        }
        if (tip - (found + 1) > 0.6D) {
            msg(player, EnumChatFormatting.YELLOW, "The hook is too high above the block: lower it until it touches the block");
            return;
        }
        String problem = liftProblem(worldObj, player, cell[0], found, cell[1]);
        if (problem != null) {
            msg(player, EnumChatFormatting.RED, problem);
            return;
        }
        // Гак стає точно над центром клітинки, інакше вантаж зачіпав би сусідні блоки.
        double dxs = cell[0] - xCoord;
        double dzs = cell[1] - zCoord;
        double ns = dxs * DX[dir] + dzs * DZ[dir];
        double nt = dxs * (-DZ[dir]) + dzs * DX[dir] - centerOffset();
        if (ns < minS() - EPS || ns > maxS() + EPS || Math.abs(nt) > trolleyRange() + EPS) {
            msg(player, EnumChatFormatting.RED, "The block is at the very edge of the crane's reach");
            return;
        }
        Block b = worldObj.getBlock(cell[0], found, cell[1]);
        carryMeta = worldObj.getBlockMetadata(cell[0], found, cell[1]);
        carryName = Block.blockRegistry.getNameForObject(b);
        worldObj.setBlock(cell[0], found, cell[1], Blocks.air, 0, 3);
        carrying = true;
        s = targetS = ns;
        t = targetT = nt;
        targetH = h;
        moving = false;
        sync();
    }

    /**
     * Відпускає вантаж у клітинку, де він висить (найближча до нього). Якщо під нею повітря, блок опускається до
     * першої опори. Якщо клітинка зайнята, вантаж лишається на гаку.
     */
    private void release(EntityPlayer player) {
        Block b = getCarriedBlock();
        if (b == null) {
            carrying = false;
            sync();
            return;
        }
        double tip = yCoord + hookTipLocal(h);
        int[] cell = hookCell();
        int y = (int) Math.round(tip - 1.0D);
        if (!isFreeForBlock(cell[0], y, cell[1])) {
            msg(player, EnumChatFormatting.YELLOW, "There is no free space to put the load here");
            return;
        }
        if (!worldObj.canMineBlock(player, cell[0], y, cell[1])) {
            msg(player, EnumChatFormatting.RED, "You cannot place blocks here (protected area)");
            return;
        }
        while (y > 0 && isFreeForBlock(cell[0], y - 1, cell[1])) y--;
        worldObj.setBlock(cell[0], y, cell[1], b, carryMeta, 3);
        carrying = false;
        carryName = "";
        carryMeta = 0;
        sync();
    }

    private boolean isFreeForBlock(int x, int y, int z) {
        if (y < 0 || y >= 256 || !worldObj.blockExists(x, y, z)) return false;
        Block c = worldObj.getBlock(x, y, z);
        return c.isAir(worldObj, x, y, z) || c.getMaterial().isReplaceable();
    }

    /** Викидає вантаж предметом, коли контролер ламають. */
    public void dropCarried() {
        if (worldObj == null || worldObj.isRemote || !carrying) return;
        Block b = getCarriedBlock();
        if (b != null) b.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, carryMeta, 0);
        carrying = false;
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
        int cant = cantilever(span()) + 2;
        
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
        tag.setBoolean("carrying", carrying);
        tag.setString("carryName", carryName);
        tag.setInteger("carryMeta", carryMeta);
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
        carrying = tag.getBoolean("carrying");
        carryName = tag.getString("carryName");
        carryMeta = tag.getInteger("carryMeta");
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
