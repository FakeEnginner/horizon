// ================= Dependencies =================
const express = require("express");
const http = require("http");
const mongodb = require("mongodb");
const expressFormidable = require("express-formidable");
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const bcryptjs = require("bcryptjs");
const jwt = require("jsonwebtoken");
const nodemailer = require("nodemailer");
const WebSocketServer = require("websocket").server;

// ================= Constants =================
const app = express();
const server = http.createServer(app);
const MongoClient = mongodb.MongoClient;
const ObjectId = mongodb.ObjectId;

const PORT = process.env.PORT || 3000;
const DB_URI = "mongodb://localhost:27017";
const DB_NAME = "horizon";
const JWT_SECRET = process.env.JWT_SECRET;
const MAIN_URL = `http://localhost:${PORT}`;
const WS_ORIGIN_ALLOWLIST = (process.env.WS_ORIGIN_ALLOWLIST || "").split(",").map(v => v.trim()).filter(Boolean);
const CORS_ORIGIN_ALLOWLIST = (process.env.CORS_ORIGIN_ALLOWLIST || "").split(",").map(v => v.trim()).filter(Boolean);
const MAX_PROFILE_IMAGE_BYTES = 5 * 1024 * 1024;

// ================= Globals =================
let db;
let users = []; // Online users
const authAttempts = new Map();

const USERNAME_REGEX = /^[a-zA-Z0-9_\-.]{3,50}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function normalizeUsername(value = "") {
  return String(value).trim();
}

function normalizeEmail(value = "") {
  return String(value).trim().toLowerCase();
}


function isRateLimited(key, maxAttempts = 10, windowMs = 10 * 60 * 1000) {
  const now = Date.now();
  const state = authAttempts.get(key) || { count: 0, windowStart: now };
  if (now - state.windowStart > windowMs) {
    state.count = 0;
    state.windowStart = now;
  }
  state.count += 1;
  authAttempts.set(key, state);
  return state.count > maxAttempts;
}

if (!JWT_SECRET || JWT_SECRET.length < 32) {
  throw new Error("Missing/weak JWT_SECRET. Set a strong value (minimum 32 chars).");
}

const profilesUploadDir = path.join(__dirname, "uploads", "profiles");
fs.mkdirSync(profilesUploadDir, { recursive: true });

// ================= Middleware =================
app.use(expressFormidable({ multiples: true, maxFileSize: MAX_PROFILE_IMAGE_BYTES }));
app.use("/public", express.static(__dirname + "/public"));
app.use("/uploads", express.static(__dirname + "/uploads"));
app.set("view engine", "ejs");
app.set("trust proxy", 1);

app.use((req, res, next) => {
  if (!db) return res.status(503).json({ status: "error", message: "Service initializing. Please retry." });
  next();
});

app.disable("x-powered-by");
app.use((req, res, next) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("X-Frame-Options", "DENY");
  res.setHeader("Referrer-Policy", "no-referrer");
  next();
});

// CORS Setup
app.use((req, res, next) => {
  const origin = req.headers.origin;
  if (origin && CORS_ORIGIN_ALLOWLIST.includes(origin)) {
    res.setHeader("Access-Control-Allow-Origin", origin);
    res.setHeader("Vary", "Origin");
  }
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE");
  res.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,Authorization");
  res.setHeader("Access-Control-Allow-Credentials", "true");

  if (req.method === "OPTIONS") {
    return res.sendStatus(204);
  }

  next();
});

// ================= Nodemailer =================
const nodemailerFrom = "support@adnan-tech.com";
const transport = nodemailer.createTransport({
  host: "", // add SMTP host
  port: 465,
  secure: true,
  auth: { user: nodemailerFrom, pass: "" } // add password
});

