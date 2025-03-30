# Contributing

We welcome submissions as PRs, or bug reports and features requests through our github repo.

## Developing

Start by cloning the repo. The gradle wrapper is included. Prism supports Java 17.

Prism is broken out into several modules:

- `prism-api` - The interfaces and core classes that may be of used to 3rd party API users.
- `prism-bukkit` - The core code for the bukkit platform version of Prism
- `prism-bukkit-loader` - Minimal code for a "loader" plugin that can bootstrap prism and download dependencies.
- `prism-core` - The core code shared by all platform-specific versions.
- `prism-loader` - The core code shared by all platform-specific loader plugins.
- `prism-sponge` - The core code for the sponge platform version of Prism.

For testing, you can use `./gradlew -p prism-bukkit-loader runServer`. This will download a paper server and run prism on it.

Server files and plugin configs will be inside `prism-bukkit-loader/run`.

For testing sponge, you can use `./gradlew -p prism-sponge runServer`. 

As of this writing, there's not much to the sponge port and it has yet to be updated for the loader effort.

A lot of `services` code inside the bukkit module will be eventually moved to core.

## Pull Requests

Any changes to the repository by contributors should be opened as a Pull Request.

1. Look for any existing issues or discuss your ideas in our discord.
2. Fork the repo and create your branch from whichever brance you're working against.
3. Make sure your code lints (`./gradlew check`).
4. Open that pull request!

## Updating Dependencies

Dependency updates should be tested thoroughly. Some dependencies are downloaded by Prism at runtime
and require updating hashes for security.

To check which gradle dependencies have updates, use:

- `./gradlew dependencyUpdates`

## Any contributions you make will be under the GNU GPL 3 Software License

Changes you submit are automatically licensed under the GNU GPL 3 license the project uses. Any code taken
from third party software needs to include their original license headers.
