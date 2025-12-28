$WshShell = New-Object -comObject WScript.Shell
$DesktopPath = [System.Environment]::GetFolderPath('Desktop')
$ShortcutFile = "$DesktopPath\Tic Tac Toe Server.lnk"
$Shortcut = $WshShell.CreateShortcut($ShortcutFile)
$Shortcut.TargetPath = "$PWD\target\Tic Tac Toe server.jar"
$Shortcut.IconLocation = "$PWD\src\main\resources\com\mycompany\server\assets\images\serverIcon.ico"
$Shortcut.WindowStyle = 1
$Shortcut.Save()
Write-Host "Shortcut created at $ShortcutFile"
