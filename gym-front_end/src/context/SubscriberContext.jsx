// src/context/SubscriberContext.jsx
import React, { createContext, useState, useContext } from 'react';

const SubscriberContext = createContext();

const defaultSubscriber = {
  id: '',
  name: '',
  phone: '',
  startDate: '',
  endDate: '',
  photo_url: '',
};

export const SubscriberProvider = ({ children }) => {
  // Single subscriber (for view/edit forms)
  const [subscriber, setSubscriber] = useState(defaultSubscriber);

  // List of subscribers (for tables/lists)
  const [subscribers, setSubscribers] = useState([]);

  // Reset subscriber to empty structure
  const clearSubscriber = () => setSubscriber(defaultSubscriber);

  return (
    <SubscriberContext.Provider
      value={{
        subscriber,
        setSubscriber,
        clearSubscriber,
        subscribers,
        setSubscribers,
      }}
    >
      {children}
    </SubscriberContext.Provider>
  );
};

export const useSubscriber = () => useContext(SubscriberContext);
