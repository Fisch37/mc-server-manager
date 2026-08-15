import { fetchApi } from "./api";

export async function getLogFiles(server_id: string): Promise<string[]> {
    return await fetchApi(`/server/${server_id}/logs`);
}

export async function getLogContent(server_id: string, log_name: string): Promise<string> {
    return await fetchApi(
        `/server/${server_id}/logs/content?log_name=${encodeURIComponent(log_name)}`
    )
}