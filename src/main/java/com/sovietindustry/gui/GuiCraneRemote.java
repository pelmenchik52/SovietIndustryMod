package com.sovietindustry.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.network.PacketCraneControl;
import com.sovietindustry.tileentities.TileEntityCraneController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

/**
 * Екран пульта. Кран рухається, поки кнопка (або клавіша) утримується, і зупиняється, коли її відпустили.
 * Клавіші: W/S кран уздовж колії, A/D візок, R/F (або стрілки вгору/вниз) гак, G захопити/відпустити, Esc закрити.
 * Світ не зупиняється й не затемнюється, щоб було видно, що робить кран.
 */
@SideOnly(Side.CLIENT)
public class GuiCraneRemote extends GuiScreen {

    private static final int B_CRANE_BACK = 0, B_CRANE_FWD = 1, B_TROLLEY_L = 2, B_TROLLEY_R = 3, B_HOOK_UP = 4,
        B_HOOK_DOWN = 5, B_GRAB = 6;

    private static final String[] DIR_NAME = { "+X", "+Z", "-X", "-Z" };
    /** Світова сторона «праворуч», якщо дивитися вздовж колії від контролера. */
    private static final String[] RIGHT_NAME = { "+Z", "-X", "-Z", "+X" };

    private final int cx, cy, cz;
    private int heldButton = -1;
    private int lastAx = 0, lastAt = 0, lastLift = 0;
    private int ticks = 0;

    public GuiCraneRemote(int x, int y, int z) {
        this.cx = x;
        this.cy = y;
        this.cz = z;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        int w = 22, hgt = 20, gap = 4;
        int panelW = 280;
        int left = (width - panelW) / 2;
        int top = height - 82;
        // три групи по дві кнопки
        int gx = left + 10;
        addPair(gx, top + 22, w, hgt, gap, B_CRANE_BACK, "<", B_CRANE_FWD, ">");
        gx += 2 * w + gap + 30;
        addPair(gx, top + 22, w, hgt, gap, B_TROLLEY_L, "<", B_TROLLEY_R, ">");
        gx += 2 * w + gap + 30;
        addPair(gx, top + 22, w, hgt, gap, B_HOOK_UP, "^", B_HOOK_DOWN, "v");
        buttonList.add(new GuiButton(B_GRAB, left + 10, top + 50, panelW - 20, 20, StatCollector.translateToLocal("gui.crane.grab")));
    }

    @SuppressWarnings("unchecked")
    private void addPair(int x, int y, int w, int h, int gap, int idA, String a, int idB, String b) {
        buttonList.add(new GuiButton(idA, x, y, w, h, a));
        buttonList.add(new GuiButton(idB, x + w + gap, y, w, h, b));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == B_GRAB) {
            send(lastAx, lastAt, lastLift, 1);
        } else {
            heldButton = button.id;
        }
    }

    @Override
    protected void mouseMovedOrUp(int mx, int my, int which) {
        super.mouseMovedOrUp(mx, my, which);
        if (which == 0) heldButton = -1;
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_G) {
            send(lastAx, lastAt, lastLift, 1);
            return;
        }
        super.keyTyped(c, key); // Esc закриває екран
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!Mouse.isButtonDown(0)) heldButton = -1;
        ticks++;

        int ax = 0, at = 0, lift = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_W) || heldButton == B_CRANE_FWD) ax++;
        if (Keyboard.isKeyDown(Keyboard.KEY_S) || heldButton == B_CRANE_BACK) ax--;
        if (Keyboard.isKeyDown(Keyboard.KEY_D) || heldButton == B_TROLLEY_R) at++;
        if (Keyboard.isKeyDown(Keyboard.KEY_A) || heldButton == B_TROLLEY_L) at--;
        if (Keyboard.isKeyDown(Keyboard.KEY_R) || Keyboard.isKeyDown(Keyboard.KEY_UP) || heldButton == B_HOOK_UP) lift++;
        if (Keyboard.isKeyDown(Keyboard.KEY_F) || Keyboard.isKeyDown(Keyboard.KEY_DOWN) || heldButton == B_HOOK_DOWN) lift--;

        boolean changed = ax != lastAx || at != lastAt || lift != lastLift;
        boolean heartbeat = (ax != 0 || at != 0 || lift != 0) && ticks % 10 == 0;
        if (changed || heartbeat) send(ax, at, lift, 0);
    }

    private void send(int ax, int at, int lift, int action) {
        lastAx = ax;
        lastAt = at;
        lastLift = lift;
        SovietIndustryMod.network.sendToServer(new PacketCraneControl(cx, cy, cz, ax, at, lift, action));
    }

    @Override
    public void onGuiClosed() {
        SovietIndustryMod.network.sendToServer(new PacketCraneControl(cx, cy, cz, 0, 0, 0, 0)); // стоп
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mx, int my, float partial) {
        int panelW = 280;
        int left = (width - panelW) / 2;
        int top = height - 82;
        drawRect(left, top - 24, left + panelW, top + 76, 0xA0000000);

        String status = StatCollector.translateToLocal("gui.crane.noCrane");
        String dirInfo = "";
        TileEntity te = mc.theWorld.getTileEntity(cx, cy, cz);
        if (te instanceof TileEntityCraneController) {
            TileEntityCraneController c = (TileEntityCraneController) te;
            if (!c.assembled) {
                status = StatCollector.translateToLocal("gui.crane.notAssembled");
            } else {
                String load = StatCollector.translateToLocal("gui.crane.empty");
                Block b = c.getCarriedBlock();
                if (b != null) load = b.getLocalizedName();
                status = StatCollector.translateToLocal("gui.crane.load") + ": " + load;
                dirInfo = StatCollector.translateToLocal("gui.crane.track") + " " + DIR_NAME[c.dir & 3] + ", "
                    + StatCollector.translateToLocal("gui.crane.right") + " " + RIGHT_NAME[c.dir & 3];
            }
        }
        drawCenteredString(fontRendererObj, status, width / 2, top - 20, 0xFFFFFF);
        drawCenteredString(fontRendererObj, dirInfo, width / 2, top - 9, 0xA0A0A0);

        int gx = left + 10;
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.crane.crane") + " (W/S)", gx + 24, top + 8, 0xFFFFFF);
        gx += 2 * 22 + 4 + 30;
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.crane.trolley") + " (A/D)", gx + 24, top + 8, 0xFFFFFF);
        gx += 2 * 22 + 4 + 30;
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.crane.hook") + " (R/F)", gx + 24, top + 8, 0xFFFFFF);

        super.drawScreen(mx, my, partial);
    }
}
