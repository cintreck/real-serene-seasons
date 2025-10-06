# Real Serene Seasons (Fabric)

Real Serene Seasons keeps Serene Seasons aligned with the real-world calendar. January begins in Mid Winter, February advances to Late Winter, March brings Early Spring, and the rotation continues the whole year (configurable).

Screenshot taken on September 16th, Early Autumn in-game:
![Early Autumn (as of September 16th)](https://cdn.modrinth.com/data/cached_images/c3402d9817f48f7d5bb5bd7544b7d7579d54ed80.jpeg)

## Features
- Syncs the Serene Seasons cycle to the real-world calendar on both singleplayer and servers.
- Optional **hemisphere toggle** via `config/real_serene_seasons.toml` or the Mod Menu + Cloth Config screen.
- Adjustable cycle cadence (12-month, 12-week, or 12-day loop) and optional smooth time-of-day interpolation.
- Timezone configuration.
- Works with Serene Seasons' dimension whitelist and networking so connected players always see the calendar-aligned season state.

## Install
- Requires [Fabric](https://fabricmc.net/), [Fabric API](https://modrinth.com/mod/fabric-api), and [Serene Seasons](https://modrinth.com/mod/serene-seasons).
- Optional UI: add [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config) to tweak the hemisphere toggle in-game.
- Place the built jar on both the server and clients so everyone receives calendar-driven seasons.

## Build
- Java 21 with Gradle + Fabric Loom.
- Versions live in `gradle.properties`. Run `./gradlew build`.

## License
- CC0-1.0. Use it freely.
