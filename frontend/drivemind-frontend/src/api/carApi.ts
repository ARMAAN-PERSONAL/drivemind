import axios from "axios"

const API = axios.create({
  baseURL: "http://localhost:8080/api"
})

// 🔥 attach token to EVERY request
API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token")
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 🔥 auto logout if token invalid
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

// existing APIs
export const getCars = () => API.get("/cars")
export const getCar = (id: number) => API.get(`/cars/${id}`)
export const addCar = (car: any) => API.post("/cars", car)
export const deleteCar = (id: number) => API.delete(`/cars/${id}`)
export const updateMileage = (id: number, mileage: number) =>
  API.put(`/cars/${id}/mileage`, mileage)