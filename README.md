# AutoAim

Client-side Fabric mod for Minecraft 1.21.1. When a hostile mob is within
3 blocks of you, your view locks onto its hitbox.

## Keybinds (Controls > AutoAim)
- Toggle AutoAim (default: R)
- AutoAim einschalten (unbound)
- AutoAim ausschalten (unbound)

## Build
1. Install JDK 21.
2. Copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the Fabric
   template (fabricmc.net/develop/template) into this folder.
3. Run `./gradlew build` (Windows: `gradlew.bat build`).
4. The mod is `build/libs/autoaim-1.0.0.jar` (NOT the `-sources` jar).

## Notes
- Client-only. Requires Fabric Loader and Fabric API.
- Many multiplayer servers prohibit aim assistance. Use at your own risk.
