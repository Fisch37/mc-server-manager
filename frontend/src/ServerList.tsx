import { HardDrivesIcon, PlayIcon, RepeatIcon, StopIcon, PlusSquareIcon } from "@phosphor-icons/react";
import { useNavigate } from "react-router";
import { getServerList, restartServer, startServer, stopServer, type Server } from "./server_api";
import { useEffect, useState } from "react";
import { Button } from "@headlessui/react";
import ServerCreator from "./ServerCreator";

const ServerList = () => {
    const navigate = useNavigate();
    const [servers, set_servers] = useState<Array<Server>>([]);
    const [is_creator_open, set_creator_open] = useState(false);
    
    useEffect(() => {
        void (async () => {
            const serverList = await getServerList();
            set_servers(serverList);
        })();
    }, []);

    return (
        <div>
            <Button onClick={() => set_creator_open(prev => !prev)}>
                <PlusSquareIcon className="inline" size={32} />
                New Server
            </Button>
            <div className={"fixed margin-auto w-1/2 bg-gray-500" + is_creator_open ? "" : " none"}>
                <ServerCreator />
            </div>
            <div className="grid">
                { /* Server Row */ }
                {
                    servers.map(server => {
                        const serverPageUrl = `/server/${server.id}`;
                        return (
                            <div key={server.id} className="">
                                <span>
                                    <HardDrivesIcon size={32} className="inline" />
                                </span>
                                <span className="align-middle ml-2">
                                    <a onClick={() => navigate(serverPageUrl)}>{server.name}</a>
                                </span>
                                <span className="align-middle ml-5">
                                    {server.status === "started" && (
                                        <span>
                                        <button className="align-middle" onClick={() => stopServer(server.id)}>
                                            <StopIcon size={24} className="align-middle" />
                                        </button>
                                        <button className="align-middle" onClick={() => startServer(server.id)}>
                                            <RepeatIcon size={24} className="align-middle" />
                                        </button>
                                        </span>
                                    ) }
                                    {server.status === "stopped" && (
                                        <button className="align-middle" onClick={() => restartServer(server.id)}>
                                            <PlayIcon size={24} className="align-middle" />
                                        </button>
                                    ) }
                                </span>
                            </div>
                        );
                    })
                }
            </div>
        </div>
    );
};

export default ServerList;