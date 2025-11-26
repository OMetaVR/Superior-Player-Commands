# Superior Player Commands

A spiritual successor to the classic Single Player Commands mod from Minecraft Beta 1.1_02. This Fabric mod brings back those beloved utility commands with modern improvements for Minecraft 1.20.1.

## Features

<details>
<summary><b>Movement Commands</b></summary>

| Command | Description |
|---------|-------------|
| `/jump` | Teleport to the block you're looking at (up to 256 blocks) |
| `/ascend` | Teleport to the next open space above you |
| `/descend` | Teleport to the next open space below you |
| `/back` | Return to your last death location |
| `/unstuck` | Teleport to the nearest safe position |

</details>

<details>
<summary><b>Waypoint System</b></summary>

| Command | Description |
|---------|-------------|
| `/set <name>` | Save your current location as a waypoint |
| `/goto <name>` | Teleport to a saved waypoint (works cross-dimension) |
| `/rem <name>` | Remove a waypoint |

</details>

<details>
<summary><b>Player & Inventory Commands</b></summary>

| Command | Description |
|---------|-------------|
| `/heal` | Restore your health to full |
| `/hunger [level]` | Show or set hunger level (0-20) |
| `/saturation` | Max out hunger and saturation |
| `/repair` | Repair the item in your hand |
| `/repairall` | Repair all items in your inventory |
| `/duplicate` | Duplicate the item stack in your hand |
| `/destroy` | Destroy the item in your hand |
| `/more` | Max out the held item stack |
| `/stack` | Merge partial stacks in inventory |

</details>

<details>
<summary><b>Power Tools</b></summary>

| Command | Description |
|---------|-------------|
| `/god` | Toggle invincibility |
| `/fly [speed]` | Toggle creative flight in survival (speed: 0-10) |
| `/noclip [speed]` | GMod-style noclip through blocks (speed: 0.1-10) |
| `/mobsignore` | Toggle hostile mob targeting |
| `/fullbright` | Toggle night vision |
| `/instamine` | Toggle instant block breaking |
| `/autosmelt` | Mined ores drop smelted items |
| `/waterwalk` | Walk on water and lava (sneak to sink) |
| `/drops` | Toggle whether blocks drop items when broken |
| `/knockback [multiplier]` | Adjust knockback strength (0-100) |
| `/hands` | Show/disable tool hands mode |
| `/woodhands` | Mine as if holding wood tools |
| `/stonehands` | Mine as if holding stone tools |
| `/ironhands` | Mine as if holding iron tools |
| `/goldhands` | Mine as if holding gold tools |
| `/diamondhands` | Mine as if holding diamond tools |
| `/netheritehands` | Mine as if holding netherite tools |

</details>

<details>
<summary><b>World Commands</b></summary>

| Command | Description |
|---------|-------------|
| `/extinguish [radius]` | Put out fires (default: 128 blocks) |
| `/ext [radius]` | Alias for extinguish |
| `/snow [radius]` | Freeze water to ice, add snow (default: 32 blocks) |
| `/thaw [radius]` | Melt ice to water, remove snow |
| `/killall [type] [radius]` | Kill entities by type/group (default: all, 128 blocks) |
| `/butcher [radius]` | Kill hostile mobs |

**Killall groups:** `all`, `hostiles`, `passives`, `zombies`, `skeletons`, `undead`, `items`, `xp`, `projectiles`

</details>

<details>
<summary><b>Utility Commands</b></summary>

| Command | Description |
|---------|-------------|
| `/calc <expression>` | In-game calculator |
| `/biome` | Show current biome |
| `/measure` | Distance to looked-at block |
| `/tps` | Show server TPS and MSPT |

</details>

<details>
<summary><b>Key Bindings</b></summary>

| Command | Description |
|---------|-------------|
| `/bind` | List all key bindings |
| `/bind <key>` | Show what's bound to a key |
| `/bind <key> <command>` | Bind a key to execute a command |
| `/unbind <key>` | Remove a key binding |

</details>

<details>
<summary><b>Command Aliases</b></summary>

Create shorthand commands for longer command strings.

| Command | Description |
|---------|-------------|
| `/alias` | List all aliases |
| `/alias <name>` | Show what an alias expands to |
| `/alias <name> <command>` | Create or update an alias |
| `/unalias <name>` | Remove an alias |

**Default aliases:**
| Alias | Expands To |
|-------|------------|
| `/gmc` | `/gamemode creative` |
| `/gms` | `/gamemode survival` |
| `/gma` | `/gamemode adventure` |
| `/gmsp` | `/gamemode spectator` |

</details>

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

Data is stored in the `config/` folder:
- `superior-player-commands-waypoints.json` - Saved waypoints (per-player)
- `superior-player-commands-binds.json` - Key bindings
- `superior-player-commands-aliases.json` - Command aliases

## Permissions

Most commands require operator level 2 (`/op`). Waypoint creation (`/set`) and key bindings (`/bind`) work for all players.

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.
