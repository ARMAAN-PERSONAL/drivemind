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
export interface Car {
  id: number
  vin: string
  make?: string
  model?: string
  year?: number
  trim?: string
  currentMileage?: number
}

export const getCars = () => API.get<Car[]>("/cars")
export const addCar = (car: any) => API.post("/cars", car)
export const deleteCar = (id: number) => API.delete(`/cars/${id}`)

// 📊 Dashboard API
export interface DashboardSummary {
  vehicleCount: number
  fuelLogCount: number
  maintenanceLogCount: number
  maintenanceRuleCount: number
  totalFuelSpend: number
}

export const getDashboardSummary = () =>
  API.get<DashboardSummary>("/dashboard/summary")

// 🤖 AI Insights API
export interface AiInsightResponse {
  summary: string
  recommendation: string
  overallStatus: "HEALTHY" | "DUE_SOON" | "OVERDUE"
  healthScore: number
  aiGenerated: boolean
}

export const getAiInsight = (carId: number) =>
  API.get<AiInsightResponse>(`/insights/${carId}/ai-summary`)

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

// ⛽ Fuel APIs
export const getFuelLogs = (carId: number) =>
  API.get(`/fuel/${carId}`)

export const addFuelLog = (carId: number, log: any) =>
  API.post(`/fuel/${carId}`, log)

// 🔧 Maintenance Rules APIs
export interface MaintenanceRule {
  id: number
  serviceType: string
  intervalKm: number
  description?: string
  car?: any
}

export const getMaintenanceRules = (carId: number) =>
  API.get<MaintenanceRule[]>(`/maintenance-rules/${carId}`)

export const addMaintenanceRule = (
  carId: number,
  rule: {
    serviceType: string
    intervalKm: number
    description?: string
  }
) => API.post(`/maintenance-rules/${carId}`, rule)

export const deleteMaintenanceRule = (ruleId: number) =>
  API.delete(`/maintenance-rules/${ruleId}`)