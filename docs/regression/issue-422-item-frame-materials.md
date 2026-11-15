# Regression plan — upstream issue #422

Target: Minecraft 26.3, Fabric, Litematica 26.3 fork, Java 25.

## Test schematic
Prepare a schematic with only entities first (no solid blocks), containing:
- one empty item frame;
- one item frame displaying a diamond;
- one glow item frame displaying an emerald;
- one painting or another pickable entity as a control.

Create an additional mixed schematic containing blocks and the same frames.

## Expected behavior
1. In Generic settings, `materialListCountItemFrameContents` defaults to `true`, retaining previous behavior.
2. With the option **off**, refresh the Material List. It counts both types of item frames but not the diamonds or emeralds displayed within them.
3. Re-enable the option and refresh. Item contents reappear; the entity control remains unaffected.
4. Check `Total`, `Missing`, `Available`, and the export for both settings.
5. The entity-only schematic must show frame entries when the option is disabled.
6. Repeat with an item frame holding an item whose pick-block form differs from its source item, to check the override path.
7. Test settings persistence after restarting the client and verify English/Russian labels.

## Notes
Changing the setting requires refreshing the Material List. This checklist is **not** an automated test. CI checks `./gradlew build` on Java 25.
