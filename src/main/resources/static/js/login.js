// Nuclear option - disable all form submissions
(function() {
    // 1. Remove all forms from DOM
    document.querySelectorAll('form').forEach(form => form.remove());

    // 2. Block all keyboard submissions
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            e.stopImmediatePropagation();
            console.log('Enter key completely disabled');
            return false;
        }
    }, true);

    // 3. Main login handler
    const loginButton = document.getElementById('loginButton');
    if (!loginButton) return;

    // Create completely new button
    const newButton = loginButton.cloneNode(true);
    loginButton.parentNode.replaceChild(newButton, loginButton);

    newButton.addEventListener('click', async function() {
        console.log('Atomic login operation started');

        // Freeze UI
        this.disabled = true;
        document.body.style.pointerEvents = 'none';
        console.log('UI completely frozen');

        try {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value.trim();

            console.log('Starting login request');
            const startTime = performance.now();

            const response = await fetch('/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ username, password }),
                credentials: 'same-origin'
            });

            console.log(`Request completed in ${(performance.now() - startTime).toFixed(2)}ms`);

            if (!response.ok) throw new Error(await response.text());

            const token = await response.text();
            localStorage.setItem('jwt', token);
            console.log('Redirecting programmatically');
            window.location.assign('/chat'); // Absolute navigation

        } catch (error) {
            console.error('Login failed:', error);
            document.getElementById('error').textContent = error.message;
        } finally {
            document.body.style.pointerEvents = '';
            newButton.disabled = false;
            console.log('UI restored');
        }
    });

    console.log('Atomic login handler installed');
})();