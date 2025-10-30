// src/components/Navbar.jsx
import React, { useEffect, useState } from "react";
import { useSubscriber } from "../context/SubscriberContext";
import { useNavigate } from "react-router-dom";
import { Button , Modal, Form } from "react-bootstrap";
import axios from "axios";

const Navbar = () => {
  const { setSubscribers, searchType, admin, setAdmin} = useSubscriber();
  const [searchName, setSearchName] = useState("");
  const [searchId, setSearchId] = useState("");
  const navigate = useNavigate();
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [showUpdatePassword, setShowUpdatePassword] = useState(false);
  const [adminName, setAdminName] = useState("");

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
        `http://localhost:8080/api/subscribers/search/name?name=${value}&type=${searchType}`,{withCredentials:true}
      );
      setSubscribers(res.data);
    } catch (err) {
      console.error("Error searching by name:", err);
    }
  };

  const handleClose = () => {
    setShowUpdatePassword(false);
    setOldPassword("");
    setNewPassword("");
    setConfirmPassword("");
    setAdminName(admin.userName)
    setError("");
    setSuccess("");
  };

  const updatePassword = async () => {
    // ✅ Frontend validation
    if (!oldPassword || !newPassword || !confirmPassword || !adminName) {
      setError("All fields are required.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("New passwords do not match.");
      return;
    }

    try {
      const body = {
        oldPassword,
        newPassword,
        adminName
      }
      // Example request to backend API
      axios.put(`http://localhost:8080/api/admin/update`,body,{withCredentials:true})
      .then((response)=>{
        setAdmin(response.data);
        setSuccess("Password updated successfully!");
        setError("");
        setTimeout(handleClose, 1500); // Close after success
      }).catch(error=>{
        console.log(error);
      })

    } catch (err) {
      setError("Incorrect old password or server error.");
      setSuccess("");
    }
  };

  useEffect(()=>{
    axios.get("http://localhost:8080/api/admin/check", {
          withCredentials: true,
        }).then((response)=>{
            setAdmin(response.data);
            setAdminName(response.data.userName);
        }).catch(error=>{
           console.log(error);
           navigate("/");
        })
    
  },[])

  const handleSearchById = async (e) => {
    const value = e.target.value;
    setSearchId(value);

    if (value.trim() === "") {
      fetchAllSubscribers();
      return;
    }

    try {
      const res = await axios.get(
        `http://localhost:8080/api/subscribers/search/id?id=${value}&type=${searchType}`,{withCredentials:true}
      );
      setSubscribers(res.data ? [res.data] : []);
    } catch (err) {
      console.error("Error searching by ID:", err);
    }
  };

  const logout = async ()=>{
    await axios.post(`http://localhost:8080/api/admin/logout`,null,{withCredentials:true}).then(
      (response)=>{
        navigate('/');
      }
    ).catch(error=>{
      console.log(error);
      navigate("/");
    })
  }

  const fetchAllSubscribers = async () => {
    try {
      if(searchType === "expired"){
        const response = await axios.get("http://localhost:8080/api/subscribers/expired",{withCredentials:true});
        setSubscribers(response.data);
      } else if(searchType === "renew"){
        const response = await axios.get("http://localhost:8080/api/subscribers/expiring-soon",{withCredentials:true});
        setSubscribers(response.data);
      }else{
        const res = await axios.get("http://localhost:8080/api/subscribers",{withCredentials:true});
        setSubscribers(res.data);
      }
    } catch (err) {
      console.error("Error fetching all subscribers:", err);
    }
  };

  return (
    <nav className="navbar d-block navbar-expand-lg navbar-dark bg-dark px-4 py-3 shadow">
      <div className="container-fluid">
        {/* Brand */}
        <span className="navbar-brand fs-3 fw-bold text-warning">POWER ZONE</span>
        {(admin !== null && admin.name !== "user")&&<Button onClick={()=>navigate("/home/summary")}>الاحصاءيات</Button>}

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

          <Button onClick={()=>navigate("/home")}>الرئيسية</Button>

          <button
            type="button"
            className="btn btn-warning fw-bold"
            onClick={() => navigate("/home/renew")}
          >
            التجديد
          </button>



          <button
            type="button"
            className="btn btn-danger fw-bold"
            onClick={() => navigate("/home/expired")}
          >
            المنتهية
          </button>
        </form>
      </div>
      <div className="d-flex justify-content-between align-items-center mt-3 px-4">
        {/* Left side: Name */}
        <span className="text-light fs-5 fw-semibold">{admin===null ? "": admin.name}</span>

        {/* Right side: Buttons */}
        <div className="d-flex gap-2">
          <button 
            type="button"
            className="btn btn-warning fw-bold"
            onClick={()=>{setShowUpdatePassword(true)}}
          >
            كلمة السر
          </button>
          <button 
            type="button"
            className="btn btn-danger fw-bold"
            title="logout"
            onClick={logout}
          >
            خروج
          </button>
        </div>
      </div>

    <Modal show={showUpdatePassword} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>تعديل البيانات</Modal.Title>
      </Modal.Header>

      <Modal.Body>
        {error && <p className="text-danger fw-bold">{error}</p>}
        {success && <p className="text-success fw-bold">{success}</p>}

        <Form>
          <Form.Group className="mb-3 text-end">
            <Form.Label>اسم المستخدم</Form.Label>
            <Form.Control
              type="text"
              value={adminName}
              onChange={(e) => setAdminName(e.target.value)}
              placeholder="user name"
            />
          </Form.Group>

          <Form.Group className="mb-3 text-end">
            <Form.Label>كلمة المرور</Form.Label>
            <Form.Control
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              placeholder="Enter old password"
            />
          </Form.Group>

          <Form.Group className="mb-3 text-end">
            <Form.Label>كلمة المرور الجديدة</Form.Label>
            <Form.Control
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Enter new password"
            />
          </Form.Group>

          <Form.Group className="mb-3 text-end">
            <Form.Label>تأكيد كلمة المرور</Form.Label>
            <Form.Control
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter new password"
            />
          </Form.Group>
        </Form>
      </Modal.Body>

      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          إلغاء
        </Button>
        <Button variant="warning" onClick={updatePassword}>
          تعديل
        </Button>
      </Modal.Footer>
    </Modal>
      
    </nav>
  );
};

export default Navbar;
