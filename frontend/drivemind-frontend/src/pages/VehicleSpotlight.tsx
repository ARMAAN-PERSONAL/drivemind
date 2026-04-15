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
  Button,
  useTheme
} from "@mui/material"
import OpenInNewIcon from "@mui/icons-material/OpenInNew"
import PhotoLibraryIcon from "@mui/icons-material/PhotoLibrary"
import SellIcon from "@mui/icons-material/Sell"
import DescriptionIcon from "@mui/icons-material/Description"
import ForumIcon from "@mui/icons-material/Forum"
import ReportProblemIcon from "@mui/icons-material/ReportProblem"

import {
  getCars,
  getVehicleSpotlight,
  type Car,
  type VehicleSpotlightResponse
} from "../api/carApi"

export default function VehicleSpotlight() {
  const theme = useTheme()
  const isDark = theme.palette.mode === "dark"

  const [cars, setCars] = useState<Car[]>([])
  const [selectedCarId, setSelectedCarId] = useState<number | "">("")
  const [loadingCars, setLoadingCars] = useState(true)
  const [loadingSpotlight, setLoadingSpotlight] = useState(false)
  const [error, setError] = useState("")
  const [spotlight, setSpotlight] = useState<VehicleSpotlightResponse | null>(null)

  useEffect(() => {
    loadCars()
  }, [])

  useEffect(() => {
    if (selectedCarId !== "") {
      loadSpotlight(selectedCarId)
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

  const loadSpotlight = async (carId: number) => {
    try {
      setLoadingSpotlight(true)
      setError("")
      const res = await getVehicleSpotlight(carId)
      setSpotlight(res.data)
    } catch (err) {
      console.error("Failed to load vehicle spotlight", err)
      setSpotlight(null)
      setError("Failed to load vehicle spotlight.")
    } finally {
      setLoadingSpotlight(false)
    }
  }

  const selectedCar = useMemo(
    () => cars.find((car) => car.id === selectedCarId) || null,
    [cars, selectedCarId]
  )

  const vehicleTitle = selectedCar
    ? `${selectedCar.year ?? ""} ${selectedCar.make ?? ""} ${selectedCar.model ?? ""} ${selectedCar.trim ?? ""}`
        .replace(/\s+/g, " ")
        .trim()
    : "Vehicle Spotlight"

  const vehicleSearchBase = vehicleTitle || "vehicle"
  const vehicleQuery = encodeURIComponent(vehicleSearchBase)
  const makeLower = (selectedCar?.make || "").toLowerCase()
  const modelLower = (selectedCar?.model || "").toLowerCase()

  const ownerDiscussionUrl = (() => {
    if (makeLower === "honda" && modelLower === "accord") {
      return `https://www.driveaccord.net/search/299681/?q=${vehicleQuery}&o=relevance`
    }

    if (makeLower === "honda" && modelLower === "civic") {
      return `https://www.civicx.com/forum/search/247781/?q=${vehicleQuery}&o=relevance`
    }

    if (makeLower === "subaru" && modelLower === "brz") {
      return `https://www.ft86club.com/forums/search.php?do=process&query=${vehicleQuery}`
    }

    if (makeLower === "volkswagen" && (modelLower === "gti" || modelLower === "gli" || modelLower === "golf")) {
      return `https://www.golfmk7.com/forums/index.php?search/1261428/?q=${vehicleQuery}&o=relevance`
    }

    if (makeLower === "bmw" && (modelLower.includes("3") || modelLower.includes("4"))) {
      return `https://g20.bimmerpost.com/forums/search.php?do=process&query=${vehicleQuery}`
    }

    return `https://www.reddit.com/search/?q=${vehicleQuery}%20owners%20forum`
  })()

  const exploreLinks = [
    {
      title: "Similar Photos",
      description: "Browse similar real-world examples and exterior shots.",
      icon: <PhotoLibraryIcon />,
      url: spotlight?.imageSearchUrl || `https://www.google.com/search?tbm=isch&q=${vehicleQuery}`
    },
    {
      title: "Market Listings",
      description: "See similar cars for sale and compare trim positioning.",
      icon: <SellIcon />,
      url: `https://www.autotrader.com/cars-for-sale/all-cars?searchRadius=0&keywordPhrases=${vehicleQuery}`
    },
   {
     title: "Reviews & Specs",
     description: "Read cleaner expert reviews and research pages for this vehicle.",
     icon: <DescriptionIcon />,
     url: `https://www.caranddriver.com/search/?q=${vehicleQuery}`
   },
    {
      title: "Owner Discussions",
      description: "Jump into communities, forums, and owner conversations.",
      icon: <ForumIcon />,
      url: ownerDiscussionUrl
    },
    {
      title: "Common Issues",
      description: "Research reliability trends and frequent complaints.",
      icon: <ReportProblemIcon />,
      url: `https://www.google.com/search?q=${vehicleQuery}%20common%20problems`
    }
  ]

  return (
    <Box>
      <Typography
        variant="h4"
        mb={3}
        fontWeight="bold"
        sx={{ color: theme.palette.text.primary }}
      >
        Vehicle Spotlight
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
            : "1px solid rgba(59,130,246,0.22)"
        }}
      >
        <Typography variant="h6" mb={1} sx={{ color: theme.palette.text.primary }}>
          Spotlight Generator
        </Typography>

        <Typography sx={{ mb: 3, color: theme.palette.text.secondary }}>
          Pick a vehicle to generate an AI spotlight and explore sharper research paths around that car.
        </Typography>

        {loadingCars ? (
          <CircularProgress size={28} />
        ) : cars.length === 0 ? (
          <Typography sx={{ color: theme.palette.text.secondary }}>
            No vehicles found. Add a vehicle first.
          </Typography>
        ) : (
          <FormControl fullWidth>
            <InputLabel id="spotlight-car-select-label">Vehicle</InputLabel>
            <Select
              labelId="spotlight-car-select-label"
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
              : "1px solid rgba(59,130,246,0.18)"
          }}
        >
          <Typography
            variant="h5"
            fontWeight="bold"
            mb={1}
            sx={{ color: theme.palette.text.primary }}
          >
            {vehicleTitle}
          </Typography>

          <Typography sx={{ mb: 3, color: theme.palette.text.secondary }}>
            VIN: {selectedCar.vin}
          </Typography>

          {loadingSpotlight ? (
            <Box sx={{ py: 5, display: "flex", justifyContent: "center" }}>
              <CircularProgress />
            </Box>
          ) : error ? (
            <Typography color="error">{error}</Typography>
          ) : spotlight ? (
            <Box sx={{ display: "grid", gap: 3 }}>
              <Paper
                elevation={0}
                sx={{
                  p: 3,
                  borderRadius: 3,
                  background: isDark ? "rgba(255,255,255,0.02)" : "rgba(255,255,255,0.88)",
                  border: isDark
                    ? "1px solid rgba(255,255,255,0.06)"
                    : "1px solid rgba(59,130,246,0.12)"
                }}
              >
                <Typography variant="h6" mb={1} sx={{ color: theme.palette.text.primary }}>
                  AI Spotlight Summary
                </Typography>
                <Typography sx={{ lineHeight: 1.9, color: theme.palette.text.secondary }}>
                  {spotlight.summary}
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
                <Typography variant="h6" mb={1} sx={{ color: theme.palette.text.primary }}>
                  Powertrain Note
                </Typography>
                <Typography sx={{ lineHeight: 1.8, color: theme.palette.text.primary }}>
                  {spotlight.powertrainNote}
                </Typography>
              </Paper>

              <Paper
                elevation={0}
                sx={{
                  p: 3,
                  borderRadius: 3,
                  background: isDark ? "rgba(255,255,255,0.02)" : "rgba(255,255,255,0.88)",
                  border: isDark
                    ? "1px solid rgba(255,255,255,0.06)"
                    : "1px solid rgba(59,130,246,0.12)"
                }}
              >
                <Typography variant="h6" mb={2} sx={{ color: theme.palette.text.primary }}>
                  Explore This Vehicle
                </Typography>

                <Box
                  sx={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fit,minmax(220px,1fr))",
                    gap: 2
                  }}
                >
                  {exploreLinks.map((item) => (
                    <Paper
                      key={item.title}
                      elevation={0}
                      sx={{
                        p: 2,
                        borderRadius: 2,
                        background: isDark
                          ? "rgba(255,255,255,0.02)"
                          : "rgba(37,99,235,0.03)",
                        border: isDark
                          ? "1px solid rgba(255,255,255,0.06)"
                          : "1px solid rgba(59,130,246,0.10)",
                        display: "flex",
                        flexDirection: "column",
                        gap: 1.5
                      }}
                    >
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <Box sx={{ color: theme.palette.text.primary, display: "flex" }}>
                          {item.icon}
                        </Box>
                        <Typography
                          fontWeight="bold"
                          sx={{ color: theme.palette.text.primary }}
                        >
                          {item.title}
                        </Typography>
                      </Box>

                      <Typography
                        variant="body2"
                        sx={{ color: theme.palette.text.secondary, minHeight: 40 }}
                      >
                        {item.description}
                      </Typography>

                      <Button
                        variant="outlined"
                        endIcon={<OpenInNewIcon />}
                        href={item.url}
                        target="_blank"
                        rel="noreferrer"
                      >
                        Open
                      </Button>
                    </Paper>
                  ))}
                </Box>
              </Paper>
            </Box>
          ) : null}
        </Paper>
      )}
    </Box>
  )
}