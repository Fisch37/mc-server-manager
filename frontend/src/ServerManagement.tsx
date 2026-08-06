import { Button, Tab, TabGroup, TabList, TabPanel, TabPanels } from "@headlessui/react";
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router";
import { getServerInfo, isStatusAlive, restartServer, sendConsole as sendConsoleAPI, renameServer as renameServerAPI, deleteServer as deleteServerAPI, startServer, stopServer, openServerStatusSocket, openConsoleSocket, openConsoleSocketSync } from "./api";
import type { ConsoleLine, Server, StatusValue, TypedSocket } from "./api";

const WS_CLOSING_STATES: Array<number> = [WebSocket.CLOSING, WebSocket.CLOSED];

const ServerManagement = () => {
    const server_id = useParams().server_id;

    const [server_info, set_server_info] = useState<Server|null>(null);
    const [console_lines, set_console_lines] = useState([]);
    const [console_input, set_console_input] = useState("");
    const [server_status, set_server_status] = useState<StatusValue|null>(null);
    
    const [server_new_name, set_server_new_name] = useState("");
    const console_socket = useRef<TypedSocket<ConsoleLine>|null>(null);

    function fetchAndSetServerInfo() {
        getServerInfo(server_id)
            .then(value => {
                set_server_info(value);
                set_server_new_name(value.name);
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
                console_socket.current.addOnMessage(line => set_console_lines(prev => [...prev, line.line]));
            }
        }
    }, [server_status]);

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
        <div>
            <div>
                <h1 className="inline">{server_info === null ? "" : server_info.name}</h1>
                <span className="ml-4">{server_status}</span>
            </div>
            <TabGroup>
                <TabList>
                    <Tab key="console">Console</Tab>
                    <Tab key="server-man">Server Management</Tab>
                </TabList>
                <TabPanels>
                    <TabPanel key="console">
                        <div className="dark:bg-gray-800 w-full rounded-t-2x1 font-mono">
                            {console_lines.map(line => (
                                <p>{line}</p>
                            ))}
                        </div>
                        <div>
                            <form onSubmit={e => {e.preventDefault(); sendConsole()}}>
                                <input className="w-full inline" placeholder="Enter a command" value={console_input} onChange={(e) => set_console_input(e.target.value)} />
                                <Button className="inline bg-blue-500 ml-2" type="submit">Send</Button>
                            </form>
                        </div>
                        <div>
                            {
                                server_status === null
                                    ? (<span>Loading status information...</span>)
                                    : (
                                    isStatusAlive(server_status)
                                        ? (
                                            <span>
                                                <Button className="inline bg-red-500" onClick={() => stopServer(server_id)}>Stop</Button>
                                                <Button className="inline bg-blue-500" onClick={() => restartServer(server_id)}>Restart</Button>
                                            </span>
                                        )
                                        : (
                                            <Button className="inline bg-green-500" onClick={() => startServer(server_id)}>Start</Button>
                                        )
                                    )
                            }
                        </div>
                    </TabPanel>
                    <TabPanel key="server-man">
                        <form onSubmit={e => {e.preventDefault(); renameServer()}}>
                            <input
                                className="inline"
                                placeholder="Enter a new name for this server"
                                value={server_new_name}
                                onChange={(e) => set_server_new_name(e.target.value)}
                            />
                            <Button className="bg-blue-500 ml-2" type="submit">Rename</Button>
                        </form>
                        <Button className="bg-red-500" onClick={() => deleteServer()}>Delete Server</Button>
                    </TabPanel>
                </TabPanels>
            </TabGroup>
        </div>
    )
};

export default ServerManagement;