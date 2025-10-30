import React, { useEffect, useState } from "react";
import axios from "axios";
import { Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useSubscriber } from "../context/SubscriberContext";

export default function GymSummary() {
  const { admin } = useSubscriber();
  const [authorized, setAuthorized] = useState(false);
  const [password, setPassword] = useState("");
  const [showPasswordModal, setShowPasswordModal] = useState(true);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  
  const [logs, setLogs] = useState([]);

  const [activeSubscribers, setActiveSubscribers] = useState(0);
  const [revenue, setRevenue] = useState(0);
  const [dalyRevenue, setDalyRevenue] = useState(0);
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedDay, setSelectedDay] = useState(new Date().getDate());
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [days, setDays] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const months = [
    "يناير","فبراير","مارس","أبريل","مايو","يونيو",
    "يوليو","أغسطس","سبتمبر","أكتوبر","نوفمبر","ديسمبر"
  ];

  const years = [];
  for (let y = 2025; y <= new Date().getFullYear(); y++) years.push(y);

  const fetchRevenue = async () => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/subscribers/revenue?month=${selectedMonth}&year=${selectedYear}&day=${selectedDay}`,
        { withCredentials: true }
      );
      setRevenue(res.data.monthRevenue);
      setDalyRevenue(res.data.dayRevenue);
    } catch (err) {
      console.error("Error fetching summary:", err);
    }
  };

  const fetchSubscribers = async () => {
    try {
      const res = await axios.get(`http://localhost:8080/api/subscribers/active`, { withCredentials: true });
      setActiveSubscribers(res.data || 0);
    } catch (err) {
      console.error("Error fetching subscribers:", err);
    }
  };

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await axios.get("http://localhost:8080/api/admin/check", { withCredentials: true });
      } catch {
        navigate("/");
      }
    };
    checkAuth();
    fetchSubscribers();
    if(admin !== null && admin.name === "oppad"){
      axios.get("http://localhost:8080/api/admin/admin-names",{withCredentials:true})
      .then(response=>{
        console.log(response.data);
      }).catch(error=>{
        console.log(error);
      })
    }
  }, []);

  useEffect(() => {
    const fetchLogs = async () => {
    try {
      const formattedDate = `${selectedYear}-${String(selectedMonth).padStart(2, "0")}-${String(selectedDay).padStart(2, "0")}`;
      const response = await axios.get(
        `http://localhost:8080/api/admin/logs?date=${formattedDate}`,
        { withCredentials: true }
      );
      setLogs(response.data || []);
    } catch (error) {
      console.error("Error fetching logs:", error);
    }
  };
    fetchLogs();
    fetchRevenue();
    const daysInMonth = new Date(selectedYear, selectedMonth, 0).getDate();
    setDays(Array.from({ length: daysInMonth }, (_, i) => i + 1));
  }, [selectedMonth, selectedYear, selectedDay]);

  const handlePasswordSubmit = async () => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/admin/show-summary?password=${password}`,
        { withCredentials: true }
      );
      if (res.data === true) {
        setAuthorized(true);
        setShowPasswordModal(false);
      } else {
        alert("❌ Wrong password!");
      }
    } catch {
      alert("Error verifying password!");
    }
  };

  const handleUpdatePassword = async () => {
    if (newPassword !== confirmPassword) {
      alert("❌ New passwords do not match!");
      return;
    }

    try {
      await axios.put(
        "http://localhost:8080/api/admin/update-summary-password",
        { oldPassword, newPassword },
        { withCredentials: true }
      );
      alert("✅ Password updated successfully!");
      setShowUpdateModal(false);
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      console.error(err);
      alert("❌ Error updating password!");
    }
  };

  if (!authorized) {
    return (
      <div className="container text-center mt-5">
        {showPasswordModal && (
          <div className="modal d-block" tabIndex="-1">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content bg-dark text-light">
                <div className="modal-header">
                  <h5 className="modal-title text-warning">🔒 Enter Password</h5>
                </div>
                <div className="modal-body">
                  <input
                    type="password"
                    className="form-control"
                    placeholder="Enter password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                <div className="modal-footer">
                  <button className="btn btn-secondary" onClick={() => navigate("/")}>Cancel</button>
                  <button className="btn btn-warning" onClick={handlePasswordSubmit}>Submit</button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="container py-5 text-center">
      <h2 className="text-center mb-5 fw-bold text-warning">🏋️‍♂️ إحصائيات الجيم</h2>

      <div className="bg-dark text-light p-4 m-2 rounded shadow">
        <h3 className="text-warning mb-3">الاشتراكات السارية</h3>
        <h1 className="display-4 fw-bold">{activeSubscribers}</h1>
      </div>

      <div className="row justify-content-center mb-4">
        <div className="col-md-3">
          <label className="form-label fw-semibold text-light">السنة</label>
          <select className="form-select" value={selectedYear} onChange={(e) => setSelectedYear(e.target.value)}>
            {years.map((y) => <option key={y} value={y}>{y}</option>)}
          </select>
        </div>
        <div className="col-md-3">
          <label className="form-label fw-semibold text-light">الشهر</label>
          <select className="form-select" value={selectedMonth} onChange={(e) => setSelectedMonth(Number(e.target.value))}>
            {months.map((m, i) => <option key={i + 1} value={i + 1}>{m}</option>)}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="text-light mt-5">
          <div className="spinner-border text-warning" role="status"></div>
          <p className="mt-2">Loading data...</p>
        </div>
      ) : (
        <div className="row text-center mt-4">
          <div className="bg-dark text-light p-4 m-2 rounded shadow">
            <h3 className="text-warning mb-3">الدخل الشهري</h3>
            <h1 className="display-4 fw-bold">{revenue}</h1>
            <p>{months[selectedMonth - 1]} {selectedYear}</p>
          </div>
        </div>
      )}

      <div className="row justify-content-center mb-4">
        <div className="col-md-3">
          <label className="form-label fw-semibold text-light">اليوم</label>
          <select className="form-select" value={selectedDay} onChange={(e) => setSelectedDay(e.target.value)}>
            {days.map((day) => <option key={day} value={day}>{day}</option>)}
          </select>
        </div>
      </div>

      <div className="bg-dark text-light p-4 m-2 rounded shadow">
        <h3 className="text-warning mb-3">الدخل اليومي</h3>
        <h1 className="display-4 fw-bold">{dalyRevenue}</h1>
        <p>{selectedYear} {months[selectedMonth - 1]} {selectedDay}</p>
      </div>


      <div className="bg-dark text-light rounded shadow p-4 mt-5">
      <h4 className="text-warning text-center fw-bold mb-4">📜 سجل النشاطات</h4>

      {/* Date Selectors */}
      <div className="row justify-content-center mb-4">
        <div className="col-md-3">
          <label className="form-label text-light">السنة</label>
          <select
            className="form-select"
            value={selectedYear}
            onChange={(e) => setSelectedYear(Number(e.target.value))}
          >
            {Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i).map((year) => (
              <option key={year} value={year}>{year}</option>
            ))}
          </select>
        </div>
        <div className="col-md-3">
          <label className="form-label text-light">الشهر</label>
          <select
            className="form-select"
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(Number(e.target.value))}
          >
            {months.map((month, index) => (
              <option key={index + 1} value={index + 1}>{month}</option>
            ))}
          </select>
        </div>
        <div className="col-md-3">
          <label className="form-label text-light">اليوم</label>
          <select
            className="form-select"
            value={selectedDay}
            onChange={(e) => setSelectedDay(Number(e.target.value))}
          >
            {days.map((d) => (
              <option key={d} value={d}>{d}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Logs Table */}
      <div
        style={{
          maxHeight: "400px",
          overflowY: "auto",
          borderRadius: "10px",
          border: "1px solid rgba(255,255,255,0.2)",
        }}
      >
        <Table
          striped
          bordered
          hover
          responsive
          className="table-dark align-middle text-center"
        >
          <thead
            className="sticky-top"
            style={{ backgroundColor: "#212529", zIndex: 2 }}
          >
            <tr>
              <th>الوقت</th>
              <th>التاريخ</th>
              <th>السعر</th>
              <th>المدة</th>
              <th>اسم المشترك</th>
              <th>اسم المشرف</th>
              <th>الإجراء</th>
            </tr>
          </thead>
          <tbody>
            {logs.length === 0 ? (
              <tr>
                <td colSpan="7" className="text-center text-muted py-3">
                  لا توجد سجلات في هذا اليوم.
                </td>
              </tr>
            ) : (
              logs.map((log, index) => {
                return(
                <tr key={index}>
                  <td>{log.time}</td>
                  <td>{log.date}</td>
                  <td>{log.price}</td>
                  <td>{log.interval_}</td>
                  <td>{log.subscriberName}</td>
                  <td>{log.adminName}</td>
                  <td className="fw-bold text-warning">{log.action}</td>
                </tr>
              )})
            )}
          </tbody>
        </Table>
      </div>
    </div>

      <button
        className="btn btn-outline-warning mt-4"
        onClick={() => setShowUpdateModal(true)}
      >
        🔑 Update Summary Password
      </button>

      {showUpdateModal && (
        <div className="modal d-block" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content bg-dark text-light">
              <div className="modal-header">
                <h5 className="modal-title text-warning">🔑 Update Password</h5>
              </div>
              <div className="modal-body">
                <input
                  type="password"
                  className="form-control mb-3"
                  placeholder="Old password"
                  value={oldPassword}
                  onChange={(e) => setOldPassword(e.target.value)}
                />
                <input
                  type="password"
                  className="form-control mb-3"
                  placeholder="New password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <input
                  type="password"
                  className="form-control"
                  placeholder="Confirm new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setShowUpdateModal(false)}>Cancel</button>
                <button className="btn btn-warning" onClick={handleUpdatePassword}>Update</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
