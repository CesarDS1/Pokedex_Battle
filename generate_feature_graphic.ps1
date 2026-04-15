Add-Type -AssemblyName System.Drawing

# Unicode char helpers (avoid PS encoding issues with special chars)
$nyo = [char]0x00F1  # n~
$eac = [char]0x00E9  # e acute
$Eac = [char]0x00C9  # E acute
$Iac = [char]0x00ED  # i acute

$W = 1024
$H = 500
$outPath = "C:\Users\ingce\AndroidStudioProjects\Pokedex\feature_graphic.png"

# ── Bitmap & Graphics ─────────────────────────────────────────────────────────
$bmp = New-Object System.Drawing.Bitmap($W, $H, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic

# ── Background gradient (dark navy) ──────────────────────────────────────────
$bgBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.Point(0, 0)),
    (New-Object System.Drawing.Point(0, $H)),
    [System.Drawing.Color]::FromArgb(255, 12, 12, 32),
    [System.Drawing.Color]::FromArgb(255, 20, 20, 52)
)
$g.FillRectangle($bgBrush, 0, 0, $W, $H)
$bgBrush.Dispose()

# ── Red accent bars top / bottom ──────────────────────────────────────────────
$RED      = [System.Drawing.Color]::FromArgb(255, 238, 21, 21)
$redBrush = New-Object System.Drawing.SolidBrush($RED)
$g.FillRectangle($redBrush, 0, 0,      $W, 8)
$g.FillRectangle($redBrush, 0, $H - 8, $W, 8)

# ── Decorative Pokeball (left, translucent) ───────────────────────────────────
$bx = 185; $by = 252; $br = 162

$g.FillEllipse(
    (New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(18, 255, 255, 255))),
    $bx-$br, $by-$br, $br*2, $br*2)

# Red top half
$g.SetClip([System.Drawing.RectangleF]::new($bx - $br, $by - $br, $br*2, $br))
$g.FillEllipse(
    (New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(32, 238, 21, 21))),
    $bx-$br, $by-$br, $br*2, $br*2)
$g.ResetClip()

# Band
$g.FillRectangle(
    (New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(55, 5, 5, 15))),
    $bx - $br, $by - 11, $br*2, 22)

# Slash on band
$slashPen           = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(55, 255, 255, 255), 3)
$slashPen.StartCap  = [System.Drawing.Drawing2D.LineCap]::Round
$slashPen.EndCap    = [System.Drawing.Drawing2D.LineCap]::Round
$g.DrawLine($slashPen, ($bx - 7), ($by + 9), ($bx + 7), ($by - 9))
$slashPen.Dispose()

# Button outer + inner
$g.FillEllipse(
    (New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(55, 5, 5, 15))),
    $bx - 18, $by - 18, 36, 36)
$g.FillEllipse(
    (New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(30, 255, 255, 255))),
    $bx - 11, $by - 11, 22, 22)

# Outline circle
$outPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(38, 255, 255, 255), 2.5)
$g.DrawEllipse($outPen, $bx-$br, $by-$br, $br*2, $br*2)
$outPen.Dispose()

# Band lines
$bandPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(28, 255, 255, 255), 1)
$g.DrawLine($bandPen, $bx-$br, $by-11, $bx+$br, $by-11)
$g.DrawLine($bandPen, $bx-$br, $by+11, $bx+$br, $by+11)
$bandPen.Dispose()

# ── Vertical divider ──────────────────────────────────────────────────────────
$divPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(28, 255, 255, 255), 1)
$g.DrawLine($divPen, 372, 42, 372, $H - 42)
$divPen.Dispose()

# ── Fonts ─────────────────────────────────────────────────────────────────────
function Get-Font($families, $size, $style = 'Regular') {
    foreach ($name in $families) {
        try {
            $f = New-Object System.Drawing.Font($name, $size, [System.Drawing.FontStyle]::$style)
            if ($f.Name -eq $name -or $f.Name.StartsWith($name)) { return $f }
            $f.Dispose()
        } catch {}
    }
    return New-Object System.Drawing.Font('Microsoft Sans Serif', $size, [System.Drawing.FontStyle]::$style)
}