// ================= Auth Middleware =================
const auth = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization || "";
    const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
    if (!token) return res.json({ status: "error", message: "Access token is required." });

    const decoded = jwt.verify(token, JWT_SECRET);
    const user = await db.collection("users").findOne({
      _id: new ObjectId(decoded.userId),
      accessToken: token
    });

    if (!user) return res.json({ status: "error", message: "Invalid or expired token." });

    req.user = user;
    next();
  } catch (error) {
    console.error("Auth error:", error);
    res.json({ status: "error", message: "Authentication failed." });
  }
};

// ================= Routes =================

// Register
app.post("/register", async (req, res) => {
  const clientKey = `register:${req.ip}`;
  if (isRateLimited(clientKey, 20)) return res.status(429).json({ status: "error", message: "Too many attempts. Try later." });

  const username = normalizeUsername(req.fields.username);
  const password = String(req.fields.password || "");
  const confirmPassword = String(req.fields.confirmPassword || "");

  if (!username || !password || !confirmPassword)
    return res.json({ status: "error", message: "Please enter all values." });

  if (!USERNAME_REGEX.test(username))
    return res.json({ status: "error", message: "Username must be 3-50 chars and only letters, numbers, dash, underscore or dot." });

  if (password !== confirmPassword)
    return res.json({ status: "error", message: "Passwords do not match." });

  if (password.length < 6)
    return res.json({ status: "error", message: "Password must be at least 6 characters long." });

  const existingUser = await db.collection("users").findOne({ username });
  if (existingUser) return res.json({ status: "error", message: "Username already exists." });

  const hash = bcryptjs.hashSync(password, bcryptjs.genSaltSync(10));

  await db.collection("users").insertOne({
    username,
    password: hash,
    accessToken: "",
    createdAt: new Date().toUTCString(),
    profileImage: null,
    isVerified: true
  });

  res.json({ status: "success", message: "Account created successfully." });
});

// Login
app.post("/login", async (req, res) => {
  const clientKey = `login:${req.ip}`;
  if (isRateLimited(clientKey, 20)) return res.status(429).json({ status: "error", message: "Too many attempts. Try later." });

  const username = normalizeUsername(req.fields.username);
  const password = String(req.fields.password || "");
  if (!username || !password) return res.json({ status: "error", message: "Please fill all fields." });

  const user = await db.collection("users").findOne({ username });
  if (!user) return res.json({ status: "error", message: "Invalid username or password." });
  if (!user.isVerified) return res.json({ status: "verificationRequired", message: "Please verify your account first." });

  if (!bcryptjs.compareSync(password, user.password))
    return res.json({ status: "error", message: "Invalid username or password." });

  const accessToken = jwt.sign(
    { userId: user._id.toString(), username: user.username },
    JWT_SECRET,
    { expiresIn: "30d" }
  );

  await db.collection("users").updateOne(
    { _id: user._id },
    { $set: { accessToken, lastLogin: new Date().toUTCString() } }
  );

  res.json({
    status: "success",
    message: "Login successful.",
    accessToken,
    user: { _id: user._id, username: user.username, profileImage: user.profileImage, createdAt: user.createdAt }
  });
});

// Logout
app.post("/logout", async (req, res) => {
  const authHeader = req.headers.authorization || "";
    const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
  if (!token) return res.json({ status: "error", message: "Access token is required." });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    await db.collection("users").updateOne(
      { _id: new ObjectId(decoded.userId) },
      { $set: { accessToken: "", lastLogout: new Date().toUTCString() } }
    );
    res.json({ status: "success", message: "Logged out successfully." });
  } catch (error) {
    console.error("Logout error:", error);
    res.json({ status: "error", message: "Invalid access token." });
  }
});

// Verify Token
app.post("/verify-token", async (req, res) => {
  const authHeader = req.headers.authorization || "";
    const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
  if (!token) return res.json({ status: "error", message: "Access token is required." });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = await db.collection("users").findOne({ _id: new ObjectId(decoded.userId), accessToken: token });
    if (!user) return res.json({ status: "error", message: "Invalid or expired token." });

    res.json({
      status: "success",
      message: "Token is valid.",
      user: { _id: user._id, username: user.username, profileImage: user.profileImage, createdAt: user.createdAt }
    });
  } catch (error) {
    console.error("Token verification error:", error);
    res.json({ status: "error", message: "Invalid or expired token." });
  }
});

