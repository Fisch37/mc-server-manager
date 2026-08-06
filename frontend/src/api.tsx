export type StatusValue = "stopping"|"stopped"|"crashed"|"starting"|"started";

export type Server = {
    id: string,
    name: string,
    status: StatusValue
};

export type ServerStatus = {
    id: string,
    status: StatusValue
}

export type ConsoleLine = {
    server_id: string,
    line: string
}

const WS_ROOT: string = "localhost:8080";
// const API_ROOT: string = "/api";
const API_ROOT: string = "http://localhost:8080"

async function fetchApi(
    url: string,
    method: string="GET",
    body: string|null=null
) {
    let response = await fetch(
        `${API_ROOT}${url}`,
        {method, body}
    );
    if (!response.ok) {
        throw `Unexpected status code ${response.status}: ${await response.text()}`
    }
    if (response.status == 204) {
        return undefined;
    }
    return await response.json();
}

export function isStatusAlive(status: StatusValue): boolean {
    switch (status) {
        case "stopping":
        case "starting":
        case "started":
            return true;
        case "stopped":
        case "crashed":
            return false;
    }
}

export async function getServerList(): Promise<Array<Server>> {
    return await fetchApi(`/server/list`);
}

export async function getServerInfo(id: string): Promise<Server> {
    return await fetchApi(`/server/${id}`);
}

export async function startServer(id: string) {
    return await fetchApi(`/server/${id}/start`, "POST");
}

export async function stopServer(id: string) {
    return await fetchApi(`/server/${id}/stop`, "POST");
}

export async function restartServer(id: string) {
    return await fetchApi(`/server/${id}/restart`, "POST");
}

export async function sendConsole(id: string, line: string) {
    return await fetchApi(`/server/${id}/console`, "POST", line);
}

export async function renameServer(id: string, newName: string) {
    return await fetchApi(`/server/${id}/name`, "PUT", newName);
}

export async function deleteServer(id: string) {
    return await fetchApi(`/server/${id}`, "DELETE")
}

export class TypedSocket<Packet> {
    ws: WebSocket

    constructor(ws: WebSocket) {
        this.ws = ws;
    }

    addOnMessage(listener: (line: Packet) => void) {
        this.ws.addEventListener("message", (e) => {
            listener(JSON.parse(e.data))
        })
    }
}

export async function openServerStatusSocket(id: string): Promise<TypedSocket<ServerStatus>> {
    return new TypedSocket(
        // evil url injection vulnerability
        await openWS(`ws://${WS_ROOT}/server/${id}/status/follow`)
    );
}

export async function openConsoleSocket(id: string): Promise<TypedSocket<ConsoleLine>> {
    return new TypedSocket(
        // evil url injection vulnerability
        await openWS(`ws://${WS_ROOT}/server/${id}/console`)
    );
}

export function openConsoleSocketSync(id: string): TypedSocket<ConsoleLine> {
    return new TypedSocket(new WebSocket(`ws://${WS_ROOT}/server/${id}/console`));
}

async function openWS(url: string|URL): Promise<WebSocket> {
    let socket = new WebSocket(url);
    const open_promise = new Promise((resolve, reject) => {
        const open_listener = () => {
            resolve(undefined);
            socket.removeEventListener("open", open_listener);
        }
        const error_or_close_listener = (e: WebSocketEventMap["error"]|WebSocketEventMap["close"]) => {
            reject(e);
            socket.removeEventListener("error", error_or_close_listener);
            socket.removeEventListener("close", error_or_close_listener);
        }
        socket.addEventListener("open", open_listener);
        socket.addEventListener("error", error_or_close_listener);
        socket.addEventListener("close", error_or_close_listener);
    })
    await open_promise;
    return socket;
}
