import "./App.css";
import { HashRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./components/LoginPage";
import Home from "./context/Home";

function App() {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/home/*" element={<Home />} />
        </Routes>
      </Router>
    </div>
  );
}

export default App;
