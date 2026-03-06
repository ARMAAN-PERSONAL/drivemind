import axios from "axios"

const API = axios.create({
  baseURL: "http://localhost:8080/api"
})

export const getCars = () => API.get("/cars")

export const getCar = (id: number) =>
  API.get(`/cars/${id}`)

export const addCar = (car: any) =>
  API.post("/cars", car)

export const deleteCar = (id: number) =>
  API.delete(`/cars/${id}`)

export const updateMileage = (id: number, mileage: number) =>
  API.put(`/cars/${id}/mileage`, mileage)