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
1. Server Management
    - version upgrades
    - progress information on server creation
2. ~~Server Templates with external version lists~~
    - ~~to support use of Piston API~~
3. Add filtering by version channel
4. Add support for multi-stage versions
    - Fabric, Forge, and Neoforge keep independent versioning for their loaders.
5. Mod Management
    - only for non-vanilla instances
        - best marked as a template property
    - list mods
    - remove mods
    - add mods (by JAR)
    - add mods (via Modrinth)
    - add mods (via CurseForge)
6. User Management
7. Map Integration
    (based on my vanilla map project?)