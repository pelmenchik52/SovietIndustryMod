#!/usr/bin/env python3
"""
Generates 16x16 Minecraft-style pixel art textures for Soviet Industry mod.
Requires: pip install Pillow
"""

from PIL import Image, ImageDraw
import os

TEXTURE_DIR = os.path.join(os.path.dirname(__file__), 
    "src", "main", "resources", "assets", "sovietindustry", "textures", "blocks")
os.makedirs(TEXTURE_DIR, exist_ok=True)

def save(img, name):
    img.save(os.path.join(TEXTURE_DIR, name + ".png"))
    print(f"Generated: {name}.png")

def new_img(color=(128,128,128)):
    return Image.new("RGBA", (16, 16), color)

# --- PDN Concrete Slab -------------------------------------------------------
# Grey concrete with diamond relief pattern
def make_pdn_slab():
    img = new_img((140, 135, 128))
    d = ImageDraw.Draw(img)
    # Base concrete color
    base = (140, 135, 128)
    dark = (100, 95, 90)
    light = (165, 160, 155)
    # Draw diamond grid pattern
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0:
                d.point((x, y), fill=dark)
            elif (x - y) % 4 == 0:
                d.point((x, y), fill=dark)
    # Add some noise for concrete texture
    import random
    random.seed(42)
    for _ in range(20):
        x, y = random.randint(0,15), random.randint(0,15)
        c = random.choice([dark, light, base])
        d.point((x, y), fill=c)
    # Cracks
    d.line([(2,3),(5,6)], fill=(90,85,80), width=1)
    d.line([(10,8),(13,11)], fill=(90,85,80), width=1)
    save(img, "pdn_slab")

# --- PO-2 Concrete Fence -----------------------------------------------------
# Grey concrete with diamond cutout pattern
def make_po2_fence():
    img = new_img((150, 145, 138))
    d = ImageDraw.Draw(img)
    dark = (100, 95, 90)
    light = (170, 165, 160)
    # Draw the characteristic diamond holes pattern
    diamonds = [(4,4), (12,4), (4,12), (12,12), (8,8)]
    for cx, cy in diamonds:
        d.polygon([(cx,cy-2),(cx+2,cy),(cx,cy+2),(cx-2,cy)], outline=dark, fill=(110,105,100))
    # Border/frame
    d.rectangle([0,0,15,15], outline=dark)
    # Vertical grooves
    for x in [5, 10]:
        d.line([(x,0),(x,15)], fill=dark)
    save(img, "po2_fence")

# --- Soviet Tiles -------------------------------------------------------------
def make_tile(color_name, grout=(200,200,200), tile_color=(220,220,220)):
    img = new_img(grout)
    d = ImageDraw.Draw(img)
    # Draw 4x4 tile grid
    for ty in range(4):
        for tx in range(4):
            x1 = tx * 4 + 1
            y1 = ty * 4 + 1
            x2 = x1 + 2
            y2 = y1 + 2
            d.rectangle([x1, y1, x2, y2], fill=tile_color)
            # Slight highlight
            d.point((x1, y1), fill=tuple(min(255, c+20) for c in tile_color))
    save(img, f"tile_{color_name}")

# --- Painted Panel -----------------------------------------------------------
def make_painted_panel():
    # Lower half: dark green oil paint; upper: white/cream plaster
    img = new_img((230, 228, 220))  # top plaster
    d = ImageDraw.Draw(img)
    # Lower 8 rows = dark green paint
    for y in range(8, 16):
        for x in range(16):
            d.point((x, y), fill=(55, 85, 55))
    # Paint wear/chips on green
    import random
    random.seed(99)
    for _ in range(8):
        x, y = random.randint(0,15), random.randint(8,15)
        d.point((x,y), fill=(80,65,45))
    # Plaster cracks on top
    d.line([(3,2),(7,5)], fill=(180,178,170))
    d.line([(10,1),(12,4)], fill=(180,178,170))
    # Dividing border line
    d.line([(0,8),(15,8)], fill=(40,65,40))
    # Save a second texture for the top face
    top = new_img((230, 228, 220))
    d2 = ImageDraw.Draw(top)
    d2.rectangle([0,0,15,15], fill=(55,85,55))
    top.save(os.path.join(TEXTURE_DIR, "panel_upper.png"))
    print("Generated: panel_upper.png")
    save(img, "panel_lower")

# --- Silicate Brick ----------------------------------------------------------
def make_silicate_brick():
    img = new_img((210, 208, 200))  # off-white
    d = ImageDraw.Draw(img)
    mortar = (170, 168, 162)
    # Brick rows (alternating offset)
    d.rectangle([0,0,15,3], fill=(215,212,205))
    d.line([(0,4),(15,4)], fill=mortar)
    d.line([(7,0),(7,3)], fill=mortar)
    d.rectangle([0,5,15,8], fill=(208,205,198))
    d.line([(0,9),(15,9)], fill=mortar)
    d.line([(3,5),(3,8)], fill=mortar)
    d.line([(11,5),(11,8)], fill=mortar)
    d.rectangle([0,10,15,13], fill=(215,212,205))
    d.line([(0,14),(15,14)], fill=mortar)
    d.line([(7,10),(7,13)], fill=mortar)
    d.rectangle([0,15,15,15], fill=(208,205,198))
    # Weathering stains
    import random
    random.seed(7)
    for _ in range(6):
        x, y = random.randint(0,15), random.randint(0,15)
        d.point((x,y), fill=(190,175,160))
    save(img, "silicate_brick")

if __name__ == "__main__":
    make_pdn_slab()
    make_po2_fence()
    make_tile("white",  grout=(190,190,190), tile_color=(235,235,230))
    make_tile("green",  grout=(90,110,90),   tile_color=(130,165,120))
    make_tile("blue",   grout=(100,120,140), tile_color=(140,180,210))
    make_tile("grey",   grout=(110,110,110), tile_color=(175,175,172))
    make_painted_panel()
    make_silicate_brick()
    print("\nAll textures generated successfully!")
