import React, { useState, useEffect } from "react";
import { useSubscriber } from "../context/SubscriberContext";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Modal, Button, Form, Table } from "react-bootstrap";
import {FaCheck} from "react-icons/fa";
export default function CurrentSubscribers() {
  const navigate = useNavigate();
  const { subscribers, setSubscribers } = useSubscriber();
  const [message, setMessage] = useState("");
  const [price, setPrice] = useState(0);
  const [showMessage, setShowMessage] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [newSubscriber, setNewSubscriber] = useState({
    id:null,
    name: "",
    phone: "",
    age: "",
    months: 1,
  });
  const [photo, setPhoto] = useState(null); // ✅ added photo state
  
  const [clicked, setClicked] = useState(false);

  // 🔹 Fetch all subscribers when component loads
  useEffect(() => {
    const fetchSubscribers = async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/subscribers");
        setSubscribers(response.data);
        console.log(response.data[0].subscriptionStart);
      } catch (error) {
        console.error("Error fetching subscribers:", error);
      }
    };

    fetchSubscribers();
  }, [setSubscribers]);

  // 🔹 Handle text input
  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewSubscriber({ ...newSubscriber, [name]: value });
  };

  // 🔹 Handle file input
  const handlePhotoChange = (e) => {
    setPhoto(e.target.files[0]);
  };

  // 🔹 Handle adding subscriber
 const handleAddSubscriber = async () => {
  try {
    const startDate = new Date();
    const endDate = new Date();
    endDate.setMonth(endDate.getMonth() + Number(newSubscriber.months));

    const subscriberData = {
      name: newSubscriber.name,
      phone: newSubscriber.phone,
      address: newSubscriber.address,
      age: newSubscriber.age,
      subscriptionStart: startDate.toISOString().split("T")[0],
      subscriptionEnd: endDate.toISOString().split("T")[0],
    };
    console.log((subscriberData.subscriptionEnd))

    // 👇 correct multipart structure
    const formData = new FormData();
    formData.append("subscriber",JSON.stringify(subscriberData));
    if (photo) {
      formData.append("photo", photo);
    }

    const response = await axios.post(
      `http://localhost:8080/api/subscribers/new-subscriber?price=${price}`,
      formData,
      { withCredentials: true, headers: { "Content-Type": "multipart/form-data" } }
    );

    setSubscribers([...subscribers, response.data]);
    setShowModal(false);
    setPrice(0);
    setNewSubscriber({
      name: "",
      phone: "",
      address: "",
      age: "",
      months: 1,
    });
    setPhoto(null);
  } catch (error) {
    console.error("Error adding subscriber:", error);
  }
};


