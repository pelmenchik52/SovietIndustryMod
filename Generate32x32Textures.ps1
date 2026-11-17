Add-Type -AssemblyName System.Drawing

$blockDir = Join-Path $PSScriptRoot "src\main\resources\assets\sovietindustry\textures\blocks"
$modelDir = Join-Path $PSScriptRoot "src\main\resources\assets\sovietindustry\textures\models"

New-Item -ItemType Directory -Path $blockDir -Force | Out-Null
New-Item -ItemType Directory -Path $modelDir -Force | Out-Null

function Save-Bmp($bmp, $path) {
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Host "Created: $path"
    $bmp.Dispose()
}

function New-Bmp($w, $h) {
    return New-Object System.Drawing.Bitmap $w, $h
}

# ==============================================================================
# 1. GLASS BLOCK (32x32) - 2x2 grid of glass tiles with detailed frames & bevels
# ==============================================================================
$b = New-Bmp 32 32
$frameDark = [System.Drawing.Color]::FromArgb(255, 65, 70, 72)
$frameLight = [System.Drawing.Color]::FromArgb(255, 120, 125, 128)
$frameMid = [System.Drawing.Color]::FromArgb(255, 90, 95, 98)

$glassBase = [System.Drawing.Color]::FromArgb(190, 145, 185, 180)
$glassHi = [System.Drawing.Color]::FromArgb(220, 195, 230, 225)
$glassShadow = [System.Drawing.Color]::FromArgb(210, 105, 145, 140)
$glassRib = [System.Drawing.Color]::FromArgb(180, 170, 210, 205)

# Fill with frame
for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $b.SetPixel($x, $y, $frameMid)
    }
}
# Frame outer highlight & shadow
for ($i = 0; $i -lt 32; $i++) {
    $b.SetPixel($i, 0, $frameLight); $b.SetPixel(0, $i, $frameLight)
    $b.SetPixel($i, 31, $frameDark); $b.SetPixel(31, $i, $frameDark)
    # Middle cross frame
    $b.SetPixel($i, 15, $frameDark); $b.SetPixel($i, 16, $frameLight)
    $b.SetPixel(15, $i, $frameDark); $b.SetPixel(16, $i, $frameLight)
}

# 4 glass tiles: Tile(1..14, 1..14), Tile(17..30, 1..14), Tile(1..14, 17..30), Tile(17..30, 17..30)
$tiles = @(@(1, 1), @(17, 1), @(1, 17), @(17, 17))
foreach ($tl in $tiles) {
    $tx0 = $tl[0]; $ty0 = $tl[1]
    for ($y = 0; $y -lt 14; $y++) {
        for ($x = 0; $x -lt 14; $x++) {
            $px = $tx0 + $x; $py = $ty0 + $y
            if ($x -eq 0 -or $y -eq 0) {
                $b.SetPixel($px, $py, $glassHi)
            } elseif ($x -eq 13 -or $y -eq 13) {
                $b.SetPixel($px, $py, $glassShadow)
            } else {
                # Ribbed industrial wave pattern
                if (($x + $y) % 3 -eq 0) {
                    $b.SetPixel($px, $py, $glassRib)
                } else {
                    $b.SetPixel($px, $py, $glassBase)
                }
            }
        }
    }
}
Save-Bmp $b (Join-Path $blockDir "glass_block.png")

# ==============================================================================
# 2. DAMAGED CONCRETE WITH REBAR (32x32) - Realistic cavity, 3D steel mesh
# ==============================================================================
$b = New-Bmp 32 32
$rnd = New-Object System.Random 1234
$cBase = 148; $cBaseG = 144; $cBaseB = 138

for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $noise = $rnd.Next(-10, 11)
        $r = [Math]::Max(0, [Math]::Min(255, $cBase + $noise))
        $g = [Math]::Max(0, [Math]::Min(255, $cBaseG + $noise))
        $bl = [Math]::Max(0, [Math]::Min(255, $cBaseB + $noise))
        $b.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $r, $g, $bl))
    }
}

# Cracks
$crackCol = [System.Drawing.Color]::FromArgb(255, 75, 72, 68)
$crackLite = [System.Drawing.Color]::FromArgb(255, 180, 175, 170)
$cracks = @(
    @(6,4),@(7,5),@(8,6),@(9,7),@(10,8),
    @(24,5),@(23,6),@(22,7),@(22,8),
    @(6,25),@(7,24),@(8,23),@(9,22),
    @(25,24),@(24,23),@(23,22),@(22,21)
)
foreach ($c in $cracks) {
    $b.SetPixel($c[0], $c[1], $crackCol)
    $b.SetPixel($c[0] + 1, $c[1], $crackLite)
}

