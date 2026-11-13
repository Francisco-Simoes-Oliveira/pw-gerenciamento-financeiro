Add-Type -AssemblyName System.Drawing

$iconsDirectory = Join-Path (Resolve-Path (Join-Path $PSScriptRoot "..")) "public\icons"
[System.IO.Directory]::CreateDirectory($iconsDirectory) | Out-Null

function New-RoundedRectanglePath {
    param(
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height,
        [float]$Radius
    )

    $diameter = $Radius * 2
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function New-FinShareIcon {
    param(
        [Parameter(Mandatory = $true)][string]$FileName,
        [Parameter(Mandatory = $true)][int]$Size,
        [switch]$Maskable
    )

    $bitmap = [System.Drawing.Bitmap]::new($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.Color]::Transparent)

    $navyBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#1e3a8a"))
    if ($Maskable) {
        $graphics.FillRectangle($navyBrush, 0, 0, $Size, $Size)
        $contentScale = 0.78
        $contentOffset = $Size * 0.11
    } else {
        $backgroundPath = New-RoundedRectanglePath 0 0 $Size $Size ($Size * 0.22)
        $graphics.FillPath($navyBrush, $backgroundPath)
        $backgroundPath.Dispose()
        $contentScale = 1.0
        $contentOffset = 0
    }

    $scale = $Size / 128.0 * $contentScale
    $x = { param([double]$Value) $contentOffset + ($Value * $scale) }
    $y = { param([double]$Value) $contentOffset + ($Value * $scale) }
    $strokeWidth = [Math]::Max(2, 8 * $scale)

    $whitePen = [System.Drawing.Pen]::new([System.Drawing.Color]::White, $strokeWidth)
    $whitePen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $walletPath = New-RoundedRectanglePath (& $x 24) (& $y 34) (80 * $scale) (60 * $scale) (14 * $scale)
    $graphics.DrawPath($whitePen, $walletPath)

    $tealPen = [System.Drawing.Pen]::new([System.Drawing.ColorTranslator]::FromHtml("#5eead4"), $strokeWidth)
    $tealPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $tealPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $tealPen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $trend = [System.Drawing.PointF[]]@(
        [System.Drawing.PointF]::new((& $x 36), (& $y 76)),
        [System.Drawing.PointF]::new((& $x 53), (& $y 59)),
        [System.Drawing.PointF]::new((& $x 68), (& $y 69)),
        [System.Drawing.PointF]::new((& $x 92), (& $y 40))
    )
    $graphics.DrawLines($tealPen, $trend)
    $graphics.DrawLine($tealPen, (& $x 82), (& $y 40), (& $x 92), (& $y 39))
    $graphics.DrawLine($tealPen, (& $x 92), (& $y 39), (& $x 91), (& $y 50))

    $outputPath = Join-Path $iconsDirectory $FileName
    $bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)

    $tealPen.Dispose()
    $whitePen.Dispose()
    $walletPath.Dispose()
    $navyBrush.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

New-FinShareIcon -FileName "finshare-180.png" -Size 180
New-FinShareIcon -FileName "finshare-192.png" -Size 192
New-FinShareIcon -FileName "finshare-512.png" -Size 512
New-FinShareIcon -FileName "finshare-maskable-192.png" -Size 192 -Maskable
New-FinShareIcon -FileName "finshare-maskable-512.png" -Size 512 -Maskable

Write-Host "Ícones PWA gerados em $iconsDirectory"
