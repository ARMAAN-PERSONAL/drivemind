import { useState } from "react";
import { loginUser } from "../api/authApi";
import { useNavigate, Link } from "react-router-dom";
import { Box, TextField, Button, Typography, Paper } from "@mui/material";
import logo from "../assets/LOGO.png";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const data = await loginUser(email, password);

      const token = data.accessToken || data.token;

      localStorage.setItem("token", token);

      navigate("/"); // ✅ clean redirect (no reload)
    } catch (err: any) {
      alert(err.response?.data?.message || "Login failed");
    }
  };

  return (
    <Box
      sx={{
        height: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#0f172a",
      }}
    >
      <Paper sx={{ p: 5, width: 350, borderRadius: 3 }}>
        <Box textAlign="center" mb={2}>
          <img src={logo} alt="logo" style={{ width: 80 }} />
          <Typography variant="h5" mt={1}>
            DriveMind
          </Typography>
        </Box>

        <TextField
          label="Email"
          fullWidth
          margin="normal"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <TextField
          label="Password"
          type="password"
          fullWidth
          margin="normal"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Button
          variant="contained"
          fullWidth
          sx={{ mt: 2 }}
          onClick={handleLogin}
        >
          Login
        </Button>

        <Typography mt={2} textAlign="center">
          Don’t have an account? <Link to="/register">Register</Link>
        </Typography>
      </Paper>
    </Box>
  );
}