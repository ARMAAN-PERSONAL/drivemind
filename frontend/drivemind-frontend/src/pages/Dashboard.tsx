import { useEffect, useState } from "react"
import { Box, Typography, Paper } from "@mui/material"
import { useNavigate } from "react-router-dom"

import { getCars, getFuelLogs } from "../api/carApi"
import logo from "../assets/logo.png"

export default function Dashboard() {

  const navigate = useNavigate()

  const [carCount, setCarCount] = useState(0)
  const [fuelCount, setFuelCount] = useState(0)

  // 🔥 LOAD DATA
  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      const carsRes = await getCars()
      const cars = Array.isArray(carsRes.data)
        ? carsRes.data
        : carsRes.data?.data || []

      setCarCount(cars.length)

      // 🔥 LOAD ALL FUEL LOGS (for all cars)
      let totalLogs = 0

      for (const car of cars) {
        try {
          const res = await getFuelLogs(car.id)

          if (Array.isArray(res.data)) {
            totalLogs += res.data.length
          } else if (Array.isArray(res.data?.data)) {
            totalLogs += res.data.data.length
          }

        } catch {
          console.log("Fuel logs failed for car", car.id)
        }
      }

      setFuelCount(totalLogs)

    } catch (err) {
      console.error("Dashboard load failed", err)
    }
  }

  return (
    <Box>

      {/* HERO */}
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

        <Typography variant="h5" sx={{ mt:2, opacity:0.85 }}>
          Intelligent Vehicle Management Platform
        </Typography>
      </Paper>

      <Typography variant="h4" mb={3}>
        Dashboard
      </Typography>

      {/* 🔥 CARDS */}
      <Box
        sx={{
          display:"grid",
          gridTemplateColumns:"repeat(auto-fit,minmax(220px,1fr))",
          gap:3
        }}
      >

        {/* VEHICLES */}
        <Paper
          sx={{ p:3, borderRadius:3, cursor:"pointer" }}
          onClick={() => navigate("/cars")}
        >
          <Typography variant="h6">Vehicles</Typography>
          <Typography variant="h3" fontWeight="bold">
            {carCount}
          </Typography>
        </Paper>

        {/* FUEL */}
        <Paper
          sx={{ p:3, borderRadius:3, cursor:"pointer" }}
          onClick={() => navigate("/fuel")}
        >
          <Typography variant="h6">Fuel Logs</Typography>
          <Typography variant="h3" fontWeight="bold">
            {fuelCount}
          </Typography>
        </Paper>

        {/* MAINTENANCE (future) */}
        <Paper sx={{ p:3, borderRadius:3 }}>
          <Typography variant="h6">Maintenance</Typography>
          <Typography variant="h3" fontWeight="bold">
            0
          </Typography>
        </Paper>

      </Box>

    </Box>
  )
}