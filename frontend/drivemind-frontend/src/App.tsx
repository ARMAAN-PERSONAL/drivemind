import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useState } from "react";
import { Box } from "@mui/material";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import Fuel from "./pages/Fuel"

// Layout
import Sidebar from "./layout/Sidebar";
import Topbar from "./layout/Topbar";

// Pages
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Cars from "./pages/Cars";

const ProtectedLayout = ({
  children,
  mode,
  setMode,
}: {
  children: React.ReactNode;
  mode: string;
  setMode: (mode: string) => void;
}) => {
  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/register" />;
  }

  return (
    <Box sx={{ display: "flex" }}>
      <Sidebar />

      <Box sx={{ flex: 1 }}>
        <Topbar mode={mode} setMode={setMode} />

        <Box sx={{ p: 3 }}>{children}</Box>
      </Box>
    </Box>
  );
};

const App = () => {
  const [mode, setMode] = useState("light");

  const theme = createTheme({
    palette: {
      mode: mode as "light" | "dark",
    },
  });

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <BrowserRouter>
        <Routes>

          {/* 🔥 DEFAULT ROUTE */}
          <Route
            path="/"
            element={
              localStorage.getItem("token")
                ? <Navigate to="/dashboard" />
                : <Navigate to="/register" />
            }
          />

          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedLayout mode={mode} setMode={setMode}>
                <Dashboard />
              </ProtectedLayout>
            }
          />

          <Route
            path="/cars"
            element={
              <ProtectedLayout mode={mode} setMode={setMode}>
                <Cars />
              </ProtectedLayout>
            }
          />

          <Route path="*" element={<Navigate to="/" />} />

          <Route
            path="/fuel"
            element={
              <ProtectedLayout mode={mode} setMode={setMode}>
                <Fuel />
              </ProtectedLayout>
            }
          />

        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
};

export default App;