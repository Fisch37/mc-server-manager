import { HardDrivesIcon, PlayIcon, RepeatIcon, StopIcon } from "@phosphor-icons/react";
import { useNavigate } from "react-router";
import { getServerList, restartServer, startServer, stopServer, type Server } from "./api";
import { useEffect, useState } from "react";

const ServerList = () => {
    const navigate = useNavigate();
    const [servers, set_servers] = useState<Array<Server>>([]);
    
    useEffect(() => {
        void (async () => {
            const serverList = await getServerList();
            set_servers(serverList);
        })();
    }, []);

    return (
        <div className="grid">
            { /* Server Row */ }
            {
                servers.map(server => {
                    const serverPageUrl = `/server/${server.id}`;
                    return (
                        <div className="">
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
    );
};

export default ServerList;