// Get current user
app.post("/me", auth, async (req, res) => {
  const user = req.user;
  res.json({
    status: "success",
    message: "Data has been fetched.",
    user: { _id: user._id, name: user.name, email: user.email, profileImage: user.profileImage }
  });
});

// Change Password
app.post("/change-password", auth, async (req, res) => {
  const user = req.user;
  const { password, newPassword, confirmPassword } = req.fields;

  if (!password || !newPassword || !confirmPassword)
    return res.json({ status: "error", message: "Please fill all fields." });

  if (newPassword !== confirmPassword)
    return res.json({ status: "error", message: "Password mis-match." });

  if (!bcryptjs.compareSync(password, user.password))
    return res.json({ status: "error", message: "Incorrect password." });

  if (newPassword.length < 8)
    return res.json({ status: "error", message: "Password must be at least 8 characters long." });

  const hash = bcryptjs.hashSync(newPassword, bcryptjs.genSaltSync(10));
  await db.collection("users").updateOne({ _id: user._id }, { $set: { password: hash } });

  res.json({ status: "success", message: "Password has been changed." });
});

// Save Profile
app.post("/save-profile", auth, async (req, res) => {
  const user = req.user;
  const name = req.fields.name || "";
  if (!name) return res.json({ status: "error", message: "Please fill all fields." });

  let profileImageObj = user.profileImage || {};
  const profileImage = req.files.profileImage;

  if (profileImage?.size > 0) {
    if (profileImage.size > MAX_PROFILE_IMAGE_BYTES) {
      return res.json({ status: "error", message: "Profile image is too large." });
    }

    const ext = profileImage.type.toLowerCase();
    if (!ext.includes("jpeg") && !ext.includes("jpg") && !ext.includes("png"))
      return res.json({ status: "error", message: "Only JPEG, JPG or PNG is allowed." });

    if (profileImageObj.path && fs.existsSync(profileImageObj.path)) fs.unlinkSync(profileImageObj.path);

    const safeName = path.basename(profileImage.name).replace(/[^a-zA-Z0-9._-]/g, "_");
    const generatedName = `${Date.now()}-${crypto.randomBytes(8).toString("hex")}-${safeName}`;
    const fileLocation = path.join(profilesUploadDir, generatedName);
    fs.copyFileSync(profileImage.path, fileLocation);
    fs.unlinkSync(profileImage.path);

    profileImageObj = {
      size: profileImage.size,
      path: `uploads/profiles/${generatedName}`,
      name: safeName,
      type: profileImage.type
    };
  }

  await db.collection("users").updateOne(
    { _id: user._id },
    { $set: { name, profileImage: profileImageObj } }
  );

  res.json({ status: "success", message: "Profile updated.", profileImage: profileImageObj });
});

// Password Recovery - Send Email
app.post("/send-password-recovery-email", async (req, res) => {
  const clientKey = `recovery:${req.ip}`;
  if (isRateLimited(clientKey, 5)) return res.status(429).json({ status: "error", message: "Too many attempts. Try later." });

  const email = normalizeEmail(req.fields.email);
  if (!email || !EMAIL_REGEX.test(email)) return res.json({ status: "error", message: "Please enter a valid email." });

  const user = await db.collection("users").findOne({ email });
  if (!user) return res.json({ status: "success", message: "If the account exists, a verification code has been sent." });

  const code = crypto.randomInt(100000, 1000000);
  await db.collection("users").updateOne(
    { _id: user._id },
    {
      $set: {
        code,
        codeExpiresAt: new Date(Date.now() + 15 * 60 * 1000)
      }
    }
  );

  const emailHtml = `Your password reset code is: <b style='font-size: 30px;'>${code}</b>`;

  try {
    await transport.sendMail({ from: nodemailerFrom, to: email, subject: "Password reset code", html: emailHtml });
  } catch (error) {
    console.error("Password recovery email error:", error);
    return res.status(500).json({ status: "error", message: "Unable to send recovery email at the moment." });
  }

  res.json({ status: "success", message: "If the account exists, a verification code has been sent." });
});

