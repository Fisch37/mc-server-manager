import { Button, TextField, Input, Label, Select, ListBox, FieldError, ErrorMessage, Disclosure, Description, Tooltip } from "@heroui/react";
import { useEffect, useRef, useState } from "react";
import { getTemplates, type VersionInfo, type TemplateSummary, type VersionSource, type ConfigurationOption } from "./api/template";
import { createServerWithSocket as createServerAPI, TypedSocket, type WSBacklog, type WSLine } from "./api/server";
import { Sliders } from "@gravity-ui/icons";
import type { ApiError } from "./api/shared";

type ServerCreatorParams = {
    onCreated: () => void
};

const ServerCreator = ({ onCreated }: ServerCreatorParams) => {
    const [templates, set_templates] = useState<Array<TemplateSummary>>([]);
    function getTemplate(id: string): TemplateSummary|null {
        let summary = templates.find(summary => summary.id === id);
        return summary === undefined ? null : summary;
    }

    const [chosen_name, set_chosen_name] = useState<string>("");
    const [chosen_template, set_chosen_template] = useState<string>("");
    const [chosen_versions, set_chosen_versions] = useState<{ [source_id: string]: string }>({});
    
    const [template_version_sources, set_template_version_sources] = useState<Array<VersionSource>>([]);
    const [template_configurations, set_template_configurations] = useState<Array<ConfigurationOption>>([]);
    const [configuration, set_configuration] = useState<{ [key: string]: string }>({});
    const [process_state, set_process_state] = useState<Array<String>|null>(null);
    const process_state_end = useRef(null);
    const [error_message, set_error_message] = useState("");

    useEffect(() => {
        getTemplates()
            .then(value => set_templates(value))
            .catch(e => {
                console.error("Failed to get templates");
                console.error(e);
            })
    }, []);
    useEffect(() => {
        let template = getTemplate(chosen_template);
        if (template !== null) {
            set_template_version_sources(template.versions);
            set_template_configurations(template.configuration_options);
        } else {
            console.debug("No template found");
        }
    }, [chosen_template]);
    useEffect(() => {
        console.log(configuration);
    }, [configuration]);
    useEffect(() => {
        process_state_end.current?.scrollIntoView(false);
    }, [process_state])

    async function createServer() {
        let process_updates: TypedSocket<WSLine|WSBacklog>;
        try {
            process_updates = await createServerAPI({
                name: chosen_name,
                template: chosen_template,
                versions: chosen_versions,
                properties: configuration
            });
        } catch (e: any) {
            if (e instanceof Object && "type" in e && e.type === "api_error") {
                let error = e as ApiError;
                if (error.response_code == 409) {
                    set_error_message("Insufficient version information");
                } else if (error.response_code == 500) {
                    set_error_message("Internal Server Error");
                } else if (error.response_code > 500) {
                    set_error_message(`Server error: ${error.response_code}`);
                } else if (error.response_code >= 400) {
                    set_error_message(`Request error: ${error.response_code}`);
                } else {
                    set_error_message(`Unexpected error code: ${error.response_code}`);
                }
            } else {
                set_error_message("Unknown error. Check your log");
                console.error(e);
            }
            return;
        }
        set_process_state([]);
        process_updates.addOnMessage(message => {
            if ("backlog" in message) {
                set_process_state(prev => [...prev, ...message.backlog]);
            } else if ("line" in message) {
                set_process_state(prev => [...prev, message.line]);
            }
        });
        process_updates.addOnClose((e) => {
            if (e.wasClean)
                onCreated();
            else
                set_error_message("Failed to create server: " + e);
        });
    }

    return (
        <div>
            <form
                onSubmit={e => {
                    e.preventDefault();
                    if (template_version_sources.every(source =>
                        chosen_versions[source.source_id] !== undefined
                    )) {
                        createServer();
                    } else {
                        return false;
                    }
                }}
            >
                <TextField isInvalid={chosen_name.length < 1}>
                    <Label>Name</Label>
                    <Input
                        value={chosen_name}
                        onChange={(e) => set_chosen_name(e.target.value)}
                        minLength={1}
                    />
                    <FieldError>Server must have a name</FieldError>
                </TextField>
                <TextField>
                    <Select
                        name="template"
                        value={chosen_template}
                        onChange={key => set_chosen_template(key.toString())}
                        placeholder="Please select a template"
                    >
                        <Label>Template</Label>
                        <Select.Trigger>
                            <Select.Value />
                            <Select.Indicator />
                        </Select.Trigger>
                        <Select.Popover>
                            <ListBox>
                                {templates.map(template => {
                                    console.log(template);
                                    return (
                                        // <option key={template.id} value={template.id}>{template.name}</option>
                                        <ListBox.Item
                                            id={template.id}
                                            textValue={template.name}
                                        >
                                            {template.name}
                                            <ListBox.ItemIndicator />
                                        </ListBox.Item>
                                    );
                                })}
                            </ListBox>
                        </Select.Popover>
                    </Select>
                </TextField>
                {template_version_sources.map(source => (
                    <TextField>
                        <VersionSelector
                            name={source.friendly_name}
                            versions={source.versions}
                            default_channels={source.default_channels}
                            value={chosen_versions[source.source_id]}
                            onValueSelect={value => set_chosen_versions(prev => {
                                let newObj = {...prev};
                                newObj[source.source_id] = value;
                                return newObj;
                            })}
                            isDisabled={false}
                        />
                    </TextField>
                ))}
                <div className="w-full">
                    <Disclosure isDisabled={!Boolean(chosen_template)}>
                        <Disclosure.Heading className="text-right">
                            <Button
                                slot="trigger"
                                variant="ghost"
                            >
                                Show advanced Config
                                <Disclosure.Indicator />
                            </Button>
                        </Disclosure.Heading>
                        <Disclosure.Content>
                            <Disclosure.Body>
                                {template_configurations.map(configOption => {
                                    const onChange = (new_value: string) => {
                                        set_configuration(prev => {
                                            let newObj = {...prev};
                                            if (new_value) {
                                                newObj[configOption.id] = new_value;
                                            } else {
                                                delete newObj[configOption.id];
                                            }
                                            return newObj;
                                        });
                                    }
                                    switch (configOption.type) {
                                        case "select":
                                            return (
                                                <Select
                                                    key={configOption.id}
                                                    className="w-full"
                                                    placeholder={configOption.placeholder}
                                                    isRequired={configOption.required}
                                                    onChange={key => onChange(key as string)}
                                                >
                                                    <Label>{configOption.name}</Label>
                                                    <Select.Trigger>
                                                        <Select.Value />
                                                        <Select.Indicator />
                                                    </Select.Trigger>
                                                    <Select.Popover>
                                                        <ListBox>
                                                            {configOption.options.map(option => (
                                                                <ListBox.Item
                                                                    id={option.id}
                                                                    textValue={option.name}
                                                                >
                                                                    {option.name}
                                                                    <ListBox.ItemIndicator />
                                                                    { /* TODO: This is not guaranteed to work */}
                                                                    {option.description &&
                                                                        <Description>{option.description}</Description>
                                                                    }
                                                                </ListBox.Item>
                                                            ))}
                                                        </ListBox>
                                                    </Select.Popover>
                                                    {configOption.description && <Description>{configOption.description}</Description>}
                                                </Select>
                                            );
                                        case "text":
                                        case "number":
                                            return (
                                                <TextField
                                                    onChange={onChange}
                                                    isRequired={configOption.required}
                                                >
                                                    <Label>{configOption.name}</Label>
                                                    <Input
                                                        pattern={configOption.value_filter}
                                                        placeholder={configOption.placeholder}
                                                        defaultValue={configOption.default_value}
                                                    />
                                                    {configOption.description && <Description>{configOption.description}</Description>}
                                                </TextField>
                                            );
                                    }
                                })}
                            </Disclosure.Body>
                        </Disclosure.Content>
                    </Disclosure>
                </div>
                {error_message && (
                    <ErrorMessage>{error_message}</ErrorMessage>
                )}
                <div className="grid">
                    <Button
                        type="submit"
                        className="place-self-end mr-2"
                        isDisabled={process_state !== null}
                    >
                        Create
                    </Button>
                </div>
                {process_state !== null && (
                    <div
                        className="bg-gray-800 w-full rounded-t-2x1 font-mono"
                    >
                        {process_state.map(line => (
                            <p>{line}</p>
                        ))}
                        <div ref={process_state_end} />
                    </div>
                )}
            </form>
        </div>
    );
}

