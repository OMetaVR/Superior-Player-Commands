# Superior Player Commands

A spiritual successor to the classic Single Player Commands mod from Minecraft Beta 1.1_02. This Fabric mod brings back those beloved utility commands with modern improvements for Minecraft 1.20.1.

## Features

### Movement Commands
| Command | Description |
|---------|-------------|
| `/jump` | Teleport to the block you're looking at (up to 256 blocks) |
| `/ascend` | Teleport to the next open space above you |
| `/descend` | Teleport to the next open space below you |

### Waypoint System
| Command | Description |
|---------|-------------|
| `/set <name>` | Save your current location as a waypoint |
| `/goto <name>` | Teleport to a saved waypoint (works cross-dimension) |
| `/rem <name>` | Remove a waypoint |

### Inventory Commands
| Command | Description |
|---------|-------------|
| `/heal` | Restore your health to full |
| `/repair` | Repair the item in your hand |
| `/repairall` | Repair all items in your inventory |
| `/duplicate` | Duplicate the item stack in your hand |
| `/destroy` | Destroy the item in your hand |
| `/replenish` | Refill consumable stacks |

### Power Tools
| Command | Description |
|---------|-------------|
| `/instamine` | Toggle instant block breaking |
| `/drops` | Toggle whether blocks drop items when broken |
| `/hands` | Show/disable tool hands mode |
| `/woodhands` | Mine as if holding wood tools |
| `/stonehands` | Mine as if holding stone tools |
| `/ironhands` | Mine as if holding iron tools |
| `/goldhands` | Mine as if holding gold tools |
| `/diamondhands` | Mine as if holding diamond tools |
| `/netheritehands` | Mine as if holding netherite tools |

### Key Bindings
| Command | Description |
|---------|-------------|
| `/bind` | List all key bindings |
| `/bind <key>` | Show what's bound to a key |
| `/bind <key> <command>` | Bind a key to execute a command |
| `/unbind <key>` | Remove a key binding |

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.15.0+
- Fabric API

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download Superior Player Commands from releases
4. Place both `.jar` files in your `mods` folder

## Building from Source

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## Configuration

Waypoints and key bindings are stored per-player in the `config/` folder:
- `superior-player-commands-waypoints.json` - Saved waypoints
- `superior-player-commands-binds.json` - Key bindings

## Permissions

Most commands require operator level 2 (`/op`). Waypoint creation (`/set`) and key bindings (`/bind`) work for all players.

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.