# Jagged cavity (вибоїна) in center (x: 10..22, y: 10..22)
$holeDark = [System.Drawing.Color]::FromArgb(255, 45, 42, 38)
$holeShadow = [System.Drawing.Color]::FromArgb(255, 65, 60, 55)
for ($y = 10; $y -le 22; $y++) {
    $dx = [Math]::Round(6 * [Math]::Sin(($y - 10) * 0.28))
    for ($x = (16 - $dx); $x -le (16 + $dx); $x++) {
        $distFromEdge = [Math]::Min($x - (16 - $dx), (16 + $dx) - $x)
        if ($distFromEdge -le 1) {
            $b.SetPixel($x, $y, $holeShadow)
        } else {
            $b.SetPixel($x, $y, $holeDark)
        }
    }
}

# Rebar mesh (арматурна сітка: сталеві прути + іржа)
$rebarSteel = [System.Drawing.Color]::FromArgb(255, 130, 135, 140)
$rebarRust1 = [System.Drawing.Color]::FromArgb(255, 165, 75, 30)
$rebarRust2 = [System.Drawing.Color]::FromArgb(255, 120, 50, 20)
$rebarHi = [System.Drawing.Color]::FromArgb(255, 200, 110, 50)

# Horizontal rebar bars at y=13, y=18
foreach ($ry in @(13, 18)) {
    for ($x = 11; $x -le 21; $x++) {
        $c = if ($x % 3 -eq 0) { $rebarHi } elseif ($x % 2 -eq 0) { $rebarRust1 } else { $rebarRust2 }
        $b.SetPixel($x, $ry, $c)
        $b.SetPixel($x, $ry + 1, $rebarRust2)
    }
}
# Vertical rebar bars at x=13, x=18
foreach ($rx in @(13, 18)) {
    for ($y = 11; $y -le 21; $y++) {
        $c = if ($y % 3 -eq 0) { $rebarHi } elseif ($y % 2 -eq 0) { $rebarRust1 } else { $rebarRust2 }
        $b.SetPixel($rx, $y, $c)
        $b.SetPixel($rx + 1, $y, $rebarRust2)
    }
}
Save-Bmp $b (Join-Path $blockDir "damaged_concrete.png")

# ==============================================================================
# 3. PDN CONCRETE SLAB (32x32) - Diamond relief grid + chamfered edges
# ==============================================================================
$b = New-Bmp 32 32
$slabBase = [System.Drawing.Color]::FromArgb(255, 150, 147, 142)
$slabLight = [System.Drawing.Color]::FromArgb(255, 185, 182, 178)
$slabShadow = [System.Drawing.Color]::FromArgb(255, 105, 102, 98)
$groove = [System.Drawing.Color]::FromArgb(255, 80, 78, 75)

for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $b.SetPixel($x, $y, $slabBase)
    }
}
# Perimeter expansion joint
for ($i = 0; $i -lt 32; $i++) {
    $b.SetPixel($i, 0, $slabLight); $b.SetPixel(0, $i, $slabLight)
    $b.SetPixel($i, 31, $groove); $b.SetPixel(31, $i, $groove)
    $b.SetPixel($i, 1, $slabBase); $b.SetPixel($i, 30, $slabShadow)
}
# Diamond pyramid grid (4x4 cells, each 8x8)
for ($cy = 0; $cy -lt 4; $cy++) {
    for ($cx = 0; $cx -lt 4; $cx++) {
        $bx = $cx * 7 + 2; $by = $cy * 7 + 2
        for ($dy = 0; $dy -lt 6; $dy++) {
            for ($dx = 0; $dx -lt 6; $dx++) {
                $px = $bx + $dx; $py = $by + $dy
                if ($px -lt 31 -and $py -lt 31) {
                    if ($dx + $dy -le 3) {
                        $b.SetPixel($px, $py, $slabLight)
                    } elseif ($dx + $dy -ge 7) {
                        $b.SetPixel($px, $py, $slabShadow)
                    } else {
                        $b.SetPixel($px, $py, $slabBase)
                    }
                }
            }
        }
    }
}
Save-Bmp $b (Join-Path $blockDir "pdn_slab.png")

