# Chat Portal with FTP-Enabled File Transfer

A simple web-based Chat Application built with Spring Boot and Thymeleaf that supports:

- ✅ Real-time chat using WebSocket (STOMP protocol)
- ✅ User registration and login with Spring Security
- ✅ File uploads to an FTP Server (FileZilla Server)
- ✅ Dashboard to navigate between Chat, Files, and Chatrooms
- ✅ Logout functionality
- ✅ Simple, clean UI with CSS styling

## 📂 Project Structure

- **Backend:** Java, Spring Boot, Spring Security, WebSocket
- **Frontend:** Thymeleaf Templates, HTML, CSS
- **FTP Server:** FileZilla Server (for file storage)

## ⚙️ Main Features

- User Registration and Login
- Real-time messaging between users
- Upload and view files stored on FileZilla FTP Server
- Search and view chat room details
- View own chat history
- Secure login/logout management

## 🚀 How to Run

1. Clone the repository
2. Install and configure FileZilla Server locally (FTP user, FTP path)
3. Configure `application.properties` for database and FTP details
4. Start the Spring Boot application
5. Open `http://localhost:8081/login` to start using the portal

## 🛠 Technologies Used

- Spring Boot
- Spring Security
- WebSocket (STOMP)
- Thymeleaf
- MySQL
- FileZilla Server (FTP)

---

