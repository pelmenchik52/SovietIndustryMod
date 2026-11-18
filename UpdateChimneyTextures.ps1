Add-Type -AssemblyName System.Drawing

$texDir = Join-Path $PSScriptRoot "src\main\resources\assets\sovietindustry\textures\blocks"
New-Item -ItemType Directory -Path $texDir -Force | Out-Null

function Save-Bmp($bmp, $name) {
    $path = Join-Path $texDir "$name.png"
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Host "Updated: $name.png"
    $bmp.Dispose()
}

$rnd = New-Object System.Random 999

# 1. chimney_0.png: Гладка прокатна сталь (без панелей, глибокий колір сталі)
$b0 = New-Object System.Drawing.Bitmap 32, 32
for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        # Плавний вертикальний прокатний градієнт сталі
        $sheen = [Math]::Sin($x * 0.2) * 5
        $grain = $rnd.Next(-3, 4)
        $val = [Math]::Max(35, [Math]::Min(110, [int](62 + $sheen + $grain)))
        $b0.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $val, [int]($val * 1.03), [int]($val * 1.08)))
    }
}
# Тонкий вертикальний зварний шов (weld seam)
for ($y = 0; $y -lt 32; $y++) {
    $b0.SetPixel(16, $y, [System.Drawing.Color]::FromArgb(255, 45, 48, 52))
    $b0.SetPixel(17, $y, [System.Drawing.Color]::FromArgb(255, 80, 85, 90))
}
Save-Bmp $b0 "chimney_0"

# 2. chimney_1.png: Сталь з темною бурою іржею (глибокий колір, не яскраво-рудий)
$b1 = New-Object System.Drawing.Bitmap 32, 32
for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $sheen = [Math]::Sin($x * 0.2) * 4
        $grain = $rnd.Next(-3, 4)
        $val = [Math]::Max(30, [Math]::Min(95, [int](55 + $sheen + $grain)))
        
        # Плями темної іржі (burnt umber / темно-коричнева)
        $isRust = (($x * 3 + $y * 7 + $rnd.Next(0, 10)) % 17 -lt 6) -or ($y -gt 18 -and $x -lt 14)
        if ($isRust) {
            $rNoise = $rnd.Next(-6, 7)
            $rR = [Math]::Max(0, [Math]::Min(255, 80 + $rNoise))
            $rG = [Math]::Max(0, [Math]::Min(255, 46 + $rNoise))
            $rB = [Math]::Max(0, [Math]::Min(255, 28 + $rNoise))
            $b1.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $rR, $rG, $rB))
        } else {
            $b1.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $val, [int]($val * 1.02), [int]($val * 1.05)))
        }
    }
}
Save-Bmp $b1 "chimney_1"

# 3. chimney_2.png: Сильно іржава сталь (темно-бура корозія, окалина)
$b2 = New-Object System.Drawing.Bitmap 32, 32
for ($y = 0; $y -lt 32; $y++) {
    for ($x = 0; $x -lt 32; $x++) {
        $noise = $rnd.Next(-8, 9)
        # Більша частина поверхні вкрита темною іржею та окалиною
        $r = [Math]::Max(30, [Math]::Min(110, [int](68 + $noise)))
        $g = [Math]::Max(20, [Math]::Min(75,  [int](38 + $noise * 0.6)))
        $bl = [Math]::Max(15, [Math]::Min(55, [int](25 + $noise * 0.4)))
        # Місцями темні потьоки (водяні сліди)
        if ($x % 5 -eq 0 -and $rnd.Next(0, 10) -lt 7) {
            $r = [int]($r * 0.75); $g = [int]($g * 0.75); $bl = [int]($bl * 0.75)
        }
        $b2.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $r, $g, $bl))
    }
}
Save-Bmp $b2 "chimney_2"

Write-Host "Chimney textures updated successfully!"
