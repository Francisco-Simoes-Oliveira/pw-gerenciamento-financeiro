import "./App.css";

import { Route, BrowserRouter, Navigate, Routes } from "react-router-dom";

import Login from "./features/auth/pages/login";
import Dashboard from "./features/home/pages/dashboard";

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
