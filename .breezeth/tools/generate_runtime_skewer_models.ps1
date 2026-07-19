param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDirectory,
    [Parameter(Mandatory = $true)]
    [string]$ProjectDirectory
)

$ErrorActionPreference = 'Stop'
$sourceFiles = @(Get-ChildItem -LiteralPath $SourceDirectory -File -Filter 'mode*.json' |
    Where-Object { $_.Name -notlike 'model*.json' })
if ($sourceFiles.Count -lt 1) {
    throw "Expected at least one mode*.json source model in $SourceDirectory"
}
$sourcePath = ($sourceFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
$source = Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8 | ConvertFrom-Json
$stateDirectory = Join-Path $ProjectDirectory 'common\src\main\resources\assets\kaleidoscope_grilling\models\item\skewer_states'
[System.IO.Directory]::CreateDirectory($stateDirectory) | Out-Null
Get-ChildItem -LiteralPath $stateDirectory -Filter '*.json' -ErrorAction SilentlyContinue | Remove-Item -Force

function Deep-Copy($Value) {
    return $Value | ConvertTo-Json -Depth 40 | ConvertFrom-Json
}

$parts = @{
    1 = @()
    2 = @()
    3 = @()
}

# Element names in the Blockbench source are not unique. Slot ownership is determined
# by the element's position along the skewer, while source order defines variants 1-3.
for ($index = 1; $index -lt $source.elements.Count; $index++) {
    $element = $source.elements[$index]
    $centerZ = ([double]$element.from[2] + [double]$element.to[2]) / 2.0
    $slot = if ($centerZ -lt 5.25) { 1 } elseif ($centerZ -lt 9.0) { 2 } else { 3 }
    $parts[$slot] += ,(Deep-Copy $element)
}

foreach ($slot in 1..3) {
    if ($parts[$slot].Count -ne 3) {
        throw "Expected exactly 3 supplied models for food slot $slot, found $($parts[$slot].Count) in $sourcePath"
    }
}

function Read-Part([int]$Slot, [int]$Variant) {
    return Deep-Copy $parts[$Slot][$Variant - 1]
}

function Set-FaceTint($Face, [int]$Tint) {
    $Face.texture = '#food'
    $Face | Add-Member -NotePropertyName tintindex -NotePropertyValue $Tint -Force
}

function Slice-Range([double]$Start, [double]$End, [int]$Index, [int]$Count) {
    return @(
        ($Start + ($End - $Start) * $Index / $Count),
        ($Start + ($End - $Start) * ($Index + 1) / $Count)
    )
}

function Get-GridCoordinate([int]$Index, [int]$Count) {
    if ($Count -le 1) { return 1 }
    return [Math]::Round($Index * 3.0 / ($Count - 1))
}

function Get-UvWidth($Face) {
    return [Math]::Abs([double]$Face.uv[2] - [double]$Face.uv[0])
}

function Get-UvHeight($Face) {
    return [Math]::Abs([double]$Face.uv[3] - [double]$Face.uv[1])
}

function Get-PartitionCount([double[]]$Values) {
    $average = ($Values | Measure-Object -Average).Average
    return [Math]::Max(1, [Math]::Round($average, [MidpointRounding]::AwayFromZero))
}

function Get-FaceTint([int]$Slot, [string]$FaceName, [int]$GridX, [int]$GridY, [int]$GridZ) {
    $faceGroups = @{
        up = 0
        down = 1
        north = 2
        south = 3
        west = 4
        east = 5
    }
    $cell = switch ($FaceName) {
        { $_ -in @('up', 'down') } { $GridZ * 4 + $GridX; break }
        { $_ -in @('north', 'south') } { $GridY * 4 + $GridX; break }
        default { $GridY * 4 + $GridZ }
    }
    return ($Slot - 1) * 128 + $faceGroups[$FaceName] * 16 + $cell
}

function New-TintedElements($Part, [int]$Slot, [int]$Variant) {
    $result = @()
    $xCount = Get-PartitionCount @(
        (Get-UvWidth $Part.faces.north), (Get-UvWidth $Part.faces.south),
        (Get-UvWidth $Part.faces.up), (Get-UvWidth $Part.faces.down)
    )
    $yCount = Get-PartitionCount @(
        (Get-UvHeight $Part.faces.north), (Get-UvHeight $Part.faces.south),
        (Get-UvHeight $Part.faces.east), (Get-UvHeight $Part.faces.west)
    )
    $zCount = Get-PartitionCount @(
        (Get-UvWidth $Part.faces.east), (Get-UvWidth $Part.faces.west),
        (Get-UvHeight $Part.faces.up), (Get-UvHeight $Part.faces.down)
    )
    for ($z = 0; $z -lt $zCount; $z++) {
        for ($y = 0; $y -lt $yCount; $y++) {
            for ($x = 0; $x -lt $xCount; $x++) {
                $voxel = Deep-Copy $Part
                $xRange = Slice-Range ([double]$Part.from[0]) ([double]$Part.to[0]) $x $xCount
                $yRange = Slice-Range ([double]$Part.from[1]) ([double]$Part.to[1]) $y $yCount
                $zRange = Slice-Range ([double]$Part.from[2]) ([double]$Part.to[2]) $z $zCount
                $voxel.from[0] = $xRange[0]
                $voxel.to[0] = $xRange[1]
                $voxel.from[1] = $yRange[0]
                $voxel.to[1] = $yRange[1]
                $voxel.from[2] = $zRange[0]
                $voxel.to[2] = $zRange[1]
                $voxel.name = "food${Slot}_model${Variant}_pixel_${x}_${y}_${z}"

                if ($z -ne 0) { $voxel.faces.PSObject.Properties.Remove('north') }
                if ($z -ne $zCount - 1) { $voxel.faces.PSObject.Properties.Remove('south') }
                if ($x -ne 0) { $voxel.faces.PSObject.Properties.Remove('west') }
                if ($x -ne $xCount - 1) { $voxel.faces.PSObject.Properties.Remove('east') }
                if ($y -ne 0) { $voxel.faces.PSObject.Properties.Remove('down') }
                if ($y -ne $yCount - 1) { $voxel.faces.PSObject.Properties.Remove('up') }

                # Fully enclosed voxels have no visible faces and are invalid model elements.
                if (@($voxel.faces.PSObject.Properties).Count -eq 0) { continue }

                foreach ($faceName in @('north', 'south')) {
                    $face = $voxel.faces.$faceName
                    if ($null -ne $face) {
                        $uv = @($Part.faces.$faceName.uv)
                        $u = Slice-Range ([double]$uv[0]) ([double]$uv[2]) $x $xCount
                        $v = Slice-Range ([double]$uv[1]) ([double]$uv[3]) $y $yCount
                        $face.uv = @($u[0], $v[0], $u[1], $v[1])
                    }
                }
                foreach ($faceName in @('east', 'west')) {
                    $face = $voxel.faces.$faceName
                    if ($null -ne $face) {
                        $uv = @($Part.faces.$faceName.uv)
                        $u = Slice-Range ([double]$uv[0]) ([double]$uv[2]) $z $zCount
                        $v = Slice-Range ([double]$uv[1]) ([double]$uv[3]) $y $yCount
                        $face.uv = @($u[0], $v[0], $u[1], $v[1])
                    }
                }
                foreach ($faceName in @('up', 'down')) {
                    $face = $voxel.faces.$faceName
                    if ($null -ne $face) {
                        $uv = @($Part.faces.$faceName.uv)
                        $u = Slice-Range ([double]$uv[0]) ([double]$uv[2]) $x $xCount
                        $v = Slice-Range ([double]$uv[1]) ([double]$uv[3]) $z $zCount
                        $face.uv = @($u[0], $v[0], $u[1], $v[1])
                    }
                }

                $gridX = Get-GridCoordinate $x $xCount
                $gridY = Get-GridCoordinate $y $yCount
                $gridZ = Get-GridCoordinate $z $zCount
                foreach ($faceProperty in $voxel.faces.PSObject.Properties) {
                    Set-FaceTint $faceProperty.Value (Get-FaceTint $Slot $faceProperty.Name $gridX $gridY $gridZ)
                }
                $result += $voxel
            }
        }
    }
    return $result
}

function New-Model([int[]]$Variants, [bool]$Outline = $false) {
    $wood = Deep-Copy $source.elements[0]
    foreach ($faceName in @('north', 'east', 'south', 'west', 'up', 'down')) {
        $wood.faces.$faceName.texture = '#stick'
    }
    $elements = @($wood)
    $selectedParts = @{}
    for ($slot = 1; $slot -le $Variants.Count; $slot++) {
        if ($Variants[$slot - 1] -gt 0) {
            $variant = $Variants[$slot - 1]
            $selectedParts[$slot] = Read-Part $slot $variant
        }
    }

    # Sub-pixel gaps between adjacent foods create false internal GUI outlines.
    # Close only gaps of at most one model pixel; larger authored gaps remain visible.
    foreach ($slot in 1..2) {
        if (!$selectedParts.ContainsKey($slot) -or !$selectedParts.ContainsKey($slot + 1)) { continue }
        $first = $selectedParts[$slot]
        $second = $selectedParts[$slot + 1]
        $gap = [double]$second.from[2] - [double]$first.to[2]
        if ($gap -gt 0 -and $gap -le 1.0) {
            $join = ([double]$first.to[2] + [double]$second.from[2]) / 2.0
            $first.to[2] = $join
            $second.from[2] = $join
        }
    }

    for ($slot = 1; $slot -le $Variants.Count; $slot++) {
        if ($selectedParts.ContainsKey($slot)) {
            $variant = $Variants[$slot - 1]
            $elements += @(New-TintedElements $selectedParts[$slot] $slot $variant)
        }
    }
    if ($Outline) {
        foreach ($element in $elements) {
            $element | Add-Member -NotePropertyName shade -NotePropertyValue $false -Force
            foreach ($face in $element.faces.PSObject.Properties.Value) {
                Set-FaceTint $face 384
            }
        }
    }
    return [ordered]@{
        ambientocclusion = $false
        gui_light = 'front'
        textures = [ordered]@{
            stick = 'kaleidoscope_grilling:item/secret_skewer_stick'
            food = if ($Outline) { 'kaleidoscope_grilling:item/skewer_outline_white' } else { 'minecraft:block/white_concrete' }
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
$outlineOverrides = @()
foreach ($variants in $states) {
    $code = $variants[0] * 16 + $variants[1] * 4 + $variants[2]
    Write-Json (Join-Path $stateDirectory "state_${code}.json") (New-Model $variants)
    Write-Json (Join-Path $stateDirectory "outline_state_${code}.json") (New-Model $variants $true)
    $overrides += [ordered]@{
        predicate = [ordered]@{ 'kaleidoscope_grilling:skewer_state' = $code / 64.0 }
        model = "kaleidoscope_grilling:item/skewer_states/state_${code}"
    }
    $outlineOverrides += [ordered]@{
        predicate = [ordered]@{
            'kaleidoscope_grilling:skewer_state' = $code / 64.0
            'kaleidoscope_grilling:skewer_outline' = 1.0
        }
        model = "kaleidoscope_grilling:item/skewer_states/outline_state_${code}"
    }
}

$overrides = $overrides | Sort-Object { [double]$_.predicate.'kaleidoscope_grilling:skewer_state' }
$outlineOverrides = $outlineOverrides | Sort-Object { [double]$_.predicate.'kaleidoscope_grilling:skewer_state' }
$baseModel = New-Model @()
$baseModel.overrides = @($overrides) + @($outlineOverrides)
$itemModelDirectory = Split-Path $stateDirectory -Parent
Write-Json (Join-Path $itemModelDirectory 'unfinished_skewer.json') $baseModel
Write-Json (Join-Path $itemModelDirectory 'secret_skewer.json') $baseModel

$sourceTexture = Join-Path $SourceDirectory 'texture1.png'
$targetTexture = Join-Path $ProjectDirectory 'common\src\main\resources\assets\kaleidoscope_grilling\textures\item\secret_skewer_stick.png'
Copy-Item -LiteralPath $sourceTexture -Destination $targetTexture -Force

Write-Output "Generated $($states.Count) runtime skewer state models from the 9 supplied geometries"
