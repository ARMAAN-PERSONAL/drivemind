import { useEffect, useState } from "react"
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  MenuItem,
  IconButton,
  useTheme
} from "@mui/material"
import DeleteIcon from "@mui/icons-material/Delete"

import {
  getCars,
  getMaintenanceRules,
  addMaintenanceRule,
  deleteMaintenanceRule
} from "../api/carApi"
import type { MaintenanceRule } from "../api/carApi"

export default function Maintenance() {
  const theme = useTheme()
  const isDark = theme.palette.mode === "dark"

  const [cars, setCars] = useState<any[]>([])
  const [selectedCar, setSelectedCar] = useState<number | "">("")
  const [rules, setRules] = useState<MaintenanceRule[]>([])

  const [serviceType, setServiceType] = useState("")
  const [intervalKm, setIntervalKm] = useState("")
  const [description, setDescription] = useState("")

  useEffect(() => {
    loadCars()
  }, [])

  useEffect(() => {
    if (selectedCar) {
      loadRules(selectedCar)
    } else {
      setRules([])
    }
  }, [selectedCar])

  const loadCars = async () => {
    try {
      const res = await getCars()
      setCars(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      console.error("Failed to load cars", err)
      setCars([])
    }
  }

  const loadRules = async (carId: number) => {
    try {
      const res = await getMaintenanceRules(carId)
      setRules(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      console.error("Failed to load maintenance rules", err)
      setRules([])
    }
  }

  const handleAddRule = async () => {
    if (!selectedCar || !serviceType.trim() || !intervalKm.trim()) {
      alert("Fill required fields")
      return
    }

    try {
      await addMaintenanceRule(Number(selectedCar), {
        serviceType: serviceType.trim(),
        intervalKm: Number(intervalKm),
        description: description.trim()
      })

      setServiceType("")
      setIntervalKm("")
      setDescription("")

      loadRules(Number(selectedCar))
    } catch (err) {
      console.error("Failed to add rule", err)
      alert("Could not save maintenance rule")
    }
  }

  const handleDeleteRule = async (ruleId: number) => {
    try {
      await deleteMaintenanceRule(ruleId)
      if (selectedCar) {
        loadRules(Number(selectedCar))
      }
    } catch (err) {
      console.error("Failed to delete rule", err)
      alert("Could not delete maintenance rule")
    }
  }

  return (
    <Box>
      <Typography
        variant="h4"
        mb={3}
        fontWeight="bold"
        sx={{ color: theme.palette.text.primary }}
      >
        Maintenance Rules
      </Typography>

      <Paper
        sx={{
          p: 3,
          mb: 3,
          borderRadius: 3,
          background: isDark
            ? "linear-gradient(180deg,#111827,#0f172a)"
            : "linear-gradient(180deg,#ffffff,#f6f9ff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.16)"
            : "1px solid rgba(59,130,246,0.18)"
        }}
      >
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

      <Paper
        sx={{
          p: 3,
          mb: 3,
          borderRadius: 3,
          background: isDark
            ? "linear-gradient(180deg,#111827,#0f172a)"
            : "linear-gradient(180deg,#ffffff,#f6f9ff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.16)"
            : "1px solid rgba(59,130,246,0.18)"
        }}
      >
        <Typography
          variant="h6"
          mb={2}
          sx={{ color: theme.palette.text.primary }}
        >
          Add Rule
        </Typography>

        <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap" }}>
          <TextField
            label="Service Type"
            value={serviceType}
            onChange={(e) => setServiceType(e.target.value)}
          />

          <TextField
            label="Interval (km)"
            type="number"
            value={intervalKm}
            onChange={(e) => setIntervalKm(e.target.value)}
          />

          <TextField
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            sx={{ minWidth: 260 }}
          />

          <Button variant="contained" onClick={handleAddRule}>
            Add Rule
          </Button>
        </Box>
      </Paper>

      <Paper
        sx={{
          p: 3,
          borderRadius: 3,
          background: isDark
            ? "linear-gradient(180deg,#111827,#0f172a)"
            : "linear-gradient(180deg,#ffffff,#f6f9ff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.16)"
            : "1px solid rgba(59,130,246,0.18)"
        }}
      >
        <Typography
          variant="h6"
          mb={2}
          sx={{ color: theme.palette.text.primary }}
        >
          Saved Rules
        </Typography>

        {!selectedCar && (
          <Typography sx={{ color: theme.palette.text.secondary }}>
            Select a car to manage maintenance rules.
          </Typography>
        )}

        {selectedCar && rules.length === 0 && (
          <Typography sx={{ color: theme.palette.text.secondary }}>
            No rules saved for this vehicle yet.
          </Typography>
        )}

        {rules.map((rule) => (
          <Paper
            key={rule.id}
            sx={{
              p: 2,
              mb: 2,
              borderRadius: 2,
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              backgroundColor: isDark
                ? "rgba(255,255,255,0.03)"
                : "rgba(37,99,235,0.04)",
              border: isDark
                ? "1px solid rgba(255,255,255,0.06)"
                : "1px solid rgba(37,99,235,0.08)"
            }}
          >
            <Box>
              <Typography
                variant="subtitle1"
                fontWeight="bold"
                sx={{ color: theme.palette.text.primary }}
              >
                {rule.serviceType}
              </Typography>
              <Typography sx={{ color: theme.palette.text.secondary }}>
                Every {rule.intervalKm} km
              </Typography>
              {rule.description && (
                <Typography
                  variant="body2"
                  sx={{ color: theme.palette.text.secondary }}
                >
                  {rule.description}
                </Typography>
              )}
            </Box>

            <IconButton onClick={() => handleDeleteRule(rule.id)}>
              <DeleteIcon />
            </IconButton>
          </Paper>
        ))}
      </Paper>
    </Box>
  )
}