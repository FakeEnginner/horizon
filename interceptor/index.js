// include express framework
const express = require("express")
 
// create an instance of it
const app = express()
 
// create http server from express instance
const http = require("http").createServer(app)
 
// database module
const mongodb = require("mongodb")
 
// client used to connect with database
const MongoClient = mongodb.MongoClient
 
// each Mongo document's unique ID
const ObjectId = mongodb.ObjectId

// Add headers before the routes are defined
app.use(function (req, res, next) {
 
    // Website you wish to allow to connect
    res.setHeader("Access-Control-Allow-Origin", "*")
 
    // Request methods you wish to allow
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE")
 
    // Request headers you wish to allow
    res.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type,Authorization")
 
    // Set to true if you need the website to include cookies in the requests sent
    // to the API (e.g. in case you use sessions)
    res.setHeader("Access-Control-Allow-Credentials", true)
 
    // Pass to next layer of middleware
    next()
})

// module required for parsing FormData values
const expressFormidable = require("express-formidable")
 
// setting the middleware
app.use(expressFormidable({
    multiples: true
}))
app.use("/public", express.static(__dirname + "/public"))
app.use("/uploads", express.static(__dirname + "/uploads"))
app.set("view engine", "ejs")

const fs = require("fs")
const bcryptjs = require("bcryptjs")

// JWT used for authentication
const jwt = require("jsonwebtoken")
// secret JWT key
global.jwtSecret = "jwtSecret1234567890"
global.mainURL = "http://localhost:3000"
global.connectionString = "mongodb://localhost:27017"

const auth = require("./modules/auth")

const nodemailer = require("nodemailer")
global.nodemailerFrom = "support@adnan-tech.com"
global.transport = nodemailer.createTransport({
    host: "",
    port: 465,
    secure: true,
    auth: {
        user: nodemailerFrom,
        pass: ""
    }
})

const port = (process.env.PORT || 3000)
 
