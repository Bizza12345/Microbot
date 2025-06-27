This repository does not include Maven dependencies by default.
To build Microbot without network access:

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

   Install the helper script prerequisites as well:
   ```bash
   sudo apt-get install -y curl unzip
   pip install --user gdown
   ```

2. Download the pre-populated Maven repository archive from:
   https://drive.google.com/file/d/1KrGFnI5IIJImsfH7jDnrQ7pHeNTNZk8n/view?usp=sharing
   Unzip it into your home directory so the contents reside in `~/.m2`.
   The helper script uses `gdown` to fetch the file:
   ```bash
   ./scripts/setup-m2.sh
   ```
   If `gdown` is not found it will attempt to install it using `pip`.

3. Build offline using Maven:
   ```bash
   mvn -o -DskipTests package
   ```
   If dependencies are missing, ensure the `.m2` archive contains all required artifacts.

The `pom.xml` configures `maven-resources-plugin` version `3.3.1` so Maven does
not try to download the default 2.6 plugin when offline.

Even though developers use JDK 17, the project compiles for Java 11 as specified
in the POM. This keeps compatibility with RuneLite while allowing JDK 17 to run
the build.

These steps let Codex build the project in future prompts without reaching external repositories.

## Useful paths
- Microbot scripts: `runelite-client/src/main/java/net/runelite/client/plugins/microbot`
- Microbot utilities: `runelite-client/src/main/java/net/runelite/client/plugins/microbot/util`
- RuneLite API sources: `runelite-api/src/main/java/net/runelite/api`
- Runelite Items API: `runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java`
- RuneLite plugins: `runelite-client/src/main/java/net/runelite/client/plugins`
- Build output: `runelite-client/target` after running Maven
- Example script: `runelite-client/src/main/java/net/runelite/client/plugins/microbot/example`

The `docs/development.md` file contains additional guidance on creating scripts and shows code samples for common tasks.

## Current task
- `runelite-client/src/main/java/net/runelite/client/plugins/microbot/nateplugins/moneymaking/natepieshells` - Uplifting the plugin to add buying of materials from the Grand Exchange.
