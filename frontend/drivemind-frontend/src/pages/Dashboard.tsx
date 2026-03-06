import { Box, Typography, Paper } from "@mui/material"
import logo from "../assets/logo.png"

export default function Dashboard() {

  return (
    <Box>

      {/* HERO HEADER WITH LOGO */}
      <Paper
        elevation={0}
        sx={{
          p:4,
          mb:4,
          textAlign:"center",
          borderRadius:3,
          background: "linear-gradient(135deg,#0B1220,#111827)"
        }}
      >

        <img
          src={logo}
          alt="DriveMind"
          style={{
            width: "320px",
            maxWidth: "100%",
            filter: "drop-shadow(0px 0px 12px rgba(30,136,229,0.6))"
          }}
        />

        <Typography
          variant="h5"
          sx={{ mt:2, opacity:0.85 }}
        >
          Intelligent Vehicle Management Platform
        </Typography>

      </Paper>


      {/* DASHBOARD TITLE */}
      <Typography variant="h4" mb={3}>
        Dashboard
      </Typography>


      {/* STATS CARDS */}
      <Box
        sx={{
          display:"grid",
          gridTemplateColumns:"repeat(auto-fit,minmax(220px,1fr))",
          gap:3
        }}
      >

        <Paper
          sx={{
            p:3,
            borderRadius:3
          }}
        >
          <Typography variant="h6">
            Vehicles
          </Typography>

          <Typography variant="h3" fontWeight="bold">
            0
          </Typography>

        </Paper>


        <Paper
          sx={{
            p:3,
            borderRadius:3
          }}
        >
          <Typography variant="h6">
            Fuel Logs
          </Typography>

          <Typography variant="h3" fontWeight="bold">
            0
          </Typography>

        </Paper>


        <Paper
          sx={{
            p:3,
            borderRadius:3
          }}
        >
          <Typography variant="h6">
            Maintenance
          </Typography>

          <Typography variant="h3" fontWeight="bold">
            0
          </Typography>

        </Paper>


      </Box>

    </Box>
  )
}