# 鍥哄畾鐑や覆妯″瀷閲嶆瀯鐢熸垚鍣?# 杈撳嚭: 姣忎釜涓讳覆涓€涓瓙鏂囦欢澶癸紝鐖舵ā鍨?+ 鐘舵€佸皬鏂囦欢
$ErrorActionPreference = 'Stop'
$src = "D:\JavaGradle\2026_BreezeObject\KaleidoscopeGrilling\common\src\main\resources\assets\kaleidoscope_grilling\models\item\fixed_skewers"
$out = "D:\JavaGradle\2026_BreezeObject\KaleidoscopeGrilling\build\skewer_refactor"

if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Force -Path $out | Out-Null

$groups = @(Get-ChildItem -File $src -Filter "*_skewer.json" | ForEach-Object { $_.Name -replace '_skewer\.json$','' } | Sort-Object -Unique)

function Get-TexVar($tex, $key) {
  $v = $tex.$key
  if ($null -eq $v) { return $null }
  $m = [regex]::Match($v, '^kaleidoscope_grilling:item/fixed_skewers/(.+)$')
  if ($m.Success) { return $m.Groups[1].Value }
  $m2 = [regex]::Match($v, '^#(.+)$')
  if ($m2.Success) { return "#$($m2.Groups[1].Value)" }
  return $v
}

