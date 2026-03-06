import { Box, Typography, Button } from "@mui/material"
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar"
import DashboardIcon from "@mui/icons-material/Dashboard"
import { Link } from "react-router-dom"

export default function Sidebar() {
  return (
    <Box
      sx={{
        width: 220,
        height: "100vh",
        backgroundColor: "background.paper",
        borderRight: "1px solid rgba(255,255,255,0.06)",
        p: 2
      }}
    >
      <Typography
        variant="h6"
        sx={{ fontWeight: "bold", mb: 4 }}
      >
        DriveMind
      </Typography>

      <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>

        <Button
          component={Link}
          to="/"
          startIcon={<DashboardIcon />}
        >
          Dashboard
        </Button>

        <Button
          component={Link}
          to="/cars"
          startIcon={<DirectionsCarIcon />}
        >
          Cars
        </Button>

      </Box>
    </Box>
  )
}