type VersionSelectorProps = {
    name: string,
    versions: Array<VersionInfo>,
    default_channels: Array<string>,
    value: string,
    onValueSelect: (value: string) => void,
    isDisabled: boolean,
};
const VersionSelector = ({ name, versions, default_channels, value, onValueSelect, isDisabled }: VersionSelectorProps) => {
    const [available_channels, _] = useState<Array<string>>(() => 
        versions.map(version => version.channel)
            // https://stackoverflow.com/a/14438954/13095869
            // O(n²). Yeesh.
            .filter((channel, index, array) => array.indexOf(channel) === index)
    );
    const [displayed_channels, set_displayed_channels] = useState<Array<string>>(default_channels);
    const [version_filter_opened, set_version_filter_opened] = useState<boolean>(false);

    return (
        <div>
            <Select
                name="version"
                value={value}
                onChange={key => onValueSelect(key.toString())}
                isDisabled={isDisabled}
                placeholder="Please select a version"
            >
                <Label>{name}</Label>
                <Select.Trigger>
                    <Select.Value />
                    <Select.Indicator />
                </Select.Trigger>
                <Select.Popover>
                    <ListBox>
                        {versions
                            .filter(v => displayed_channels.includes(v.channel))
                            .map(v => v.id)
                            .map(version => (
                                <ListBox.Item
                                    id={version}
                                    textValue={version}
                                    key={version}
                                >
                                    {version}
                                    <ListBox.ItemIndicator />
                                </ListBox.Item>
                            )
                        )}
                    </ListBox>
                </Select.Popover>
            </Select>
            { available_channels.length > 1 &&
                <div className="w-full flex">
                    <div className="flex-grow">
                        <div className={version_filter_opened ? "growFromUpperRight" : "shrinkToUpperRight"}>
                            <Select
                                selectionMode="multiple"
                                value={displayed_channels}
                                isDisabled={isDisabled}
                                onChange={keys => set_displayed_channels(keys as string[])}
                            >
                                <Label>Visible Channels</Label>
                                <Select.Trigger>
                                    <Select.Value />
                                    <Select.Indicator />
                                </Select.Trigger>
                                <Select.Popover>
                                    <ListBox selectionMode="multiple">
                                        {available_channels.map(channel => (
                                            <ListBox.Item
                                                id={channel}
                                                textValue={channel}
                                                key={channel}
                                            >
                                                {channel}
                                                <ListBox.ItemIndicator />
                                            </ListBox.Item>
                                        ))}
                                    </ListBox>
                                </Select.Popover>
                            </Select>
                        </div>
                    </div>
                    { /* TODO: This straight up does nothing. No tooltip. */ }
                    <Tooltip delay={0}>
                        <Sliders
                            onClick={() => {
                                set_version_filter_opened(prev => !prev);
                                console.log("Click!");
                            }}
                            width={18} height={18}
                            className="flex-none mr-2 cursor-pointer"
                            aria-label="Version Channel Filter"
                        />
                        <Tooltip.Content>
                            <p>Filter Versions</p>
                        </Tooltip.Content>
                    </Tooltip>
                </div>
            }
        </div>
    )
};

export default ServerCreator;