import { fetchApi } from "./shared"

export type VersionInfo = {
    id: string,
    channel: string
}

export type VersionSource = {
    source_id: string,
    friendly_name: string,
    versions: Array<VersionInfo>,
    default_channels: Array<string>
}

export type TemplateSummary = {
    id: string,
    name: string,
    has_mods: boolean,
    versions: Array<VersionSource>,
    configuration_options: Array<ConfigurationOption>
}

type ConfigurationOptionBase<Type, T> = {
    id: string,
    name: string,
    placeholder?: string,
    description?: string,
    required: boolean,
    type: Type,
    default_value?: T
}
type ConfigurationOptionArbitraryValue<Type, T> = ConfigurationOptionBase<Type, T> & {
    value_filter?: string
}
export type ConfigurationOptionText = ConfigurationOptionArbitraryValue<"text", string>;
export type ConfigurationOptionNumber = ConfigurationOptionArbitraryValue<"number", number>;
export type ConfigurationOptionSelect = ConfigurationOptionArbitraryValue<"select", string> & {
    options: Array<SelectOption>
}
export type SelectOption = {
    id: string,
    name: string,
    description?: string
}

export type ConfigurationOption = ConfigurationOptionSelect|ConfigurationOptionText|ConfigurationOptionNumber;

export async function getTemplates(): Promise<Array<TemplateSummary>> {
    return await fetchApi(`/templates`);
}