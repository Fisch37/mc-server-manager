import { Button, Field, Input, Label, Select } from "@headlessui/react";
import { useEffect, useState } from "react";
import { getTemplates, type TemplateSummary } from "./template_api";
import { createServer as createServerAPI } from "./server_api";

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
                <Field>
                    <Label>Name</Label>
                    <Input value={chosen_name} onChange={(e) => set_chosen_name(e.target.value)}></Input>
                </Field>
                <Field>
                    <Label>Template</Label>
                    <Select
                        name="template"
                        value={chosen_template}
                        onChange={e => set_chosen_template(e.target.value)}
                    >
                        <option value="">Please select a template</option>
                        {templates.map(template => {
                            console.log(template);
                            return (
                                <option key={template.id} value={template.id}>{template.name}</option>
                            );
                        })}
                    </Select>
                </Field>
                <Field>
                    <Label>Version</Label>
                    <Select
                        name="version"
                        value={chosen_version}
                        onChange={e => set_chosen_version(e.target.value)}
                        disabled={chosen_template==""}
                    >
                        <option value="">Please select a version</option>
                        {getTemplate(chosen_template)?.versions.map(version => (
                            <option key={version} value={version}>{version}</option>
                        ))}
                    </Select>
                </Field>
                <Button type="submit">Create</Button>
            </form>
        </div>
    );
}

export default ServerCreator;