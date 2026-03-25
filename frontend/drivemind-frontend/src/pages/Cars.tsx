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

  const [vin,setVin]=useState("")
  const [make,setMake]=useState("")
  const [model,setModel]=useState("")
  const [year,setYear]=useState<number | "">("")
  const [trim,setTrim]=useState("")
  const [mileage,setMileage]=useState("")

  // 🔥 VIN state
const [vinData,setVinData]=useState<any>(null)
  const [open,setOpen]=useState(false)
  const [loading,setLoading]=useState(false)
  const [error,setError]=useState("")

  const loadCars=async()=>{
    const res=await getCars()
    setCars(res.data)
  }

  useEffect(()=>{
    loadCars()
  },[])

  // 🔥 DECODE VIN (LEVEL 2)
  const handleDecodeVin=async()=>{
    if(!vin) return alert("Enter VIN")

    try{
      setLoading(true)
      setError("")

      const data = await decodeVin(vin)

      setVinData(data)
      setOpen(true)

      // 🔥 SAFE AUTO-FILL
      setMake(data.make || "")
      setModel(data.model || "")
      setYear(data.year || "")
      setTrim(data.trim || "")

    }catch(err:any){
      setError(err.message || "VIN decode failed")
    }finally{
      setLoading(false)
    }
  }

  const handleAddCar=async()=>{

    const car={
      vin,
      make,
      model,
      year,
      trim,
      currentMileage:parseInt(mileage)
    }

    await addCar(car)

    setVin("")
    setMake("")
    setModel("")
    setYear("")
    setTrim("")
    setMileage("")

    loadCars()
  }

  const handleDelete=async(id:number)=>{
    await deleteCar(id)
    loadCars()
  }

  return(
    <Box>

      <Typography variant="h4" mb={3}>
        Cars
      </Typography>

      <Paper sx={{p:3,mb:4}}>

        <Typography variant="h6" mb={2}>
          Add Vehicle
        </Typography>

        <Box sx={{
          display:"grid",
          gridTemplateColumns:"repeat(auto-fit,minmax(200px,1fr))",
          gap:2
        }}>

          <TextField
            label="VIN"
            value={vin}
            onChange={(e)=>setVin(e.target.value)}
          />

          <Button
            variant="outlined"
            onClick={handleDecodeVin}
          >
            {loading ? <CircularProgress size={20}/> : "Decode VIN"}
          </Button>

          {/* 🔥 ERROR DISPLAY */}
          {error && (
            <Alert severity="error">{error}</Alert>
          )}

          <TextField label="Make" value={make} onChange={(e)=>setMake(e.target.value)} />
          <TextField label="Model" value={model} onChange={(e)=>setModel(e.target.value)} />
          <TextField label="Year" value={year} onChange={(e)=>setYear(Number(e.target.value))} />
          <TextField label="Trim" value={trim} onChange={(e)=>setTrim(e.target.value)} />
          <TextField label="Mileage" value={mileage} onChange={(e)=>setMileage(e.target.value)} />

          <Button variant="contained" onClick={handleAddCar}>
            Add Car
          </Button>

        </Box>
      </Paper>

      {/* 🔥 VIN POPUP (LEVEL 2) */}
      <Dialog open={open} onClose={()=>setOpen(false)} fullWidth>

        <DialogTitle>Decoded VIN Details</DialogTitle>

        <DialogContent>

          {vinData && (

            <Box>

              {/* 🔥 STATUS ALERT */}
              {vinData.status === "PARTIAL" && (
                <Alert severity="warning" sx={{ mb:2 }}>
                  {vinData.message || "Partial VIN data available"}
                </Alert>
              )}

              {vinData.status === "SUCCESS" && (
                <Alert severity="success" sx={{ mb:2 }}>
                  VIN decoded successfully
                </Alert>
              )}

              {/* 🚗 VEHICLE */}
              <Typography fontWeight="bold" mt={1}>Vehicle</Typography>
              <Typography>Make: {vinData.make || "—"}</Typography>
              <Typography>Model: {vinData.model || "—"}</Typography>
              <Typography>Year: {vinData.year || "—"}</Typography>
              <Typography>Trim: {vinData.trim || "—"}</Typography>

              {/* ⚙️ ENGINE */}
              <Typography fontWeight="bold" mt={2}>Engine</Typography>
              <Typography>{vinData.engineDescription || "—"}</Typography>

              {/* 🧾 EXTRA */}
              <Typography fontWeight="bold" mt={2}>Details</Typography>
              <Typography>Fuel: {vinData.fuelType || "—"}</Typography>
              <Typography>Transmission: {vinData.transmission || "—"}</Typography>
              <Typography>Drivetrain: {vinData.drivetrain || "—"}</Typography>

            </Box>
          )}

        </DialogContent>

        <DialogActions>
          <Button onClick={()=>setOpen(false)}>Close</Button>
        </DialogActions>

      </Dialog>

      <Typography variant="h6" mb={2}>
        Your Vehicles
      </Typography>

      {cars.map((car)=>(
        <Paper
          key={car.id}
          sx={{
            p:2,
            mb:2,
            display:"flex",
            justifyContent:"space-between",
            alignItems:"center"
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

          <Button color="error" onClick={()=>handleDelete(car.id)}>
            Delete
          </Button>
        </Paper>
      ))}

    </Box>
  )
}