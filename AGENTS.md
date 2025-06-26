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

2. Download the pre-populated Maven repository archive from:
   https://drive.google.com/file/d/1KrGFnI5IIJImsfH7jDnrQ7pHeNTNZk8n/view?usp=sharing
   Unzip it into your home directory so the contents reside in `~/.m2`.

3. Build offline using Maven:
   ```bash
   mvn -o -DskipTests package
   ```
   If dependencies are missing, ensure the `.m2` archive contains all required artifacts.

The `pom.xml` configures `maven-resources-plugin` version `3.3.1` so Maven does
not try to download the default 2.6 plugin when offline.

These steps let Codex build the project in future prompts without reaching external repositories.
