import { Box, IconButton } from "@mui/material";
import LightModeIcon from "@mui/icons-material/LightMode";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LogoutIcon from "@mui/icons-material/Logout";
import { useNavigate } from "react-router-dom";

const Topbar = ({
  mode,
  setMode,
}: {
  mode: string;
  setMode: (mode: string) => void;
}) => {
  const navigate = useNavigate();

  const toggleTheme = () => {
    setMode(mode === "light" ? "dark" : "light");
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/register");
  };

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "flex-end",
        p: 2,
        gap: 1,
      }}
    >
      <IconButton onClick={toggleTheme}>
        {mode === "light" ? <DarkModeIcon /> : <LightModeIcon />}
      </IconButton>

      <IconButton onClick={handleLogout} color="error">
        <LogoutIcon />
      </IconButton>
    </Box>
  );
};

export default Topbar;