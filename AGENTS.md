This repository does not include Maven dependencies by default.
To build Microbo

1. Install Java 17 and Maven:
   ```bash
   sudo apt-get update -y
   sudo apt-get install -y openjdk-17-jdk maven
   ```
   Then select Java 17:
   ```bash
   sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
   sudo update-alternatives --set javac /usr/lib/jvm/java-17-openjdk-amd64/bin/javac
   ```

2. Download dependencies (likely to encounter network errors)

3. Build offline using Maven:
   ```bash
   mvn -o -DskipTests package
   ```
   If dependencies are missing, ensure the `.m2` archive contains all required artifacts. If no m2 archive has been provided, skip building.

The `pom.xml` configures `maven-resources-plugin` version `3.3.1` so Maven does
not try to download the default 2.6 plugin when offline.

Even though developers use JDK 17, the project compiles for Java 11 as specified
in the POM. This keeps compatibility with RuneLite while allowing JDK 17 to run
the build.

These steps let Codex build the project in future prompts without reaching external repositories.

## Useful paths
- All API .MD Docs: `docs/api` - Search here FIRST AND ALWAYS for relevant information on how to communicate with the runelite API when crafting scripts and needing to reference the api.
- Microbot NaturalMouse Implementation: `https://github.com/Bizza12345/Microbot/tree/bizzadex/Improve-pie-shell-maker/runelite-client/src/main/java/net/runelite/client/plugins/microbot/util/mouse/naturalmouse`
- Microbot scripts: `runelite-client/src/main/java/net/runelite/client/plugins/microbot`
- Microbot utilities: `runelite-client/src/main/java/net/runelite/client/plugins/microbot/util`
- RuneLite API sources: `runelite-api/src/main/java/net/runelite/api`
- Runelite Items API: `runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java`
- Rs2Keyboard API: `/docs/api/Rs2Keyboard.md`
- RuneLite plugins: `runelite-client/src/main/java/net/runelite/client/plugins`
- Build output: `runelite-client/target` after running Maven
- Example script: `runelite-client/src/main/java/net/runelite/client/plugins/microbot/example`

The `docs/development.md` file contains additional guidance on creating scripts and shows code samples for common tasks.

## Development Rules
- **Never use methods marked as deprecated.**
  If a code comment or annotation indicates a method is deprecated,
  replace it with the suggested alternative before committing any code.

## Current task
- `runelite-client/src/main/java/net/runelite/client/plugins/microbot/tanner` - Improve tanner script with automatic NPC interaction and debug logs.
