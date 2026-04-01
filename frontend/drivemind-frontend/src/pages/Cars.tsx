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
  Alert
} from "@mui/material"

import { getCars, addCar, deleteCar, decodeVin } from "../api/carApi"

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

export default function Cars() {

  const [cars, setCars] = useState<any[]>([])

  const [vin, setVin] = useState("")
  const [make, setMake] = useState("")
  const [model, setModel] = useState("")
  const [year, setYear] = useState<number | "">("")
  const [trim, setTrim] = useState("")
  const [mileage, setMileage] = useState("")

  const [vinData, setVinData] = useState<any>(null)
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const models = make ? carData[make] || [] : []

  const loadCars = async () => {
    try {
      const res = await getCars()
      setCars(Array.isArray(res.data) ? res.data : res || [])
    } catch {
      setCars([])
    }
  }

  useEffect(() => {
    loadCars()
  }, [])

  // 🔥 BULLETPROOF VIN HANDLER
  const handleDecodeVin = async () => {
    if (!vin) return alert("Enter VIN")

    try {
      setLoading(true)
      setError("")

      const res = await decodeVin(vin)

      // ✅ HANDLE BOTH CASES (IMPORTANT)
      const data =
        res?.data && typeof res.data === "object"
          ? res.data
          : res

      console.log("FINAL VIN DATA:", data)

      if (!data || typeof data !== "object") {
        setError("Invalid VIN response")
        return
      }

      // ✅ SET ONCE
      setVinData(data)
      setOpen(true)

      // ✅ SAFE AUTOFILL
      setMake(data.make ?? "")
      setModel(data.model ?? "")
      setYear(data.year ?? "")
      setTrim(data.trim ?? "")

    } catch (err) {
      console.error(err)
      setError("VIN decode failed")
    } finally {
      setLoading(false)
    }
  }

  const handleAddCar = async () => {
    try {
      await addCar({
        vin,
        make,
        model,
        year,
        trim,
        currentMileage: parseInt(mileage)
      })

      setVin("")
      setMake("")
      setModel("")
      setYear("")
      setTrim("")
      setMileage("")

      loadCars()
    } catch {
      alert("Failed to add car")
    }
  }

  const handleDelete = async (id: number) => {
    await deleteCar(id)
    loadCars()
  }

  return (
    <Box>

      <Typography variant="h4" mb={3}>Cars</Typography>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" mb={2}>Add Vehicle</Typography>

        <Box sx={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit,minmax(200px,1fr))",
          gap: 2
        }}>

          <TextField label="VIN" value={vin} onChange={(e) => setVin(e.target.value)} />

          <Button variant="outlined" onClick={handleDecodeVin}>
            {loading ? <CircularProgress size={20}/> : "Decode VIN"}
          </Button>

          {error && <Alert severity="error">{error}</Alert>}

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
            {Object.keys(carData).map((m) => (
              <option key={m} value={m}>{m}</option>
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
              <option key={m} value={m}>{m}</option>
            ))}
          </TextField>

          <TextField label="Year" value={year} onChange={(e) => setYear(Number(e.target.value))} />
          <TextField label="Trim" value={trim} onChange={(e) => setTrim(e.target.value)} />
          <TextField label="Mileage" value={mileage} onChange={(e) => setMileage(e.target.value)} />

          <Button variant="contained" onClick={handleAddCar}>
            Add Car
          </Button>

        </Box>
      </Paper>

      {/* 🔥 DIALOG THAT NEVER BREAKS */}
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth>
        <DialogTitle>Decoded VIN Details</DialogTitle>

        <DialogContent>
          {vinData ? (
            <Box>

              <Typography fontWeight="bold">Vehicle</Typography>
              <Typography>Make: {vinData.make ?? "—"}</Typography>
              <Typography>Model: {vinData.model ?? "—"}</Typography>
              <Typography>Year: {vinData.year ?? "—"}</Typography>
              <Typography>Trim: {vinData.trim ?? "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>Engine</Typography>
              <Typography>{vinData.engineDescription ?? "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>Details</Typography>
              <Typography>Fuel: {vinData.fuelType ?? "—"}</Typography>
              <Typography>Transmission: {vinData.transmission ?? "—"}</Typography>
              <Typography>Drivetrain: {vinData.drivetrain ?? "—"}</Typography>

            </Box>
          ) : (
            <Typography>Loading...</Typography>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      <Typography variant="h6" mb={2}>Your Vehicles</Typography>

      {cars.length === 0 && <Typography>No cars found</Typography>}

      {cars.map((car) => (
        <Paper key={car.id} sx={{ p:2, mb:2, display:"flex", justifyContent:"space-between" }}>
          <Box>
            <Typography fontWeight="bold">
              {car.year} {car.make} {car.model}
            </Typography>
            <Typography>
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