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

  // 🔥 SAFE ARRAY EXTRACTOR
  const extractArray = (data: any) => {
    if (Array.isArray(data)) return data
    if (Array.isArray(data?.data)) return data.data
    if (Array.isArray(data?.cars)) return data.cars
    return []
  }

  const loadCars = async () => {
    try {
      const res = await getCars()

      console.log("CARS RESPONSE:", res.data)

      setCars(extractArray(res.data))

    } catch (err) {
      console.error("Failed to load cars", err)
      setCars([])
    }
  }

  useEffect(() => {
    loadCars()
  }, [])

  // 🔥 DECODE VIN
  const handleDecodeVin = async () => {
    if (!vin) return alert("Enter VIN")

    try {
      setLoading(true)
      setError("")

      const res = await decodeVin(vin)
      const data = res.data

      setVinData(data)
      setOpen(true)

      // SAFE AUTO-FILL
      setMake(data?.make || "")
      setModel(data?.model || "")
      setYear(data?.year || "")
      setTrim(data?.trim || "")

    } catch (err: any) {
      console.error(err)
      setError("VIN decode failed")
    } finally {
      setLoading(false)
    }
  }

  const handleAddCar = async () => {
    try {
      const car = {
        vin,
        make,
        model,
        year,
        trim,
        currentMileage: parseInt(mileage)
      }

      await addCar(car)

      // reset form
      setVin("")
      setMake("")
      setModel("")
      setYear("")
      setTrim("")
      setMileage("")

      loadCars()

    } catch (err) {
      console.error("Add car failed", err)
      alert("Failed to add car")
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteCar(id)
      loadCars()
    } catch (err) {
      console.error("Delete failed", err)
    }
  }

  return (
    <Box>

      <Typography variant="h4" mb={3}>
        Cars
      </Typography>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" mb={2}>
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
            onChange={(e) => setVin(e.target.value)}
          />

          <Button variant="outlined" onClick={handleDecodeVin}>
            {loading ? <CircularProgress size={20} /> : "Decode VIN"}
          </Button>

          {error && <Alert severity="error">{error}</Alert>}

          <TextField label="Make" value={make} onChange={(e) => setMake(e.target.value)} />
          <TextField label="Model" value={model} onChange={(e) => setModel(e.target.value)} />
          <TextField label="Year" value={year} onChange={(e) => setYear(Number(e.target.value))} />
          <TextField label="Trim" value={trim} onChange={(e) => setTrim(e.target.value)} />
          <TextField label="Mileage" value={mileage} onChange={(e) => setMileage(e.target.value)} />

          <Button variant="contained" onClick={handleAddCar}>
            Add Car
          </Button>

        </Box>
      </Paper>

      {/* VIN POPUP */}
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth>
        <DialogTitle>Decoded VIN Details</DialogTitle>

        <DialogContent>
          {vinData && (
            <Box>

              <Typography fontWeight="bold">Vehicle</Typography>
              <Typography>Make: {vinData.make || "—"}</Typography>
              <Typography>Model: {vinData.model || "—"}</Typography>
              <Typography>Year: {vinData.year || "—"}</Typography>
              <Typography>Trim: {vinData.trim || "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>Engine</Typography>
              <Typography>{vinData.engineDescription || "—"}</Typography>

              <Typography fontWeight="bold" mt={2}>Details</Typography>
              <Typography>Fuel: {vinData.fuelType || "—"}</Typography>
              <Typography>Transmission: {vinData.transmission || "—"}</Typography>
              <Typography>Drivetrain: {vinData.drivetrain || "—"}</Typography>

            </Box>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      <Typography variant="h6" mb={2}>
        Your Vehicles
      </Typography>

      {/* 🔥 SAFE RENDER */}
      {cars.length === 0 && (
        <Typography color="text.secondary">
          No cars found
        </Typography>
      )}

      {Array.isArray(cars) && cars.map((car) => (
        <Paper
          key={car.id}
          sx={{
            p: 2,
            mb: 2,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center"
          }}
        >
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