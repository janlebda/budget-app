// src/components/TestApiComponent.tsx
import { useEffect, useState } from "react";
import axiosClient from "../api/axiosClient";

const TestApiComponent = () => {
  const [message, setMessage] = useState<string>("");

  useEffect(() => {
    const fetchMessage = async () => {
      try {
        const response = await axiosClient.get("/test");
        setMessage(response.data);
      } catch (error) {
        setMessage("Błąd podczas łączenia z API" + error);
      }
    };
    fetchMessage();
  }, []);

  return (
    <div>
      <h2>Test API</h2>
      <p>{message}</p>
    </div>
  );
};

export default TestApiComponent;
