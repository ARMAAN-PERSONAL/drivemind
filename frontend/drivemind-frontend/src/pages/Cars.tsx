import { useEffect, useState } from "react"
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  Chip,
  useTheme
} from "@mui/material"

import { getCars, addCar, deleteCar, decodeEnrichedVin } from "../api/carApi"

// 🔥 KEEP YOUR DATASET
const carData: Record<string, string[]> = {
  Acura: ["ILX", "TLX", "RLX", "Integra", "MDX", "RDX", "NSX", "ZDX", "CSX", "EL"],
  Audi: ["A3", "A4", "A5", "A6", "A7", "A8", "Q3", "Q5", "Q7", "Q8", "TT", "R8"],
  BMW: ["1 Series", "2 Series", "3 Series", "4 Series", "5 Series", "7 Series", "8 Series", "X1", "X3", "X5", "X7", "Z4", "i4", "i7"],
  Buick: ["Encore", "Encore GX", "Envision", "Enclave", "LaCrosse", "Regal", "Verano", "Cascada", "Park Avenue", "Rainier"],
  Cadillac: ["ATS", "CTS", "CT4", "CT5", "CT6", "Escalade", "XT4", "XT5", "XT6", "XTS"],
  Chevrolet: ["Spark", "Sonic", "Cruze", "Malibu", "Impala", "Camaro", "Corvette", "Equinox", "Traverse", "Tahoe", "Suburban", "Silverado"],
  Chrysler: ["200", "300", "Pacifica", "Voyager", "Aspen", "Crossfire", "Concorde", "Sebring", "Town & Country", "LHS"],
  Dodge: ["Charger", "Challenger", "Durango", "Journey", "Grand Caravan", "Dart", "Neon", "Viper", "Magnum", "Avenger"],
  Ford: ["Fiesta", "Focus", "Fusion", "Taurus", "Mustang", "Escape", "Edge", "Explorer", "Expedition", "Bronco", "F-150", "Ranger"],
  Genesis: ["G70", "G80", "G90", "GV60", "GV70", "GV80", "Essentia", "Mint", "X Concept", "Electrified G80"],
  GMC: ["Terrain", "Acadia", "Yukon", "Yukon XL", "Sierra 1500", "Sierra 2500", "Canyon", "Hummer EV", "Envoy", "Jimmy"],
  Honda: ["Civic", "Accord", "Insight", "Clarity", "CR-V", "HR-V", "Pilot", "Passport", "Ridgeline", "Odyssey", "S2000"],
  Hyundai: ["Accent", "Elantra", "Sonata", "Azera", "Veloster", "Kona", "Tucson", "Santa Fe", "Palisade", "Ioniq", "Ioniq 5", "Ioniq 6"],
  Infiniti: ["Q50", "Q60", "Q70", "QX50", "QX55", "QX60", "QX80", "G35", "G37", "FX35"],
  Jeep: ["Wrangler", "Cherokee", "Grand Cherokee", "Compass", "Renegade", "Gladiator", "Commander", "Patriot", "Liberty", "Wagoneer"],
  Kia: ["Rio", "Forte", "Optima", "K5", "Stinger", "Soul", "Seltos", "Sportage", "Sorento", "Telluride", "Carnival"],
  Lexus: ["IS", "ES", "GS", "LS", "RC", "LC", "NX", "RX", "GX", "LX", "UX"],
  Mazda: ["Mazda2", "Mazda3", "Mazda6", "CX-3", "CX-30", "CX-5", "CX-9", "CX-90", "MX-5 Miata", "RX-8"],
  Mercedes: ["A-Class", "C-Class", "E-Class", "S-Class", "CLA", "CLS", "GLA", "GLC", "GLE", "GLS", "G-Class"],
  Mitsubishi: ["Mirage", "Lancer", "Outlander", "Outlander Sport", "Eclipse Cross", "RVR", "Pajero", "Galant", "Montero", "Endeavor"],
  Nissan: ["Versa", "Sentra", "Altima", "Maxima", "370Z", "GT-R", "Rogue", "Murano", "Pathfinder", "Armada", "Frontier"],
  Subaru: ["Impreza", "Legacy", "WRX", "BRZ", "Crosstrek", "Forester", "Outback", "Ascent", "Baja", "Tribeca"],
  Tesla: ["Model S", "Model 3", "Model X", "Model Y", "Cybertruck", "Roadster", "Semi", "Model 2 (Rumored)", "Roadster 2", "Tesla Truck"],
  Toyota: ["Corolla", "Camry", "Avalon", "Prius", "Supra", "GR86", "C-HR", "RAV4", "Highlander", "4Runner", "Sequoia", "Tacoma", "Tundra"],
  Volkswagen: ["Jetta", "Passat", "Golf", "GTI", "GLI", "Arteon", "Tiguan", "Atlas", "Beetle", "Touareg"],
  Volvo: ["S60", "S90", "V60", "V90", "XC40", "XC60", "XC90", "C30", "C70", "V40"]
}

