import { useEffect, useState } from "react"
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  MenuItem,
  Select,
  InputLabel,
  FormControl
} from "@mui/material"

import { getCars, addCar, deleteCar } from "../api/carApi"



const carModels: Record<string, string[]> = {

  Acura:["ILX","Integra","TLX","RDX","MDX"],

  "Alfa Romeo":["Giulia","Stelvio","Tonale"],

  "Aston Martin":["Vantage","DB11","DB12","DBX"],

  Audi:["A3","A4","A5","A6","A7","A8","Q3","Q5","Q7","Q8","e-tron"],

  Bentley:["Bentayga","Continental GT","Flying Spur"],

  BMW:["2 Series","3 Series","4 Series","5 Series","7 Series","8 Series","X1","X3","X5","X7","i4","i7","iX"],

  Buick:["Encore","Encore GX","Envision","Enclave"],

  Cadillac:["CT4","CT5","XT4","XT5","XT6","Escalade","Lyriq"],

  Chevrolet:["Spark","Malibu","Camaro","Corvette","Trailblazer","Equinox","Blazer","Traverse","Tahoe","Suburban","Silverado"],

  Chrysler:["300","Pacifica"],

  Dodge:["Charger","Challenger","Durango","Hornet"],

  Ferrari:["Roma","F8","296 GTB","SF90","Purosangue"],

  Fiat:["500","500X"],

  Ford:["Mustang","Escape","Bronco","Explorer","Expedition","F-150","Ranger","Maverick"],

  Genesis:["G70","G80","G90","GV60","GV70","GV80"],

  GMC:["Terrain","Acadia","Yukon","Canyon","Sierra"],

  Honda:["Civic","Accord","CR-V","Pilot","HR-V","Ridgeline","Odyssey"],

  Hyundai:["Elantra","Sonata","Venue","Kona","Tucson","Santa Fe","Palisade","Ioniq 5","Ioniq 6"],

  INEOS:["Grenadier"],

  Infiniti:["Q50","QX50","QX55","QX60","QX80"],

  Jaguar:["XE","XF","F-Type","E-Pace","F-Pace","I-Pace"],

  Jeep:["Compass","Cherokee","Grand Cherokee","Wrangler","Gladiator"],

  Kia:["Forte","K5","Soul","Seltos","Sportage","Sorento","Telluride","EV6"],

  Lamborghini:["Huracan","Revuelto","Urus"],

  "Land Rover":["Discovery","Discovery Sport","Range Rover","Range Rover Sport","Range Rover Velar","Range Rover Evoque"],

  Lexus:["IS","ES","LS","UX","NX","RX","GX","LX"],

  Lincoln:["Corsair","Nautilus","Aviator","Navigator"],

  Lotus:["Emira","Eletre"],

  Lucid:["Air","Gravity"],

  Maserati:["Ghibli","Quattroporte","Levante","MC20"],

  Mazda:["Mazda3","Mazda6","CX-30","CX-5","CX-9","CX-90","MX-5 Miata"],

  McLaren:["570S","720S","750S","Artura"],

  "Mercedes-Benz":["A-Class","C-Class","E-Class","S-Class","CLA","CLS","GLA","GLB","GLC","GLE","GLS","EQB","EQE","EQS"],

  MINI:["Cooper","Clubman","Countryman"],

  Mitsubishi:["Mirage","Outlander","Outlander Sport"],

  Nissan:["Versa","Sentra","Altima","Maxima","Kicks","Rogue","Murano","Pathfinder","Frontier","Titan","Z","GT-R"],

  Polestar:["Polestar 2","Polestar 3","Polestar 4"],

  Porsche:["718 Cayman","718 Boxster","911","Taycan","Macan","Cayenne","Panamera"],

  Ram:["1500","2500","3500","ProMaster"],

  Rivian:["R1T","R1S"],

  "Rolls-Royce":["Ghost","Phantom","Cullinan","Spectre"],

  Subaru:["Impreza","WRX","Legacy","Outback","Forester","Crosstrek","Ascent","BRZ"],

  Tesla:["Model 3","Model S","Model X","Model Y","Cybertruck"],

  Toyota:["Corolla","Camry","Prius","Supra","GR86","C-HR","RAV4","Highlander","Sequoia","Tacoma","Tundra","Sienna"],

  VinFast:["VF6","VF7","VF8","VF9"],

  Volkswagen:["Jetta","Passat","Golf","GTI","Tiguan","Atlas","ID.4"],

  Volvo:["S60","S90","XC40","XC60","XC90","EX30","EX90"]
}



const years = Array.from({ length: 30 }, (_, i) => new Date().getFullYear() - i)



export default function Cars() {

  const [cars, setCars] = useState<any[]>([])

  const [vin,setVin]=useState("")
  const [make,setMake]=useState("")
  const [model,setModel]=useState("")
  const [year,setYear]=useState<number | "">("")
  const [trim,setTrim]=useState("")
  const [mileage,setMileage]=useState("")



  const loadCars=async()=>{
    const res=await getCars()
    setCars(res.data)
  }

  useEffect(()=>{
    loadCars()
  },[])



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



          <FormControl>

            <InputLabel>Make</InputLabel>

            <Select
              value={make}
              label="Make"
              onChange={(e)=>{
                setMake(e.target.value)
                setModel("")
              }}
            >

              {Object.keys(carModels).map((m)=>(
                <MenuItem key={m} value={m}>
                  {m}
                </MenuItem>
              ))}

            </Select>

          </FormControl>



          <FormControl disabled={!make}>

            <InputLabel>Model</InputLabel>

            <Select
              value={model}
              label="Model"
              onChange={(e)=>setModel(e.target.value)}
            >

              {(carModels[make] || []).map((m)=>(
                <MenuItem key={m} value={m}>
                  {m}
                </MenuItem>
              ))}

            </Select>

          </FormControl>



          <FormControl>

            <InputLabel>Year</InputLabel>

            <Select
              value={year}
              label="Year"
              onChange={(e)=>setYear(Number(e.target.value))}
            >

              {years.map((y)=>(
                <MenuItem key={y} value={y}>
                  {y}
                </MenuItem>
              ))}

            </Select>

          </FormControl>



          <TextField
            label="Trim"
            value={trim}
            onChange={(e)=>setTrim(e.target.value)}
          />


          <TextField
            label="Mileage"
            value={mileage}
            onChange={(e)=>setMileage(e.target.value)}
          />



          <Button
            variant="contained"
            onClick={handleAddCar}
          >
            Add Car
          </Button>



        </Box>

      </Paper>



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

          <Button
            color="error"
            onClick={()=>handleDelete(car.id)}
          >
            Delete
          </Button>

        </Paper>
      ))}

    </Box>

  )
}