param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDirectory
)

$ErrorActionPreference = 'Stop'
$sourcePath = Join-Path $SourceDirectory 'model1.json'
$source = Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8 | ConvertFrom-Json
$wood = $source.elements[0]
$display = $source.display
$textures = $source.textures

function New-FoodElement {
    param(
        [string]$Name,
        [double[]]$From,
        [double[]]$To,
        [double]$Angle,
        [double[]]$Origin,
        [hashtable]$Uv
    )

    [ordered]@{
        name = $Name
        from = $From
        to = $To
        rotation = [ordered]@{ angle = $Angle; axis = 'y'; origin = $Origin }
        color = 4
        faces = [ordered]@{
            north = [ordered]@{ uv = $Uv.north; texture = '#missing' }
            east  = [ordered]@{ uv = $Uv.east;  texture = '#missing' }
            south = [ordered]@{ uv = $Uv.south; texture = '#missing' }
            west  = [ordered]@{ uv = $Uv.west;  texture = '#missing' }
            up    = [ordered]@{ uv = $Uv.up;    texture = '#missing' }
            down  = [ordered]@{ uv = $Uv.down;  texture = '#missing' }
        }
    }
}

function Copy-Element {
    param($Element, [string]$Name)
    $copy = $Element | ConvertTo-Json -Depth 30 | ConvertFrom-Json
    $copy.name = $Name
    return $copy
}

function New-UvSet {
    param([double]$Width, [double]$Height, [double]$Depth)
    @{
        north = @(0, 0, $Width, $Height)
        east = @(0, 0, $Depth, $Height)
        south = @(0, 0, $Width, $Height)
        west = @(0, 0, $Depth, $Height)
        up = @(0, 0, $Width, $Depth)
        down = @(0, 0, $Width, $Depth)
    }
}

$parts = @{
    '1-1' = Copy-Element $source.elements[1] 'food1_model1'
    '2-1' = Copy-Element $source.elements[2] 'food2_model1'
    '3-1' = Copy-Element $source.elements[3] 'food3_model1'

    # food1 is closest to the handle. The alternatives stay inside its original 2.5-5.5 Z range.
    '1-2' = New-FoodElement 'food1_model2' @(6.0, 0.0, 2.75) @(9.5, 2.25, 5.25) 22.5 @(7.75, 1.125, 4.0) (New-UvSet 3.5 2.25 2.5)
    '1-3' = New-FoodElement 'food1_model3' @(6.75, -0.25, 2.5) @(9.25, 2.5, 5.5) -22.5 @(8.0, 1.125, 4.0) (New-UvSet 2.5 2.75 3.0)

    # food2 remains visually offset from food1 and food3, matching the original skewed middle piece.
    '2-2' = New-FoodElement 'food2_model2' @(6.25, -0.25, 5.5) @(9.75, 2.25, 8.0) 22.5 @(8.0, 1.0, 6.75) (New-UvSet 3.5 2.5 2.5)
    '2-3' = New-FoodElement 'food2_model3' @(6.75, 0.0, 5.25) @(9.25, 2.75, 8.25) 0 @(8.0, 1.375, 6.75) (New-UvSet 2.5 2.75 3.0)

    # food3 stays clear of the pointed end of the wooden skewer at Z 12.5.
    '3-2' = New-FoodElement 'food3_model2' @(6.0, 0.0, 8.75) @(9.5, 2.25, 11.5) 22.5 @(7.75, 1.125, 10.125) (New-UvSet 3.5 2.25 2.75)
    '3-3' = New-FoodElement 'food3_model3' @(6.75, -0.25, 8.5) @(9.25, 2.5, 11.75) -22.5 @(8.0, 1.125, 10.125) (New-UvSet 2.5 2.75 3.25)
}

function New-ModelDocument {
    param([object[]]$FoodElements)
    $elements = @($wood) + $FoodElements
    [ordered]@{
        format_version = $source.format_version
        credit = $source.credit
        textures = $textures
        elements = $elements
        display = $display
        groups = @(
            0,
            [ordered]@{
                name = 'food'
                origin = @(8, 1, 7)
                scope = 0
                color = 0
                children = @(1..$FoodElements.Count)
            }
        )
    }
}

function Write-Json {
    param([string]$Path, $Value)
    $json = $Value | ConvertTo-Json -Depth 30
    [System.IO.File]::WriteAllText($Path, $json, [System.Text.UTF8Encoding]::new($false))
}

$partsDirectory = Join-Path $SourceDirectory 'food_parts'
$combinedDirectory = Join-Path $SourceDirectory 'generated_27'
[System.IO.Directory]::CreateDirectory($partsDirectory) | Out-Null
[System.IO.Directory]::CreateDirectory($combinedDirectory) | Out-Null

foreach ($slot in 1..3) {
    foreach ($variant in 2..3) {
        $model = New-ModelDocument @($parts["$slot-$variant"])
        Write-Json (Join-Path $partsDirectory "food${slot}_model${variant}.json") $model
    }
}

foreach ($food1 in 1..3) {
    foreach ($food2 in 1..3) {
        foreach ($food3 in 1..3) {
            $model = New-ModelDocument @($parts["1-$food1"], $parts["2-$food2"], $parts["3-$food3"])
            Write-Json (Join-Path $combinedDirectory "model_${food1}${food2}${food3}.json") $model
        }
    }
}

Write-Output "Generated 6 food-part previews and 27 combined models in $SourceDirectory"
