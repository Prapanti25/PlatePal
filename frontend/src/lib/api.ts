import axios from "axios";
import { firebaseAuth } from "./firebase";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use(async (request) => {
  const user = firebaseAuth?.currentUser;
  if (user) request.headers.Authorization = `Bearer ${await user.getIdToken()}`;
  return request;
});

export default api;