Add-Type -AssemblyName System.Drawing

$texDir = Join-Path $PSScriptRoot "src\main\resources\assets\sovietindustry\textures\blocks"
Write-Host "Output directory: $texDir"

if (-not (Test-Path $texDir)) {
    New-Item -ItemType Directory -Path $texDir -Force | Out-Null
}

function Save-Bmp($bmp, $name) {
    $path = Join-Path $texDir "$name.png"
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Host "Created: $name.png"
    $bmp.Dispose()
}

function New-SolidBmp($r, $g, $b, $a = 255) {
    $bmp = New-Object System.Drawing.Bitmap 16, 16
    $color = [System.Drawing.Color]::FromArgb($a, $r, $g, $b)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $bmp.SetPixel($x, $y, $color)
        }
    }
    return $bmp
}

# 1. Glass Block (Фальконьє / рифлене скло)
$b = New-SolidBmp 130 175 170 190
$border = [System.Drawing.Color]::FromArgb(240, 90, 130, 130)
$ribLight = [System.Drawing.Color]::FromArgb(220, 180, 220, 215)
$ribDark = [System.Drawing.Color]::FromArgb(210, 110, 150, 145)
for ($i = 0; $i -lt 16; $i++) {
    $b.SetPixel($i, 0, $border)
    $b.SetPixel($i, 15, $border)
    $b.SetPixel(0, $i, $border)
    $b.SetPixel(15, $i, $border)
}
for ($x = 2; $x -lt 14; $x += 3) {
    for ($y = 1; $y -lt 15; $y++) {
        $b.SetPixel($x, $y, $ribDark)
        if ($x + 1 -lt 15) { $b.SetPixel($x + 1, $y, $ribLight) }
    }
}
Save-Bmp $b "glass_block"

# 2. Slate (Хвилястий шифер)
$b = New-SolidBmp 150 150 148
$waveLight = [System.Drawing.Color]::FromArgb(185, 185, 180)
$waveDark = [System.Drawing.Color]::FromArgb(115, 115, 112)
$moss = [System.Drawing.Color]::FromArgb(120, 135, 100)
for ($x = 0; $x -lt 16; $x++) {
    $col = ($x % 4)
    for ($y = 0; $y -lt 16; $y++) {
        if ($col -eq 0 -or $col -eq 1) {
            $b.SetPixel($x, $y, $waveDark)
        } else {
            $b.SetPixel($x, $y, $waveLight)
        }
    }
}
$b.SetPixel(3, 4, $moss); $b.SetPixel(4, 4, $moss); $b.SetPixel(4, 5, $moss)
$b.SetPixel(11, 10, $moss); $b.SetPixel(12, 10, $moss); $b.SetPixel(11, 11, $moss)
Save-Bmp $b "slate"

# 3. Corrugated Iron (Іржавий профнастил)
$b = New-SolidBmp 125 128 132
$ribL = [System.Drawing.Color]::FromArgb(160, 165, 170)
$ribD = [System.Drawing.Color]::FromArgb(90, 93, 98)
$rust1 = [System.Drawing.Color]::FromArgb(150, 75, 40)
$rust2 = [System.Drawing.Color]::FromArgb(115, 50, 25)
for ($x = 0; $x -lt 16; $x++) {
    for ($y = 0; $y -lt 16; $y++) {
        if ($x % 3 -eq 0) { $b.SetPixel($x, $y, $ribD) }
        elseif ($x % 3 -eq 1) { $b.SetPixel($x, $y, $ribL) }
    }
}
$rustCoords = @(@(2,3),@(3,3),@(3,4),@(4,4),@(8,8),@(8,9),@(9,9),@(10,9),@(13,12),@(14,12),@(13,13),@(6,14),@(7,14))
foreach ($pt in $rustCoords) {
    $b.SetPixel($pt[0], $pt[1], $rust1)
}
$b.SetPixel(3, 3, $rust2); $b.SetPixel(9, 9, $rust2); $b.SetPixel(13, 12, $rust2)
Save-Bmp $b "corrugated_iron"

