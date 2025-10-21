// src/components/Navbar.jsx
import React, { useState } from "react";
import { useSubscriber } from "../context/SubscriberContext";
import { useNavigate } from "react-router-dom";
import { Button } from "react-bootstrap";
import axios from "axios";

const Navbar = () => {
  const { setSubscribers } = useSubscriber();
  const [searchName, setSearchName] = useState("");
  const [searchId, setSearchId] = useState("");
  const navigate = useNavigate();

  const handleSearchByName = async (e) => {
    const value = e.target.value;
    setSearchName(value);

    if (value.trim() === "") {
      // if field is empty, load all subscribers
      fetchAllSubscribers();
      return;
    }

    try {
      const res = await axios.get(
        `http://localhost:8080/api/subscribers/search/name?name=${value}`
      );
      setSubscribers(res.data);
    } catch (err) {
      console.error("Error searching by name:", err);
    }
  };

  const handleSearchById = async (e) => {
    const value = e.target.value;
    setSearchId(value);

    if (value.trim() === "") {
      fetchAllSubscribers();
      return;
    }

    try {
      const res = await axios.get(
        `http://localhost:8080/api/subscribers/search/id?id=${value}`
      );
      setSubscribers(res.data ? [res.data] : []);
    } catch (err) {
      console.error("Error searching by ID:", err);
    }
  };

  const fetchAllSubscribers = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/subscribers");
      setSubscribers(res.data);
    } catch (err) {
      console.error("Error fetching all subscribers:", err);
    }
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark px-4 py-3 shadow">
      <div className="container-fluid">
        {/* Brand */}
        <span className="navbar-brand fs-3 fw-bold text-warning">POWER ZONE</span>

        {/* Search Fields */}
        <form className="d-flex ms-auto gap-3 align-items-center">
          <input
            type="text"
            className="form-control"
            placeholder="Search by ID"
            value={searchId}
            onChange={handleSearchById}
            style={{ width: "200px" }}
          />
          <input
            type="text"
            className="form-control"
            placeholder="Search by Name"
            value={searchName}
            onChange={handleSearchByName}
            style={{ width: "200px" }}
          />

          {/* Navigation Buttons */}

          <Button onClick={()=>navigate("/")}>الرئيسية</Button>

          <button
            type="button"
            className="btn btn-warning fw-bold"
            onClick={() => navigate("/renew")}
          >
            التجديد
          </button>



          <button
            type="button"
            className="btn btn-danger fw-bold"
            onClick={() => navigate("/expired")}
          >
            المنتهية
          </button>
        </form>
      </div>
    </nav>
  );
};

export default Navbar;