// start the server at port 3000 (for local) or for hosting server port
http.listen(port, function () {
    console.log("Server has been started at: " + port)
 
    // connect with database
    MongoClient.connect(connectionString, function (error, client) {
        if (error) {
            console.error(error)
            return
        }
 
        // database name
        global.db = client.db("horizon")
        console.log("Database connected")

        app.post("/change-password", auth, async function (request, result) {
            const user = request.user
            const password = request.fields.password
            const newPassword = request.fields.newPassword
            const confirmPassword = request.fields.confirmPassword

            if (!password || !newPassword || !confirmPassword) {
                result.json({
                    status: "error",
                    message: "Please fill all fields."
                })

                return
            }

            if (newPassword != confirmPassword) {
                result.json({
                    status: "error",
                    message: "Password mis-match."
                })

                return
            }

            // check if password is correct
            const isVerify = await bcryptjs.compareSync(password, user.password)

            if (!isVerify) {
                result.json({
                    status: "error",
                    message: "In-correct password."
                })

                return
            }

            const salt = bcryptjs.genSaltSync(10)
            const hash = await bcryptjs.hashSync(newPassword, salt)
 
            await db.collection("users").findOneAndUpdate({
                _id: user._id
            }, {
                $set: {
                    password: hash
                }
            })

            result.json({
                status: "success",
                message: "Password has been changed."
            })
        })

        app.post("/save-profile", auth, async function (request, result) {
            const user = request.user
            const name = request.fields.name || ""

            if (!name) {
                result.json({
                    status: "error",
                    message: "Please fill all fields."
                })

                return
            }

            if (Array.isArray(request.files.profileImage)) {
                result.json({
                    status: "error",
                    message: "Only 1 file is allowed."
                })

                return
            }

            const profileImage = request.files.profileImage
            let profileImageObj = user.profileImage || {}

            // const files = []
            // if (Array.isArray(request.files.profileImage)) {
            //     for (let a = 0; a < request.files.profileImage.length; a++) {
            //         if (request.files.profileImage[a].size > 0) {
            //             files.push(request.files.profileImage[a])
            //         }
            //     }
            // } else if (request.files.profileImage.size > 0) {
            //     files.push(request.files.profileImage)
            // }

            if (profileImage?.size > 0) {

                const tempType = profileImage.type.toLowerCase()
                if (!tempType.includes("jpeg") && !tempType.includes("jpg") && !tempType.includes("png")) {
                    result.json({
                        status: "error",
                        message: "Only JPEG, JPG or PNG is allowed."
                    })
                    return
                }

                if (await fs.existsSync(profileImageObj.path))
                    await fs.unlinkSync(profileImageObj.path)

                const fileData = await fs.readFileSync(profileImage.path)
                const fileLocation = "uploads/profiles/" + (new Date().getTime()) + "-" + profileImage.name
                await fs.writeFileSync(fileLocation, fileData)
                await fs.unlinkSync(profileImage.path)

                profileImageObj = {
                    size: profileImage.size,
                    path: fileLocation,
                    name: profileImage.name,
                    type: profileImage.type
                }
            }

            await db.collection("users")
                .findOneAndUpdate({
                    _id: user._id
                }, {
                    $set: {
                        name: name,
                        profileImage: profileImageObj
                    }
                })

            result.json({
                status: "success",
                message: "Profile has been updated.",
                profileImage: profileImageObj
            })
        })

        app.post("/verify-account", async function (request, result) {
            const email = request.fields.email
            const code = request.fields.code

            if (!email || !code) {
                result.json({
                    status: "error",
                    message: "Please fill all fields."
                })

                return
            }
         
            // update JWT of user in database
            const user = await db.collection("users").findOne({
                $and: [{
                    email: email
                }, {
                    verificationToken: parseInt(code)
                }]
            })

            if (user == null) {
                result.json({
                    status: "error",
                    message: "Invalid email code."
                })

                return
            }

            await db.collection("users").findOneAndUpdate({
                _id: user._id
            }, {
                $set: {
                    isVerified: true
                },

                // $unset: {
                //     verificationToken: ""
                // }
            })

            result.json({
                status: "success",
                message: "Account has been account. Kindly login again."
            })
        })

        app.post("/reset-password", async function (request, result) {
            const email = request.fields.email
            const code = request.fields.code
            const password = request.fields.password

            if (!email || !code || !password) {
                result.json({
                    status: "error",
                    message: "Please fill all fields."
                })

                return
            }
         
            // update JWT of user in database
            const user = await db.collection("users").findOne({
                $and: [{
                    email: email
                }, {
                    code: parseInt(code)
                }]
            })

            if (user == null) {
                result.json({
                    status: "error",
                    message: "Invalid email code."
                })

                return
            }

            const salt = bcryptjs.genSaltSync(10)
            const hash = await bcryptjs.hashSync(password, salt)

            await db.collection("users").findOneAndUpdate({
                _id: user._id
            }, {
                $set: {
                    password: hash
                },

                $unset: {
                    code: ""
                }
            })

            result.json({
                status: "success",
                message: "Password has been changed."
            })
        })

        app.post("/send-password-recovery-email", async function (request, result) {
            const email = request.fields.email

            if (!email) {
                result.json({
                    status: "error",
                    message: "Please fill all fields."
                })

                return
            }
         
            // update JWT of user in database
            const user = await db.collection("users").findOne({
                email: email
            })

            if (user == null) {
                result.json({
                    status: "error",
                    message: "Email does not exists."
                })

                return
            }

            const minimum = 0
            const maximum = 999999
            const randomNumber = Math.floor(Math.random() * (maximum - minimum + 1)) + minimum

            await db.collection("users").findOneAndUpdate({
                _id: user._id
            }, {
                $set: {
                    code: randomNumber
                }
            })

            const emailHtml = "Your password reset code is: <b style='font-size: 30px;'>" + randomNumber + "</b>."
            const emailPlain = "Your password reset code is: " + randomNumber + "."

            transport.sendMail({
                from: nodemailerFrom,
                to: email,
                subject: "Password reset code",
                text: emailPlain,
                html: emailHtml
            }, function (error, info) {
                console.log("Mail sent: ", info)
            })
         
            result.json({
                status: "success",
                message: "A verification code has been sent on your email address."
            })
        })

        // route for logout request
        app.post("/logout", async function (request, result) {
            const accessToken = request.headers.authorization

            if (!accessToken) {
                result.json({
                    status: "error",
                    message: "Access token is required."
                })
                return
            }

            try {
                // Verify and decode JWT
                const decoded = jwt.verify(accessToken.replace('Bearer ', ''), jwtSecret)

                // Clear access token from database
                await db.collection("users").findOneAndUpdate({
                    _id: new ObjectId(decoded.userId)
                }, {
                    $set: {
                        accessToken: "",
                        lastLogout: new Date().toUTCString()
                    }
                })

                result.json({
                    status: "success",
                    message: "Logged out successfully."
                })
            } catch (error) {
                console.error("Logout error:", error)
                result.json({
                    status: "error",
                    message: "Invalid access token."
                })
            }
        })

        app.post("/verify-token", async function (request, result) {
            const accessToken = request.headers.authorization

            if (!accessToken) {
                result.json({
                    status: "error",
                    message: "Access token is required."
                })
                return
            }

            try {
                // Verify JWT
                const decoded = jwt.verify(accessToken.replace('Bearer ', ''), jwtSecret)

                // Get user from database
                const user = await db.collection("users").findOne({
                    _id: new ObjectId(decoded.userId),
                    accessToken: accessToken.replace('Bearer ', '')
                })

                if (!user) {
                    result.json({
                        status: "error",
                        message: "Invalid or expired token."
                    })
                    return
                }

                result.json({
                    status: "success",
                    message: "Token is valid.",
                    user: {
                        _id: user._id,
                        username: user.username,
                        profileImage: user.profileImage,
                        createdAt: user.createdAt
                    }
                })
            } catch (error) {
                console.error("Token verification error:", error)
                result.json({
                    status: "error",
                    message: "Invalid or expired token."
                })
            }
        })

        app.post("/me", auth, async function (request, result) {
            const user = request.user
         
            result.json({
                status: "success",
                message: "Data has been fetched.",
                user: {
                    _id: user._id,
                    name: user.name,
                    email: user.email,
                    profileImage: user.profileImage
                }
            })
        })

        // route for login requests
       app.post("/login", async function (request, result) {
         const username = request.fields.username
         const password = request.fields.password
         if (!username || !password) {
             result.json({
                 status: "error",
                 message: "Please fill all fields."
             })
             return
         }
         try {
             const user = await db.collection("users").findOne({
                 username: username
             })
             if (user == null) {
                 result.json({
                     status: "error",
                     message: "Username does not exist."
                 })
                 return
             }
             if (!user.isVerified) {
                 result.json({
                     status: "verificationRequired",
                     message: "Please verify your account first."
                 })
                 return
             }
             const isVerify = bcryptjs.compareSync(password, user.password)
             if (isVerify) {
                 const accessToken = jwt.sign({
                     userId: user._id.toString(),
                     username: user.username,
                     time: new Date().getTime()
                 }, jwtSecret, {
                     expiresIn: (60 * 60 * 24 * 30)
                 })
                 await db.collection("users").findOneAndUpdate({
                     username: username
                 }, {
                     $set: {
                         accessToken: accessToken,
                         lastLogin: new Date().toUTCString()
                     }
                 })
                 result.json({
                     status: "success",
                     message: "Login successful.",
                     accessToken: accessToken,
                     user: {
                         _id: user._id,
                         username: user.username,
                         profileImage: user.profileImage,
                         createdAt: user.createdAt
                     }
                 })
                 return
             }
             result.json({
                 status: "error",
                 message: "Password is not correct."
             })
         } catch (error) {
             console.error("Login error:", error)
             result.json({
                 status: "error",
                 message: "An error occurred during login. Please try again."
             })
         }
     })
       app.post("/register", async function (request, result) {
            const username = request.fields.username
            const password = request.fields.password
            const confirmPassword = request.fields.confirmPassword
            const createdAt = new Date().toUTCString()

            // Validate required fields
            if (!username || !password || !confirmPassword) {
                result.json({
                    status: "error",
                    message: "Please enter all values."
                })
                return
            }

            // Check if passwords match
            if (password !== confirmPassword) {
                result.json({
                    status: "error",
                    message: "Passwords do not match."
                })
                return
            }

            // Check password strength (optional - add your own requirements)
            if (password.length < 6) {
                result.json({
                    status: "error",
                    message: "Password must be at least 6 characters long."
                })
                return
            }

            try {
                // Check if username already exists
                const existingUser = await db.collection("users").findOne({
                    username: username
                })

                if (existingUser != null) {
                    result.json({
                        status: "error",
                        message: "Username already exists."
                    })
                    return
                }

                // Hash the password
                const salt = bcryptjs.genSaltSync(10)
                const hash = bcryptjs.hashSync(password, salt)

                // Insert user in database
                await db.collection("users").insertOne({
                    username: username,
                    password: hash,
                    accessToken: "",
                    createdAt: createdAt,
                    profileImage: null,
                    isVerified: true // Set to false if you want email verification
                })

                result.json({
                    status: "success",
                    message: "Account has been created successfully."
                })
            } catch (error) {
                console.error("Registration error:", error)
                result.json({
                    status: "error",
                    message: "Failed to create account. Please try again."
                })
            }
        })
        app.get("/change-password", function (request, result) {
            result.render("change-password", {
                mainURL: mainURL
            })
        })

        app.get("/profile", function (request, result) {
            result.render("profile", {
                mainURL: mainURL
            })
        })

        app.get("/reset-password/:email", function (request, result) {
            result.render("reset-password", {
                mainURL: mainURL,
                email: request.params.email || ""
            })
        })

        app.get("/verify-email/:email", function (request, result) {
            result.render("verify-email", {
                mainURL: mainURL,
                email: request.params.email || ""
            })
        })

        app.get("/forgot-password", function (request, result) {
            result.render("forgot-password", {
                mainURL: mainURL
            })
        })

        app.get("/profile", function (request, result) {
            result.render("profile", {
                mainURL: mainURL
            })
        })

        app.get("/register", function (request, result) {
            result.render("register", {
                mainURL: mainURL
            })
        })

        app.get("/login", function (request, result) {
            result.render("login", {
                mainURL: mainURL
            })
        })

        app.get("/", function (request, result) {
            result.render("index", {
                mainURL: mainURL
            })
        })

        // app._router.stack.forEach(function(r){
        //     if (r.route){
        //         console.log({
        //             path: r.route.path,
        //             method: r.route.methods.post ? "POST" : "GET"
        //         })
        //     }
        // })
    })
 
})