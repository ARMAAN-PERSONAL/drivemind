import axios from "axios";

const API = "http://localhost:8080/api/auth";

export const loginUser = async (email: string, password: string) => {
  const res = await axios.post(`${API}/login`, {
    email,
    password,
  });

  return res.data;
};

export const registerUser = async (
  fullName: string,
  email: string,
  password: string
) => {
  const res = await axios.post(`${API}/register`, {
    fullName,
    email,
    password,
  });

  return res.data;
};