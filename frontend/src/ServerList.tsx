import { ArrowsRotateRight, Play, Server as ServerIcon, ServerPlus, Stop } from "@gravity-ui/icons";
import { useNavigate } from "react-router";
import { getServerList, isStatusAlive, restartServer, startServer, stopServer, type Server } from "./api/server";
import { useEffect, useState } from "react";
import { Button, Modal } from "@heroui/react";
import ServerCreator from "./ServerCreator";

const ServerList = () => {
    const navigate = useNavigate();
    const [servers, set_servers] = useState<Array<Server>>([]);
    const [is_creator_open, set_creator_open] = useState(false);
    
    useEffect(() => {
        refresh_servers();
    }, []);

    async function refresh_servers() {
        const serverList = await getServerList();
        set_servers(serverList);
    }

    function on_modal_submit() {
        set_creator_open(false);
        refresh_servers();
    }

    return (
        <div>
            <Modal>
                <Button onClick={() => set_creator_open(true)}>
                    <ServerPlus className="inline" width={32} />
                    New Server
                </Button>
                <Modal.Backdrop
                    isOpen={is_creator_open}
                    onOpenChange={state => set_creator_open(state)}
                >
                    <Modal.Container>
                        <Modal.Dialog>
                            <Modal.CloseTrigger />
                            <Modal.Header>
                                <Modal.Heading>Create a new Server</Modal.Heading>
                            </Modal.Header>
                            <Modal.Body>
                                <ServerCreator
                                    onCreated={on_modal_submit}
                                />
                            </Modal.Body>
                        </Modal.Dialog>
                    </Modal.Container>
                </Modal.Backdrop>
            </Modal>
            <div className="grid grid-auto-rows">
                {
                    servers.map(server => {
                        const serverPageUrl = `/server/${server.id}`;
                        return (
                            /* Server Row */
                            <div key={server.id} className="row-span-1 my-1">
                                <span className="mx-1">
                                    <ServerIcon width={24} height={24} className="inline" />
                                </span>
                                <span className="align-middle ml-2">
                                    <a onClick={() => navigate(serverPageUrl)}>{server.name}</a>
                                </span>
                                <span className="align-middle ml-5">
                                    {isStatusAlive(server.status) && (
                                        <span>
                                        <button className="align-middle" onClick={() => stopServer(server.id)}>
                                            <Stop width={24} className="align-middle" />
                                        </button>
                                        <button className="align-middle" onClick={() => restartServer(server.id)}>
                                            <ArrowsRotateRight width={24} className="align-middle" />
                                        </button>
                                        </span>
                                    ) }
                                    {!isStatusAlive(server.status) && (
                                        <button className="align-middle" onClick={() => startServer(server.id)}>
                                            <Play width={24} className="align-middle" />
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