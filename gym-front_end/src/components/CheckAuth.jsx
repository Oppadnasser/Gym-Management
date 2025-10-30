import axios from "axios";
import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";


export default function useCheckAuth(){
    const navigate = useNavigate();
      useEffect(()=>{
        const checkAuth = ()=>{
            try {
         axios.get("http://localhost:8080/api/admin/check", {
          withCredentials: true,
        }).catch((error)=>{
            navigate("/"); // Redirect to login page
        })
      } catch (error) {
        console.error("Not logged in:", error);
      }
        }
        checkAuth();
      },[navigate])
};
