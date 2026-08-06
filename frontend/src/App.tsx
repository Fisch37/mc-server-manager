import { BrowserRouter, Route, Routes } from 'react-router';
import './App.css';
import ServerList from './ServerList';
import ServerManagement from './ServerManagement';

const App = () => {
  return (
    <BrowserRouter>
    <Routes>
      <Route path="/" element={<ServerList />} />
      <Route path="/server/:server_id" element={<ServerManagement />} />
    </Routes>
    </BrowserRouter>
  );
};

export default App;