foreach ($g in $groups) {
  Write-Host "=== $g ==="
  $files = @(Get-ChildItem -File $src -Filter "${g}_skewer*.json" | Sort-Object Name)
  # 涓绘ā鍨嬶紙鍑犱綍鏈€鍏ㄧ殑 = 鏃犲悗缂€涓绘枃浠讹級
  $main = Get-Content (Join-Path $src "${g}_skewer.json") -Raw | ConvertFrom-Json
  $baseJson = @{
    parent = "kaleidoscope_grilling:item/fixed_skewers/${g}/${g}_skewer_geo"
  }
  # 鎵惧嚭鎵€鏈夐鐗╃汗鐞嗛敭锛堥潪 stick锛?  $stickKey = $null
  $foodKeys = New-Object System.Collections.Generic.List[string]
  $texProps = @($main.textures.PSObject.Properties)
  foreach ($prop in $texProps) {
    if ($prop.Name -eq 'particle') { continue }
    $val = Get-TexVar $main.textures $prop.Name
    if ($val -eq 'skewer_stick') { $stickKey = $prop.Name } else { $foodKeys.Add($prop.Name) | Out-Null }
  }
  $foodKeysArr = $foodKeys.ToArray()
  Write-Host ("  stickKey={0} foodKeys=[{1}]" -f $stickKey, ($foodKeysArr -join ','))
  $gdir = Join-Path $out $g
  New-Item -ItemType Directory -Force -Path $gdir | Out-Null

  # 1) 鍑犱綍鐖舵ā鍨嬶紙浠庝富妯″瀷澶嶅埗 geometry+display锛屽幓鎺?textures.food 鍏蜂綋鍊? 淇濈暀鍘熷涓轰富锛?  #    鍑犱綍鐖跺彧鍚?elements/display/gui_light/ambientocclusion + particle
  $geo = @{}
  if ($main.PSObject.Properties.Name -contains 'ambientocclusion') { $geo['ambientocclusion'] = $main.ambientocclusion }
  $geo['textures'] = @{ particle = $main.textures.particle }
  $geo['elements'] = $main.elements
  if ($main.PSObject.Properties.Name -contains 'gui_light') { $geo['gui_light'] = $main.gui_light }
  if ($main.PSObject.Properties.Name -contains 'display') { $geo['display'] = $main.display }
  $geo | ConvertTo-Json -Depth 30 | Set-Content -Path (Join-Path $gdir "${g}_skewer_geo.json") -Encoding UTF8

  # 2) 鐘舵€佸皬鏂囦欢: raw/cooked/burnt/stage_1..3 鈫?parent geo + food 璐村浘
  $stateMap = @{ raw = "${g}_skewer_raw"; cooked = "${g}_skewer_cooked"; burnt = "${g}_skewer_burnt";
                 stage_1 = "${g}_skewer_stage_1"; stage_2 = "${g}_skewer_stage_2"; stage_3 = "${g}_skewer_stage_3" }
  foreach ($st in $stateMap.Keys) {
    $suffix = if ($st -eq 'raw') { '' } else { "_$st" }
    $fileName = if ($st -eq 'raw') { "${g}_skewer.json" } else { "${g}_skewer_${st}.json" }
    $child = @{ parent = "kaleidoscope_grilling:item/fixed_skewers/${g}/${g}_skewer_geo"; textures = @{} }
    foreach ($fk in $foodKeysArr) {
      $texName = $stateMap[$st]
      if ($g -eq 'potato_slice') {
        # potato_slice 鐗规畩: food_1/food_2 鍦?stage 鐢?_1_/_2_ 鍚庣紑
        if ($st -like 'stage_*') {
          $n = $st -replace 'stage_',''
          if ($fk -eq 'food_1') { $texName = "${g}_skewer_1_stage_$n" }
          else { $texName = "${g}_skewer_2_stage_$n" }
        } elseif ($st -eq 'raw') {
          $texName = if ($fk -eq 'food_1') { "${g}_skewer_raw_1" } else { "${g}_skewer_raw_2" }
        }
      }
      $child.textures[$fk] = "kaleidoscope_grilling:item/fixed_skewers/$texName"
    }
    $child | ConvertTo-Json -Depth 10 | Set-Content -Path (Join-Path $gdir $fileName) -Encoding UTF8
    Write-Host ("  {0}" -f $fileName)
  }

  # 3) bite 妗ｄ綅鍑犱綍鐖舵ā鍨?+ bite/raw_bite 灏忔枃浠?  #    鎸?bite_N 鐨?elements 涓庝富妯″瀷瀵规瘮锛屾壘鍑烘瘡涓?bite 妗ｄ綅缂哄皯鐨勫厓绱?  $bites = @(Get-ChildItem -File $src -Filter "${g}_skewer_bite*.json" | Where-Object { $_.Name -notmatch 'raw' } | Sort-Object Name)
  foreach ($b in $bites) {
    $bj = Get-Content $b.FullName -Raw | ConvertFrom-Json
    $bName = $b.Name.Replace("${g}_skewer_",'').Replace('.json','')  # e.g. bite_1
    $geoFile = "${g}_skewer_${bName}_geo.json"
    $bg = @{}
    if ($bj.PSObject.Properties.Name -contains 'ambientocclusion') { $bg['ambientocclusion'] = $bj.ambientocclusion }
    $bg['textures'] = @{ particle = $bj.textures.particle }
    $bg['elements'] = $bj.elements
    if ($bj.PSObject.Properties.Name -contains 'gui_light') { $bg['gui_light'] = $bj.gui_light }
    if ($bj.PSObject.Properties.Name -contains 'display') { $bg['display'] = $bj.display }
    $bg | ConvertTo-Json -Depth 30 | Set-Content -Path (Join-Path $gdir $geoFile) -Encoding UTF8
    # bite_N: parent geo + cooked; raw_bite_N: parent geo + raw
    $child = @{ parent = "kaleidoscope_grilling:item/fixed_skewers/${g}/${geoFile.Replace('.json','')}"; textures = @{} }
    foreach ($fk in $foodKeysArr) {
      $texName = "${g}_skewer_cooked"
      if ($g -eq 'potato_slice') { $texName = "${g}_skewer_cooked" }
      $child.textures[$fk] = "kaleidoscope_grilling:item/fixed_skewers/$texName"
    }
    $child | ConvertTo-Json -Depth 10 | Set-Content -Path (Join-Path $gdir "${g}_skewer_${bName}.json") -Encoding UTF8
    $childR = @{ parent = "kaleidoscope_grilling:item/fixed_skewers/${g}/${geoFile.Replace('.json','')}"; textures = @{} }
    foreach ($fk in $foodKeysArr) {
      $texName = "${g}_skewer_raw"
      if ($g -eq 'potato_slice') { $texName = if ($fk -eq 'food_1') { "${g}_skewer_raw_1" } else { "${g}_skewer_raw_2" } }
      $childR.textures[$fk] = "kaleidoscope_grilling:item/fixed_skewers/$texName"
    }
    $childR | ConvertTo-Json -Depth 10 | Set-Content -Path (Join-Path $gdir "${g}_skewer_raw_${bName}.json") -Encoding UTF8
    Write-Host ("  {0} + raw_{0}" -f $bName)
  }

  # 4) piece 绯诲垪: 鍑犱綍鐖?+ cooked/raw 灏忔枃浠?  $pieces = @(Get-ChildItem -File $src -Filter "${g}_skewer_piece*.json" | Where-Object { $_.Name -notmatch 'raw' } | Sort-Object Name)
  foreach ($p in $pieces) {
    $pj = Get-Content $p.FullName -Raw | ConvertFrom-Json
    $pName = $p.Name.Replace("${g}_skewer_",'').Replace('.json','')  # piece_1 / piece_3
    $geoFile = "${g}_skewer_${pName}_geo.json"
    $pg = @{}
    if ($pj.PSObject.Properties.Name -contains 'ambientocclusion') { $pg['ambientocclusion'] = $pj.ambientocclusion }
    $pg['textures'] = @{ particle = $pj.textures.particle }
    $pg['elements'] = $pj.elements
    if ($pj.PSObject.Properties.Name -contains 'gui_light') { $pg['gui_light'] = $pj.gui_light }
    if ($pj.PSObject.Properties.Name -contains 'display') { $pg['display'] = $pj.display }
    $pg | ConvertTo-Json -Depth 30 | Set-Content -Path (Join-Path $gdir $geoFile) -Encoding UTF8
    $child = @{ parent = "kaleidoscope_grilling:item/fixed_skewers/${g}/${geoFile.Replace('.json','')}"; textures = @{} }
    foreach ($fk in $foodKeysArr) {
      $child.textures[$fk] = "kaleidoscope_grilling:item/fixed_skewers/${g}_skewer_cooked"
    }
    $child | ConvertTo-Json -Depth 10 | Set-Content -Path (Join-Path $gdir "${g}_skewer_${pName}.json") -Encoding UTF8
    $childR = @{ parent = "kaleidoscope_grilling:item/fixed_skewers/${g}/${geoFile.Replace('.json','')}"; textures = @{} }
    foreach ($fk in $foodKeysArr) {
      $childR.textures[$fk] = "kaleidoscope_grilling:item/fixed_skewers/${g}_skewer_raw"
    }
    $childR | ConvertTo-Json -Depth 10 | Set-Content -Path (Join-Path $gdir "${g}_skewer_raw_${pName}.json") -Encoding UTF8
    Write-Host ("  {0} + raw_{0}" -f $pName)
  }
}
Write-Host ""
Write-Host "DONE -> $out"
