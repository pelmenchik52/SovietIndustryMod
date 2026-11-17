using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

class TextureGen {
    static string DIR = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, 
        "src", "main", "resources", "assets", "sovietindustry", "textures", "blocks");

    static void Main() {
        Directory.CreateDirectory(DIR);
        MakePdnSlab();
        MakePo2Fence();
        MakeTile("tile_white",  Color.FromArgb(190,190,190), Color.FromArgb(235,235,230));
        MakeTile("tile_green",  Color.FromArgb(90,110,90),   Color.FromArgb(130,165,120));
        MakeTile("tile_blue",   Color.FromArgb(100,120,140), Color.FromArgb(140,180,210));
        MakeTile("tile_grey",   Color.FromArgb(110,110,110), Color.FromArgb(175,175,172));
        MakePaintedPanel();
        MakeSilicateBrick();
        Console.WriteLine("\nAll textures generated!");
    }

    static void Save(Bitmap b, string name) {
        b.Save(Path.Combine(DIR, name + ".png"), ImageFormat.Png);
        Console.WriteLine("Generated: " + name + ".png");
        b.Dispose();
    }

    static Bitmap Solid(Color c) {
        var b = new Bitmap(16,16);
        using var g = Graphics.FromImage(b);
        g.Clear(c);
        return b;
    }

    static void MakePdnSlab() {
        var b = Solid(Color.FromArgb(140,135,128));
        var dark = Color.FromArgb(100,95,90);
        var crack = Color.FromArgb(90,85,80);
        for (int y=0;y<16;y++) for (int x=0;x<16;x++)
            if ((x+y)%4==0 || (x-y)%4==0) b.SetPixel(x,y,dark);
        for (int i=0;i<=3;i++) { b.SetPixel(2+i,3+i,crack); b.SetPixel(10+i,8+i,crack); }
        Save(b, "pdn_slab");
    }

    static void MakePo2Fence() {
        var b = Solid(Color.FromArgb(150,145,138));
        var dark = Color.FromArgb(100,95,90);
        var fill = Color.FromArgb(110,105,100);
        int[,] diamonds = {{4,4},{12,4},{4,12},{12,12},{8,8}};
        for (int d=0;d<5;d++) {
            int cx=diamonds[d,0], cy=diamonds[d,1];
            b.SetPixel(cx,cy-2,dark); b.SetPixel(cx+2,cy,dark);
            b.SetPixel(cx,cy+2,dark); b.SetPixel(cx-2,cy,dark);
            b.SetPixel(cx,cy-1,fill); b.SetPixel(cx+1,cy,fill);
            b.SetPixel(cx,cy+1,fill); b.SetPixel(cx-1,cy,fill);
            b.SetPixel(cx,cy,fill);
        }
        for (int i=0;i<16;i++) { b.SetPixel(i,0,dark); b.SetPixel(i,15,dark); b.SetPixel(0,i,dark); b.SetPixel(15,i,dark); }
        for (int y=0;y<16;y++) { b.SetPixel(5,y,dark); b.SetPixel(10,y,dark); }
        Save(b, "po2_fence");
    }

    static void MakeTile(string name, Color grout, Color tile) {
        var b = Solid(grout);
        var hi = Color.FromArgb(Math.Min(255,tile.R+20), Math.Min(255,tile.G+20), Math.Min(255,tile.B+20));
        for (int ty=0;ty<4;ty++) for (int tx=0;tx<4;tx++) {
            int x1=tx*4+1, y1=ty*4+1;
            for (int dy=0;dy<2;dy++) for (int dx=0;dx<2;dx++) b.SetPixel(x1+dx,y1+dy,tile);
            b.SetPixel(x1,y1,hi);
        }
        Save(b, name);
    }

    static void MakePaintedPanel() {
        var b = Solid(Color.FromArgb(230,228,220));
        var green = Color.FromArgb(55,85,55);
        var border = Color.FromArgb(40,65,40);
        for (int y=8;y<16;y++) for (int x=0;x<16;x++) b.SetPixel(x,y,green);
        for (int x=0;x<16;x++) b.SetPixel(x,8,border);
        Save(b, "panel_lower");
        var top = Solid(green);
        Save(top, "panel_upper");
    }

    static void MakeSilicateBrick() {
        var b = Solid(Color.FromArgb(210,208,200));
        var mortar = Color.FromArgb(170,168,162);
        var r1 = Color.FromArgb(215,212,205);
        var r2 = Color.FromArgb(208,205,198);
        // Row 1 (y 0-3)
        for (int x=0;x<16;x++) for (int y=0;y<=3;y++) b.SetPixel(x,y,r1);
        for (int x=0;x<16;x++) b.SetPixel(x,4,mortar);
        for (int y=0;y<=3;y++) b.SetPixel(7,y,mortar);
        // Row 2 (y 5-8)
        for (int x=0;x<16;x++) for (int y=5;y<=8;y++) b.SetPixel(x,y,r2);
        for (int x=0;x<16;x++) b.SetPixel(x,9,mortar);
        for (int y=5;y<=8;y++) { b.SetPixel(3,y,mortar); b.SetPixel(11,y,mortar); }
        // Row 3 (y 10-13)
        for (int x=0;x<16;x++) for (int y=10;y<=13;y++) b.SetPixel(x,y,r1);
        for (int x=0;x<16;x++) b.SetPixel(x,14,mortar);
        for (int y=10;y<=13;y++) b.SetPixel(7,y,mortar);
        // Row 4 (y 15)
        for (int x=0;x<16;x++) b.SetPixel(x,15,r2);
        Save(b, "silicate_brick");
    }
}
