import { Box, Typography, Button } from "@mui/material"
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar"
import DashboardIcon from "@mui/icons-material/Dashboard"
import LocalGasStationIcon from "@mui/icons-material/LocalGasStation"
import { Link, useLocation } from "react-router-dom"

export default function Sidebar() {

  const location = useLocation()

  const isActive = (path: string) => location.pathname === path

  const navButtonStyle = (path: string) => ({
    justifyContent: "flex-start",
    color: isActive(path) ? "#fff" : "#94a3b8",
    backgroundColor: isActive(path) ? "#1e293b" : "transparent",
    fontWeight: isActive(path) ? "bold" : "normal",
    borderRadius: "10px",
    px: 2,
    py: 1.2,
    textTransform: "none",
    "&:hover": {
      backgroundColor: "#1e293b",
      color: "#fff"
    }
  })

  return (
    <Box
      sx={{
        width: 230,
        height: "100vh",
        backgroundColor: "#020617",
        borderRight: "1px solid rgba(255,255,255,0.05)",
        p: 3
      }}
    >
      {/* LOGO */}
      <Typography
        variant="h6"
        sx={{ fontWeight: "bold", mb: 5 }}
      >
        DriveMind
      </Typography>

      {/* NAV ITEMS */}
      <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>

        <Button
          component={Link}
          to="/"
          startIcon={<DashboardIcon />}
          sx={navButtonStyle("/")}
        >
          Dashboard
        </Button>

        <Button
          component={Link}
          to="/cars"
          startIcon={<DirectionsCarIcon />}
          sx={navButtonStyle("/cars")}
        >
          Cars
        </Button>

        <Button
          component={Link}
          to="/fuel"
          startIcon={<LocalGasStationIcon />}
          sx={navButtonStyle("/fuel")}
        >
          Fuel
        </Button>

      </Box>
    </Box>
  )
}[]