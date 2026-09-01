// const WS_ROOT = () => "ws://localhost:8080";
// const API_ROOT = () => "http://localhost:8080";
const WS_ROOT = () => `${window.location.origin.replace(RegExp("^http"), "ws")}/api`;
const API_ROOT = () => `${window.location.origin}/api`;

export type ApiError = {
    type: "api_error"
    response_code: number
};

export async function fetchApi(
    path: string,
    method: string="GET",
    body: string|null=null,
    content_type: string="application/json"
) {
    let response = await fetch(
        `${API_ROOT()}${path}`,
        {method, body, headers: {"Content-Type": content_type}}
    );
    if (!response.ok) {
        throw {
            type: "api_error",
            response_code: response.status
        };
    }
    if (response.status == 204) {
        return undefined;
    }
    let resp_content_type = response.headers.get("Content-Type");
    if (resp_content_type.startsWith("application/json")) {
        return await response.json();
    } else if (resp_content_type.startsWith("text/plain")) {
        return await response.text();
    } else {
        throw "Unexpected content type in API response: " + resp_content_type;
    }
}

export function getGatewaySocket(token: string): WebSocket {
    return new WebSocket(getWSUrl("/gateway") + `?token=${encodeURIComponent(token)}`)
}

export function getWSUrl(path: string): string {
    return `${WS_ROOT()}${path}`;
}