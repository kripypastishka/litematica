# Regression plan — upstream issue #411

Target: Minecraft 26.3 / Fabric / Java 25, multiplayer with command-based Fill / Replace / Delete / Paste.

## Root cause
When a single layer's horizontal footprint exceeded `commandFillMaxVolume`, the old height-only split computed `singleBoxHeight == 0` and aborted. Large multiplications could also overflow signed `int`.

## Automatic geometry coverage
The standalone Java harness at `tools/tests/fi/dy/masa/litematica/util/FillVolumeSplitterTest.java` is invoked by GitHub Actions in this branch.

- 256 × 256 × 3 with a maximum command volume of 32768 (expected six commands).
- 128 × 256 × 1 exactly at the limit.
- 200 × 200 × 4 with the default limit.
- Non-origin negative coordinates and a very small command limit.
- Near the maximum signed integer height.
- Two hundred deterministic randomized small 3D volumes.
- Reject zero limits and inverted coordinates.

Every fragment must be within the requested bounds, respect the configured limit, cover all blocks exactly once, and never overlap.

## Gameplay verification
1. Set `commandFillNoChunkClamp=true`, `commandFillMaxVolume=32768`, `commandUseWorldEdit=false` in multiplayer with command permissions.
2. Delete, Fill and Replace a 200 × 200 × 5 test selection. No early termination or unfilled slices should occur.
3. Repeat with 256 × 256 × 1, 1 × 40000 × 1, and a narrow but tall selection.
4. Paste a schematic with large uniform regions through the command paste path, checking exact extents and block replacement behavior.
5. Repeat with `commandFillNoChunkClamp=false` and with WorldEdit mode to ensure existing paths remain unaffected.
6. Compare command count, TPS, and block results for small ordinary fills. Inspect results around chunk and region boundaries.

**Important:** Automatic tests cover splitting mathematics only; they do not send actual Minecraft commands.
