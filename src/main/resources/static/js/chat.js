let stompClient = null;
let currentRoomSubscription = null;

function connect() {

    const username = document.getElementById('username').textContent;
    const room = document.getElementById('room').textContent;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // Subscribe to the specific room topic
        // stompClient.subscribe(`/topic/${room}`, function(message) {
        //     onMessageReceived(JSON.parse(message.body));
        // });

        stompClient.subscribe('/user/queue/private', function(message) {
            const privateMsg = JSON.parse(message.body);
            onMessageReceived(privateMsg); // show in chat window or popup
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
            chatRoom: room
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        onMessageReceived(chatMessage, false);

    }
    messageInput.value = '';

}

function sendPrivateMessage(receiverUsername, content) {
    const sender = document.getElementById('username').textContent;

    const privateMessage = {
        sender: sender,
        receiver: receiverUsername,
        content: content,
        type: 'CHAT'
    };

    stompClient.send("/app/chat.sendPrivate", {}, JSON.stringify(privateMessage));
}

function onMessageReceived(message, isPrivate) {

    const messageArea = document.getElementById('messageArea');
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');

    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        messageElement.innerHTML = `<em>${message.content}</em>`;
    } else {
        messageElement.classList.add(isPrivate ? 'private-message' : 'chat-message');
        messageElement.innerHTML = `
             <strong>${message.sender}:</strong>
            <span>${message.content}</span>
            <span class="timestamp">${new Date().toLocaleTimeString()}</span>
        `;
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function loadGroupHistory() {
    const room = document.getElementById('room').textContent;
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

document.addEventListener('DOMContentLoaded', function() {
    connect();
    function handleLinkClick(isPrivate) {
        return function(e) {
            e.preventDefault();

            document.querySelectorAll('.private-link, .room-link').forEach(l => l.classList.remove('active'));

            this.classList.add('active');

            const selectedUser = this.getAttribute('data-user');
            updateChatHeading(isPrivate, selectedUser);

            document.getElementById('room').textContent = selectedUser;

            const messageArea = document.getElementById('messageArea');
            messageArea.innerHTML = '';

            if (currentRoomSubscription) {
                currentRoomSubscription.unsubscribe();
                currentRoomSubscription = null;
            }
            if (!isPrivate) {
                currentRoomSubscription = stompClient.subscribe(`/topic/${selectedUser}`, function(message) {
                    onMessageReceived(JSON.parse(message.body), false);
                });
                const chatMessage = {
                    sender: document.getElementById('username').textContent,
                    content: `${document.getElementById('username').textContent} joined!`,
                    type: 'JOIN',
                    chatRoom: selectedUser
                };
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
                loadGroupHistory();
            } else {
                loadPrivateHistory(selectedUser);
            }
        };
    }

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

    document.getElementById('fileInput').addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Handle file upload (e.g., via Fetch API or form submission)
            console.log('Selected file:', file.name);
        }
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