$pref       = @('Segoe UI', 'Arial', 'Calibri', 'Tahoma', 'Verdana')
$fontTitle  = Get-Font $pref 58 'Bold'
$fontSub    = Get-Font $pref 19 'Regular'
$fontCard   = Get-Font $pref 15 'Bold'
$fontDesc   = Get-Font $pref 12 'Regular'
$fontTag    = Get-Font $pref 11 'Regular'

# ── Title area ────────────────────────────────────────────────────────────────
$tx = 395; $ty = 82

# Red left accent bar
$g.FillRectangle($redBrush, $tx, $ty, 6, 108)

# Shadow
$shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(90, 0, 0, 0))
$g.DrawString("DEX BATTLE VG", $fontTitle, $shadowBrush, [float]($tx+24), [float]($ty+4))
$shadowBrush.Dispose()

# Measure "DEX" width to split title colors
$sf   = [System.Drawing.StringFormat]::GenericTypographic
$dexW = $g.MeasureString("DEX", $fontTitle, 9999, $sf).Width

$whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
$grayBrush  = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(175, 175, 205))

$g.DrawString("DEX",        $fontTitle, $redBrush,   [float]($tx+20), [float]$ty)
$g.DrawString(" BATTLE VG", $fontTitle, $whiteBrush, [float]($tx+20+$dexW), [float]$ty)

# Subtitle (unicode chars via variables)
$subtitle = "Tu compa${nyo}ero definitivo para Pokes"
$g.DrawString($subtitle, $fontSub, $grayBrush, [float]($tx+20), [float]($ty+100))

# ── Feature cards (2 rows x 3 cols) ──────────────────────────────────────────
$features = @(
    [pscustomobject]@{ Name="DEX";               Desc="Explora +1000 Pokes";        R=220; G=68;  B=68  },
    [pscustomobject]@{ Name="TIPOS";             Desc="Tabla de debilidades";       R=255; G=193; B=7   },
    [pscustomobject]@{ Name="ESTAD${Iac}STICAS"; Desc="Stats y movimientos";        R=33;  G=150; B=243 },
    [pscustomobject]@{ Name="EVOLUCIONES";       Desc="Cadenas evolutivas";         R=76;  G=175; B=80  },
    [pscustomobject]@{ Name="FAVORITOS";         Desc="Guarda tus favoritos";       R=233; G=30;  B=99  },
    [pscustomobject]@{ Name="EQUIPOS";           Desc="Construye tu equipo";        R=255; G=152; B=0   }
)

$cw = 178; $ch = 78; $gapX = 10; $gapY = 10
$sx = $tx + 20; $sy = 218

for ($i = 0; $i -lt $features.Count; $i++) {
    $f   = $features[$i]
    $row = [Math]::Floor($i / 3)
    $col = $i % 3
    $cx  = $sx + $col * ($cw + $gapX)
    $cy  = $sy + $row * ($ch + $gapY)

    # Card fill
    $cardB = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 38, 38, 75))
    $g.FillRectangle($cardB, $cx, $cy, $cw, $ch)
    $cardB.Dispose()

    # Left color accent strip
    $accB = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, $f.R, $f.G, $f.B))
    $g.FillRectangle($accB, $cx, $cy, 5, $ch)
    $accB.Dispose()

    # Feature name
    $nameB = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $g.DrawString($f.Name, $fontCard, $nameB, [float]($cx+14), [float]($cy+12))
    $nameB.Dispose()

    # Description
    $descB = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(155, 155, 195))
    $g.DrawString($f.Desc, $fontDesc, $descB, [float]($cx+14), [float]($cy+46))
    $descB.Dispose()
}

# ── Cleanup & save ────────────────────────────────────────────────────────────
$redBrush.Dispose(); $whiteBrush.Dispose(); $grayBrush.Dispose()
$fontTitle.Dispose(); $fontSub.Dispose(); $fontCard.Dispose()
$fontDesc.Dispose(); $fontTag.Dispose()
$g.Dispose()

$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

$info = Get-Item $outPath
Write-Host ("Listo. Dimensiones: {0}x{1} px | Tamano: {2:N0} KB" -f 1024, 500, ($info.Length / 1KB))