// Reset Password
app.post("/reset-password", async (req, res) => {
  const clientKey = `reset-password:${req.ip}`;
  if (isRateLimited(clientKey, 10)) return res.status(429).json({ status: "error", message: "Too many attempts. Try later." });

  const email = normalizeEmail(req.fields.email);
  const code = req.fields.code;
  const password = String(req.fields.password || "");
  if (!email || !code || !password)
    return res.json({ status: "error", message: "Please fill all fields." });

  if (password.length < 8)
    return res.json({ status: "error", message: "Password must be at least 8 characters long." });

  const user = await db.collection("users").findOne({
    email,
    code: parseInt(code),
    codeExpiresAt: { $gt: new Date() }
  });
  if (!user) return res.json({ status: "error", message: "Invalid email/code." });

  const hash = bcryptjs.hashSync(password, bcryptjs.genSaltSync(10));
  await db.collection("users").updateOne(
    { _id: user._id },
    { $set: { password: hash }, $unset: { code: "", codeExpiresAt: "" } }
  );

  res.json({ status: "success", message: "Password has been reset." });
});

// Verify Account
app.post("/verify-account", async (req, res) => {
  const { email, code } = req.fields;
  if (!email || !code) return res.json({ status: "error", message: "Please fill all fields." });

  const user = await db.collection("users").findOne({ email, verificationToken: parseInt(code) });
  if (!user) return res.json({ status: "error", message: "Invalid email/code." });

  await db.collection("users").updateOne({ _id: user._id }, { $set: { isVerified: true } });

  res.json({ status: "success", message: "Account verified. Please login again." });
});

// Health Check
app.get("/healthz", (req, res) => res.json({ status: "ok" }));

// ================= View Routes =================
app.get("/", (req, res) => res.render("index", { mainURL: MAIN_URL }));
app.get("/register", (req, res) => res.render("register", { mainURL: MAIN_URL }));
app.get("/login", (req, res) => res.render("login", { mainURL: MAIN_URL }));
app.get("/profile", (req, res) => res.render("profile", { mainURL: MAIN_URL }));
app.get("/forgot-password", (req, res) => res.render("forgot-password", { mainURL: MAIN_URL }));
app.get("/change-password", (req, res) => res.render("change-password", { mainURL: MAIN_URL }));
app.get("/reset-password/:email", (req, res) => res.render("reset-password", { mainURL: MAIN_URL, email: req.params.email || "" }));
app.get("/verify-email/:email", (req, res) => res.render("verify-email", { mainURL: MAIN_URL, email: req.params.email || "" }));

// ================= Start Server & Connect DB =================
server.listen(PORT, async () => {
  try {
    const client = await MongoClient.connect(DB_URI);
    db = client.db(DB_NAME);
    console.log(`✅ Database connected`);
    console.log(`🚀 Server running at ${MAIN_URL}`);
  } catch (error) {
    console.error("DB connection error:", error);
  }
});

// ================= WebSocket Server with WebRTC Support =================
const wsServer = new WebSocketServer({ httpServer: server });

// Helper functions for WebSocket
function findUser(username) {
  return users.find(u => u.name === username);
}

function sendToUser(username, message) {
  const user = findUser(username);
  if (user && user.conn.connected) {
    try {
      user.conn.send(JSON.stringify(message));
      return true;
    } catch (err) {
      console.error(`❌ Failed to send message to ${username}:`, err.message);
      removeUser(user.conn);
      return false;
    }
  }
  return false;
}