# ==============================================================================
# 4. TRUE TRIANGULAR WARNING SIGN (32x32) - 100% transparent outside triangle!
# ==============================================================================
$b = New-Bmp 32 32
$trans = [System.Drawing.Color]::FromArgb(0, 0, 0, 0)
for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $b.SetPixel($x, $y, $trans)
    }
}

$yBase = [System.Drawing.Color]::FromArgb(255, 248, 210, 18)
$yLight = [System.Drawing.Color]::FromArgb(255, 255, 235, 75)
$yShadow = [System.Drawing.Color]::FromArgb(255, 215, 175, 10)
$blackBorder = [System.Drawing.Color]::FromArgb(255, 25, 25, 25)
$boltGlow = [System.Drawing.Color]::FromArgb(255, 255, 255, 180)
$rivet = [System.Drawing.Color]::FromArgb(255, 80, 80, 85)

# Fill equilateral triangle from tip (16, 3) to base (3..29, 28)
for ($y = 3; $y -le 28; $y++) {
    $halfW = [Math]::Floor(($y - 3) * 0.54)
    $xStart = 16 - $halfW; $xEnd = 16 + $halfW
    for ($x = $xStart; $x -le $xEnd; $x++) {
        $dist = [Math]::Min($x - $xStart, $xEnd - $x)
        if ($dist -le 1 -or $y -ge 27) {
            # Outer black protective enamel border
            $b.SetPixel($x, $y, $blackBorder)
        } else {
            # Rich yellow fill with lighting
            if ($x -lt 16 -and $y -lt 16) { $b.SetPixel($x, $y, $yLight) }
            elseif ($x -gt 18 -or $y -gt 23) { $b.SetPixel($x, $y, $yShadow) }
            else { $b.SetPixel($x, $y, $yBase) }
        }
    }
}

# Iconic sharp black lightning bolt
$lightning = @(
    @(16,8),@(17,8),
    @(15,9),@(16,9),
    @(14,10),@(15,10),@(16,10),
    @(14,11),@(15,11),
    @(13,12),@(14,12),@(15,12),@(16,12),@(17,12),@(18,12),@(19,12), # horizontal zag
    @(17,13),@(18,13),
    @(16,14),@(17,14),
    @(15,15),@(16,15),
    @(14,16),@(15,16),
    @(13,17),@(14,17),
    @(14,18),@(15,18), # arrow head
    @(14,19)
)
foreach ($pt in $lightning) {
    $b.SetPixel($pt[0], $pt[1], $blackBorder)
}
# White highlight spark on lightning zag
$b.SetPixel(15, 11, $boltGlow)

# Mounting corner bolts
$b.SetPixel(16, 5, $rivet)
$b.SetPixel(6, 26, $rivet); $b.SetPixel(26, 26, $rivet)

Save-Bmp $b (Join-Path $blockDir "warning_sign.png")

# ==============================================================================
# 5. PO-2 FENCE MODEL TEXTURE (64x64) - Exactly matching User Reference (Image 1)!
# ==============================================================================
$b = New-Bmp 64 64
$frameC = [System.Drawing.Color]::FromArgb(255, 168, 165, 160)
$frameLight = [System.Drawing.Color]::FromArgb(255, 205, 202, 198)
$frameDark = [System.Drawing.Color]::FromArgb(255, 95, 92, 88)
$recessDark = [System.Drawing.Color]::FromArgb(255, 55, 58, 62)

# Central waffle panel (0..48, 0..48) with 4 rows x 5 columns of pyramids
# Pyramids palette:
$pyrTop = [System.Drawing.Color]::FromArgb(255, 215, 218, 222)    # Top slope highlight
$pyrLeft = [System.Drawing.Color]::FromArgb(255, 175, 178, 182)   # Left slope
$pyrRight = [System.Drawing.Color]::FromArgb(255, 110, 115, 122)  # Right slope
$pyrBottom = [System.Drawing.Color]::FromArgb(255, 60, 68, 78)    # Bottom deep shadow
$pyrFace = [System.Drawing.Color]::FromArgb(255, 150, 155, 160)   # Flat center

for ($y = 0; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        $b.SetPixel($x, $y, $frameC)
    }
}

