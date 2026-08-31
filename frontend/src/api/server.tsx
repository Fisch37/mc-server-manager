import { fetchApi, getWSUrl } from "./shared";

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
export type ConsoleBacklog = {
    server_id: string,
    backlog: Array<string>
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

export async function createServer(
    name: string,
    template: string,
    versions: { [source_id: string]: string },
    properties: { [key: string]: string }
) {
    await fetchApi(
        `/server/new`,
        "POST",
        JSON.stringify({
            name,
            template,
            versions,
            properties
        })
    )
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
    return await fetchApi(`/server/${id}/console`, "POST", line, "text/plain");
}

export async function renameServer(id: string, newName: string) {
    return await fetchApi(`/server/${id}/name`, "PUT", newName, "text/plain");
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
        await openWS(`/server/${id}/status/follow`)
    );
}

export async function openConsoleSocket(id: string): Promise<TypedSocket<ConsoleLine>> {
    return new TypedSocket(
        // evil url injection vulnerability
        await openWS(`/server/${id}/console`)
    );
}

export function openConsoleSocketSync(id: string): TypedSocket<ConsoleLine> {
    return new TypedSocket(new WebSocket(getWSUrl(`/server/${id}/console`)));
}

async function openWS(url: string): Promise<WebSocket> {
    let socket = new WebSocket(getWSUrl(url));
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
