import { useEffect, useMemo, useState } from "react"
import {
  Box,
  Typography,
  Paper,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  CircularProgress,
  Chip,
  useTheme
} from "@mui/material"

import {
  getCars,
  getAiInsight,
  type Car,
  type AiInsightResponse
} from "../api/carApi"

export default function Insights() {
  const theme = useTheme()
  const isDark = theme.palette.mode === "dark"

  const [cars, setCars] = useState<Car[]>([])
  const [selectedCarId, setSelectedCarId] = useState<number | "">("")
  const [loadingCars, setLoadingCars] = useState(true)
  const [loadingInsight, setLoadingInsight] = useState(false)
  const [error, setError] = useState("")
  const [insight, setInsight] = useState<AiInsightResponse | null>(null)

  useEffect(() => {
    loadCars()
  }, [])

  useEffect(() => {
    if (selectedCarId !== "") {
      loadInsight(selectedCarId)
    }
  }, [selectedCarId])

  const loadCars = async () => {
    try {
      setLoadingCars(true)
      setError("")

      const res = await getCars()
      const carList = res.data || []

      setCars(carList)

      if (carList.length > 0) {
        setSelectedCarId(carList[0].id)
      }
    } catch (err) {
      console.error("Failed to load cars", err)
      setError("Failed to load vehicles.")
    } finally {
      setLoadingCars(false)
    }
  }

  const loadInsight = async (carId: number) => {
    try {
      setLoadingInsight(true)
      setError("")

      const res = await getAiInsight(carId)
      setInsight(res.data)
    } catch (err) {
      console.error("Failed to load AI insight", err)
      setInsight(null)
      setError("Failed to load AI vehicle insight.")
    } finally {
      setLoadingInsight(false)
    }
  }

  const selectedCar = useMemo(
    () => cars.find((car) => car.id === selectedCarId) || null,
    [cars, selectedCarId]
  )

  const getStatusChipColor = (status?: string) => {
    switch (status) {
      case "HEALTHY":
        return "success"
      case "DUE_SOON":
        return "warning"
      case "OVERDUE":
        return "error"
      default:
        return "default"
    }
  }

  const vehicleTitle = selectedCar
    ? `${selectedCar.year ?? ""} ${selectedCar.make ?? ""} ${selectedCar.model ?? ""} ${selectedCar.trim ?? ""}`
        .replace(/\s+/g, " ")
        .trim()
    : "Vehicle Insight"

  return (
    <Box>
      <Typography
        variant="h4"
        mb={3}
        fontWeight="bold"
        sx={{ color: theme.palette.text.primary }}
      >
        AI Vehicle Insights
      </Typography>

      <Paper
        elevation={0}
        sx={{
          p: 3,
          mb: 3,
          borderRadius: 3,
          background: isDark
            ? "linear-gradient(135deg,#0B1220,#111827)"
            : "linear-gradient(135deg,#e8f1ff,#f8fbff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.18)"
            : "1px solid rgba(59,130,246,0.22)",
          boxShadow: isDark
            ? "0 10px 30px rgba(0,0,0,0.24)"
            : "0 10px 24px rgba(37,99,235,0.08)"
        }}
      >
        <Typography
          variant="h6"
          mb={1}
          sx={{ color: theme.palette.text.primary }}
        >
          Insight Engine
        </Typography>

        <Typography sx={{ mb: 3, color: theme.palette.text.secondary }}>
          Select a vehicle and generate a smart maintenance-focused summary based on its tracked data.
        </Typography>

        {loadingCars ? (
          <Box sx={{ py: 2 }}>
            <CircularProgress size={28} />
          </Box>
        ) : cars.length === 0 ? (
          <Typography sx={{ color: theme.palette.text.secondary }}>
            No vehicles found. Add a vehicle first to unlock AI insights.
          </Typography>
        ) : (
          <FormControl fullWidth>
            <InputLabel id="car-select-label">Vehicle</InputLabel>
            <Select
              labelId="car-select-label"
              value={selectedCarId}
              label="Vehicle"
              onChange={(e) => setSelectedCarId(Number(e.target.value))}
            >
              {cars.map((car) => (
                <MenuItem key={car.id} value={car.id}>
                  {`${car.year ?? ""} ${car.make ?? ""} ${car.model ?? ""} ${car.trim ?? ""}`
                    .replace(/\s+/g, " ")
                    .trim()}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}
      </Paper>

      {selectedCar && (
        <Paper
          elevation={0}
          sx={{
            p: 3,
            borderRadius: 3,
            background: isDark
              ? "linear-gradient(180deg,#111827,#0f172a)"
              : "linear-gradient(180deg,#ffffff,#f6f9ff)",
            border: isDark
              ? "1px solid rgba(59,130,246,0.16)"
              : "1px solid rgba(59,130,246,0.18)",
            boxShadow: isDark
              ? "0 10px 30px rgba(0,0,0,0.22)"
              : "0 10px 24px rgba(37,99,235,0.08)"
          }}
        >
          <Box
            sx={{
              display: "flex",
              flexWrap: "wrap",
              justifyContent: "space-between",
              alignItems: "center",
              gap: 2,
              mb: 3
            }}
          >
            <Box>
              <Typography
                variant="h5"
                fontWeight="bold"
                sx={{ color: theme.palette.text.primary }}
              >
                {vehicleTitle}
              </Typography>

              <Typography sx={{ mt: 0.5, color: theme.palette.text.secondary }}>
                VIN: {selectedCar.vin}
              </Typography>

              <Typography sx={{ mt: 0.5, color: theme.palette.text.secondary }}>
                Current Mileage: {selectedCar.currentMileage ?? 0} km
              </Typography>
            </Box>

            {insight && (
              <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
                <Chip
                  label={insight.overallStatus.replace("_", " ")}
                  color={getStatusChipColor(insight.overallStatus) as "success" | "warning" | "error" | "default"}
                />
                <Chip label={`Health Score: ${insight.healthScore}/100`} />
                <Chip label={insight.aiGenerated ? "AI Generated" : "Fallback Summary"} />
              </Box>
            )}
          </Box>

          {loadingInsight ? (
            <Box sx={{ py: 5, display: "flex", justifyContent: "center" }}>
              <CircularProgress />
            </Box>
          ) : error ? (
            <Typography color="error">{error}</Typography>
          ) : insight ? (
            <Box sx={{ display: "grid", gap: 3 }}>
              <Paper
                elevation={0}
                sx={{
                  p: 3,
                  borderRadius: 3,
                  background: isDark
                    ? "rgba(255,255,255,0.02)"
                    : "rgba(255,255,255,0.88)",
                  border: isDark
                    ? "1px solid rgba(255,255,255,0.06)"
                    : "1px solid rgba(59,130,246,0.12)"
                }}
              >
                <Typography
                  variant="h6"
                  mb={1}
                  sx={{ color: theme.palette.text.primary }}
                >
                  AI Summary
                </Typography>

                <Typography
                  sx={{
                    lineHeight: 1.9,
                    color: theme.palette.text.secondary
                  }}
                >
                  {insight.summary}
                </Typography>
              </Paper>

              <Paper
                elevation={0}
                sx={{
                  p: 3,
                  borderRadius: 3,
                  background: isDark
                    ? "linear-gradient(135deg, rgba(37,99,235,0.16), rgba(59,130,246,0.08))"
                    : "linear-gradient(135deg, rgba(37,99,235,0.10), rgba(59,130,246,0.05))",
                  border: isDark
                    ? "1px solid rgba(59,130,246,0.22)"
                    : "1px solid rgba(59,130,246,0.18)"
                }}
              >
                <Typography
                  variant="h6"
                  mb={1}
                  sx={{ color: theme.palette.text.primary }}
                >
                  Recommended Next Step
                </Typography>

                <Typography
                  sx={{
                    lineHeight: 1.8,
                    color: theme.palette.text.primary
                  }}
                >
                  {insight.recommendation}
                </Typography>
              </Paper>
            </Box>
          ) : null}
        </Paper>
      )}
    </Box>
  )
}