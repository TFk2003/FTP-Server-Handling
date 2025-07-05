function logout() {
    localStorage.removeItem("jwt");  // 🔥 Remove the token
    window.location.href = "/login"; // 🔁 Redirect to login
}
