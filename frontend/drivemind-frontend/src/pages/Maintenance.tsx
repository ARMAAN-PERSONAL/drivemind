import { useEffect, useState } from "react"
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  MenuItem,
  IconButton,
  useTheme,
  Alert,
  CircularProgress,
  Chip
} from "@mui/material"
import DeleteIcon from "@mui/icons-material/Delete"
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome"

import {
  getCars,
  getMaintenanceRules,
  addMaintenanceRule,
  deleteMaintenanceRule,
  generateStarterMaintenanceRules
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

  const [generating, setGenerating] = useState(false)
  const [lastProfile, setLastProfile] = useState("")
  const [banner, setBanner] = useState<{ type: "success" | "info" | "error"; text: string } | null>(null)

  useEffect(() => {
    loadCars()
  }, [])

  useEffect(() => {
    if (selectedCar) {
      loadRules(Number(selectedCar))
    } else {
      setRules([])
      setLastProfile("")
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
      setBanner({ type: "success", text: "Maintenance rule added." })

      loadRules(Number(selectedCar))
    } catch (err) {
      console.error("Failed to add rule", err)
      setBanner({ type: "error", text: "Could not save maintenance rule." })
    }
  }

  const handleGenerateStarterRules = async () => {
    if (!selectedCar) {
      setBanner({ type: "info", text: "Select a car first to generate starter rules." })
      return
    }

    try {
      setGenerating(true)
      setBanner(null)

      const res = await generateStarterMaintenanceRules(Number(selectedCar))
      const addedCount = res.data?.addedCount ?? 0
      const maintenanceProfile = res.data?.maintenanceProfile ?? "General Vehicle"

      setLastProfile(maintenanceProfile)

      if (addedCount > 0) {
        setBanner({
          type: "success",
          text: `${addedCount} AI starter maintenance rule${addedCount === 1 ? "" : "s"} added for profile: ${maintenanceProfile}.`
        })
      } else {
        setBanner({
          type: "info",
          text: `No new starter rules were added. Existing rules already cover the ${maintenanceProfile} profile well enough.`
        })
      }

      loadRules(Number(selectedCar))
    } catch (err) {
      console.error("Failed to generate starter rules", err)
      setBanner({ type: "error", text: "Could not generate starter maintenance rules." })
    } finally {
      setGenerating(false)
    }
  }

  const handleDeleteRule = async (ruleId: number) => {
    try {
      await deleteMaintenanceRule(ruleId)
      setBanner({ type: "success", text: "Maintenance rule deleted." })

      if (selectedCar) {
        loadRules(Number(selectedCar))
      }
    } catch (err) {
      console.error("Failed to delete rule", err)
      setBanner({ type: "error", text: "Could not delete maintenance rule." })
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

      {banner && (
        <Alert severity={banner.type} sx={{ mb: 3 }}>
          {banner.text}
        </Alert>
      )}

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
            ? "linear-gradient(180deg,#0f172a,#111827)"
            : "linear-gradient(180deg,#eef4ff,#f8fbff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.18)"
            : "1px solid rgba(59,130,246,0.20)"
        }}
      >
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            gap: 2,
            flexWrap: "wrap"
          }}
        >
          <Box>
            <Typography
              variant="h6"
              sx={{ color: theme.palette.text.primary }}
            >
              AI Starter Rules
            </Typography>

            <Typography
              sx={{ mt: 0.5, color: theme.palette.text.secondary, maxWidth: 720 }}
            >
              Generate a starter maintenance schedule that changes by vehicle profile instead of giving every car the exact same interval pattern.
              You can still add your own custom rules anytime.
            </Typography>

            {lastProfile && (
              <Box sx={{ mt: 1.5 }}>
                <Chip label={`Detected Profile: ${lastProfile}`} />
              </Box>
            )}
          </Box>

          <Button
            variant="contained"
            startIcon={generating ? undefined : <AutoAwesomeIcon />}
            onClick={handleGenerateStarterRules}
            disabled={generating || !selectedCar}
          >
            {generating ? <CircularProgress size={20} color="inherit" /> : "Generate Starter Rules"}
          </Button>
        </Box>
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
          Add Custom Rule
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