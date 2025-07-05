// const urlParams = new URLSearchParams(window.location.search);
// let token = urlParams.get('token') || localStorage.getItem("jwt");
//
// if (!token) {
//     window.location.href = "/login";
//     throw new Error("No token found");
// }
//
// // Store token if it came from URL
// if (urlParams.get('token')) {
//     localStorage.setItem("jwt", token);
//     // Clean URL
//     window.history.replaceState({}, document.title, "/chat");
// }
//
// // Load chat content
// fetch("/chat", {
//     headers: {
//         "Authorization": `Bearer ${token}`
//     }
// })
//     .then(response => {
//         if (!response.ok) throw new Error("Unauthorized");
//         return response.text();
//     })
//     .then(data => {
//         document.getElementById("content").innerHTML = data;
//     })
//     .catch(error => {
//         console.error("Error:", error);
//         localStorage.removeItem("jwt");
//         window.location.href = "/login";
//     });
let stompClient = null;
function connect() {
    const username = document.getElementById('username').textContent;
    const room = document.getElementById('room').textContent;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // Subscribe to the specific room topic
        stompClient.subscribe(`/topic/${room}`, function(message) {
            onMessageReceived(JSON.parse(message.body));
        });

        // Tell server the user has joined
        const chatMessage = {
            sender: username,
            content: `${username} joined!`,
            type: 'JOIN',
            chatRoom: room
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    }, onError);
}
function onError(error) {
    console.error('Error:', error);
    setTimeout(connect, 5000);
}

function sendMessage() {
    const username = document.getElementById('username').textContent;
    const room = document.getElementById('room').textContent;
    const messageInput = document.getElementById('message');
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            chatRoom: room
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}
function onMessageReceived(message) {
    const messageArea = document.getElementById('messageArea');
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');

    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        messageElement.innerHTML = `<em>${message.content}</em>`;
    } else {
        messageElement.classList.add('chat-message');
        messageElement.innerHTML = `
            <strong>${message.sender}:</strong>
            <span>${message.content}</span>
            <span class="timestamp">${new Date().toLocaleTimeString()}</span>
        `;
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}
document.addEventListener('DOMContentLoaded', function() {
    connect();

    const messageInput = document.getElementById('message');
    const sendButton = document.getElementById('sendButton');

    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    sendButton.addEventListener('click', sendMessage);
});
document.getElementById('fileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        // Handle file upload (e.g., via Fetch API or form submission)
        console.log('Selected file:', file.name);
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const loadHistoryButton = document.getElementById('loadHistoryButton');
    const messageArea = document.getElementById('messageArea');
    const room = document.getElementById('room').textContent;
    const token = localStorage.getItem("jwt");

    loadHistoryButton.addEventListener('click', function () {
        fetch(`/chat/history?room=${room}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(res => {
                if (!res.ok) throw new Error("Unauthorized or not found");
                return res.json();
            })
            .then(messages => {
                messages.forEach(msg => {
                    const div = document.createElement('div');
                    div.classList.add('message', 'chat-message');
                    div.innerHTML = `
                        <strong>${msg.sender}:</strong>
                        <span>${msg.content}</span>
                        <span class="timestamp">${new Date(msg.timestamp).toLocaleTimeString()}</span>
                    `;
                    messageArea.appendChild(div);
                });
            })
            .catch(err => {
                console.error("Error loading history:", err);
                alert("Unable to load message history.");
            });
    });
});
