$cache = Join-Path $env:USERPROFILE ".gradle\caches\minecraft"

$clientDir = Join-Path $cache "net\minecraft\minecraft\1.7.10"
$serverDir = Join-Path $cache "net\minecraft\minecraft_server\1.7.10"
$indexDir  = Join-Path $cache "assets\indexes"

New-Item -ItemType Directory -Path $clientDir -Force | Out-Null
New-Item -ItemType Directory -Path $serverDir -Force | Out-Null
New-Item -ItemType Directory -Path $indexDir  -Force | Out-Null

$clientJar = Join-Path $clientDir "minecraft-1.7.10.jar"
$serverJar = Join-Path $serverDir "minecraft_server-1.7.10.jar"
$indexJson = Join-Path $indexDir  "1.7.10.json"

[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
$wc = New-Object System.Net.WebClient

# 1. Client jar
if (-not (Test-Path $clientJar) -or (Get-Item $clientJar).Length -lt 1000000) {
    Write-Host "Downloading Minecraft 1.7.10 client.jar from Mojang..."
    try {
        $wc.DownloadFile("https://launcher.mojang.com/v1/objects/a412fd69db1f81bb3f511c14295b947e4fb926f3/client.jar", $clientJar)
        Write-Host "[OK] client.jar ready."
    } catch {
        Write-Warning "Could not download client.jar directly: $_"
    }
}

# 2. Server jar
if (-not (Test-Path $serverJar) -or (Get-Item $serverJar).Length -lt 1000000) {
    Write-Host "Downloading Minecraft 1.7.10 server.jar from Mojang..."
    try {
        $wc.DownloadFile("https://launcher.mojang.com/v1/objects/c8dc0bd6ef5031077146461973f622944b257eed/server.jar", $serverJar)
        Write-Host "[OK] server.jar ready."
    } catch {
        Write-Warning "Could not download server.jar directly: $_"
    }
}

# 3. Index json
if (-not (Test-Path $indexJson) -or (Get-Item $indexJson).Length -lt 1000) {
    $tlIndex = Join-Path $env:APPDATA ".tlauncher\legacy\Minecraft\game\assets\indexes\1.7.10.json"
    if (Test-Path $tlIndex) {
        Copy-Item $tlIndex $indexJson -Force
        Write-Host "[OK] 1.7.10.json copied from local launcher."
    }
}

Write-Host "Pre-download check complete."
