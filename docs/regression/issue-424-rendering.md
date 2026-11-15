# Regression plan — upstream issue #424

Target: Minecraft 26.3, Fabric, Litematica 26.3 fork, Java 25.

## Reported failure
`ConcurrentModificationException` in `WorldRendererSchematic.prepareBlockEntities` while rendering schematic block entities.

## Test matrix
1. Load a schematic containing hundreds of renderable block entities (for example chests, shulker boxes, furnaces and signs).
2. With the schematic visible, move the camera and fly rapidly across chunk boundaries.
3. Change schematic rendering settings and render-layer range repeatedly while the mesh cache rebuilds.
4. Unload and reload the schematic several times, including during movement.
5. Repeat using off-screen rendered block entities (for example beacons) to cover the no-cull collection.
6. Stress test the same scenario in a multiplayer world with active chunk updates and normal gameplay.

## Expected behavior
- No `ConcurrentModificationException`, including after cache clear/rebuild.
- All retained block entities render normally after the rebuild completes; no permanent disappearances or duplicates.
- Rendering recovers after toggling visibility; no abnormal continuous allocations or FPS degradation.
- The same scenario passes with both ordinary and no-cull render paths.

## Notes
This checklist describes manual runtime testing, **not** an automated test or proof of a passing build. CI checks `./gradlew build` on Java 25.
