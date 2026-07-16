param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDirectory,
    [Parameter(Mandatory = $true)]
    [string]$ProjectDirectory
)

$ErrorActionPreference = 'Stop'
$source = Get-Content -LiteralPath (Join-Path $SourceDirectory 'model1.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$stateDirectory = Join-Path $ProjectDirectory 'common\src\main\resources\assets\kaleidoscope_grilling\models\item\skewer_states'
[System.IO.Directory]::CreateDirectory($stateDirectory) | Out-Null
Get-ChildItem -LiteralPath $stateDirectory -Filter '*.json' -ErrorAction SilentlyContinue | Remove-Item -Force

function Deep-Copy($Value) {
    return $Value | ConvertTo-Json -Depth 40 | ConvertFrom-Json
}

function Read-Part([int]$Slot, [int]$Variant) {
    if ($Variant -eq 1) {
        return Deep-Copy $source.elements[$Slot]
    }
    $path = Join-Path $SourceDirectory "food_parts\food${Slot}_model${Variant}.json"
    $model = Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
    return Deep-Copy $model.elements[1]
}

function New-TintedVoxels($Part, [int]$Slot) {
    $result = @()
    $lengths = @(
        ([double]$Part.to[0] - [double]$Part.from[0])
        ([double]$Part.to[1] - [double]$Part.from[1])
        ([double]$Part.to[2] - [double]$Part.from[2])
    )
    $counts = @(
        [Math]::Max(1, [Math]::Round($lengths[0]))
        [Math]::Max(1, [Math]::Round($lengths[1]))
        [Math]::Max(1, [Math]::Round($lengths[2]))
    )
    for ($x = 0; $x -lt $counts[0]; $x++) {
        for ($y = 0; $y -lt $counts[1]; $y++) {
            for ($z = 0; $z -lt $counts[2]; $z++) {
                $from = @(
                    ([double]$Part.from[0] + $lengths[0] * $x / $counts[0])
                    ([double]$Part.from[1] + $lengths[1] * $y / $counts[1])
                    ([double]$Part.from[2] + $lengths[2] * $z / $counts[2])
                )
                $to = @(
                    ([double]$Part.from[0] + $lengths[0] * ($x + 1) / $counts[0])
                    ([double]$Part.from[1] + $lengths[1] * ($y + 1) / $counts[1])
                    ([double]$Part.from[2] + $lengths[2] * ($z + 1) / $counts[2])
                )
                # Offset all three axes so neighboring voxels rarely share one sampled color.
                $colorIndex = ($x + 2 * $y + 3 * $z + $Slot - 1) % 6
                $tint = ($Slot - 1) * 8 + $colorIndex
                $faces = [ordered]@{}
                foreach ($face in @('north', 'east', 'south', 'west', 'up', 'down')) {
                    $faces[$face] = [ordered]@{
                        uv = @(0, 0, 16, 16)
                        texture = '#food'
                        tintindex = $tint
                    }
                }
                $result += [ordered]@{
                    name = "food${Slot}_voxel_${x}_${y}_${z}"
                    from = $from
                    to = $to
                    rotation = $Part.rotation
                    faces = $faces
                }
            }
        }
    }
    return $result
}

function New-Model([int[]]$Variants) {
    $wood = Deep-Copy $source.elements[0]
    foreach ($faceName in @('north', 'east', 'south', 'west', 'up', 'down')) {
        $wood.faces.$faceName.texture = '#stick'
    }
    $elements = @($wood)
    for ($slot = 1; $slot -le $Variants.Count; $slot++) {
        if ($Variants[$slot - 1] -gt 0) {
            $elements += New-TintedVoxels (Read-Part $slot $Variants[$slot - 1]) $slot
        }
    }
    return [ordered]@{
        ambientocclusion = $false
        textures = [ordered]@{
            stick = 'kaleidoscope_grilling:item/secret_skewer_stick'
            food = 'minecraft:block/white_concrete'
            particle = '#stick'
        }
        elements = $elements
        display = $source.display
    }
}

function Write-Json([string]$Path, $Value) {
    $json = $Value | ConvertTo-Json -Depth 40
    [System.IO.File]::WriteAllText($Path, $json, [System.Text.UTF8Encoding]::new($false))
}

$states = @()
foreach ($food1 in 1..3) {
    $states += ,@($food1, 0, 0)
    foreach ($food2 in 1..3) {
        $states += ,@($food1, $food2, 0)
        foreach ($food3 in 1..3) {
            $states += ,@($food1, $food2, $food3)
        }
    }
}

$overrides = @()
foreach ($variants in $states) {
    $code = $variants[0] * 16 + $variants[1] * 4 + $variants[2]
    Write-Json (Join-Path $stateDirectory "state_${code}.json") (New-Model $variants)
    $overrides += [ordered]@{
        predicate = [ordered]@{ 'kaleidoscope_grilling:skewer_state' = $code / 64.0 }
        model = "kaleidoscope_grilling:item/skewer_states/state_${code}"
    }
}

$overrides = $overrides | Sort-Object { [double]$_.predicate.'kaleidoscope_grilling:skewer_state' }
$baseModel = New-Model @()
$baseModel.overrides = @($overrides)
$itemModelDirectory = Split-Path $stateDirectory -Parent
Write-Json (Join-Path $itemModelDirectory 'unfinished_skewer.json') $baseModel
Write-Json (Join-Path $itemModelDirectory 'secret_skewer.json') $baseModel

Write-Output "Generated $($states.Count) runtime skewer state models"
