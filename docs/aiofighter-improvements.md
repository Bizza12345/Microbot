# AIO Fighter Plugin Improvements

This document lists potential improvements identified while reviewing the plugin.

1. **Use Sets for NPC name lookups**
   - `AttackNpcScript` frequently checks if an NPC name is contained in a comma-separated string. Converting this list into a `Set` would provide quicker lookups and reduce repeated parsing.
2. **Centralize task scheduling**
   - Many scripts create their own `ScheduledExecutorService`. Consolidating these into a single scheduler can reduce thread overhead and simplify shutdown handling.
3. **Improve documentation**
   - Several classes lack Javadoc comments or usage examples. Adding documentation would help contributors understand the flow of the plugin and make future maintenance easier.