type EnrichedVinData = {
  make: string
  model: string
  year: number | null
  trim: string
  color: string
  drivetrain: string
  vehicleType: string
  bodyType: string
  engineConfiguration: string
  engineCylinders: number | null
  engineDisplacement: number | null
  enginePower: number | null
  fuelType: string
  transmission: string
  engineDescription: string
  vehicleCategory: string
  shortSummary: string
  confidenceLabel: string
  hasCoreInfo: boolean
  hasEngineInfo: boolean
  aiGenerated: boolean
  dataSource: string
  status: string
  message?: string
}

export default function Cars() {
  const theme = useTheme()
  const isDark = theme.palette.mode === "dark"

  const [cars, setCars] = useState<any[]>([])

  const [vin, setVin] = useState("")
  const [make, setMake] = useState("")
  const [model, setModel] = useState("")
  const [year, setYear] = useState<number | "">("")
  const [trim, setTrim] = useState("")
  const [mileage, setMileage] = useState("")

  const [vinData, setVinData] = useState<EnrichedVinData | null>(null)
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")

  const makeOptions = Array.from(
    new Set([
      ...Object.keys(carData),
      ...(make ? [make] : [])
    ])
  ).sort()

  const models = Array.from(
    new Set([
      ...(make ? carData[make] || [] : []),
      ...(model ? [model] : [])
    ])
  ).sort()

  const loadCars = async () => {
    try {
      const res = await getCars()
      setCars(Array.isArray(res.data) ? res.data : [])
    } catch {
      setCars([])
    }
  }

  useEffect(() => {
    loadCars()
  }, [])

  const resetForm = () => {
    setVin("")
    setMake("")
    setModel("")
    setYear("")
    setTrim("")
    setMileage("")
    setVinData(null)
    setError("")
  }

  const handleDecodeVin = async () => {
    if (!vin.trim()) {
      alert("Enter VIN")
      return
    }

    try {
      setLoading(true)
      setError("")

      const data = await decodeEnrichedVin(vin.trim())

      if (!data || typeof data !== "object") {
        setError("Invalid enriched VIN response")
        return
      }

      setVinData(data)
      setOpen(true)

      setMake(data.make ?? "")
      setModel(data.model ?? "")
      setYear(data.year ?? "")
      setTrim(data.trim ?? "")
    } catch (err) {
      console.error(err)
      setError("VIN decode and enrichment failed")
    } finally {
      setLoading(false)
    }
  }

  const handleAddCar = async () => {
    if (!vin.trim() || !make || !model || !year || !mileage.trim()) {
      alert("Please complete VIN, make, model, year, and mileage.")
      return
    }

    try {
      setSaving(true)

      await addCar({
        vin: vin.trim(),
        make,
        model,
        year,
        trim,
        currentMileage: parseInt(mileage, 10)
      })

      resetForm()
      loadCars()
    } catch (err) {
      console.error(err)
      alert("Failed to add car")
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteCar(id)
      loadCars()
    } catch (err) {
      console.error(err)
      alert("Failed to delete car")
    }
  }

  return (
    <Box>
      <Typography
        variant="h4"
        mb={3}
        sx={{ color: theme.palette.text.primary }}
      >
        Cars
      </Typography>

      <Paper
        elevation={0}
        sx={{
          p: 3,
          mb: 4,
          borderRadius: 3,
          background: isDark
            ? "linear-gradient(180deg,#111827,#0f172a)"
            : "linear-gradient(180deg,#ffffff,#f6f9ff)",
          border: isDark
            ? "1px solid rgba(59,130,246,0.14)"
            : "1px solid rgba(59,130,246,0.14)",
          boxShadow: isDark
            ? "0 10px 24px rgba(0,0,0,0.22)"
            : "0 10px 24px rgba(37,99,235,0.06)"
        }}
      >
        <Typography
          variant="h6"
          mb={2}
          sx={{ color: theme.palette.text.primary }}
        >
          Add Vehicle
        </Typography>

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit,minmax(200px,1fr))",
            gap: 2
          }}
        >
          <TextField
            label="VIN"
            value={vin}
            onChange={(e) => setVin(e.target.value.toUpperCase())}
          />

          <Button
            variant="outlined"
            onClick={handleDecodeVin}
            disabled={loading}
            sx={{ minHeight: 56 }}
          >
            {loading ? <CircularProgress size={20} /> : "Decode VIN"}
          </Button>

          <TextField
            select
            label="Make"
            value={make}
            onChange={(e) => {
              setMake(e.target.value)
              setModel("")
            }}
            SelectProps={{ native: true }}
            InputLabelProps={{ shrink: true }}
          >
            <option value="">Select Make</option>
            {makeOptions.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </TextField>

          <TextField
            select
            label="Model"
            value={model}
            onChange={(e) => setModel(e.target.value)}
            disabled={!make}
            SelectProps={{ native: true }}
            InputLabelProps={{ shrink: true }}
          >
            <option value="">Select Model</option>
            {models.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </TextField>

          <TextField
            label="Year"
            value={year}
            onChange={(e) => {
              const value = e.target.value
              setYear(value === "" ? "" : Number(value))
            }}
          />

          <TextField
            label="Trim"
            value={trim}
            onChange={(e) => setTrim(e.target.value)}
          />

          <TextField
            label="Mileage"
            value={mileage}
            onChange={(e) => setMileage(e.target.value)}
          />

          <Button
            variant="contained"
            onClick={handleAddCar}
            disabled={saving}
            sx={{ minHeight: 56 }}
          >
            {saving ? <CircularProgress size={20} color="inherit" /> : "Add Car"}
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}

        {vinData && (
          <Paper
            elevation={0}
            sx={{
              mt: 3,
              p: 2.5,
              borderRadius: 3,
              background: isDark
                ? "rgba(255,255,255,0.02)"
                : "rgba(37,99,235,0.04)",
              border: isDark
                ? "1px solid rgba(255,255,255,0.06)"
                : "1px solid rgba(59,130,246,0.12)"
            }}
          >
            <Box
              sx={{
                display: "flex",
                flexWrap: "wrap",
                justifyContent: "space-between",
                gap: 2
              }}
            >
              <Box>
                <Typography
                  variant="subtitle1"
                  fontWeight="bold"
                  sx={{ color: theme.palette.text.primary }}
                >
                  AI Decode Preview
                </Typography>

                <Typography sx={{ color: theme.palette.text.secondary, mt: 0.5 }}>
                  {vinData.shortSummary}
                </Typography>
              </Box>

              <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
                <Chip label={`Confidence: ${vinData.confidenceLabel}`} />
                <Chip label={vinData.aiGenerated ? "AI Enriched" : "Fallback Decode"} />
                <Chip label={vinData.status || "PARTIAL"} />
              </Box>
            </Box>
          </Paper>
        )}
      </Paper>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Decoded VIN Details</DialogTitle>

        <DialogContent>
          {vinData ? (
            <Box sx={{ pt: 1 }}>
              <Typography fontWeight="bold">Vehicle</Typography>
              <Typography>Make: {vinData.make ?? "—"}</Typography>
              <Typography>Model: {vinData.model ?? "—"}</Typography>
              <Typography>Year: {vinData.year ?? "—"}</Typography>
              <Typography>Trim: {vinData.trim ?? "—"}</Typography>
              <Typography>Category: {vinData.vehicleCategory ?? "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>
                AI Summary
              </Typography>
              <Typography>{vinData.shortSummary ?? "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>
                Engine
              </Typography>
              <Typography>{vinData.engineDescription ?? "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>
                Details
              </Typography>
              <Typography>Fuel: {vinData.fuelType ?? "—"}</Typography>
              <Typography>Transmission: {vinData.transmission ?? "—"}</Typography>
              <Typography>Drivetrain: {vinData.drivetrain ?? "—"}</Typography>
              <Typography>Body Type: {vinData.bodyType ?? "—"}</Typography>
              <Typography>Vehicle Type: {vinData.vehicleType ?? "—"}</Typography>
              <Typography>Color: {vinData.color ?? "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>
                Decode Quality
              </Typography>
              <Typography>Confidence: {vinData.confidenceLabel ?? "—"}</Typography>
              <Typography>Source: {vinData.dataSource ?? "—"}</Typography>
              <Typography>Status: {vinData.status ?? "—"}</Typography>
              <Typography>Message: {vinData.message ?? "—"}</Typography>
            </Box>
          ) : (
            <Typography>Loading...</Typography>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      <Typography
        variant="h6"
        mb={2}
        sx={{ color: theme.palette.text.primary }}
      >
        Your Vehicles
      </Typography>

      {cars.length === 0 && (
        <Typography sx={{ color: theme.palette.text.secondary }}>
          No cars found
        </Typography>
      )}

      {cars.map((car) => (
        <Paper
          key={car.id}
          elevation={0}
          sx={{
            p: 2,
            mb: 2,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            borderRadius: 2,
            background: isDark
              ? "rgba(255,255,255,0.02)"
              : "#ffffff",
            border: isDark
              ? "1px solid rgba(255,255,255,0.06)"
              : "1px solid rgba(15,23,42,0.06)"
          }}
        >
          <Box>
            <Typography
              fontWeight="bold"
              sx={{ color: theme.palette.text.primary }}
            >
              {car.year} {car.make} {car.model}
            </Typography>

            <Typography sx={{ color: theme.palette.text.secondary }}>
              {car.trim} • {car.currentMileage} km
            </Typography>
          </Box>

          <Button color="error" onClick={() => handleDelete(car.id)}>
            Delete
          </Button>
        </Paper>
      ))}
    </Box>
  )
}