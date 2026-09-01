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

### PUT /server/{id}/name
Change the name of the specified server.

Request Body: A string with the new name.

Response:
    - the new [server object](#server-object)
    - 404 Not Found if no server exists with that id

## Server Execution
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
    "status": "stopping"|"stopped"|"crashed"|"starting"|"started"
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
    "value_filter"?: <regular expression> // only when type is "text" or "number"
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