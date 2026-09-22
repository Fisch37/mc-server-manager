import { Description, Input, Label, ListBox, Select, TextField } from "@heroui/react";
import type { ServerConfiguration } from "./api/server";
import { useState } from "react";

type ConfigurationEditorParams = {
    current_values: Array<ServerConfiguration>,
    onValueChange: (generator: (prev: {[id: string]: string}) => {[id: string]: string}) => any
}

const ConfigurationEditor = ({current_values: configuration_values, onValueChange}: ConfigurationEditorParams) => {
    // TODO: This is more of a band-aid than an actual solution
    //  The real problem is that we rerender this component on every value change
    const [changes, set_changes] = useState({ });
    return (
        <>
        {configuration_values.map(({value, description: configOption}) => {
            const onChange = (new_value: string) => {
                set_changes(prev => {
                    let newObj = {...prev};
                    newObj[configOption.id] = new_value;
                    return newObj;
                })
                onValueChange((prev) => {
                    let newObj = {...prev};
                    if (new_value) {
                        newObj[configOption.id] = new_value;
                    } else {
                        delete newObj[configOption.id];
                    }
                    return newObj;
                });
            }
            const getInputValue = (value) => {
                return changes[configOption.id] === undefined ? value : changes[configOption.id];
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
                            value={getInputValue(value)}
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
                                value={getInputValue(value)}
                            />
                            {configOption.description && <Description>{configOption.description}</Description>}
                        </TextField>
                    );
            }
        })}
        </>
    )
}

export default ConfigurationEditor;