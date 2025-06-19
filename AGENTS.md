Microbot is a RuneLite-based automation client that uses a plugin system for scripts. Utility classes prefixed with `Rs2` (e.g., `Rs2Player`, `Rs2Npc`) expose game interactions. Build the project by running a Maven install on the `runelite-parent` module, then choose "Generate sources and update folders" in IntelliJ. Launch with `runelite-client` (or run via IntelliJ). Dependencies may fail to resolve; ensure annotations are enabled and that the JDK is set to version 17.

`ExampleScript` demonstrates API usage:
```java
public class ExampleScript extends Script {
    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                Rs2Npc.attack("guard");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
        return true;
    }
}
```

A new branch `5b4vnq-codex/duplicate-and-modify-aiofighter-script` contains ongoing work on `BizzaAIOFighter` located under `runelite-client/src/main/java/net/runelite/client/plugins/microbot/bizzaaiofighter`. This duplicates the original AIO Fighter plugin and introduces `HardAttackStyle` through `AttackStyleScript` to force a specified combat style. Until this plugin is ready, open pull requests against `5b4vnq-codex/duplicate-and-modify-aiofighter-script` instead of `dev`.
