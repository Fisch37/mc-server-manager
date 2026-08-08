export const WS_ROOT: string = "localhost:8080";
// const API_ROOT: string = "/api";
const API_ROOT: string = "http://localhost:8080"

export async function fetchApi(
    url: string,
    method: string="GET",
    body: string|null=null,
    content_type: string="application/json"
) {
    let response = await fetch(
        `${API_ROOT}${url}`,
        {method, body, headers: {"Content-Type": content_type}}
    );
    if (!response.ok) {
        throw `Unexpected status code ${response.status}: ${await response.text()}`
    }
    if (response.status == 204) {
        return undefined;
    }
    return await response.json();
}
