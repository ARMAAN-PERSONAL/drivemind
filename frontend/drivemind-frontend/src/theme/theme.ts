import { createTheme } from "@mui/material/styles"

export const getTheme = (mode: "light" | "dark") =>
  createTheme({
    palette: {
      mode,
      primary: {
        main: "#1E88E5"
      },
      background: {
        default: mode === "dark" ? "#0B1220" : "#F6F9FC",
        paper: mode === "dark" ? "#111827" : "#FFFFFF"
      }
    },
    shape: {
      borderRadius: 12
    }
  })