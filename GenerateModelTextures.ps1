Add-Type -AssemblyName System.Drawing

$modelDir = Join-Path $PSScriptRoot "src\main\resources\assets\sovietindustry\textures\models"
$blockDir = Join-Path $PSScriptRoot "src\main\resources\assets\sovietindustry\textures\blocks"

New-Item -ItemType Directory -Path $modelDir -Force | Out-Null

function Save-Bmp($bmp, $path) {
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Host "Created: $path"
    $bmp.Dispose()
}

# 1. Knife Switch texture (32x32)
$b = New-Object System.Drawing.Bitmap 32, 32
$g = [System.Drawing.Graphics]::FromImage($b)
$g.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))

$slateGrey = [System.Drawing.Color]::FromArgb(110, 108, 105)
$ceramicWhite = [System.Drawing.Color]::FromArgb(215, 210, 200)
$copper = [System.Drawing.Color]::FromArgb(200, 115, 45)
$steel = [System.Drawing.Color]::FromArgb(180, 185, 190)
$redHandle = [System.Drawing.Color]::FromArgb(190, 25, 20)

for ($y = 0; $y -lt 14; $y++) {
    for ($x = 0; $x -lt 14; $x++) {
        $b.SetPixel($x, $y, $ceramicWhite)
    }
}
for ($i = 0; $i -lt 14; $i++) {
    $b.SetPixel($i, 0, $slateGrey); $b.SetPixel($i, 13, $slateGrey)
    $b.SetPixel(0, $i, $slateGrey); $b.SetPixel(13, $i, $slateGrey)
}

for ($y = 0; $y -lt 10; $y++) {
    for ($x = 14; $x -lt 24; $x++) {
        $b.SetPixel($x, $y, $copper)
    }
}

for ($y = 14; $y -lt 24; $y++) {
    for ($x = 0; $x -lt 4; $x++) {
        $b.SetPixel($x, $y, $steel)
    }
}

for ($y = 14; $y -lt 24; $y++) {
    for ($x = 4; $x -lt 12; $x++) {
        $b.SetPixel($x, $y, $redHandle)
    }
}

Save-Bmp $b (Join-Path $modelDir "knife_switch.png")

# 2. PO-2 Fence texture (64x64)
$b = New-Object System.Drawing.Bitmap 64, 64
$concrete = [System.Drawing.Color]::FromArgb(150, 146, 140)
$darkRelief = [System.Drawing.Color]::FromArgb(105, 100, 95)
$lightRelief = [System.Drawing.Color]::FromArgb(180, 175, 170)
$footing = [System.Drawing.Color]::FromArgb(130, 126, 120)

for ($y = 0; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        $b.SetPixel($x, $y, $concrete)
    }
}

for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $rx = $x % 8
        $ry = $y % 8
        if ($rx + $ry -eq 4 -or [Math]::Abs($rx - $ry) -eq 4) {
            $b.SetPixel($x, $y, $darkRelief)
        } elseif ($rx + $ry -lt 4) {
            $b.SetPixel($x, $y, $lightRelief)
        }
    }
}

$moss = [System.Drawing.Color]::FromArgb(110, 125, 95)
for ($y = 32; $y -lt 64; $y++) {
    for ($x = 0; $x -lt 64; $x++) {
        $b.SetPixel($x, $y, $footing)
        if (($x + $y * 3) % 7 -eq 0) {
            $b.SetPixel($x, $y, $moss)
        }
    }
}

Save-Bmp $b (Join-Path $modelDir "po2_fence.png")

# 3. Warning Sign (16x16)
$b = New-Object System.Drawing.Bitmap 16, 16
$borderSteel = [System.Drawing.Color]::FromArgb(115, 118, 122)
$brightYellow = [System.Drawing.Color]::FromArgb(250, 210, 20)
$pureBlack = [System.Drawing.Color]::FromArgb(15, 15, 15)
$boltGlow = [System.Drawing.Color]::FromArgb(255, 245, 100)

for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $b.SetPixel($x, $y, $borderSteel)
    }
}

for ($y = 2; $y -le 13; $y++) {
    $w = [Math]::Floor(($y - 2) * 0.95)
    for ($x = (7 - $w); $x -le (8 + $w); $x++) {
        if ($x -ge 1 -and $x -le 14) {
            $b.SetPixel($x, $y, $brightYellow)
        }
    }
}

for ($y = 2; $y -le 13; $y++) {
    $w = [Math]::Floor(($y - 2) * 0.95)
    $l = 7 - $w
    $r = 8 + $w
    if ($l -ge 1) { $b.SetPixel($l, $y, $pureBlack) }
    if ($r -le 14) { $b.SetPixel($r, $y, $pureBlack) }
}
for ($x = 2; $x -le 13; $x++) { $b.SetPixel($x, 13, $pureBlack) }

$b.SetPixel(8, 5, $pureBlack)
$b.SetPixel(7, 6, $pureBlack); $b.SetPixel(8, 6, $boltGlow)
$b.SetPixel(7, 7, $pureBlack); $b.SetPixel(8, 7, $pureBlack); $b.SetPixel(9, 7, $pureBlack)
$b.SetPixel(6, 8, $pureBlack); $b.SetPixel(7, 8, $pureBlack)
$b.SetPixel(7, 9, $pureBlack); $b.SetPixel(8, 9, $pureBlack)
$b.SetPixel(7, 10, $pureBlack)
$b.SetPixel(6, 11, $pureBlack)

$b.SetPixel(1, 1, $pureBlack); $b.SetPixel(14, 1, $pureBlack)
$b.SetPixel(1, 14, $pureBlack); $b.SetPixel(14, 14, $pureBlack)

Save-Bmp $b (Join-Path $blockDir "warning_sign.png")

Write-Host "All textures created successfully!"
