# 分析固定烤串模型结构，输出重构所需的全部信息
# 用法: pwsh -File analyze_skewers.ps1
$ErrorActionPreference = 'Stop'
$dir = "D:\JavaGradle\2026_BreezeObject\KaleidoscopeGrilling\common\src\main\resources\assets\kaleidoscope_grilling\models\item\fixed_skewers"

# 所有主串前缀（xxx_skewer.json 的 xxx）
$groups = Get-ChildItem -File $dir -Filter "*_skewer.json" | ForEach-Object {
  $_.Name -replace '_skewer\.json$',''
} | Sort-Object -Unique

Write-Host "=== 主串: $($groups.Count) ==="
$groups -join ', '

# 对每组：主模型 + 变体清单
foreach ($g in $groups) {
  $files = Get-ChildItem -File $dir -Filter "${g}_skewer*.json" | Sort-Object Name
  $variants = @()
  foreach ($f in $files) {
    $j = Get-Content $f.FullName -Raw | ConvertFrom-Json
    # 提取纹理键（非 particle）
    $keys = @($j.textures.PSObject.Properties | Where-Object { $_.Name -ne 'particle' } | ForEach-Object {
      $v = $_.Value
      if ($v -match '^kaleidoscope_grilling:item/fixed_skewers/(.+)$') { "$($_.Name)=$($matches[1])" }
      else { "$($_.Name)=$v" }
    })
    $variant = $f.Name.Replace("${g}_skewer_",'').Replace('.json','')
    if ($variant -eq '') { $variant = 'MAIN' }
    $variants += ("{0}:{1}" -f $variant, ($keys -join '|'))
  }
  Write-Host ""
  Write-Host "=== $g ==="
  $variants | ForEach-Object { Write-Host "  $_" }
}
