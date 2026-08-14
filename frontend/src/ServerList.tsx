import { HardDrivesIcon, PlayIcon, RepeatIcon, StopIcon, PlusSquareIcon } from "@phosphor-icons/react";
import { useNavigate } from "react-router";
import { getServerList, restartServer, startServer, stopServer, type Server } from "./server_api";
import { useEffect, useState } from "react";
import { Button, Modal } from "@heroui/react";
import ServerCreator from "./ServerCreator";

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
        <div>
            <Modal>
                <Button>
                    <PlusSquareIcon className="inline" size={32} />
                    New Server
                </Button>
                <Modal.Backdrop>
                    <Modal.Container>
                        <Modal.Dialog>
                            <Modal.CloseTrigger />
                            <Modal.Header>
                                <Modal.Heading>Create a new Server</Modal.Heading>
                            </Modal.Header>
                            <Modal.Body>
                                <ServerCreator />
                            </Modal.Body>
                        </Modal.Dialog>
                    </Modal.Container>
                </Modal.Backdrop>
            </Modal>
            <div className="grid">
                {
                    servers.map(server => {
                        const serverPageUrl = `/server/${server.id}`;
                        return (
                            /* Server Row */
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
                                        <button className="align-middle" onClick={() => restartServer(server.id)}>
                                            <RepeatIcon size={24} className="align-middle" />
                                        </button>
                                        </span>
                                    ) }
                                    {server.status === "stopped" && (
                                        <button className="align-middle" onClick={() => startServer(server.id)}>
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