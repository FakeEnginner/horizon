// --- Dependencies ---
const express = require("express")
const http = require("http")
const mongodb = require("mongodb")
const expressFormidable = require("express-formidable")
const fs = require("fs")
const bcryptjs = require("bcryptjs")
const jwt = require("jsonwebtoken")
const nodemailer = require("nodemailer")

// --- Constants ---
const app = express()
const server = http.createServer(app)
const MongoClient = mongodb.MongoClient
const ObjectId = mongodb.ObjectId

const PORT = process.env.PORT || 3000
const DB_URI = "mongodb://localhost:27017"
const DB_NAME = "horizon"
const JWT_SECRET = "jwtSecret1234567890"
const MAIN_URL = `http://localhost:${PORT}`

// --- Globals ---
let db

// --- Middleware ---
app.use(expressFormidable({ multiples: true }))
app.use("/public", express.static(__dirname + "/public"))
app.use("/uploads", express.static(__dirname + "/uploads"))
app.set("view engine", "ejs")

// CORS Setup
app.use((req, res, next) => {
  res.setHeader("Access-Control-Allow-Origin", "*")
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE")
  res.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,Authorization")
  res.setHeader("Access-Control-Allow-Credentials", true)
  next()
})

// --- Nodemailer ---
const nodemailerFrom = "support@adnan-tech.com"
const transport = nodemailer.createTransport({
  host: "",
  port: 465,
  secure: true,
  auth: { user: nodemailerFrom, pass: "" }
})

// --- Auth Middleware ---
const auth = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.replace("Bearer ", "")
    if (!token) return res.json({ status: "error", message: "Access token is required." })

    const decoded = jwt.verify(token, JWT_SECRET)
    const user = await db.collection("users").findOne({
      _id: new ObjectId(decoded.userId),
      accessToken: token
    })

    if (!user) return res.json({ status: "error", message: "Invalid or expired token." })

    req.user = user
    next()
  } catch (error) {
    console.error("Auth error:", error)
    res.json({ status: "error", message: "Authentication failed." })
  }
}

// --- Routes ---

// Register
app.post("/register", async (req, res) => {
  const { username, password, confirmPassword } = req.fields
  if (!username || !password || !confirmPassword)
    return res.json({ status: "error", message: "Please enter all values." })

  if (password !== confirmPassword)
    return res.json({ status: "error", message: "Passwords do not match." })

  if (password.length < 6)
    return res.json({ status: "error", message: "Password must be at least 6 characters long." })

  const existingUser = await db.collection("users").findOne({ username })
  if (existingUser) return res.json({ status: "error", message: "Username already exists." })

  const hash = bcryptjs.hashSync(password, bcryptjs.genSaltSync(10))

  await db.collection("users").insertOne({
    username,
    password: hash,
    accessToken: "",
    createdAt: new Date().toUTCString(),
    profileImage: null,
    isVerified: true // change to false if email verification is required
  })

  res.json({ status: "success", message: "Account created successfully." })
})

// Login
app.post("/login", async (req, res) => {
  const { username, password } = req.fields
  if (!username || !password) return res.json({ status: "error", message: "Please fill all fields." })

  const user = await db.collection("users").findOne({ username })
  if (!user) return res.json({ status: "error", message: "Username does not exist." })
  if (!user.isVerified) return res.json({ status: "verificationRequired", message: "Please verify your account first." })

  if (!bcryptjs.compareSync(password, user.password))
    return res.json({ status: "error", message: "Password is not correct." })

  const accessToken = jwt.sign(
    { userId: user._id.toString(), username: user.username },
    JWT_SECRET,
    { expiresIn: "30d" }
  )

  await db.collection("users").updateOne({ _id: user._id }, { $set: { accessToken, lastLogin: new Date().toUTCString() } })

  res.json({
    status: "success",
    message: "Login successful.",
    accessToken,
    user: { _id: user._id, username: user.username, profileImage: user.profileImage, createdAt: user.createdAt }
  })
})

// Logout
app.post("/logout", async (req, res) => {
  const token = req.headers.authorization?.replace("Bearer ", "")
  if (!token) return res.json({ status: "error", message: "Access token is required." })

  try {
    const decoded = jwt.verify(token, JWT_SECRET)
    await db.collection("users").updateOne(
      { _id: new ObjectId(decoded.userId) },
      { $set: { accessToken: "", lastLogout: new Date().toUTCString() } }
    )
    res.json({ status: "success", message: "Logged out successfully." })
  } catch (error) {
    console.error("Logout error:", error)
    res.json({ status: "error", message: "Invalid access token." })
  }
})

// Verify Token
app.post("/verify-token", async (req, res) => {
  const token = req.headers.authorization?.replace("Bearer ", "")
  if (!token) return res.json({ status: "error", message: "Access token is required." })

  try {
    const decoded = jwt.verify(token, JWT_SECRET)
    const user = await db.collection("users").findOne({ _id: new ObjectId(decoded.userId), accessToken: token })
    if (!user) return res.json({ status: "error", message: "Invalid or expired token." })

    res.json({
      status: "success",
      message: "Token is valid.",
      user: { _id: user._id, username: user.username, profileImage: user.profileImage, createdAt: user.createdAt }
    })
  } catch (error) {
    console.error("Token verification error:", error)
    res.json({ status: "error", message: "Invalid or expired token." })
  }
})

