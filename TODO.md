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
3. Support for custom components and versioning in templates
    - should allow for manually specified versions (to match with overlays)
4. Mod Management
    - only for non-vanilla instances
        - best marked as a template property
    - list mods
    - remove mods
    - add mods (by JAR)
    - add mods (via Modrinth)
    - add mods (via CurseForge)
5. User Management
6. Map Integration
    (based on my vanilla map project?)