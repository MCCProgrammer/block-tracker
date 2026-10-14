# 🧭 Block Tracker

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-1f2328?style=flat-square)
![Fabric](https://img.shields.io/badge/mod%20loader-Fabric-1f2328?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-1f2328?style=flat-square&logo=openjdk&logoColor=white)

A client-side **Fabric mod** for Minecraft 1.21.11 that finds valuable blocks around you and shows them on a HUD and a radar.

## Features

- **Block finder:** every second it scans the loaded chunks around you for **chests**, **shulker boxes**, **spawners** and **enchanting tables**.
- **HUD:** for each type, how many there are and the coordinates of the nearest one, in the top-right corner.
- **Block list:** every block found, with its coordinates and distance.
- **Radar:** a minimap with the blocks and the players around you, with a zoom toggle.
- **Friends and enemies:** mark other players as friend, enemy or neutral. They show up in green or red on the radar.
- **Coordinate HUD:** your current position on screen.
- Settings and player marks are saved in `config/blocktracker_state.json`.

The settings screen also has **Glow**, **Waypoints** and **Pathfinding** toggles. They are not implemented yet.

> Use it in singleplayer or on servers that allow it. Most multiplayer servers treat block and player finders as cheats.

## Keys

| Key | Action |
| --- | --- |
| <kbd>O</kbd> | Settings |
| <kbd>P</kbd> | Players (friends and enemies) |
| <kbd>K</kbd> | Refresh the radar and blocks |
| <kbd>Numpad +</kbd> | Toggle radar zoom |

You can change them in *Options → Controls → Key Binds → Miscellaneous*.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11 and the [Fabric API](https://modrinth.com/mod/fabric-api) mod.
2. Build the mod (below) and copy `build/libs/block-tracker-1.0.0.jar` into your `.minecraft/mods` folder.

## Building

Needs **JDK 21**.

```bash
./gradlew build
```

The jar is written to `build/libs/`. Every push also builds it on GitHub Actions and attaches it as an artifact.

## License

[CC0 1.0](LICENSE) (public domain).
