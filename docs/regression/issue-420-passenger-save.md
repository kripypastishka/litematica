# Regression plan — upstream issue #420

Target: Minecraft 26.3 / Fabric / Litematica fork, Java 25.

## Reported bug
Mounts with passengers can be stored twice: passengers already embedded in vehicle NBT are also written as independent top-level schematic entities. In the client-only save path, `Entity.save()` refuses to write a passenger when its vehicle falls outside the selection.

## Save modes to exercise
- Regular chunked save through the schematic save UI.
- Non-chunked conversion through the `WorldUtils`/converter path.
- Multiplayer with Servux entity data enabled, multiplayer without Servux, and single-player.

## Selection test matrix
1. **Vehicle and passenger inside a single region:** one top-level vehicle entry; the rider only appears under its `Passengers` hierarchy. Loading/pasting spawns exactly one of each.
2. **Vehicle outside, rider inside:** the rider is a top-level schematic entity. Loading/pasting preserves the rider without bringing in the vehicle.
3. **Vehicle inside, rider outside:** the rider is not independently stored; document whether vanilla vehicle serialization includes the external rider as a nested passenger.
4. **Multiple passengers and nested riding chains:** each selected descendant is serialized at most once, across two or more levels.
5. **Cross-chunk mount:** root vehicle and rider in adjacent chunks; save in either processing order.
6. **Different and overlapping subregions:** a vehicle in one region and rider in another should not create a duplicate top-level rider.
7. **No vehicle (ordinary entity):** standard serialization behavior remains unchanged.
8. **Players as riders/vehicles:** players should not become independent schematic entities.

## NBT validation
Read the saved `.litematic` with a trusted NBT viewer. For each case, inspect the top-level `Entities` list and any nested `Passengers`. Count UUIDs and verify transformed coordinates after paste. Check for missing entity type IDs and leaked server-only tags.

## Acceptance criteria
No duplicated or silently dropped passengers in the specified cases. No differences in ordinary non-mounted entities. Run both modes with and without server-provided NBT.

**Important:** This is a manual test plan, not an executed game test. A passing `./gradlew build` confirms compilation only.