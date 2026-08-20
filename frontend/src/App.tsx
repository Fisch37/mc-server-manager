import { BrowserRouter, Route, Routes } from 'react-router';
import './App.css';
import ServerList from './ServerList';
import ServerManagement from './ServerManagement';
import AlertQueue, { type AlertInfo } from './AlertQueue';

var alert_queue_sender: (alert: AlertInfo) => void;

export function queue_alert(alert: AlertInfo) {
  alert_queue_sender(alert);
}

const App = () => {
  return (
    <>
      <AlertQueue receiver={sender => alert_queue_sender = sender} />
      <>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<ServerList />} />
            <Route path="/server/:server_id" element={<ServerManagement />} />
          </Routes>
        </BrowserRouter>
      </>
    </>
  );
};

export default App;
