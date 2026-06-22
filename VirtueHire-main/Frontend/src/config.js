// src/config.js
const isLocal =
  window.location.hostname === "localhost" ||
  window.location.hostname === "127.0.0.1";

const configuredApiUrl = process.env.REACT_APP_API_BASE_URL;
const configuredWebSocketUrl = process.env.REACT_APP_WS_BASE_URL;

export const API_BASE_URL = isLocal
  ? "http://localhost:8081/api"
  : configuredApiUrl || "https://admin.virtuehire.in/api";

export const WS_BASE_URL = isLocal
  ? "http://localhost:8081"
  : configuredWebSocketUrl || "https://admin.virtuehire.in";

const config = {
  API_BASE_URL,
  WS_BASE_URL,
};

export default config;
