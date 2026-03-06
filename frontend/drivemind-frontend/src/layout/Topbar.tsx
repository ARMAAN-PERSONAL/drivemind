import { Box, IconButton } from "@mui/material"
import DarkModeIcon from "@mui/icons-material/DarkMode"
import LightModeIcon from "@mui/icons-material/LightMode"

export default function Topbar({ mode, setMode }: any) {

  const toggleTheme = () => {
    setMode(mode === "dark" ? "light" : "dark")
  }

  return (
    <Box
      sx={{
        height: 60,
        display: "flex",
        alignItems: "center",
        justifyContent: "flex-end",
        pr: 3,
        borderBottom: "1px solid rgba(255,255,255,0.06)"
      }}
    >
      <IconButton onClick={toggleTheme}>
        {mode === "dark" ? <LightModeIcon /> : <DarkModeIcon />}
      </IconButton>
    </Box>
  )
}