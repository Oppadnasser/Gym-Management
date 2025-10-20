import { useSubscriber } from "../context/SubscriberContext";
import {React, useState, useEffect} from "react";
import axios from "axios";
import { Modal, Button, Form, Table } from "react-bootstrap";

export default function Expired(){
      const { subscribers, setSubscribers, subscriber, setSubscriber } = useSubscriber();

      const [message, setMessage] = useState("");
      const [showMessage, setShowMessage] = useState(false);
      const [today, setToday] = useState(false);
      const [months , setMonths] = useState(1);
      const [price, setPrice] = useState(0);
      

      useEffect(()=>{
        const fetchSubscribers = async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/subscribers/expired");
        setSubscribers(response.data);
      } catch (error) {
        console.error("Error fetching subscribers:", error);
      }
    };

    fetchSubscribers();
  }, [setSubscribers])

  const renew = async ()=>{
    await axios.put(`http://localhost:8080/api/subscribers/renew?id=${subscriber.id}&months=${months}&today=${today}&price=${price}`)
    .then((response)=>{
        setSubscribers(subscribers.filter(sub=>sub.id !== subscriber.id));
        setShowMessage(false);
    }).catch(error=>{
        console.log(error);
    })
  }

      return(
        <div className="container mt-4">
            <h3>الاشترلاكات المنتهية</h3>
            <Table striped bordered hover>
        <thead>
          <tr>
            <th></th>
            <th>Name</th>
            <th>Age</th>
            <th>Start Date</th>
            <th>End Date</th>
            <th>تجديد</th>
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
              <tr key={sub.id}>
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
                <td>{sub.subscriptionEnd}</td>
                <td><button className="btn btn-primary  align-items-center justify-content-center"
                 onClick={e=>{
                  e.stopPropagation();
                  setSubscriber(sub);
                  setShowMessage(true)}}>تجديد</button></td>
                </tr>
            )})
          )}
        </tbody>
      </Table>

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
              display:"grid",
              backgroundColor: "white",
              padding: "30px 40px",
              borderRadius: "12px",
              textAlign: "center",
              boxShadow: "0 4px 20px rgba(0,0,0,0.2)",
              minWidth: "300px",
            }}
          >
            <div className="mb-3">
            <label className="form-label fw-semibold me-2">اليوم</label>
                <input
                    type="checkbox"
                    checked={today}
                    onChange={(e) => setToday(e.target.checked)}
                    className="form-check-input me-2"
                />
            </div>

            <div className="mb-3">
            <label className="form-label fw-semibold">مدة الاشتراك (بالشهور)</label>
            <input
                type="number"
                min="1"
                className="form-control mt-1"
                placeholder="أدخل عدد الشهور"
                value={months}
                onChange={(e) => setMonths(e.target.value)}
            />
            </div>

            <button
              onClick={() => renew()}
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
            <button
              onClick={() => setShowMessage(false)}
              style={{
                marginTop:"20px",
                backgroundColor: "red",
                color: "white",
                border: "none",
                padding: "10px 20px",
                borderRadius: "8px",
                cursor: "pointer",
                fontSize: "16px",
              }}
            >
              إلغاء
            </button>
          </div>
        </div>
      )}
        </div>
      )
}