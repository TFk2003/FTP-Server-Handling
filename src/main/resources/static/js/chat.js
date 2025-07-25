let stompClient = null;
let currentRoomSubscription = null;

function connect() {

    // const username = document.getElementById('username').textContent;
    // const room = document.getElementById('room').textContent;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // Subscribe to the specific room topic
        // stompClient.subscribe(`/topic/${room}`, function(message) {
        //     onMessageReceived(JSON.parse(message.body));
        // });
        ['General', 'Random', 'Help'].forEach(room => {
            stompClient.subscribe(`/topic/${room}`, function (message) {
                const currentRoom = document.getElementById('room').textContent;
                const msg = JSON.parse(message.body);
console.log(currentRoom.toLowerCase())
                // Only display message if user is currently in that room
                if (msg.chatRoom === currentRoom) {
                    onMessageReceived(msg, false);
                }
            });
        });

        stompClient.subscribe('/user/queue/private', function(message) {
            const privateMsg = JSON.parse(message.body);
            onMessageReceived(privateMsg); // show in chat window or popup
        });
        const defaultRoom = document.querySelector('.room-link.active') || document.querySelector('.room-link');
        if (defaultRoom) defaultRoom.click();
        // Tell server the user has joined
        // const chatMessage = {
        //     sender: username,
        //     content: `${username} joined!`,
        //     type: 'JOIN',
        //     chatRoom: room
        // };
        // stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
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


    if (!messageContent || !stompClient) return;

    if (!(room === 'General' || room === 'Random' || room === 'Help')) {
        // ✅ Send private message
        sendPrivateMessage(room, messageContent);
    } else {
        // ✅ Send group message
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            chatRoom: room,
            timestamp: new Date().toISOString(),
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
       // onMessageReceived(chatMessage, false);

    }
    messageInput.value = '';

}

function sendPrivateMessage(receiverUsername, content) {
    const sender = document.getElementById('username').textContent;

    const privateMessage = {
        sender: sender,
        receiver: receiverUsername,
        content: content,
        type: 'CHAT',
        timestamp: new Date().toISOString(),
    };

    stompClient.send("/app/chat.sendPrivate", {}, JSON.stringify(privateMessage));
}

function onMessageReceived(message, isPrivate) {

    const messageArea = document.getElementById('messageArea');
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');
    const formattedTime = new Date(message.timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: true
    });

    if (!isPrivate && (message.type === 'JOIN' || message.type === 'LEAVE')) {
        messageElement.classList.add('event-message');
        messageElement.innerHTML = `<em>${message.content}</em>`;
    } else {
        messageElement.classList.add(isPrivate ? 'private-message' : 'chat-message');
        messageElement.innerHTML = `
             <strong>${message.sender}:</strong>
            <span>${message.content}</span>
            <span class="timestamp">${formattedTime}</span>
        `;
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function loadGroupHistory(room) {
    fetch(`/chat/history?room=${room}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    })
        .then(res => {
            if (!res.ok || res.headers.get('content-type')?.includes('text/html')) {
                throw new Error("Unauthorized or bad content");
            }
            return res.json();
        })
        .then(messages => {
            const messageArea = document.getElementById('messageArea');
            messageArea.innerHTML = '';
            messages.forEach(msg => {
                onMessageReceived(msg, false);
            });
        })
        .catch(err => {
            console.error("Error loading group history:", err);
        });
}

function loadPrivateHistory(withUser) {
    fetch(`/chat/private/history?with=${withUser}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    })
        .then(res => {
            if (!res.ok || res.headers.get('content-type')?.includes('text/html')) {
                throw new Error("Unauthorized or bad content");
            }
            return res.json();
        })
        .then(messages => {
            const messageArea = document.getElementById('messageArea');
            messageArea.innerHTML = '';
            messages.forEach(msg => {
                onMessageReceived(msg, true);
            });
        })
        .catch(err => {
            console.error("Failed to load private messages", err);
        });
}

function handleLinkClick(isPrivate) {
    return function(e) {
        e.preventDefault();

        document.querySelectorAll('.private-link, .room-link').forEach(l => l.classList.remove('active'));

        this.classList.add('active');

        const selectedUser = this.getAttribute('data-user');
        updateChatHeading(isPrivate, selectedUser);

        const room = document.getElementById('room');
        room.textContent = selectedUser;

        const messageArea = document.getElementById('messageArea');
        messageArea.innerHTML = '';

        if (currentRoomSubscription) {
            currentRoomSubscription.unsubscribe();
            currentRoomSubscription = null;
        }
        if (!isPrivate) {
            // currentRoomSubscription = stompClient.subscribe(`/topic/${selectedUser}`, function(message) {
            //     onMessageReceived(JSON.parse(message.body), false);
            // });
            // const chatMessage = {
            //     sender: document.getElementById('username').textContent,
            //     content: `${document.getElementById('username').textContent} joined!`,
            //     type: 'JOIN',
            //     chatRoom: selectedUser
            // };
            // stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
            console.log(room.textContent);
            loadGroupHistory(room.textContent);
        } else {
            loadPrivateHistory(selectedUser);
        }
    };
}

document.addEventListener('DOMContentLoaded', function() {
    connect();

    document.querySelectorAll('.private-link').forEach(link => {
        link.addEventListener('click', handleLinkClick(true));
    });

    document.querySelectorAll('.room-link').forEach(link => {
        link.addEventListener('click', handleLinkClick(false));
    });

    const messageInput = document.getElementById('message');
    const sendButton = document.getElementById('sendButton');


    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    sendButton.addEventListener('click', sendMessage);

    document.getElementById('fileInput').addEventListener('change', function (e) {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        fetch("/files/upload", {
            method: "POST",
            body: formData,
            credentials: "same-origin" // 🔸 important to maintain session (for Spring Security)
        })
            .then(res => {
                if (!res.ok) throw new Error("Upload failed");
                return res.text();
            })
            .then(() => {
                const username = document.getElementById("username").textContent;
                const room = document.getElementById("room").textContent;

                const chatMessage = {
                    sender: username,
                    content: `${username} uploaded file: <a href="/files/download/${username}/${file.name}" target="_blank">${file.name}</a>`,
                    type: "CHAT",
                    chatRoom: room,
                    timestamp: new Date().toISOString()
                };

                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
            })
            .catch(err => {
                alert("Upload failed: " + err.message);
                console.error("Upload error:", err);
            });
    });
});
function updateChatHeading(isPrivate, title) {
    const headingEl = document.getElementById('heading');
    if (isPrivate) {
        headingEl.innerHTML = `Chat Room: <span  id="room" >${title}</span>`;
    } else {
        headingEl.innerHTML = `Chat Room: <span  id="room" >${title}</span>`;
    }
}

