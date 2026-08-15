# MCSM (MineCraft Server Manager)
very indev server manager, currently for Minecraft, with plans to expand to other app formats.

You can find the api definition in [api.md](api.md) and the template definition [here](server-templates.md). There is also a Draw.IO diagram of the backend's class structure in [Klassendiagramm.drawio](Klassendiagramm.drawio). Also see [TODO.md](TODO.md) for the current state of the project.

## Build & Run
The best way to use this project is through docker.

If you don't want to use docker, you can start the backend with Maven. You may want to set the `MCSM_DATA_PATH` to change the storage location of the application state.
If you also want a working frontend and don't want to use docker, figure it out yourself.

```bash
docker build -t mcsm .
docker run -p 8080:80 -p 25565:25565 -v ./run:/var/mcsm mcsm:latest
```
_if you want to play with the API, add `-p 8081:8080` before `mcsm:latest`_
