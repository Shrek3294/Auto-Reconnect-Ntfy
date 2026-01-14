
Add-Type -AssemblyName System.Drawing
$path = "src/main/resources/assets/autoreconnect/icon.png"
$newPath = "src/main/resources/assets/autoreconnect/icon_fixed.png"

try {
    $img = [System.Drawing.Image]::FromFile($path)
    Write-Host "Original Size: $($img.Width)x$($img.Height)"
    
    $resized = new-object System.Drawing.Bitmap($img, 128, 128)
    $resized.Save($newPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $img.Dispose()
    $resized.Dispose()
    
    Write-Host "Resized to 128x128"
} catch {
    Write-Error $_
}
