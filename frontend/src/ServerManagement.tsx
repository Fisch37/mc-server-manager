import { Button, Chip, Input, ListBox, Select, Tabs } from "@heroui/react";
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router";
import { getServerInfo, isStatusAlive, restartServer, sendConsole as sendConsoleAPI, renameServer as renameServerAPI, deleteServer as deleteServerAPI, startServer, stopServer, openServerStatusSocket, openConsoleSocket, openConsoleSocketSync } from "./server_api";
import type { ConsoleBacklog, ConsoleLine, Server, StatusValue, TypedSocket } from "./server_api";
import { getLogContent, getLogFiles } from "./log_api";

const WS_CLOSING_STATES: Array<number> = [WebSocket.CLOSING, WebSocket.CLOSED];

const ServerManagement = () => {
    const server_id = useParams().server_id;

    const [server_info, set_server_info] = useState<Server|null>(null);
    const [server_status, set_server_status] = useState<StatusValue|null>(null);
    
    const [console_lines, set_console_lines] = useState([]);
    const [console_input, set_console_input] = useState("");
    
    const [selected_log, set_selected_log] = useState("");
    const [available_logs, set_available_logs] = useState<Array<string>>([]);
    const [log_content, set_log_content] = useState("");
    
    const [server_new_name, set_server_new_name] = useState("");
    const console_socket = useRef<TypedSocket<ConsoleLine|ConsoleBacklog>|null>(null);

    function fetchAndSetServerInfo() {
        getServerInfo(server_id)
            .then(value => {
                set_server_info(value);
                set_server_new_name(value.name);
                set_server_status(value.status);
            })
            .catch(e => console.error(`Failed to get server info ${e}`))
    }

    useEffect(fetchAndSetServerInfo, []);
    useEffect(() => {
        openServerStatusSocket(server_id)
            .then(socket => socket.addOnMessage(status => {
                set_server_status(status.status);
            }))
            .catch(e => console.error(`Failed to open status socket ${e}`));
    }, []);

    useEffect(() => {
        if (isStatusAlive(server_status)) {
            if (console_socket.current == null || WS_CLOSING_STATES.includes(console_socket.current.ws.readyState)) {
                console_socket.current = openConsoleSocketSync(server_id);
                console_socket.current.addOnMessage(message => {
                    if ("line" in message) {
                        set_console_lines(prev => [...prev, message.line])
                    } else if ("backlog" in message) {
                        set_console_lines([...message.backlog]);
                    } else {
                        console.warn("Received unexpected message: " + message);
                    }
                });
            }
        }
    }, [server_status]);

    useEffect(() => {
        getLogFiles(server_id).then(
            files => {
                set_available_logs([...files]);
                if (files.length > 0) 
                    set_selected_log(files[0])
            }
        ).catch(
            e => console.error("Failed to fetch log files: " + e)
        );
    }, []);
    useEffect(() => {
        if (!selected_log) return;
        getLogContent(server_id, selected_log).then(
            content => set_log_content(content)
        ).catch(
            e => console.error("Failed to fetch log content: " + e)
        );
    }, [selected_log])

    async function sendConsole() {
        await sendConsoleAPI(server_id, console_input);
        set_console_input("");
    }

    async function renameServer() {
        await renameServerAPI(server_id, server_new_name);
        fetchAndSetServerInfo();
    }

    async function deleteServer() {
        await deleteServerAPI(server_id);
    }

    return (
        <div className="w-full h-screen">
            <div>
                <span style={{"fontSize": "x-large"}}>{server_info === null ? "" : server_info.name}</span>
                <span className="ml-4">
                    {
                        // type StatusValue = "stopping" | "stopped" | "crashed" | "starting" | "started"
                        (() => {
                            let color, text;
                            switch (server_status) {
                                case "stopping":
                                    color = "default";
                                    text = "Stopping";
                                    break;
                                case "stopped":
                                    color = "default";
                                    text = "Stopped";
                                    break;
                                case "crashed":
                                    color = "danger";
                                    text = "Crashed";
                                    break;
                                case "starting":
                                    color = "success";
                                    text = "Starting";
                                    break;
                                case "started":
                                    color = "success";
                                    text = "Started";
                                    break;
                                default:
                                    color = "danger";
                                    text = `Unknown state '${server_status}'`;
                            }
                            return (
                                <Chip className="text-lg" color={color}>{text}</Chip>
                            )
                        })()
                    }
                </span>
            </div>
            <Tabs className="w-full h-full">
                <Tabs.ListContainer>
                    <Tabs.List>
                        <Tabs.Tab id="console">
                            Console
                            <Tabs.Indicator />
                        </Tabs.Tab>
                        <Tabs.Tab id="logs">
                            Logs
                            <Tabs.Indicator />
                        </Tabs.Tab>
                        <Tabs.Tab id="server-man">
                            Server Management
                            <Tabs.Indicator />
                        </Tabs.Tab>
                    </Tabs.List>
                </Tabs.ListContainer>
                <Tabs.Panel
                    id="console"
                    className="w-full h-full"
                >
                    <div className="bg-gray-800 w-full h-2/3 overflow-scroll rounded-t-2x1 font-mono">
                        {console_lines.map(line => (
                            <p>{line}</p>
                        ))}
                    </div>
                    <div>
                        <form
                            className="flex"
                            onSubmit={e => {e.preventDefault(); sendConsole()}}
                        >
                            <Input
                                className="inline flex-1"
                                placeholder="Enter a command"
                                value={console_input}
                                onChange={(e) => set_console_input(e.target.value)}
                            />
                            <Button
                                className="inline bg-blue-500 ml-2 flex-none"
                                type="submit"
                            >
                                Send
                            </Button>
                        </form>
                    </div>
                    <div className="mt-4">
                        {
                            server_status === null
                                ? (<span>Loading status information...</span>)
                                : (
                                isStatusAlive(server_status)
                                    ? (
                                        <div className="mx-auto size-fit">
                                            <Button className="inline bg-red-500 mx-2" onClick={() => stopServer(server_id)}>Stop</Button>
                                            <Button className="inline bg-blue-500" onClick={() => restartServer(server_id)}>Restart</Button>
                                        </div>
                                    )
                                    : (
                                        <div className="mx-auto size-fit">
                                            <Button className="inline bg-green-500" onClick={() => startServer(server_id)}>Start</Button>
                                        </div>
                                    )
                                )
                        }
                    </div>
                </Tabs.Panel>
                <Tabs.Panel
                    id="logs"
                    className="w-full h-full"
                >
                    <div className="bg-gray-800 w-full h-2/3 overflow-scroll rounded-t-2x1 font-mono">
                        { /* TODO: Prevent rendering of massive log outputs, possible by pagination */ }
                        {log_content.split("\n").map(line => {
                            return (
                                <p>{line}</p>
                            )
                        })}
                    </div>
                    <Select
                        value={selected_log}
                        onChange={key => set_selected_log(key.toString())}
                    >
                        <Select.Trigger>
                            <Select.Value />
                            <Select.Indicator />
                        </Select.Trigger>
                        <Select.Popover>
                            <ListBox>
                                {available_logs.map(log => {
                                    return (
                                        <ListBox.Item
                                            id={log}
                                            textValue={log}
                                        >
                                            {log}
                                            <ListBox.ItemIndicator />
                                        </ListBox.Item>
                                    );
                                })}
                            </ListBox>
                        </Select.Popover>
                    </Select>
                </Tabs.Panel>
                <Tabs.Panel id="server-man">
                    <form onSubmit={e => {e.preventDefault(); renameServer()}}>
                        <Input
                            className="inline"
                            placeholder="Enter a new name for this server"
                            value={server_new_name}
                            onChange={(e) => set_server_new_name(e.target.value)}
                        />
                        <Button className="bg-blue-500 ml-2" type="submit">Rename</Button>
                    </form>
                    <Button className="bg-red-500" onClick={() => deleteServer()}>Delete Server</Button>
                </Tabs.Panel>
            </Tabs>
        </div>
    )
};

export default ServerManagement;