const enter = async (id,e) => {
  await axios.post(`http://localhost:8080/api/subscribers/enter?id=${id}`)
  .then((response)=>{
    e.target.innerHTML = "&#10003;"
    setTimeout(()=>{e.target.innerHTML = "حضور"}, 2000)
  }).catch(error=>{
    if(error.status == 409){
      console.log(error.status);
      setMessage(error.response.data);
      setShowMessage(true);
    }
  })
}
const exit = async (id,e)=>{
  await axios.put(`http://localhost:8080/api/subscribers/exit?id=${id}`)
  .then((response)=>{
    e.target.innerHTML = "&#10003;"
    setTimeout(()=>{e.target.innerHTML = "انصراف"}, 2000)
  }).catch(error=>{
    if(error.status == 404){
      setMessage(error.response.data);
      setShowMessage(true);
    }
    else{
      console.log(error);
    }
  })
}

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3>إدارة الاشتراكات</h3>
        <Button variant="primary" onClick={() => setShowModal(true)}>
          ➕ مشترك جديد
        </Button>
      </div>

      <Table striped bordered hover>
        <thead>
          <tr>
            <th>الصورة</th>
            <th>الاسم</th>
            <th>السن</th>
            <th>يبدأ</th>
            <th>ينتهي</th>
            <th>دخول</th>
            <th>خروج</th>
          </tr>
        </thead>
        <tbody>
          {subscribers.length === 0 ? (
            <tr>
              <td colSpan="9" className="text-center text-muted">
                No subscribers found.
              </td>
            </tr>
          ) : (
            subscribers.map((sub) => {
              return (
              <tr key={sub.id} onClick={()=>{
                navigate(`/${sub.id}`)
              }}>
                <td>
                  {sub.photo_url ? (
                    <img
                      src={`data:image/jpeg;base64,${sub.photo_url}`}
                      alt={sub.name}
                      style={{
                        width: "40px",
                        height: "40px",
                        objectFit: "cover",
                        borderRadius: "50%",
                      }}
                    />
                  ) : (
                    <span className="text-muted">No Photo</span>
                  )}
                </td>
                <td>{sub.name}</td>
                <td>{sub.age}</td>
                <td>{sub.subscriptionStart}</td>
                <td style={{color:`${sub.subscriptionEnd < new Date().toISOString().split('T')[0]? "red": "black"}`}}>{sub.subscriptionEnd}</td>
                <td><button className="btn btn-primary  align-items-center justify-content-center"
                 onClick={e=>{
                  e.stopPropagation();
                  enter(sub.id,e)}}
                  disabled={sub.subscriptionEnd < new Date().toISOString().split('T')[0]?"disabled": ""}
                  >حضور</button></td>
                <td><button className="btn btn-primary align-items-center justify-content-center"
                onClick={e=>{
                  e.stopPropagation();
                  exit(sub.id,e)}}
                  disabled={sub.subscriptionEnd < new Date().toISOString().split('T')[0]?"disabled": ""}
                  >انصراف</button></td>
              </tr>
            )})
          )}
        </tbody>
      </Table>

      {/* Add Subscriber Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Add New Subscriber</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-2">
              <Form.Label>Name</Form.Label>
              <Form.Control
                name="name"
                value={newSubscriber.name}
                onChange={handleChange}
              />
            </Form.Group>

            <Form.Group className="mb-2">
              <Form.Label>Phone</Form.Label>
              <Form.Control
                name="phone"
                value={newSubscriber.phone}
                onChange={handleChange}
              />
            </Form.Group>


            <Form.Group className="mb-2">
              <Form.Label>Age</Form.Label>
              <Form.Control
                type="number"
                name="age"
                value={newSubscriber.age}
                onChange={handleChange}
              />
            </Form.Group>

            <Form.Group className="mb-2">
              <Form.Label>Months</Form.Label>
              <Form.Control
                type="number"
                name="months"
                value={newSubscriber.months}
                onChange={handleChange}
              />
            </Form.Group>

            <Form.Group className="mb-2">
              <Form.Label>السعر</Form.Label>
              <Form.Control
                name="name"
                value={price}
                onChange={(e)=>setPrice(e.target.value)}
              />
            </Form.Group>

            {/* ✅ Add Photo Upload */}
            <Form.Group className="mb-2">
              <Form.Label>Photo</Form.Label>
              <Form.Control type="file" onChange={handlePhotoChange} accept="image/*" />
            </Form.Group>
          </Form>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Cancel
          </Button>
          <Button variant="success" onClick={handleAddSubscriber}>
            Save
          </Button>
        </Modal.Footer>
      </Modal>
      {showMessage && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100vw",
            height: "100vh",
            backgroundColor: "rgba(0, 0, 0, 0.5)", // overlay
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: "white",
              padding: "30px 40px",
              borderRadius: "12px",
              textAlign: "center",
              boxShadow: "0 4px 20px rgba(0,0,0,0.2)",
              minWidth: "300px",
            }}
          >
            <h2 style={{ marginBottom: "20px" }}>{message}</h2>
            <button
              onClick={() => setShowMessage(false)}
              style={{
                backgroundColor: "#007bff",
                color: "white",
                border: "none",
                padding: "10px 20px",
                borderRadius: "8px",
                cursor: "pointer",
                fontSize: "16px",
              }}
            >
              تم
            </button>
          </div>
        </div>
      )}

    </div>
  );
}