function removeUser(connection) {
  const initialCount = users.length;
  users = users.filter(u => u.conn !== connection);
  if (users.length !== initialCount) {
    console.log(`👋 User disconnected. Remaining users: ${users.map(u => u.name).join(', ')}`);
    broadcastOnlineUsers();
  }
}

function broadcastOnlineUsers() {
  const onlineUsernames = users.map(u => u.name);
  const payload = JSON.stringify({ type: "online_users", users: onlineUsernames });
  users.forEach(u => {
    try { 
      if (u.conn.connected) {
        u.conn.send(payload); 
      }
    } catch (err) {
      console.error(`❌ Failed to broadcast to ${u.name}:`, err.message);
    }
  });
}

function isAuthorizedSignalSender(connection, payload) {
  const from = String(payload?.from || "").trim();
  if (!from) return false;
  return connection?.auth?.username === from;
}

wsServer.on("request", (req) => {
  if (WS_ORIGIN_ALLOWLIST.length > 0 && req.origin && !WS_ORIGIN_ALLOWLIST.includes(req.origin)) {
    req.reject(403, "Forbidden origin");
    return;
  }

  const query = new URL(req.httpRequest.url, MAIN_URL).searchParams;
  const queryToken = query.get("token");
  const authHeader = req.httpRequest.headers["authorization"] || "";
  const bearerToken = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
  const token = bearerToken || queryToken;

  if (!token) {
    req.reject(401, "WebSocket auth token required");
    return;
  }

  let tokenPayload;
  try {
    tokenPayload = jwt.verify(token, JWT_SECRET);
  } catch (err) {
    req.reject(401, "Invalid WebSocket auth token");
    return;
  }

  const connection = req.accept();
  connection.auth = {
    username: tokenPayload.username || "",
    userId: tokenPayload.userId || ""
  };
  console.log("✅ New WebSocket connection");

  connection.on("message", (message) => {
    if (message.type === "utf8") {
      try {
        const data = JSON.parse(message.utf8Data);
        console.log(`📨 Received message type: ${data.type} from ${data.from || 'unknown'}`);

        switch (data.type) {
          case "store_user": {
            const username = data.username;
            if (!username) return;
            if (!connection.auth?.username || username !== connection.auth.username) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized user mapping",
                errorCode: "UNAUTHORIZED_USERNAME"
              }));
              return;
            }
            
            // Check if user already exists
            const existingUser = findUser(username);
            if (existingUser) {
              // Update connection for existing user
              existingUser.conn = connection;
              console.log(`🔄 Updated connection for existing user: ${username}`);
            } else {
              // Add new user
              users.push({ name: username, conn: connection });
              console.log(`✅ New user stored: ${username}`);
            }
            
            broadcastOnlineUsers();
            break;
          }

          // ================= WebRTC Signaling Messages =================
          case "call_request": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to } = data;
            console.log(`📞 Call request from ${from} to ${to}`);
            
            const success = sendToUser(to, {
              type: "call_request",
              from: from,
              timestamp: Date.now()
            });
            
            if (!success) {
              // Send error back to caller
              const caller = findUser(from);
              if (caller) {
                caller.conn.send(JSON.stringify({
                  type: "call_error",
                  message: `User ${to} is not available`,
                  errorCode: "USER_NOT_FOUND"
                }));
              }
            }
            break;
          }

          case "call_accepted": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to } = data;
            console.log(`✅ Call accepted by ${from} to ${to}`);
            
            sendToUser(to, {
              type: "call_accepted",
              from: from,
              timestamp: Date.now()
            });
            break;
          }

          case "call_rejected": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to } = data;
            console.log(`❌ Call rejected by ${from} to ${to}`);
            
            sendToUser(to, {
              type: "call_rejected",
              from: from,
              timestamp: Date.now()
            });
            break;
          }

          case "sdp_offer": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to, sdp } = data;
            console.log(`📋 SDP Offer from ${from} to ${to}`);
            
            const success = sendToUser(to, {
              type: "sdp_offer",
              from: from,
              sdp: sdp,
              timestamp: Date.now()
            });

            if (!success) {
              console.log(`❌ Failed to deliver SDP offer to ${to}`);
            }
            break;
          }

          case "sdp_answer": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to, sdp } = data;
            console.log(`📋 SDP Answer from ${from} to ${to}`);
            
            const success = sendToUser(to, {
              type: "sdp_answer",
              from: from,
              sdp: sdp,
              timestamp: Date.now()
            });

            if (!success) {
              console.log(`❌ Failed to deliver SDP answer to ${to}`);
            }
            break;
          }

          case "ice_candidate": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to, candidate, sdpMid, sdpMLineIndex } = data;
            console.log(`🧊 ICE Candidate from ${from} to ${to}`);
            
            const success = sendToUser(to, {
              type: "ice_candidate",
              from: from,
              candidate: candidate,
              sdpMid: sdpMid,
              sdpMLineIndex: sdpMLineIndex,
              timestamp: Date.now()
            });

            if (!success) {
              console.log(`❌ Failed to deliver ICE candidate to ${to}`);
            }
            break;
          }

          case "call_end": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const { from, to } = data;
            console.log(`📴 Call ended by ${from} to ${to}`);
            
            sendToUser(to, {
              type: "call_end",
              from: from,
              timestamp: Date.now()
            });
            break;
          }

          // ================= Legacy Message Types =================
          case "send_to_user": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const targetUser = findUser(data.to);
            if (targetUser) {
              sendToUser(data.to, {
                type: "private_message",
                from: data.from,
                message: data.message,
                timestamp: Date.now()
              });
            }
            break;
          }

          case "request_online_users": {
            connection.send(JSON.stringify({
              type: "online_users",
              users: users.map(u => u.name)
            }));
            break;
          }

          case "ping": {
            connection.send(JSON.stringify({
              type: "pong",
              timestamp: Date.now()
            }));
            break;
          }

          case "broadcast": {
            if (!isAuthorizedSignalSender(connection, data)) {
              connection.send(JSON.stringify({
                type: "call_error",
                message: "Unauthorized sender",
                errorCode: "UNAUTHORIZED_SENDER"
              }));
              break;
            }
            const broadcastMessage = {
              type: "broadcast",
              message: data.message,
              timestamp: Date.now()
            };
            
            users.forEach(user => {
              try {
                if (user.conn.connected) {
                  user.conn.send(JSON.stringify(broadcastMessage));
                }
              } catch (err) {
                console.error(`❌ Failed to broadcast to ${user.name}:`, err.message);
              }
            });
            break;
          }

          default:
            console.log(`❓ Unknown message type: ${data.type}`);
            break;
        }
      } catch (err) {
        console.error("❌ Invalid JSON:", message.utf8Data, err.message);
      }
    }
  });

  connection.on("close", () => {
    removeUser(connection);
  });

  connection.on("error", (error) => {
    console.error("❌ WebSocket connection error:", error);
    removeUser(connection);
  });
});

// Cleanup disconnected users every 30 seconds
setInterval(() => {
  const initialCount = users.length;
  users = users.filter(user => {
    try {
      return user.conn.connected;
    } catch (err) {
      return false;
    }
  });
  
  if (users.length !== initialCount) {
    console.log(`🧹 Cleaned up disconnected users. Active users: ${users.length}`);
    broadcastOnlineUsers();
  }
}, 30000);



app.use((err, req, res, next) => {
  console.error("Unhandled API error:", err);
  if (res.headersSent) return next(err);
  res.status(500).json({ status: "error", message: "Internal server error." });
});

process.on("unhandledRejection", (reason) => {
  console.error("Unhandled rejection:", reason);
});

process.on("uncaughtException", (error) => {
  console.error("Uncaught exception:", error);
});

console.log("🎯 WebRTC Signaling Server ready for connections");
