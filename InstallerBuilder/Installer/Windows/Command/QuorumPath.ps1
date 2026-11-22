# Adds a folder to the system PATH, or removes it. Quorum's Windows installer
# runs this to put <Quorum>\bin, where quorum.cmd is, on the PATH, and the
# uninstaller runs it to take it off again:
#
#   QuorumPath.ps1 -Action Add -Folder "C:\Program Files\Quorum\bin"
#   QuorumPath.ps1 -Action Remove -Folder "C:\Program Files\Quorum\bin"
#
# NSIS's own strings are cut off at 1024 characters, and a PATH is often
# longer, so the installer doesn't edit it itself. This reads and writes the
# registry value as it is: entries such as %SystemRoot% stay unexpanded, and
# the value stays REG_EXPAND_SZ. The installer then tells running programs
# that the environment changed (WM_SETTINGCHANGE), so new consoles see it.
param(
    [Parameter(Mandatory = $true)][ValidateSet("Add", "Remove")][string]$Action,
    [Parameter(Mandatory = $true)][string]$Folder
)

$ErrorActionPreference = "Stop"
$keyPath = "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"
$key = [Microsoft.Win32.Registry]::LocalMachine.OpenSubKey($keyPath, $true)
try {
    $path = $key.GetValue("Path", "", [Microsoft.Win32.RegistryValueOptions]::DoNotExpandEnvironmentNames)
    $folderName = $Folder.TrimEnd("\")

    # Every entry except this folder (in any letter case, with or without a
    # trailing backslash), and no empty ones.
    $entries = @($path -split ";" | Where-Object { $_ -ne "" -and $_.TrimEnd("\") -ine $folderName })
    if ($Action -eq "Add") {
        $entries += $folderName
    }

    $key.SetValue("Path", ($entries -join ";"), [Microsoft.Win32.RegistryValueKind]::ExpandString)
}
finally {
    $key.Close()
}