# 4. Damaged Concrete (Пошкоджений бетон з арматурою)
$b = New-SolidBmp 138 134 126
$darkHole = [System.Drawing.Color]::FromArgb(65, 62, 58)
$crack = [System.Drawing.Color]::FromArgb(85, 80, 75)
$rebar = [System.Drawing.Color]::FromArgb(150, 65, 30)
for ($y = 6; $y -le 11; $y++) {
    for ($x = 5; $x -le 11; $x++) {
        $b.SetPixel($x, $y, $darkHole)
    }
}
for ($x = 6; $x -le 10; $x++) { $b.SetPixel($x, 8, $rebar) }
for ($y = 7; $y -le 10; $y++) { $b.SetPixel(7, $y, $rebar); $b.SetPixel(9, $y, $rebar) }
$b.SetPixel(4, 5, $crack); $b.SetPixel(3, 4, $crack); $b.SetPixel(2, 3, $crack)
$b.SetPixel(12, 11, $crack); $b.SetPixel(13, 12, $crack); $b.SetPixel(14, 13, $crack)
Save-Bmp $b "damaged_concrete"

# 5. Damaged PO-2 Fence (Пошкоджений паркан ПО-2)
$b = New-SolidBmp 150 145 138
$dark = [System.Drawing.Color]::FromArgb(100, 95, 90)
$hole = [System.Drawing.Color]::FromArgb(60, 58, 55)
for ($i = 0; $i -lt 16; $i++) {
    $b.SetPixel($i, 0, $dark); $b.SetPixel($i, 15, $dark)
    $b.SetPixel(0, $i, $dark); $b.SetPixel(15, $i, $dark)
}
for ($y = 7; $y -le 10; $y++) {
    for ($x = 6; $x -le 10; $x++) {
        $b.SetPixel($x, $y, $hole)
    }
}
$b.SetPixel(4, 2, $dark); $b.SetPixel(5, 3, $dark); $b.SetPixel(4, 4, $dark); $b.SetPixel(3, 3, $dark)
Save-Bmp $b "po2_damaged"

# 6. Knife Switch Off (Рубильник вимкнений)
$b = New-SolidBmp 85 85 83
$basePlate = [System.Drawing.Color]::FromArgb(145, 140, 130)
$copper = [System.Drawing.Color]::FromArgb(200, 125, 50)
$knife = [System.Drawing.Color]::FromArgb(180, 185, 190)
$handle = [System.Drawing.Color]::FromArgb(160, 30, 25)
for ($y = 2; $y -le 13; $y++) {
    for ($x = 4; $x -le 11; $x++) {
        $b.SetPixel($x, $y, $basePlate)
    }
}
$b.SetPixel(6, 4, $copper); $b.SetPixel(9, 4, $copper)
$b.SetPixel(6, 11, $copper); $b.SetPixel(9, 11, $copper)
$b.SetPixel(6, 10, $knife); $b.SetPixel(7, 10, $knife); $b.SetPixel(8, 10, $knife); $b.SetPixel(9, 10, $knife)
$b.SetPixel(7, 12, $handle); $b.SetPixel(8, 12, $handle); $b.SetPixel(7, 13, $handle); $b.SetPixel(8, 13, $handle)
Save-Bmp $b "knife_switch_off"

# 7. Knife Switch On (Рубильник увімкнений)
$b = New-SolidBmp 85 85 83
for ($y = 2; $y -le 13; $y++) {
    for ($x = 4; $x -le 11; $x++) {
        $b.SetPixel($x, $y, $basePlate)
    }
}
$b.SetPixel(6, 4, $copper); $b.SetPixel(9, 4, $copper)
$b.SetPixel(6, 11, $copper); $b.SetPixel(9, 11, $copper)
$b.SetPixel(6, 5, $knife); $b.SetPixel(7, 5, $knife); $b.SetPixel(8, 5, $knife); $b.SetPixel(9, 5, $knife)
$b.SetPixel(7, 2, $handle); $b.SetPixel(8, 2, $handle); $b.SetPixel(7, 3, $handle); $b.SetPixel(8, 3, $handle)
$spark = [System.Drawing.Color]::FromArgb(255, 235, 60)
$b.SetPixel(5, 4, $spark); $b.SetPixel(10, 4, $spark)
Save-Bmp $b "knife_switch_on"

