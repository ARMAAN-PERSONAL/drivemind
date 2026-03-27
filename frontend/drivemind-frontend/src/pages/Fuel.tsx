import { useEffect, useState } from "react"
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  MenuItem
} from "@mui/material"

import { getCars, getFuelLogs, addFuelLog } from "../api/carApi"

export default function Fuel() {

  const [cars, setCars] = useState<any[]>([])
  const [selectedCar, setSelectedCar] = useState<number | "">("")
  const [logs, setLogs] = useState<any[]>([])

  const [liters, setLiters] = useState("")
  const [price, setPrice] = useState("")
  const [mileage, setMileage] = useState("")

  const [loading, setLoading] = useState(false)

  // 🔥 LOAD USER CARS
  useEffect(() => {
    loadCars()
  }, [])

  const loadCars = async () => {
    try {
      const res = await getCars()
      setCars(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      console.error("Failed to load cars", err)
      setCars([])
    }
  }

  // 🔥 LOAD LOGS FOR SELECTED CAR (SAFE)
  const loadLogs = async (carId: number) => {
    try {
      const res = await getFuelLogs(carId)

      console.log("Fuel Logs Response:", res.data)

      // ✅ SAFE HANDLING
      if (Array.isArray(res.data)) {
        setLogs(res.data)
      } else if (Array.isArray(res.data?.data)) {
        setLogs(res.data.data)
      } else {
        setLogs([])
      }

    } catch (err) {
      console.error("Failed to load logs", err)
      setLogs([])
    }
  }

  const handleSelectCar = (id: number) => {
    setSelectedCar(id)
    loadLogs(id)
  }

  // 🔥 ADD FUEL LOG (SAFE)
  const handleAddFuel = async () => {
    if (!selectedCar) {
      alert("Select a car")
      return
    }

    try {
      setLoading(true)

      await addFuelLog(selectedCar, {
        liters: Number(liters),
        price: Number(price),
        mileage: Number(mileage),
        date: new Date().toISOString().split("T")[0]
      })

      // reset inputs
      setLiters("")
      setPrice("")
      setMileage("")

      // reload logs
      loadLogs(selectedCar as number)

    } catch (err) {
      console.error("Failed to add fuel log", err)
      alert("Failed to add fuel log")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box>

      <Typography variant="h4" mb={3}>
        Fuel Logs
      </Typography>

      {/* SELECT CAR */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <TextField
          select
          label="Select Car"
          fullWidth
          value={selectedCar}
          onChange={(e) => handleSelectCar(Number(e.target.value))}
        >
          {cars.map((car) => (
            <MenuItem key={car.id} value={car.id}>
              {car.year} {car.make} {car.model}
            </MenuItem>
          ))}
        </TextField>
      </Paper>

      {/* ADD FUEL */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" mb={2}>
          Add Fuel Entry
        </Typography>

        <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap" }}>
          <TextField
            label="Liters"
            value={liters}
            onChange={(e) => setLiters(e.target.value)}
          />

          <TextField
            label="Price"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />

          <TextField
            label="Mileage"
            value={mileage}
            onChange={(e) => setMileage(e.target.value)}
          />

          <Button
            variant="contained"
            onClick={handleAddFuel}
            disabled={loading}
          >
            {loading ? "Adding..." : "Add"}
          </Button>
        </Box>
      </Paper>

      {/* LOGS */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" mb={2}>
          History
        </Typography>

        {logs.length === 0 && (
          <Typography color="text.secondary">
            No fuel logs yet
          </Typography>
        )}

        {Array.isArray(logs) && logs.map((log) => (
          <Box key={log.id} sx={{ mb: 1 }}>
            {log.date} • {log.liters}L • ${log.price} • {log.mileage} km
          </Box>
        ))}
      </Paper>

    </Box>
  )
}