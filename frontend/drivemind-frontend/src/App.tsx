import { useState } from "react"
import { ThemeProvider, CssBaseline, Box } from "@mui/material"
import { BrowserRouter, Routes, Route } from "react-router-dom"
import { getTheme } from "./theme/theme"

import Sidebar from "./layout/Sidebar"
import Topbar from "./layout/Topbar"

import Dashboard from "./pages/Dashboard"
import Cars from "./pages/Cars"

export default function App() {

  const [mode, setMode] = useState<"light" | "dark">("dark")

  const theme = getTheme(mode)

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <BrowserRouter>

        <Box sx={{ display: "flex" }}>

          <Sidebar />

          <Box sx={{ flex: 1 }}>

            <Topbar mode={mode} setMode={setMode} />

            <Box sx={{ p: 3 }}>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/cars" element={<Cars />} />
              </Routes>
            </Box>

          </Box>

        </Box>

      </BrowserRouter>

    </ThemeProvider>
  )
}