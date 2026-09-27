# API Specification
Specification for the backend API.

All request and response bodies are JSON encoded unless otherwise specified.
If no response code is specified, 200 is assumed.

## Servers
### POST /server/new
Create a new server from a template.

Request Body: A [server builder object](#server-builder-object)

Response:
    - A [server object](#server-object)
    - 409 Conflict, if one or more of the specified versions doesn't exist

### POST /server/new/follow
Create a new server from a template and track the creation process.

Request Body: A [server builder object](#server-builder-object)

Response:
    - A plain-text [gateway token](#gateway-token) to a [creation socket](#creation-socket)
    - 409 Conflict, if one or more of the specified versions doesn't exist

### GET /server/list
Get a list of servers.

Response: a list of [server objects](#server-object)

### GET /server/{id}
Get information about a specific server.

Response:
    - a [server object](#server-object)
    - 404 Not Found if no server exists with that id

### DELETE /server/{id}
Delete this server.

Response:
    - 204 No Content on a success
    - 404 Not Found if no server exists with that id
    - 409 Conflict, if the server is not `"started"` or `"crashed"`,
        containing the current [server status](#server-status-object)

### PATCH /server/{id}
Change the settings of the server.

Request Body: A [server change object](#server-change-object)

Response:
    - 204 No Content on a success
    - 404 Not Found if no server exists with that id

### GET /server/{id}/configuration
Get the current configuration of a server.

Response:
    - a list of [server configuration objects](#server-configuration-object)
    - 404 Not Found if no server exists with that id

### PATCH /server/{id}/configuration
Change the configuration of a server.

Request Body: A [server configuration change object](#server-configuration-change-object)

Response:
    - 204 No Content on a success
    - 409 Conflict if one or more of the configuration values either does not exist or is not a legal value for the configuration.
    - 404 Not Found if no server exists with that id

## Server Execution
### GET /server/status
Get the status for every server.

Response:
```json
{
    <server id>: <status>*
}
```

### GET /server/status/follow
Get a [server status socket](#server-status-socket) for all servers.

### GET /server/{id}/status
Get the current server status.

Response: A [server status object](#server-status-object).

### GET /server/{id}/status/follow
Get a websocket that tracks the status of this server.

Response: A [server status socket](#server-status-socket).

### POST /server/{id}/start
Start the server.
May receive a `follow` query parameter with no value.

Response:
    - 204 No Content,
        unless `follow` is specified, in which case a new [server status socket](#server-status-socket) is returned.
    - 409 Conflict, if the server is not `"stopped"` or `"crashed"`,
        containing the current [server status](#server-status-object)
    - 418 I'm a Teapot, if the server can't start due to a missing runtime.

### POST /server/{id}/stop
Stop the server.

- Response
    - 204 No Content
    - 409 Conflict, if the server is not `"started"`,
        containing the current [server status](#server-status-object)

### POST /server/{id}/restart
Restart the server.
May receive a `follow` query parameter with no value.

- Response
    - 204 No Content,
        unless `follow` is specified, in which case a new [server status socket](#server-status-socket) is returned.
    - 409 Conflict, if the server is not `"started"`,
        containing the current [server status](#server-status-object)

### GET /server/{id}/console
Get a websocket for the console output.

Response: A [console socket](#console-socket)

### POST /server/{id}/console
Send a line to the console input.

Request Body: A JSON string.

Response:
    - 204 No Content on a success
    - 409 Conflict if the server is not `"started"`, `"starting"` or `"stopping"`

## Server Logs
### GET /server/{id}/logs
Get a list of log files for this server.
Logs should be ordered by their creation date, in descending order.
The exact format of the log file names is left unspecified,
but should be somewhat human readable.

Response:
```json
[
    <string>*
]
```

### GET /server/{id}/logs/content?log_name=<string>
Get a specific log file.

Response:
    - A `text/plain` response, which is the log file.
    - 404 if either no server of the UUID or no log file of that name exists.

_Note: In a previous iteration this was `/logs/{log_name}`, but this had to be discarded, due to the fact that Spring simply does not allow escaped slashes (i.e. `%2F`) in path variables._

## Server Files
### GET /server/{id}/files?path={file_path}
Get information about a file.

- Response:
    - a [file-like summary object](#file-like-summary-objects)
    - 403 Forbidden if the path is inaccessible

### GET /server/{id}/files/contents?path={file_path}
Get the file contents.

- Response:
    - the raw contents of the file if the target is a file or, in case of a  symlink, the raw contents of the target file
    - 403 Forbidden if the path is inaccessible or the file is not readable
    - 409 Conflict if the file is a directory

### GET /server/{id}/files/list?path={file_path}
Get the files in a directory.

- Response:
    - an array of [file-like summary objects](#file-like-summary-objects)
    - 403 Forbidden if the path is inaccessible or the directory is not readable
    - 409 Conflict if the file is not a directory or symlink to a directory

### PUT /server/{id}/files?path={file_path}
Write to a file (creating it, if it doesn't exist).

- Request Body: the raw contents of the file

- Response:
    - 204 No Content on a success
    - 403 Forbidden if the file is not writable
    - 409 Conflict if the target is not a file (including if it is a symlink to a file)

### DELETE /server/{id}/files?path={file_path}
Delete the given file, symlink, or directory.

- Response:
    - 204 No Content on a success
    - 403 Forbidden if the file is not writable
    - 409 Conflict if the target does not exist

## Templates
### GET /templates
Get a list of templates.

Response: a list of [template summary objects](#template-summary-object)

### GET /templates/{id}
Get information about a single template.

- Response:
    - A [template summary object](#template-summary-object)
    - 404 Not Found if there is no template of that name

# Gateway
## GET /gateway?token=<gateway token>
- Response:
    - A socket defined by the whatever issued the gateway token
    - 404 Not Found if no socket for that token exists

## Gateway Token
Some opaque string.

# WebSockets
## Server Status Socket
### Sends
- a [server status object](#server-status-socket) for the selected server, 
    - when the socket is first opened
    - and then once every five seconds
    - **or** when the server status changes

## Console Socket
### Sends
- A [console backlog object](#console-backlog-object)
    - when the connection is first made.
- A [console line object](#console-line-object)
    - when the server has written a new line into the console.

# Objects
### Server Object
```json
{
    "id": <uuid string>,
    "name": <string>,
    "status": "stopping"|"stopped"|"crashed"|"starting"|"started",
    "autostart": <boolean>
}
```

### Server Builder Object
```json
{
    "name": <string>,
    "template": <template id string>,
    "versions": {
        <version source identifier string>: <version string>
    },
    "properties": {
        <key string>: <value string>
    }
}
```

### Server Change Object
```json
{
    "name"?: <string>,
    "autostart"?: <boolean>
}
```

### Server Configuration Object
```json
{
    "value"?: <string|number>,
    "description": <configuration option object>
}
```

### Server Configuration Change Object
```json
{
    <configuration id>: <string>
}
```

### Server Status Object
```json
{
    "server_id": <uuid string>,
    "status": "stopping"|"stopped"|"crashed"|"starting"|"started"
}
```

### Console Line Object
```json
{
    "server_id": <uuid string>,
    "line": <string>
}
```

### Console Backlog Object
```json
{
    "server_id": <uuid string>,
    "backlog": [
        <string>*
    ]
}
```

### Template Summary Object
```json
{
    "id": <cleartext string>,
    "name": <cleartext string>,
    "has_mods": <boolean>,
    "versions": [
        <version source object>*
    ],
    "configuration_options": [
        <configuration option object>*
    ]
}
```

### Configuration Option Object
```json
{
    "id": <string>,
    "name": <string>,
    "placeholder"?: <string>,
    "description"?: <string>,
    "required": <boolean>,
    "type": "select"|"text"|"number",
    "options": [ // only if type is "select"
        {
            <configuration select option>*
        }
    ],
    "default_value"?: <string|number>, // number only when type is "number" (in which case only a number is allowed) otherwise string
    "value_filter"?: <regular expression>, // only when type is "text" or "number"
    "min"?: <number>, // only when type is "number"
    "max"?: <number> // only when type is "number"
}
```

### Configuration Select Option
```json
{
    "id": <string>,
    "name": <string>,
    "description"?: <string>
}
```

### Version Source Object
```json
{
    "source_id": <version source identifier string>,
    "friendly_name": <string>,
    "versions": [
        <version info object>*
    ],
    "default_channels": [
        <string>*
    ]
}
```

### Version Info Object
```json
{
    "id": <version string>,
    "channel": <channel string>
}
```

## File-like Summary Objects
### File Summary Object
```json
{
    "type": "file",
    "path": <string>,
    "size": <integer>,
    "created_at": <ISO-8601 date time with timezone>,
    "last_modified": <ISO-8601 date time with timezone>,
    "permissions": <file permissions object>
}
```

### Directory Summary Object
```json
{
    "type": "directory",
    "path": <string>,
    "created_at": <ISO-8601 date time with timezone>,
    "last_modified": <ISO-8601 date time with timezone>,
    "permissions": <file permissions object>
}
```

### Symlink Summary Object
```json
{
    "type": "symlink",
    "path": <string>,
    "created_at": <ISO-8601 date time with timezone>,
    "last_modified": <ISO-8601 date time with timezone>,
    "permissions": <file permissions object>,
    // target may be undefined if it is outside the readable area
    "target"?: <file summary object|directory summary object|symlink summary object>
}
```

### File Permissions Object
```json
{
    "read": <boolean>,
    "write": <boolean>,
    // for directories this means the directory can be viewed
    "execute": <boolean>
}
```