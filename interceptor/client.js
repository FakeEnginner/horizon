const WebSocket = require("ws");

const ws = new WebSocket("ws://localhost:3000");

ws.on("open", () => {
  console.log("Connected ✅");
  ws.send(JSON.stringify({ type: "store_user", username: "dakshqwert" }));
    ws.send(JSON.stringify({ type: "store_user", username: "dakshqwert" }));
  ws.send(JSON.stringify({ type: "store_user", username: "dakshqwert" }));
});

ws.on("message", (msg) => {
  console.log("📩 From server:", msg.toString());
});
