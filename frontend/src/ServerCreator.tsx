import { Button, TextField, Input, Label, Select, ListBox, FieldError, type Key } from "@heroui/react";
import { useEffect, useState } from "react";
import { getTemplates, type VersionInfo, type TemplateSummary } from "./api/template";
import { createServer as createServerAPI } from "./api/server";
import { Sliders } from "@gravity-ui/icons";
import { useRef } from "react";

const ServerCreator = () => {
    const [templates, set_templates] = useState<Array<TemplateSummary>>([]);
    function getTemplate(id: string): TemplateSummary|null {
        let summary = templates.find(summary => summary.id === id);
        return summary === undefined ? null : summary;
    }

    const [chosen_name, set_chosen_name] = useState<string>("");
    const [chosen_template, set_chosen_template] = useState<string>("");
    const [chosen_version, set_chosen_version] = useState<string>("");
    const [displayed_channels, set_displayed_channels] = useState<Array<string>>([]);
    
    const [template_version_channels, set_template_version_channels] = useState<Array<string>>([]);
    const [template_versions, set_template_versions] = useState<Array<VersionInfo>>([]);

    useEffect(() => {
        getTemplates()
            .then(value => set_templates(value))
            .catch(e => console.error("Failed to get templates " + e))
    }, []);
    useEffect(() => {
        debugger;
        let template = getTemplate(chosen_template);
        if (template !== null) {
            console.log("Helo!");
            set_template_versions(template.versions);
            let channels = template.versions.map(version => version.channel)
            // https://stackoverflow.com/a/14438954/13095869
            .filter((channel, index, array) => array.indexOf(channel) === index);
            set_template_version_channels([...channels]);
            set_displayed_channels([...channels]);
            console.log(channels);
            console.log(template.versions);
            console.log(template.versions.filter(v => channels.includes(v.channel)));
        } else {
            console.debug("No template found");
        }
    }, [chosen_template]);

    async function createServer() {
        await createServerAPI(
            chosen_name,
            chosen_template,
            chosen_version
        );
    }

    const [version_filter_opened, set_version_filter_opened] = useState<boolean>(false);

    return (
        <div>
            <form onSubmit={e => { e.preventDefault(); createServer() }}>
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
                <TextField>
                    <Select
                        name="version"
                        value={chosen_version}
                        onChange={key => set_chosen_version(key.toString())}
                        isDisabled={!chosen_template}
                        placeholder="Please select a version"
                    >
                        <Label>Version</Label>
                        <Select.Trigger>
                            <Select.Value />
                            <Select.Indicator />
                        </Select.Trigger>
                        <Select.Popover>
                            <ListBox>
                                {template_versions
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
                                    isDisabled={!chosen_template}
                                    onChange={keys => set_displayed_channels(keys as string[])}
                                >
                                    <Label>Visible Channels</Label>
                                    <Select.Trigger>
                                        <Select.Value />
                                        <Select.Indicator />
                                    </Select.Trigger>
                                    <Select.Popover>
                                        <ListBox selectionMode="multiple">
                                            {template_version_channels.map(channel => (
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
                </TextField>
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

export default ServerCreator;