# 8. Electrical Cabinet Front (Щиток спереду)
$b = New-SolidBmp 120 122 125
$darkLine = [System.Drawing.Color]::FromArgb(70, 72, 75)
$yellow = [System.Drawing.Color]::FromArgb(245, 205, 30)
$black = [System.Drawing.Color]::FromArgb(25, 25, 25)
for ($i = 1; $i -lt 15; $i++) {
    $b.SetPixel($i, 1, $darkLine); $b.SetPixel($i, 14, $darkLine)
    $b.SetPixel(1, $i, $darkLine); $b.SetPixel(14, $i, $darkLine)
}
for ($x = 4; $x -le 11; $x++) {
    $b.SetPixel($x, 3, $darkLine)
    $b.SetPixel($x, 5, $darkLine)
}
for ($y = 8; $y -le 11; $y++) {
    $w = $y - 8
    for ($x = (7 - $w); $x -le (7 + $w); $x++) {
        $b.SetPixel($x, $y, $yellow)
    }
}
$b.SetPixel(7, 9, $black); $b.SetPixel(8, 9, $black); $b.SetPixel(7, 10, $black)
$b.SetPixel(12, 9, $black); $b.SetPixel(12, 10, $black)
Save-Bmp $b "electrical_cabinet_front"

# 9. Electrical Cabinet Side (Щиток збоку)
$b = New-SolidBmp 120 122 125
$rivet = [System.Drawing.Color]::FromArgb(85, 87, 90)
$rivetLight = [System.Drawing.Color]::FromArgb(160, 162, 165)
$corners = @(@(2,2), @(13,2), @(2,13), @(13,13))
foreach ($c in $corners) {
    $b.SetPixel($c[0], $c[1], $rivet)
    $b.SetPixel($c[0] + 1, $c[1], $rivetLight)
}
for ($y = 0; $y -lt 16; $y++) {
    $b.SetPixel(8, $y, $darkLine)
}
Save-Bmp $b "electrical_cabinet_side"

# 10. Industrial Lamp (Світильник РСП)
$b = New-SolidBmp 50 52 55
$metalBell = [System.Drawing.Color]::FromArgb(100, 105, 110)
$bulbGlow = [System.Drawing.Color]::FromArgb(255, 255, 220)
$glowOuter = [System.Drawing.Color]::FromArgb(235, 215, 130)
for ($x = 5; $x -le 10; $x++) { $b.SetPixel($x, 2, $metalBell) }
for ($x = 3; $x -le 12; $x++) { $b.SetPixel($x, 3, $metalBell); $b.SetPixel($x, 4, $metalBell) }
for ($x = 2; $x -le 13; $x++) { $b.SetPixel($x, 5, $metalBell) }
for ($y = 6; $y -le 12; $y++) {
    for ($x = 4; $x -le 11; $x++) {
        $b.SetPixel($x, $y, $glowOuter)
    }
}
for ($y = 7; $y -le 11; $y++) {
    for ($x = 5; $x -le 10; $x++) {
        $b.SetPixel($x, $y, $bulbGlow)
    }
}
for ($x = 2; $x -le 13; $x += 3) {
    for ($y = 6; $y -le 13; $y++) {
        $b.SetPixel($x, $y, $metalBell)
    }
}
Save-Bmp $b "industrial_lamp"

# 11. Industrial Pipe (Теплотраса в ізоляції)
$b = New-SolidBmp 120 115 105
$pipeDark = [System.Drawing.Color]::FromArgb(85, 80, 72)
$band = [System.Drawing.Color]::FromArgb(175, 180, 185)
$bandShadow = [System.Drawing.Color]::FromArgb(95, 100, 105)
for ($y = 0; $y -lt 16; $y++) {
    $b.SetPixel(0, $y, $pipeDark); $b.SetPixel(15, $y, $pipeDark)
}
for ($x = 0; $x -lt 16; $x++) {
    $b.SetPixel($x, 3, $band); $b.SetPixel($x, 4, $bandShadow)
    $b.SetPixel($x, 11, $band); $b.SetPixel($x, 12, $bandShadow)
}
$b.SetPixel(7, 7, $pipeDark); $b.SetPixel(8, 7, $pipeDark)
Save-Bmp $b "industrial_pipe"