# Draw 4 rows x 5 cols of truncated pyramids (each 9x9 pixels)
for ($row = 0; $row -lt 4; $row++) {
    for ($col = 0; $col -lt 5; $col++) {
        $ox = $col * 9 + 4; $oy = $row * 9 + 4
        # Bevel slopes
        for ($dy = 0; $dy -lt 8; $dy++) {
            for ($dx = 0; $dx -lt 8; $dx++) {
                $px = $ox + $dx; $py = $oy + $dy
                if ($dy -eq 0 -or ($dy -eq 1 -and $dx -ge 1 -and $dx -le 6)) {
                    $b.SetPixel($px, $py, $pyrTop)
                } elseif ($dx -eq 0 -or ($dx -eq 1 -and $dy -ge 1 -and $dy -le 6)) {
                    $b.SetPixel($px, $py, $pyrLeft)
                } elseif ($dx -ge 6) {
                    $b.SetPixel($px, $py, $pyrRight)
                } elseif ($dy -ge 6) {
                    $b.SetPixel($px, $py, $pyrBottom)
                } else {
                    $b.SetPixel($px, $py, $pyrFace)
                }
            }
        }
        # Recessed shadow groove under each pyramid
        for ($i = 0; $i -lt 9; $i++) {
            $b.SetPixel($ox + $i, $oy + 8, $recessDark)
            $b.SetPixel($ox + 8, $oy + $i, $recessDark)
        }
    }
}

# Rust drips from mounting loops
$rustDrip = [System.Drawing.Color]::FromArgb(255, 135, 75, 45)
$b.SetPixel(15, 2, $rustDrip); $b.SetPixel(15, 3, $rustDrip); $b.SetPixel(16, 4, $rustDrip)
$b.SetPixel(38, 2, $rustDrip); $b.SetPixel(38, 3, $rustDrip); $b.SetPixel(37, 4, $rustDrip)

# Footing стакани ФО-2 texture (bottom 16 rows)
$footBase = [System.Drawing.Color]::FromArgb(255, 135, 132, 128)
$footDark = [System.Drawing.Color]::FromArgb(255, 85, 82, 78)
$moss = [System.Drawing.Color]::FromArgb(255, 105, 120, 85)

for ($y = 48; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        $c = if ($y -eq 48) { $frameLight } elseif ($y -ge 60) { $footDark } else { $footBase }
        $b.SetPixel($x, $y, $c)
        if (($x * 3 + $y * 7) % 11 -eq 0 -and $y -ge 55) {
            $b.SetPixel($x, $y, $moss)
        }
    }
}

Save-Bmp $b (Join-Path $modelDir "po2_fence.png")

# ==============================================================================
# 6. 3D INDUSTRIAL LAMP TEXTURE (64x64)
# ==============================================================================
$b = New-Bmp 64 64
$bellEnamel = [System.Drawing.Color]::FromArgb(255, 70, 78, 80)
$bellHi = [System.Drawing.Color]::FromArgb(255, 115, 125, 130)
$bellShadow = [System.Drawing.Color]::FromArgb(255, 45, 50, 52)
$bulbGlow = [System.Drawing.Color]::FromArgb(255, 255, 250, 210)
$steelCage = [System.Drawing.Color]::FromArgb(255, 150, 155, 160)

for ($y = 0; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        $b.SetPixel($x, $y, $bellEnamel)
    }
}
# Bell highlights
for ($y = 15; $y -lt 40; $y++) {
    for ($x = 0; $x -lt 20; $x++) {
        if ($y % 4 -eq 0) { $b.SetPixel($x, $y, $bellHi) }
        elseif ($y % 4 -eq 3) { $b.SetPixel($x, $y, $bellShadow) }
    }
}
# Glowing bulb area
for ($y = 0; $y -lt 12; $y++) {
    for ($x = 34; $x -lt 46; $x++) {
        $b.SetPixel($x, $y, $bulbGlow)
    }
}
# Cage steel
for ($y = 44; $y -lt 55; $y++) {
    for ($x = 0; $x -lt 30; $x++) {
        $b.SetPixel($x, $y, $steelCage)
    }
}
Save-Bmp $b (Join-Path $modelDir "industrial_lamp.png")

