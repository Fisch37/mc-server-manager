import { fetchApi } from "./shared"

export type VersionInfo = {
    id: string,
    channel: string
}

export type TemplateSummary = {
    id: string,
    name: string,
    has_mods: boolean,
    versions: Array<VersionInfo>
}

export async function getTemplates(): Promise<Array<TemplateSummary>> {
    return await fetchApi(`/templates`);
}