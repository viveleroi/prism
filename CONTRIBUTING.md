# Contributing

We welcome PRs or bug reports/features requests through our github repository.

## Repository

Prism is broken out into several modules:

The API:

- `prism-api` - The Prism interfaces and core classes that may be of used to 3rd party API users.
- `prism-bukkit-api` - The bukkit-specific Prism API classes.

The core:

- `prism-core` - The core code shared by all plugins.

The loaders:

- `prism-loader` - The core code shared by all loader plugins.
- `prism-bukkit-loader` - Minimal code for a "loader" plugin that bootstraps prism on bukkit.

The Bukkit Plugin:

- `prism-bukkit` - The core code for the bukkit/spigot platform version of Prism.

This approach was chosen to allow future development of prism for other platforms.

A lot of `services` and related code inside the bukkit module will be eventually moved to core.

## Setup Spigot

1. Download spigot [BuildTools](https://www.spigotmc.org/wiki/buildtools/) and run it to get a jar for the needed mc version.
2. Inside the `prism-bukkit-loader` module, create a `spigot-jars` directory with the necessary spigot jar.
3. Match or update the version used compared to what's defined in `prism-bukkit-loader/build.gradle`.

## Paper?

If you wish to test on paper, you can modify the `runServer` task inside `prism-bukkit-loader/build.gradle` as
paper is the default, or you can download paper yourself.
 
## Setup Databases

If testing with sqlite/h2, you don't need to do anything. Sqlite is the default for prism.

If working in MySQL/MariaDB/Postgres, install any or all.

We recommend using [docker](https://www.docker.com/) and the official images for each. This allows you to
start/stop/destroy/etc each service as needed.

## Setup Server

Install the required and any relevant plugins into `prism-bukkit-loader/run/plugins`

1. Required: [NBT-API](https://modrinth.com/plugin/nbtapi)
2. Optional: [LuckPerms](https://luckperms.net/) - For testing permissions
3. Optional: [WorldEdit](https://enginehub.org/worldedit) - For testing WorldEdit integration

## Development

Configure IntelliJ to run the `prism-bukkit-loader - runServer` task. It will build the plugin and run the server with it. 

Or, use gradle: `./gradlew -p prism-bukkit-loader runServer`.

Server files and plugin configs will be inside `prism-bukkit-loader/run`.

## Pull Requests

Any changes to the repository by contributors should be opened as a Pull Request.

1. Look for any existing issues or discuss your ideas in our discord.
2. Fork the repo and create your branch from whichever branch you're working against.
3. Make sure your code lints (`./gradlew check`).
4. Open that pull request!

## Updating Dependencies

Dependency updates should be tested thoroughly. Some dependencies are downloaded by Prism at runtime
and require updating hashes for security.

To check which gradle dependencies have updates, use:

- `./gradlew dependencyUpdates`
