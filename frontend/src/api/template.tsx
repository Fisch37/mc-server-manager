import { fetchApi } from "./shared"

export type TemplateSummary = {
    id: string,
    name: string,
    has_mods: boolean,
    versions: Array<string>
}

export async function getTemplates(): Promise<Array<TemplateSummary>> {
    return await fetchApi(`/templates`);
}