## NeoForge 1.21.1 Port Plan (Updated with Shared Resources + Mixin Strategy)

### Summary
Port `autoreconnect` to a multiloader monorepo (`common`, `fabric`, `neoforge`) with full NeoForge 1.21.1 feature parity, same mod identity/config keys, shared core logic in `common`, loader-specific wiring in loader modules.

### Repository Layout (Decision-Complete)
- Shared Java logic goes in `common/src/main/java`.
- Shared resources go in `common/src/main/resources`.
- Shared assets/lang/data paths:
- `common/src/main/resources/assets/autoreconnect/lang/en_us.json`
- `common/src/main/resources/assets/autoreconnect/icon.png`
- Any shared data files under `common/src/main/resources`.
- Fabric metadata stays loader-side:
- `fabric/src/main/resources/fabric.mod.json`
- NeoForge metadata stays loader-side:
- `neoforge/src/main/resources/META-INF/neoforge.mods.toml`

### Mixins (Chosen Strategy + Fallback)
- Start with **Option A** only if mixins remain truly identical across loaders:
- `common/src/main/resources/autoreconnect.mixins.json`
- `common/src/main/java/com/example/autoreconnect/mixin/...`
- Fabric references mixins via `fabric.mod.json`.
- NeoForge references same mixin config via `[[mixins]] config="autoreconnect.mixins.json"` in `neoforge.mods.toml`.
- If mapping/signature divergence appears during first NeoForge compile/runtime pass, switch immediately to **Option B** (split early):
- `fabric/src/main/resources/autoreconnect.fabric.mixins.json`
- `neoforge/src/main/resources/autoreconnect.neoforge.mixins.json`
- Loader-specific mixin classes under each loader module.

### Public Interfaces / API Impact
- Keep all `/autoreconnect ...` commands unchanged.
- Keep `modid=autoreconnect`.
- Keep config schema and keys unchanged for migration continuity.
- Add internal platform bridge interfaces for loader event/config-screen registration only.

### Implementation Plan
1. Create multiloader Gradle structure with `common`, `fabric`, `neoforge`.
2. Move shared reconnect/config/ntfy/Discord/profile/state logic into `common`.
3. Keep Fabric as a thin adapter for Fabric events + ModMenu config hook.
4. Add NeoForge thin adapter with `@Mod(..., dist=Dist.CLIENT)` and NeoForge event registration.
5. Implement NeoForge config screen registration with `IConfigScreenFactory` while still using Cloth Config.
6. Apply mixin strategy gate:
- Try common mixins first.
- If not stable/identical, split mixins immediately before feature validation.
7. Ensure metadata/resources packaging is loader-correct (shared resources in common, mod files loader-side).
8. Run build + runtime parity verification on both loaders and prepare release artifacts.

### Test Cases and Scenarios
1. Both loaders build successfully (`:fabric:build`, `:neoforge:build`).
2. Shared resources (icon/lang) are present in both final jars.
3. Disconnect countdown/cancel/reconnect-anyway behavior matches Fabric baseline.
4. ntfy stop/reconnect commands work on disconnect screen.
5. Discord webhook lifecycle notifications fire with same toggles/privacy behavior.
6. Hub detection and auto-commands execute with same timing/rules.
7. Auto-join last server and reliability state reset behavior match.
8. Mixin loading succeeds on both loaders (or split-mixin fallback passes).

### Assumptions and Defaults
- NeoForge target baseline: `21.1.219`.
- ModDevGradle baseline: `2.0.140`.
- Cloth Config on NeoForge remains enabled (`cloth-config-neoforge`).
- Default mixin path is shared-common only if truly identical; otherwise split early (no prolonged hybrid state).
- Client-only mod behavior remains unchanged.