# ==============================================================================
# 7. 3D INDUSTRIAL PIPE TEXTURE (64x64)
# ==============================================================================
$b = New-Bmp 64 64
$pipeInsul = [System.Drawing.Color]::FromArgb(255, 115, 110, 102)
$pipeWrapDark = [System.Drawing.Color]::FromArgb(255, 80, 75, 70)
$pipeWrapLight = [System.Drawing.Color]::FromArgb(255, 145, 140, 132)
$flangeSteel = [System.Drawing.Color]::FromArgb(255, 165, 170, 175)
$boltHead = [System.Drawing.Color]::FromArgb(255, 45, 48, 50)

for ($y = 0; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        # Cylindrical gradient
        $c = if ($y % 8 -le 2) { $pipeWrapLight } elseif ($y % 8 -ge 5) { $pipeWrapDark } else { $pipeInsul }
        $b.SetPixel($x, $y, $c)
    }
}
# Flanges & tightening rings
for ($y = 44; $y -lt 58; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $b.SetPixel($x, $y, $flangeSteel)
        if ($x % 6 -eq 0 -or $x % 6 -eq 1) {
            $b.SetPixel($x, $y, $boltHead)
        }
    }
}
Save-Bmp $b (Join-Path $modelDir "industrial_pipe.png")

# ==============================================================================
# 8. SILICATE BRICK (32x32)
# ==============================================================================
$b = New-Bmp 32 32
$brickC = [System.Drawing.Color]::FromArgb(255, 218, 215, 208)
$brickL = [System.Drawing.Color]::FromArgb(255, 235, 232, 226)
$brickD = [System.Drawing.Color]::FromArgb(255, 195, 192, 185)
$mortar = [System.Drawing.Color]::FromArgb(255, 135, 132, 126)

for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $b.SetPixel($x, $y, $brickC)
    }
}
# 4 brick rows (each height 7, mortar height 1)
$rows = @(7, 15, 23, 31)
foreach ($ry in $rows) {
    for ($x = 0; $x -lt 32; $x++) {
        $b.SetPixel($x, $ry, $mortar)
    }
}
# Vertical joints with brick offsets
for ($y = 0; $y -lt 7; $y++) { $b.SetPixel(15, $y, $mortar) }
for ($y = 8; $y -lt 15; $y++) { $b.SetPixel(7, $y, $mortar); $b.SetPixel(23, $y, $mortar) }
for ($y = 16; $y -lt 23; $y++) { $b.SetPixel(15, $y, $mortar) }
for ($y = 24; $y -lt 31; $y++) { $b.SetPixel(7, $y, $mortar); $b.SetPixel(23, $y, $mortar) }

# Highlights on top of each brick
for ($x = 0; $x -lt 32; $x++) {
    $b.SetPixel($x, 0, $brickL); $b.SetPixel($x, 8, $brickL)
    $b.SetPixel($x, 16, $brickL); $b.SetPixel($x, 24, $brickL)
}
Save-Bmp $b (Join-Path $blockDir "silicate_brick.png")

# ==============================================================================
# 9. PAINTED WALL PANEL (32x32) - Soviet oil green paint & white plaster
# ==============================================================================
$b = New-Bmp 32 32
$plaster = [System.Drawing.Color]::FromArgb(255, 232, 230, 222)
$plasterD = [System.Drawing.Color]::FromArgb(255, 205, 202, 195)
$oilGreen = [System.Drawing.Color]::FromArgb(255, 48, 88, 55)
$oilGreenL = [System.Drawing.Color]::FromArgb(255, 68, 115, 75)
$oilGreenD = [System.Drawing.Color]::FromArgb(255, 32, 62, 38)
$dividerLine = [System.Drawing.Color]::FromArgb(255, 24, 45, 28)

for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $c = if (($x + $y) % 7 -eq 0) { $plasterD } else { $plaster }
        $b.SetPixel($x, $y, $c)
    }
}
# Divider stripe at y=16
for ($x = 0; $x -lt 32; $x++) {
    $b.SetPixel($x, 15, $plasterD)
    $b.SetPixel($x, 16, $dividerLine)
    $b.SetPixel($x, 17, $oilGreenL) # Specular gloss line of oil paint!
}
for ($y = 18; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $c = if ($y % 4 -eq 0) { $oilGreenL } elseif ($y % 4 -eq 3) { $oilGreenD } else { $oilGreen }
        $b.SetPixel($x, $y, $c)
    }
}
Save-Bmp $b (Join-Path $blockDir "panel_lower.png")

Write-Host "All 32x32 HD textures generated successfully!"
