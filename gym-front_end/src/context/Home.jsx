import React from "react";
import Navbar from "../components/Navbar.jsx";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Expired from "../components/Expired.jsx";
import CurrentSubscribers from "../components/CurrentSubscribers";
import Renewal from "../components/Renewal.jsx";
import SubscriberDetails from "../components/SubscriberDetails.jsx";
import GymSummary from "../components/GymSummary.jsx";
export default function Home(){
    return(
        <div>
            <Navbar/>
            <Routes>
                <Route path="/" element={<CurrentSubscribers/>}/>
                <Route path="/expired" element={<Expired/>}/>
                <Route path="/renew" element={<Renewal/>}/>
                <Route path="/:id" element={<SubscriberDetails/>}/>
                <Route path="/summary" element={<GymSummary/>}/>
            </Routes>
        </div>
    )
}