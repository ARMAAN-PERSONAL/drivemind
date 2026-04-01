import { useEffect, useState } from "react"
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  MenuItem
} from "@mui/material"

import { getCars } from "../api/carApi"

export default function Maintenance() {

  const [cars, setCars] = useState<any[]>([])
  const [selectedCar, setSelectedCar] = useState<number | "">("")
  const [rules, setRules] = useState<any[]>([])

  const [type, setType] = useState("")
  const [intervalKm, setIntervalKm] = useState("")
  const [lastDoneKm, setLastDoneKm] = useState("")

  useEffect(() => {
    loadCars()
  }, [])

  const loadCars = async () => {
    try {
      const res = await getCars()
      setCars(Array.isArray(res.data) ? res.data : res || [])
    } catch {
      setCars([])
    }
  }

  // 🔥 ADD RULE
  const handleAddRule = () => {
    if (!selectedCar || !type || !intervalKm) {
      alert("Fill required fields")
      return
    }

    const newRule = {
      id: Date.now(),
      carId: selectedCar,
      type,
      intervalKm: Number(intervalKm),
      lastDoneKm: Number(lastDoneKm) || 0
    }

    setRules((prev) => [...prev, newRule])

    setType("")
    setIntervalKm("")
    setLastDoneKm("")
  }

  const filteredRules = rules.filter(r => r.carId === selectedCar)

  return (
    <Box>

      <Typography variant="h4" mb={3}>
        Maintenance Rules
      </Typography>

      {/* SELECT CAR */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <TextField
          select
          label="Select Car"
          fullWidth
          value={selectedCar}
          onChange={(e) => setSelectedCar(Number(e.target.value))}
        >
          {cars.map((car) => (
            <MenuItem key={car.id} value={car.id}>
              {car.year} {car.make} {car.model}
            </MenuItem>
          ))}
        </TextField>
      </Paper>

      {/* ADD RULE */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" mb={2}>
          Add Rule
        </Typography>

        <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap" }}>

          <TextField
            label="Type"
            value={type}
            onChange={(e) => setType(e.target.value)}
          />

          <TextField
            label="Interval (km)"
            value={intervalKm}
            onChange={(e) => setIntervalKm(e.target.value)}
          />

          <TextField
            label="Last Done (km)"
            value={lastDoneKm}
            onChange={(e) => setLastDoneKm(e.target.value)}
          />

          <Button variant="contained" onClick={handleAddRule}>
            Add
          </Button>

        </Box>
      </Paper>

      {/* RULES */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" mb={2}>
          Rules
        </Typography>

        {filteredRules.length === 0 && (
          <Typography>No rules yet</Typography>
        )}

        {filteredRules.map((rule) => (
          <Box key={rule.id} sx={{ mb: 1 }}>
            🔧 {rule.type} • every {rule.intervalKm} km • last at {rule.lastDoneKm} km
          </Box>
        ))}

      </Paper>

    </Box>
  )
}