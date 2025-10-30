import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import { useSubscriber } from "../context/SubscriberContext";

const LoginPage = () => {
  const {setAdmin} = useSubscriber();
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await axios.post(
        "http://localhost:8080/api/admin/login",
        { userName, password },
        { withCredentials: true } // 🔥 important: sends/receives cookies
      );

      if (response.status === 200) {
        setAdmin(response.data);
        navigate("/home"); // ✅ go to home page
      }
    } catch (err) {
      console.error(err);
      setError("Invalid username or password");
    }
  };

  useEffect(()=>{
        axios.get("http://localhost:8080/api/admin/check", {
          withCredentials: true,
        }).then((response)=>{
            setAdmin(response.data);
            navigate("/home");
        }).catch(error=>{
            navigate("/");
        })
  },[])

  return (
    <div 
  className="d-flex justify-content-center align-items-center vh-100"
  style={{
    backgroundImage: "url('power zone.png')",
    backgroundSize: "cover",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    position: "relative",
  }}
>
  {/* Dark overlay */}
  <div
    style={{
      position: "absolute",
      top: 0,
      left: 0,
      width: "100%",
      height: "100%",
      backgroundColor: "rgba(0, 0, 0, 0.6)", // 60% dark overlay
      zIndex: 1,
    }}
  ></div>

  {/* Login Card */}
  <div className=" p-5 rounded shadow col-md-4" style={{ zIndex: 2,backgroundColor: "rgba(88, 87, 87, 0.8)" }}>
    <h2 className="text-center text-warning mb-4">Admin Login</h2>

    <form onSubmit={handleLogin}>
      <div className="mb-3">
        <label className="form-label">Username</label>
        <input
          type="text"
          className="form-control"
          value={userName}
          onChange={(e) => setUserName(e.target.value)}
          required
        />
      </div>

      <div className="mb-3">
        <label className="form-label">Password</label>
        <input
          type="password"
          className="form-control"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </div>

      {error && <p className="text-danger text-center">{error}</p>}

      <button type="submit" className="btn btn-warning w-100 fw-bold">
        Login
      </button>
    </form>
  </div>
</div>


  );
};

export default LoginPage;
