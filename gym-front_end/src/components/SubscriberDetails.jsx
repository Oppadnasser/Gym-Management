import React, { useContext, useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useSubscriber } from "../context/SubscriberContext";
import axios from "axios";

export default function SubscriberDetails() {
  const navigate = useNavigate();
  const { id } = useParams();
  const {subscriber, setSubscriber} = useSubscriber();
  const [attendance, setAttendance] = useState([]);
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showEdit, setShowEdit] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [updatedData, setUpdatedData] = useState({});
  const [reload, setReload] = useState(false);
  const [photo, setPhoto] = useState(null);



  const handleUpdate = async () => {
  const formData = new FormData();
  formData.append("subscriber",JSON.stringify(updatedData));
  if (photo !== null && photo[0] instanceof File) {
      formData.append("photo", photo[0]);
  }

  try {
      await axios.put(`http://localhost:8080/api/subscribers/update`, formData, {
      headers: { "Content-Type": "multipart/form-data" }, withCredentials:true
      });
      setShowEdit(false);
      setReload(!reload);
      alert("Subscriber updated successfully!");
  } catch (err) {
      console.error("Error updating subscriber:", err);
  }
  };



 


  const handleDelete = async () => {
    try {
      await axios.delete(`http://localhost:8080/api/subscribers/${id}`);
      alert("Subscriber deleted successfully!");
      navigate(-1);
    } catch (err) {
      console.error("Error deleting subscriber:", err);
    }
  };


    useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(`http://localhost:8080/api/subscribers/${id}`);
        setSubscriber(res.data.subscriber);
        setAttendance(res.data.attendanceHistory);
        setSubscriptions(res.data.subscriptionHistory);
        setUpdatedData(res.data.subscriber);
        console.log(res.data);
      } catch (err) {
        console.error("Error fetching subscriber:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id,reload]);
  if (loading) return <p className="text-center mt-5">Loading...</p>;
  if (!subscriber) return <p className="text-center mt-5 text-danger">Subscriber not found</p>;

  return (
    <div className="container mt-5">
      <div className="card shadow-lg p-4">
        <div className="d-flex align-items-center mb-4">
          <img
            src={`data:image/jpeg;base64,${subscriber.photo_url}`}
            alt="Subscriber"
            style={{ width: "120px", height: "120px", borderRadius: "50%", objectFit: "cover" }}
          />
          <div className="ms-4">
            <h3>{subscriber.name}</h3>
            <p>ID: {subscriber.id}</p>
            <p>Phone: {subscriber.phone}</p>
            <p>Age: {subscriber.age}</p>
          </div>
          <div className="ms-auto">
            <button
              className="btn btn-warning me-2"
              onClick={() => setShowEdit(true)}
            >
              Update
            </button>
            <button
              className="btn btn-danger"
              onClick={() => setShowDeleteConfirm(true)}
            >
              Delete
            </button>
          </div>
        </div>

        {/* Attendance Table */}
        <h4 className="mt-4">Attendance History</h4>
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Date</th>
              <th>Time In</th>
              <th>Time Out</th>
            </tr>
          </thead>
          <tbody>
            {attendance.map((a, index) => {
                return(
              <tr key={index}>
                <td>{a.attendance_date}</td>
                <td>{a.time_in}</td>
                <td>{a.time_out}</td>
              </tr>
            )})}
          </tbody>
        </table>

        {/* Subscriptions Table */}
        <h4 className="mt-4">Subscription History</h4>
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Price</th>
            </tr>
          </thead>
          <tbody>
            {subscriptions.map((s, index) => (
              <tr key={index}>
                <td>{s.start_date}</td>
                <td>{s.end_date}</td>
                <td>{s.price}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 🟨 Update Modal */}
      {showEdit && (
        <div className="modal show d-block" tabIndex="-1">
            <div className="modal-dialog">
            <div className="modal-content">
                <div className="modal-header">
                <h5 className="modal-title">Update Subscriber</h5>
                <button
                    className="btn-close"
                    onClick={() => setShowEdit(false)}
                ></button>
                </div>

                <div className="modal-body">
                {/* Photo Upload */}
                <div className="text-center mb-3">
                    <img
                    src={
                        photo? photo[0]
                            ?URL.createObjectURL(photo[0])
                        : `${`data:image/jpeg;base64,${subscriber.photo_url}`}` : `${`data:image/jpeg;base64,${subscriber.photo_url}`}`
                    }
                    alt="Subscriber"
                    style={{
                        width: "120px",
                        height: "120px",
                        borderRadius: "50%",
                        objectFit: "cover",
                    }}
                    />
                    <input
                    type="file"
                    className="form-control mt-2"
                    accept="image/*"
                    onChange={(e) =>
                        setPhoto(e.target.files)
                    }
                    />
                </div>

                {/* Text Fields */}
                <input
                    type="text"
                    className="form-control mb-2"
                    value={updatedData.name}
                    onChange={(e) =>
                    setUpdatedData({ ...updatedData, name: e.target.value })
                    }
                    placeholder="Name"
                />
                <input
                    type="text"
                    className="form-control mb-2"
                    value={updatedData.phone}
                    onChange={(e) =>
                    setUpdatedData({ ...updatedData, phone: e.target.value })
                    }
                    placeholder="Phone"
                />
                <input
                    type="number"
                    className="form-control mb-2"
                    value={updatedData.age}
                    onChange={(e) =>
                    setUpdatedData({ ...updatedData, age: e.target.value })
                    }
                    placeholder="Age"
                />
                <input
                    type="date"
                    className="form-control mb-2"
                    value={updatedData.subscriptionStart}
                    onChange={(e) =>
                    setUpdatedData({ ...updatedData, subscriptionStart: e.target.value })
                    }
                />
                <input
                    type="date"
                    className="form-control mb-2"
                    value={updatedData.subscriptionEnd}
                    onChange={(e) =>
                    setUpdatedData({ ...updatedData, subscriptionEnd: e.target.value })
                    }
                />
                </div>

                <div className="modal-footer">
                <button
                    className="btn btn-secondary"
                    onClick={() => {setShowEdit(false);
                        setUpdatedData(subscriber);
                        console.log(photo);
                        setPhoto(null);
                        

                    }}
                >
                    Cancel
                </button>
                <button className="btn btn-primary" onClick={handleUpdate}>
                    Save Changes
                </button>
                </div>
            </div>
            </div>
        </div>
)}


      {/* 🟥 Delete Confirmation */}
      {showDeleteConfirm && (
        <div className="modal show d-block" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header bg-danger text-white">
                <h5 className="modal-title">Confirm Deletion</h5>
                <button className="btn-close" onClick={() => setShowDeleteConfirm(false)}></button>
              </div>
              <div className="modal-body">
                Are you sure you want to delete <b>{subscriber.name}</b>?
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setShowDeleteConfirm(false)}>Cancel</button>
                <button className="btn btn-danger" onClick={handleDelete}>Yes, Delete</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
