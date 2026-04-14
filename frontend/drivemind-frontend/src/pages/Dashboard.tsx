import { useEffect, useState } from "react"
import { Box, Typography, Paper, useTheme } from "@mui/material"
import { useNavigate } from "react-router-dom"

import { getDashboardSummary } from "../api/carApi"
import type { DashboardSummary } from "../api/carApi"
import logo from "../assets/logo.png"

export default function Dashboard() {
  const navigate = useNavigate()
  const theme = useTheme()
  const isDark = theme.palette.mode === "dark"

  const [summary, setSummary] = useState<DashboardSummary>({
    vehicleCount: 0,
    fuelLogCount: 0,
    maintenanceLogCount: 0,
    maintenanceRuleCount: 0,
    totalFuelSpend: 0
  })

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      const res = await getDashboardSummary()
      setSummary(res.data)
    } catch (err) {
      console.error("Dashboard summary load failed", err)
    }
  }

  const cardSx = {
    p: 3,
    borderRadius: 3,
    cursor: "pointer",
    background: isDark
      ? "linear-gradient(180deg,#111827,#0f172a)"
      : "linear-gradient(180deg,#ffffff,#f6f9ff)",
    border: isDark
      ? "1px solid rgba(59,130,246,0.16)"
      : "1px solid rgba(59,130,246,0.18)"
  }

  return (
    <Box>
      <Paper
        elevation={0}
        sx={{
          p: 4,
          mb: 4,
          textAlign: "center",
          borderRadius: 3,
          background: isDark
            ? "linear-gradient(135deg,#0B1220,#111827)"
            : "linear-gradient(135deg,#e8f1ff,#f8fbff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.18)"
            : "1px solid rgba(59,130,246,0.22)",
          boxShadow: isDark
            ? "0 10px 30px rgba(0,0,0,0.28)"
            : "0 10px 24px rgba(37,99,235,0.08)"
        }}
      >
        <img
          src={logo}
          alt="DriveMind"
          style={{
            width: "320px",
            maxWidth: "100%",
            filter: "drop-shadow(0px 0px 12px rgba(30,136,229,0.35))"
          }}
        />

        <Typography
          variant="h5"
          sx={{
            mt: 2,
            color: theme.palette.text.primary
          }}
        >
          Intelligent Vehicle Management Platform
        </Typography>

        <Typography
          variant="body1"
          sx={{
            mt: 1,
            color: theme.palette.text.secondary
          }}
        >
          Track vehicles, fuel history, maintenance activity, and service plans in one place.
        </Typography>
      </Paper>

      <Typography
        variant="h4"
        mb={3}
        fontWeight="bold"
        sx={{ color: theme.palette.text.primary }}
      >
        Dashboard
      </Typography>

      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit,minmax(220px,1fr))",
          gap: 3
        }}
      >
        <Paper sx={cardSx} onClick={() => navigate("/cars")}>
          <Typography variant="h6" sx={{ color: theme.palette.text.secondary }}>
            Vehicles
          </Typography>
          <Typography
            variant="h3"
            fontWeight="bold"
            sx={{ color: theme.palette.text.primary }}
          >
            {summary.vehicleCount}
          </Typography>
        </Paper>

        <Paper sx={cardSx} onClick={() => navigate("/fuel")}>
          <Typography variant="h6" sx={{ color: theme.palette.text.secondary }}>
            Fuel Logs
          </Typography>
          <Typography
            variant="h3"
            fontWeight="bold"
            sx={{ color: theme.palette.text.primary }}
          >
            {summary.fuelLogCount}
          </Typography>
        </Paper>

        <Paper sx={cardSx} onClick={() => navigate("/maintenance")}>
          <Typography variant="h6" sx={{ color: theme.palette.text.secondary }}>
            Maintenance Logs
          </Typography>
          <Typography
            variant="h3"
            fontWeight="bold"
            sx={{ color: theme.palette.text.primary }}
          >
            {summary.maintenanceLogCount}
          </Typography>
        </Paper>

        <Paper sx={cardSx} onClick={() => navigate("/maintenance")}>
          <Typography variant="h6" sx={{ color: theme.palette.text.secondary }}>
            Maintenance Rules
          </Typography>
          <Typography
            variant="h3"
            fontWeight="bold"
            sx={{ color: theme.palette.text.primary }}
          >
            {summary.maintenanceRuleCount}
          </Typography>
        </Paper>

        <Paper
          sx={{
            ...cardSx,
            cursor: "default"
          }}
        >
          <Typography variant="h6" sx={{ color: theme.palette.text.secondary }}>
            Total Fuel Spend
          </Typography>
          <Typography
            variant="h3"
            fontWeight="bold"
            sx={{ color: theme.palette.text.primary }}
          >
            ${summary.totalFuelSpend.toFixed(2)}
          </Typography>
        </Paper>
      </Box>
    </Box>
  )
}