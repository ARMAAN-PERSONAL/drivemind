import { useState } from "react";
import { registerUser } from "../api/authApi";
import { useNavigate, Link } from "react-router-dom";
import { Box, TextField, Button, Typography, Paper } from "@mui/material";
import logo from "../assets/LOGO.png";

export default function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleRegister = async () => {
    try {
      await registerUser(fullName, email, password);

      alert("Registered successfully! Now login.");

      navigate("/login"); // ✅ flow continues
    } catch (err: any) {
      alert(err.response?.data?.message || "Register failed");
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
          label="Full Name"
          fullWidth
          margin="normal"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
        />

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
          onClick={handleRegister}
        >
          Register
        </Button>

        <Typography mt={2} textAlign="center">
          Already have an account? <Link to="/login">Login</Link>
        </Typography>
      </Paper>
    </Box>
  );
}