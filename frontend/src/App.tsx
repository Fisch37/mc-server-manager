import { BrowserRouter, Route, Routes } from 'react-router';
import './App.css';
import ServerList from './ServerList';
import ServerManagement from './ServerManagement';
import AlertQueue, { type AlertInfo } from './AlertQueue';
import { House } from '@gravity-ui/icons';
import { Modal } from '@heroui/react';

var alert_queue_sender: (alert: AlertInfo) => void;

export function queue_alert(alert: AlertInfo) {
    alert_queue_sender(alert);
}

const App = () => {
    return (
        <>
            <AlertQueue receiver={sender => alert_queue_sender = sender} />
            <Modal isOpen={false}>
                <Modal.Backdrop>
                    <Modal.Container>
                        <Modal.Dialog>
                            <Modal.Header>
                                Server is offline
                            </Modal.Header>
                            <Modal.Body>

                            </Modal.Body>
                        </Modal.Dialog>
                    </Modal.Container>
                </Modal.Backdrop>
            </Modal>
            <div className="w-full h-full flex flex-row my-4 ml-4">
                <div>
                    <a href="/"><House width={32} height={32} /></a>
                </div>
                <div className="w-full h-full flex flex-col pl-4">
                    <div>
                        <a href="/" className="text-2xl">My Custom Server Manager</a>
                    </div>
                    <div>
                        <BrowserRouter>
                            <Routes>
                                <Route path="/" element={<ServerList />} />
                                <Route path="/server/:server_id" element={<ServerManagement />} />
                            </Routes>
                        </BrowserRouter>
                    </div>
                </div>
            </div>
        </>
    );
};

export default App;
