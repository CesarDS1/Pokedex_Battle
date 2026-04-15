Add-Type -AssemblyName System.Drawing

$W = 512; $H = 512
$outPath = "C:\Users\ingce\AndroidStudioProjects\Pokedex\store_icon.png"

# ── Bitmap & Graphics ─────────────────────────────────────────────────────────
$bmp = New-Object System.Drawing.Bitmap($W, $H, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAlias

# ── Dark navy background ──────────────────────────────────────────────────────
$g.FillRectangle(
    (New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 12, 12, 32))),
    0, 0, $W, $H)

# ── Pokeball ──────────────────────────────────────────────────────────────────
$cx = 256; $cy = 256; $r = 220
$DARK   = [System.Drawing.Color]::FromArgb(255, 26, 26, 26)
$RED    = [System.Drawing.Color]::FromArgb(255, 238, 21, 21)
$WHITE  = [System.Drawing.Color]::White

# White circle body
$g.FillEllipse((New-Object System.Drawing.SolidBrush($WHITE)), $cx-$r, $cy-$r, $r*2, $r*2)

# Red top half (clip to upper semicircle)
$g.SetClip([System.Drawing.RectangleF]::new($cx - $r, $cy - $r, $r*2, $r))
$g.FillEllipse((New-Object System.Drawing.SolidBrush($RED)), $cx-$r, $cy-$r, $r*2, $r*2)
$g.ResetClip()

# Dark band (40 px tall, centered on cy)
$bandTop = $cy - 20
$g.FillRectangle((New-Object System.Drawing.SolidBrush($DARK)), $cx-$r, $bandTop, $r*2, 40)


# Slash "/" on band
$slashPen           = New-Object System.Drawing.Pen($WHITE, 7)
$slashPen.StartCap  = [System.Drawing.Drawing2D.LineCap]::Round
$slashPen.EndCap    = [System.Drawing.Drawing2D.LineCap]::Round
$g.DrawLine($slashPen, $cx - 20, $cy + 18, $cx + 20, $cy - 18)
$slashPen.Dispose()

# Dark outline
$outPen = New-Object System.Drawing.Pen($DARK, 6)
$g.DrawEllipse($outPen, $cx-$r, $cy-$r, $r*2, $r*2)
$outPen.Dispose()

# ── Fonts ─────────────────────────────────────────────────────────────────────
function Get-Font($names, $size, $style = 'Bold') {
    foreach ($n in $names) {
        try {
            $f = New-Object System.Drawing.Font($n, $size, [System.Drawing.FontStyle]::$style)
            if ($f.Name -eq $n) { return $f }
            $f.Dispose()
        } catch {}
    }
    return New-Object System.Drawing.Font('Microsoft Sans Serif', $size, [System.Drawing.FontStyle]::Bold)
}

$pref       = @('Segoe UI', 'Arial', 'Calibri', 'Tahoma', 'Verdana')
$fontDex    = Get-Font $pref 96
$fontBattle = Get-Font $pref 72

# Centered StringFormat
$sf = New-Object System.Drawing.StringFormat
$sf.Alignment     = [System.Drawing.StringAlignment]::Center
$sf.LineAlignment = [System.Drawing.StringAlignment]::Center

# ── "DEX" – white, centered in top red half ───────────────────────────────────
# Top half area: circle top ($cy-$r) to band top ($bandTop), with 10px margin
$topRect = [System.Drawing.RectangleF]::new(
    [float]($cx - $r + 10),
    [float]($cy - $r + 10),
    [float](($r - 10) * 2),
    [float]($r - 30))   # = bandTop - (cy-r) - 10px margin

$whiteBrush = New-Object System.Drawing.SolidBrush($WHITE)
$g.DrawString("DEX", $fontDex, $whiteBrush, $topRect, $sf)

# ── "BATTLE" – dark, centered in bottom white half ───────────────────────────
# Bottom half area: band bottom ($bandTop+40) to circle bottom ($cy+$r), with margin
$botRect = [System.Drawing.RectangleF]::new(
    [float]($cx - $r + 10),
    [float]($bandTop + 40 + 10),
    [float](($r - 10) * 2),
    [float]($r - 30))

$darkBrush = New-Object System.Drawing.SolidBrush($DARK)
$g.DrawString("BATTLE", $fontBattle, $darkBrush, $botRect, $sf)

# ── Cleanup & save ────────────────────────────────────────────────────────────
$whiteBrush.Dispose(); $darkBrush.Dispose()
$fontDex.Dispose(); $fontBattle.Dispose()
$sf.Dispose(); $g.Dispose()

$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

$info = Get-Item $outPath
Write-Host ("Guardado: $outPath")
Write-Host ("Dimensiones: 512x512 px | Tamano: {0:N0} KB" -f ($info.Length / 1KB))
