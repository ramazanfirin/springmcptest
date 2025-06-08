document.addEventListener('DOMContentLoaded', () => {
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const chatHistory = document.getElementById('chat-history');

    function addMessageToHistory(message, type) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message');
        messageDiv.classList.add(type === 'my' ? 'my-message' : 'other-message');
        messageDiv.textContent = message;
        chatHistory.appendChild(messageDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight; // Scroll to bottom
    }

    async function sendMessage() {
        const message = messageInput.value.trim();
        if (message === '') {
            return; // Boş mesaj gönderme
        }

        addMessageToHistory(message, 'my'); // Kendi mesajımı history'ye ekle

        // GET isteği gönderme
        const apiUrl = `http://192.168.1.101:8080/persons/freeText/${encodeURIComponent(message)}`;
        try {
            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.text();
            // Gelen mesajı history'ye ekle
            addMessageToHistory(data || "Mesaj alındı.", 'other'); // Örnek olarak bir 'reply' alanı bekliyoruz
        } catch (error) {
            console.error('Mesaj gönderilirken bir hata oluştu:', error);
            addMessageToHistory("Mesaj gönderilemedi veya bir hata oluştu.", 'other');
        }

        messageInput.value = ''; // Mesaj kutusunu temizle
    }

    // Gönder butonuna tıklanınca
    sendButton.addEventListener('click', sendMessage);

    // Enter tuşuna basınca
    messageInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    // Sayfa yüklendiğinde örnek mesajlar ekleyelim (isteğe bağlı)
    addMessageToHistory("Merhaba! Nasıl yardımcı olabilirim?", 'other');
    addMessageToHistory("Selam, bir test mesajı gönderiyorum.", 'my');
});