# 12. Barrel Side (Іржава бочка)
$b = New-SolidBmp 60 78 82
$bDark = [System.Drawing.Color]::FromArgb(40, 55, 60)
$bRib = [System.Drawing.Color]::FromArgb(85, 105, 110)
$bRust = [System.Drawing.Color]::FromArgb(150, 75, 35)
for ($x = 0; $x -lt 16; $x++) {
    $b.SetPixel($x, 0, $bDark); $b.SetPixel($x, 15, $bDark)
    $b.SetPixel($x, 5, $bRib); $b.SetPixel($x, 6, $bDark)
    $b.SetPixel($x, 10, $bRib); $b.SetPixel($x, 11, $bDark)
}
$b.SetPixel(4, 2, $bRust); $b.SetPixel(4, 3, $bRust); $b.SetPixel(5, 4, $bRust)
$b.SetPixel(11, 7, $bRust); $b.SetPixel(11, 8, $bRust); $b.SetPixel(12, 9, $bRust)
Save-Bmp $b "barrel_side"

# 13. Barrel Top (Кришка бочки)
$b = New-SolidBmp 60 78 82
for ($i = 0; $i -lt 16; $i++) {
    $b.SetPixel($i, 0, $bDark); $b.SetPixel($i, 15, $bDark)
    $b.SetPixel(0, $i, $bDark); $b.SetPixel(15, $i, $bDark)
}
for ($y = 4; $y -le 6; $y++) {
    for ($x = 10; $x -le 12; $x++) {
        $b.SetPixel($x, $y, $bRust)
    }
}
$b.SetPixel(11, 5, [System.Drawing.Color]::FromArgb(30, 30, 30))
Save-Bmp $b "barrel_top"

# 14. Wooden Crate (Дерев'яний ящик)
$b = New-SolidBmp 165 130 85
$frame = [System.Drawing.Color]::FromArgb(120, 90, 55)
$stencil = [System.Drawing.Color]::FromArgb(45, 40, 35)
for ($i = 0; $i -lt 16; $i++) {
    $b.SetPixel($i, 0, $frame); $b.SetPixel($i, 1, $frame)
    $b.SetPixel($i, 14, $frame); $b.SetPixel($i, 15, $frame)
    $b.SetPixel(0, $i, $frame); $b.SetPixel(1, $i, $frame)
    $b.SetPixel(14, $i, $frame); $b.SetPixel(15, $i, $frame)
}
for ($i = 2; $i -lt 14; $i++) {
    $b.SetPixel($i, $i, $frame)
}
$b.SetPixel(4, 6, $stencil); $b.SetPixel(5, 5, $stencil); $b.SetPixel(6, 6, $stencil)
$b.SetPixel(5, 6, $stencil); $b.SetPixel(5, 7, $stencil); $b.SetPixel(5, 8, $stencil)
Save-Bmp $b "wooden_crate"

# 15. Warning Sign (Табличка «Не влазь — уб'є!»)
$b = New-SolidBmp 110 110 110
$yCol = [System.Drawing.Color]::FromArgb(245, 205, 20)
$black = [System.Drawing.Color]::FromArgb(20, 20, 20)
for ($y = 2; $y -le 13; $y++) {
    $w = [Math]::Floor(($y - 2) * 0.9)
    for ($x = (7 - $w); $x -le (8 + $w); $x++) {
        if ($x -ge 0 -and $x -lt 16) {
            $b.SetPixel($x, $y, $yCol)
        }
    }
}
for ($y = 2; $y -le 13; $y++) {
    $w = [Math]::Floor(($y - 2) * 0.9)
    $left = 7 - $w
    $right = 8 + $w
    if ($left -ge 0) { $b.SetPixel($left, $y, $black) }
    if ($right -lt 16) { $b.SetPixel($right, $y, $black) }
}
for ($x = 1; $x -le 14; $x++) { $b.SetPixel($x, 13, $black) }
$b.SetPixel(8, 5, $black); $b.SetPixel(7, 6, $black)
$b.SetPixel(8, 7, $black); $b.SetPixel(9, 7, $black)
$b.SetPixel(7, 8, $black); $b.SetPixel(8, 9, $black)
$b.SetPixel(7, 10, $black)
Save-Bmp $b "warning_sign"

Write-Host "All new textures generated successfully!"
