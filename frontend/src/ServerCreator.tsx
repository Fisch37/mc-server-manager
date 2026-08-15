import { Button, TextField, Input, Label, Select, ListBox, FieldError } from "@heroui/react";
import { useEffect, useState } from "react";
import { getTemplates, type TemplateSummary } from "./api/template";
import { createServer as createServerAPI } from "./api/server";

const ServerCreator = () => {
    const [templates, set_templates] = useState<Array<TemplateSummary>>([]);
    function getTemplate(id: string): TemplateSummary|null {
        let summary = templates.find(summary => summary.id === id);
        return summary === undefined ? null : summary;
    }

    const [chosen_name, set_chosen_name] = useState<string>("");
    const [chosen_template, set_chosen_template] = useState<string>("");
    const [chosen_version, set_chosen_version] = useState<string>("");

    useEffect(() => {
        getTemplates()
            .then(value => set_templates(value))
            .catch(e => console.error("Failed to get templates " + e))
    }, []);

    async function createServer() {
        await createServerAPI(
            chosen_name,
            chosen_template,
            chosen_version
        );
    }

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
                        isDisabled={chosen_template==""}
                        placeholder="Please select a version"
                    >
                        <Label>Version</Label>
                        <Select.Trigger>
                            <Select.Value />
                            <Select.Indicator />
                        </Select.Trigger>
                        <Select.Popover>
                            <ListBox>
                                {getTemplate(chosen_template)?.versions.map(version => (
                                    // <option key={version} value={version}>{version}</option>
                                    <ListBox.Item
                                        id={version}
                                        textValue={version}
                                    >
                                        {version}
                                        <ListBox.ItemIndicator />
                                    </ListBox.Item>
                                ))}
                            </ListBox>
                        </Select.Popover>
                    </Select>
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