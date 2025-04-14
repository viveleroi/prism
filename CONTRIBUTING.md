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

## Development

1. Inside the `prism-bukkit-loader` module, create a `spigot-jars` directory with the necessary spigot jar.
2. Verify or modify the version needed in `prism-bukkit-loader/build.gradle`.
3. Use your IDE to run the `runServer` task. It will build the plugin and run the server with it.
4. Or, use gradle: `./gradlew -p prism-bukkit-loader runServer`.

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
