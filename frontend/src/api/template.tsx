import { fetchApi } from "./shared"

export type VersionInfo = {
    id: string,
    channel: string
}

export type VersionSource = {
    source_id: string,
    friendly_name: string,
    versions: Array<VersionInfo>
}

export type TemplateSummary = {
    id: string,
    name: string,
    has_mods: boolean,
    versions: Array<VersionSource>
}

export async function getTemplates(): Promise<Array<TemplateSummary>> {
    return await fetchApi(`/templates`);
}