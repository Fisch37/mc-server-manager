import { Button, TextField, Input, Label, Select, ListBox, FieldError, ErrorMessage } from "@heroui/react";
import { useEffect, useState } from "react";
import { getTemplates, type VersionInfo, type TemplateSummary, type VersionSource } from "./api/template";
import { createServer as createServerAPI } from "./api/server";
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
        debugger;
        let template = getTemplate(chosen_template);
        if (template !== null) {
            console.log("Helo!");
            set_template_version_sources(template.versions);
            console.log(template.versions);
        } else {
            console.debug("No template found");
        }
    }, [chosen_template]);

    async function createServer() {
        try {
            await createServerAPI(
                chosen_name,
                chosen_template,
                chosen_versions
            );
        } catch (e: any) {
            if ("type" in e && e.type === "api_error") {
                let error = e as ApiError;
                if (error.response_code == 409) {
                    set_error_message("Insufficient verison information");
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
        onCreated();
    }

    return (
        <div>
            <form onSubmit={e => {
                e.preventDefault();
                if (template_version_sources.every(source =>
                    chosen_versions[source.source_id] !== undefined
                )) {
                    createServer();
                } else {
                    return false;
                }
            }}>
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
                {error_message && (
                    <ErrorMessage>{error_message}</ErrorMessage>
                )}
                <div className="grid">
                    <Button
                        type="submit"
                        className="place-self-end mr-2"
                    >
                        Create
                    </Button>
                </div>
            </form>
        </div>
    );
}

type VersionSelectorProps = {
    name: string,
    versions: Array<VersionInfo>,
    value: string,
    onValueSelect: (value: string) => void,
    isDisabled: boolean
};
const VersionSelector = ({ name, versions, value, onValueSelect, isDisabled }: VersionSelectorProps) => {
    // FIXME: This will cause lag during every rerender, b/c it is O(n²) on a potentially large list
    const available_channels = versions.map(version => version.channel)
            // https://stackoverflow.com/a/14438954/13095869
            .filter((channel, index, array) => array.indexOf(channel) === index);
    const [displayed_channels, set_displayed_channels] = useState<Array<string>>([...available_channels]);
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
                <Sliders
                    onClick={() => {
                        set_version_filter_opened(prev => !prev);
                        console.log("Click!");
                    }}
                    width={18} height={18}
                    className="flex-none mr-2"
                    aria-label="Version Channel Filter"
                />
            </div>
        </div>
    )
};

export default ServerCreator;