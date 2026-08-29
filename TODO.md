This project is a multi-server management application
designed specifically for Minecraft.

# MVP
- ~~Server Creation~~
    - ~~based on templates~~
- ~~Server Launch, Stop, Restart, Status Query~~
    - ~~includes WS for updates~~
- ~~Server Console~~
    - ~~WS for received console lines~~
    - ~~sending commands~~
- ~~Server Management~~
    - ~~Deletion~~
    - ~~Renames~~

# Post-MVP
1. progress information on server creation
    - best done using a websocket with ConsoleLines
        (where the console lines are arbitrary strings sent by the backend)
    - possibly with an optional query param on /servers/new
2. Server Management
    - version upgrades
3. ~~Access to logs (both latest and historical) and crash reports~~
    - ~~sorted by descending date, obviously~~
4. ~~Server Templates with external version lists~~
    - ~~to support use of Piston API~~
5. ~~Add filtering by version channel~~
6. ~~Proper feedback on actions (Server Create, Server Start, Stop, Restart)~~
7. Add support for multi-stage versions
    - Fabric, Forge, and Neoforge keep independent versioning for their loaders.
8. Support for custom components and versioning in templates
    - should allow for manually specified versions (to match with overlays)
9. Mod Management
    - only for non-vanilla instances
        - best marked as a template property
    - list mods
    - remove mods
    - add mods (by JAR)
    - add mods (via Modrinth)
    - add mods (via CurseForge)
10. User Management
11. Map Integration
    (based on my vanilla map project?)