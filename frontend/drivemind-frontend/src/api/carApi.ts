import axios from "axios"

const API = axios.create({
  baseURL: "http://localhost:8080/api"
})

// 🔐 attach token
API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token")
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 🔐 auto logout
API.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("token")
      window.location.href = "/login"
    }
    return Promise.reject(err)
  }
)


// 🚗 Cars APIs
export const getCars = () => API.get("/cars")
export const addCar = (car: any) => API.post("/cars", car)
export const deleteCar = (id: number) => API.delete(`/cars/${id}`)


// 🔥 EXPORT THIS (IMPORTANT)
export interface VinData {
  make?: string
  model?: string
  year?: number
  trim?: string

  color?: string

  drivetrain?: string
  vehicleType?: string
  bodyType?: string

  engineConfiguration?: string
  engineCylinders?: number
  engineDisplacement?: number
  enginePower?: number

  fuelType?: string
  transmission?: string

  engineDescription?: string

  status: "SUCCESS" | "PARTIAL" | "FAILED"
  message?: string
  dataSource?: string
  hasCoreInfo?: boolean
  hasEngineInfo?: boolean
}


// 🔥 VIN DECODE
export const decodeVin = async (vin: string): Promise<VinData> => {
  const res = await API.get(`/vin/${vin}`)
  return res.data
}