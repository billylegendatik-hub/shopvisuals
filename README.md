# ShopVisuals

* `mod/`    - the Fabric 1.21.1 mod (HUD: watermark, module list, keystrokes; GUI on Right Shift)
* `loader/` - the installer app (Swing) that installs Fabric, Fabric API and the mod into .minecraft

## Getting the .exe (no local tools needed)

1. Create an empty GitHub repository and upload/push the contents of this folder (including `.github/`).
2. Open the repo -> **Actions** -> "build-loader-exe" (runs on every push, or press "Run workflow").
3. When it is green, open the run and download the artifact **ShopVisualsLoader-exe**. It contains:
   * `ShopVisualsLoader-1.0.exe` - single-file setup (installs the loader, adds a shortcut)
   * `ShopVisualsLoader-portable.zip` - unzip and run `ShopVisualsLoader/ShopVisualsLoader.exe`

Java is bundled in both, players do not need to install it. The exe is unsigned, so Windows SmartScreen may warn
until you sign it with a code-signing certificate.
