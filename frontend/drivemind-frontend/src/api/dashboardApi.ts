import axios from "axios"

const API = axios.create({
  baseURL: "http://localhost:8080/api"
})

export const getStats = () =>
  API.get("/dashboard/stats")