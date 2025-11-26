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
| `/return` | Return to your last teleport location |
| `/unstuck` | Teleport to the nearest safe position |
| `/useportal` | Instant nether/overworld teleport with correct coordinate scaling |

</details>

<details>
<summary><b>Waypoint System</b></summary>

| Command | Description |
|---------|-------------|
| `/set <name>` | Save your current location as a waypoint |
| `/goto <name>` | Teleport to a saved waypoint (works cross-dimension) |
| `/rem <name>` | Remove a waypoint (or stand on one and use `/rem`) |
| `/listwaypoints` | List all your waypoints (alias: `/l`) |

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
| `/duplicate [all] [store]` | Duplicate held item, entire inventory, or store in chest |
| `/destroy` | Destroy the item in your hand |
| `/more [all]` | Max out held item stack or all stacks |
| `/stack` | Merge partial stacks in inventory |
| `/ench <enchantment> [level]` | Apply any enchantment at any level (bypasses restrictions) |
| `/drop` | Drop ALL items from inventory instantly (no OP required) |
| `/dropstore` | Store main inventory (not hotbar) in a chest placed nearby |

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
| `/setjump <value\|reset>` | Set jump height multiplier (0.1-50) |
| `/setspeed <value\|reset>` | Set movement speed multiplier (0.1-20) |
| `/falldamage` | Toggle fall damage |
| `/firedamage` | Toggle fire/lava damage |
| `/drowndamage` | Toggle drowning damage |
| `/health <min\|max\|infinite\|hearts>` | Set health mode or specific heart count (1-500) |
| `/ride` | Force-mount the entity you're looking at |
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
| `/explode [size] [x y z]` | Create an explosion (default: TNT size at player) |
| `/lightning [duration] [x y z]` | Strike lightning (duration in ms for continuous strikes) |
| `/defuse [radius\|all]` | Defuse lit TNT (default: 15 blocks) |
| `/extinguish [radius]` | Put out fires (default: 128 blocks) |
| `/ext [radius]` | Alias for extinguish |
| `/snow [radius]` | Freeze water to ice, add snow (default: 32 blocks) |
| `/thaw [radius]` | Melt ice to water, remove snow |
| `/freeze` | Toggle AI freeze for all mobs (GMod-style) |
| `/killall [type] [radius]` | Kill entities by type/group (default: all, 128 blocks) |
| `/butcher [radius]` | Kill hostile mobs |
| `/grow [radius] [type]` | Grow plants/saplings (default: 10 blocks, all types) |
| `/spawnstack <mobs...> <count>` | Spawn stacked mobs (e.g., `/spawnstack creeper skeleton 3`) |

**Killall groups:** `all`, `hostiles`, `passives`, `zombies`, `skeletons`, `undead`, `items`, `xp`, `projectiles`

**Grow types:** `wheat`, `carrot`, `potato`, `beetroot`, `melon`, `pumpkin`, `sapling`, `seed`, `crop`, `all`

</details>

<details>
<summary><b>Utility Commands</b></summary>

| Command | Description |
|---------|-------------|
| `/calc <expression>` | In-game calculator |
| `/biome` | Show current biome |
| `/coords` | Show current coordinates and facing direction |
| `/measure` | Distance to looked-at block |
| `/tps` | Show server TPS and MSPT |
| `/music <play\|pause\|skip\|back> [volume]` | Control game music |
| `/hideresponses` | Toggle command feedback messages |
| `/help [command]` | Show command help (alias: `/h`) |

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
| `/l` | `/listwaypoints` |

</details>

#### ~ Total Count: 59 unique commands + 6 hands variants = 65 total commands ~

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

## Settings GUI

Access the settings screen in multiple ways:
- Press `,` (comma) key (configurable in Controls → Superior Player Commands)
- Run `/spcsettings` command
- Click the "SPC" button in the pause menu (top-right corner)

The settings screen has three tabs:

| Tab | Features |
|-----|----------|
| **Commands** | Master toggle, enable/disable individual commands, click category headers to toggle entire categories |
| **Aliases** | Create, edit, and delete command aliases with a visual editor |
| **Binds** | Create, edit, and delete key bindings with press-to-bind key capture |

## Configuration

Data is stored in the `config/` folder:
- `superior-player-commands-config.json` - Command toggles and master enable/disable
- `superior-player-commands-waypoints.json` - Saved waypoints (per-player)
- `superior-player-commands-binds.json` - Key bindings
- `superior-player-commands-aliases.json` - Command aliases

## Permissions

Most commands require operator level 2 (`/op`). Some commands like `/set`, `/bind`, `/drop`, `/coords`, `/music`, and `/hideresponses` work for all players.

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.