// Get current user
app.post("/me", auth, async (req, res) => {
  const user = req.user
  res.json({
    status: "success",
    message: "Data has been fetched.",
    user: { _id: user._id, name: user.name, email: user.email, profileImage: user.profileImage }
  })
})

// Change Password
app.post("/change-password", auth, async (req, res) => {
  const user = req.user
  const { password, newPassword, confirmPassword } = req.fields

  if (!password || !newPassword || !confirmPassword)
    return res.json({ status: "error", message: "Please fill all fields." })

  if (newPassword !== confirmPassword)
    return res.json({ status: "error", message: "Password mis-match." })

  if (!bcryptjs.compareSync(password, user.password))
    return res.json({ status: "error", message: "Incorrect password." })

  const hash = bcryptjs.hashSync(newPassword, bcryptjs.genSaltSync(10))
  await db.collection("users").updateOne({ _id: user._id }, { $set: { password: hash } })

  res.json({ status: "success", message: "Password has been changed." })
})

// Save Profile
app.post("/save-profile", auth, async (req, res) => {
  const user = req.user
  const name = req.fields.name || ""
  if (!name) return res.json({ status: "error", message: "Please fill all fields." })

  let profileImageObj = user.profileImage || {}
  const profileImage = req.files.profileImage

  if (profileImage?.size > 0) {
    const ext = profileImage.type.toLowerCase()
    if (!ext.includes("jpeg") && !ext.includes("jpg") && !ext.includes("png"))
      return res.json({ status: "error", message: "Only JPEG, JPG or PNG is allowed." })

    if (fs.existsSync(profileImageObj.path)) fs.unlinkSync(profileImageObj.path)

    const fileLocation = `uploads/profiles/${Date.now()}-${profileImage.name}`
    fs.copyFileSync(profileImage.path, fileLocation)
    fs.unlinkSync(profileImage.path)

    profileImageObj = { size: profileImage.size, path: fileLocation, name: profileImage.name, type: profileImage.type }
  }

  await db.collection("users").updateOne(
    { _id: user._id },
    { $set: { name, profileImage: profileImageObj } }
  )

  res.json({ status: "success", message: "Profile updated.", profileImage: profileImageObj })
})

// Password Recovery - Send Email
app.post("/send-password-recovery-email", async (req, res) => {
  const { email } = req.fields
  if (!email) return res.json({ status: "error", message: "Please fill all fields." })

  const user = await db.collection("users").findOne({ email })
  if (!user) return res.json({ status: "error", message: "Email does not exist." })

  const code = Math.floor(100000 + Math.random() * 900000)
  await db.collection("users").updateOne({ _id: user._id }, { $set: { code } })

  const emailHtml = `Your password reset code is: <b style='font-size: 30px;'>${code}</b>`
  transport.sendMail({ from: nodemailerFrom, to: email, subject: "Password reset code", html: emailHtml })

  res.json({ status: "success", message: "A verification code has been sent to your email." })
})

// Reset Password
app.post("/reset-password", async (req, res) => {
  const { email, code, password } = req.fields
  if (!email || !code || !password)
    return res.json({ status: "error", message: "Please fill all fields." })

  const user = await db.collection("users").findOne({ email, code: parseInt(code) })
  if (!user) return res.json({ status: "error", message: "Invalid email/code." })

  const hash = bcryptjs.hashSync(password, bcryptjs.genSaltSync(10))
  await db.collection("users").updateOne({ _id: user._id }, { $set: { password: hash }, $unset: { code: "" } })

  res.json({ status: "success", message: "Password has been reset." })
})

// Verify Account
app.post("/verify-account", async (req, res) => {
  const { email, code } = req.fields
  if (!email || !code) return res.json({ status: "error", message: "Please fill all fields." })

  const user = await db.collection("users").findOne({ email, verificationToken: parseInt(code) })
  if (!user) return res.json({ status: "error", message: "Invalid email/code." })

  await db.collection("users").updateOne({ _id: user._id }, { $set: { isVerified: true } })

  res.json({ status: "success", message: "Account verified. Please login again." })
})

// --- View Routes ---
app.get("/", (req, res) => res.render("index", { mainURL: MAIN_URL }))
app.get("/register", (req, res) => res.render("register", { mainURL: MAIN_URL }))
app.get("/login", (req, res) => res.render("login", { mainURL: MAIN_URL }))
app.get("/profile", (req, res) => res.render("profile", { mainURL: MAIN_URL }))
app.get("/forgot-password", (req, res) => res.render("forgot-password", { mainURL: MAIN_URL }))
app.get("/change-password", (req, res) => res.render("change-password", { mainURL: MAIN_URL }))
app.get("/reset-password/:email", (req, res) => res.render("reset-password", { mainURL: MAIN_URL, email: req.params.email || "" }))
app.get("/verify-email/:email", (req, res) => res.render("verify-email", { mainURL: MAIN_URL, email: req.params.email || "" }))

// --- Start Server & Connect DB ---
server.listen(PORT, async () => {
  try {
    const client = await MongoClient.connect(DB_URI)
    db = client.db(DB_NAME)
    console.log(`✅ Database connected`)
    console.log(`🚀 Server running at ${MAIN_URL}`)
  } catch (error) {
    console.error("DB connection error:", error)
  }
})
