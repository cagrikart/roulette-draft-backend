# WebSocket Mesaj Örnekleri

WebSocket endpoint: `ws://localhost:8080/ws`

Postman WebSocket'i tam olarak test edemez, ancak aşağıdaki örneklerle test edebilirsiniz veya bir WebSocket istemcisi kullanabilirsiniz.

## Client → Server Mesajları

### 1. Subscribe Room

Bir odaya abone olmak için:

```json
{
  "type": "SUBSCRIBE_ROOM",
  "roomId": "room-id-123",
  "userId": "user-123"
}
```

### 2. Pick Player

Oyuncu seçmek için:

```json
{
  "type": "PICK_PLAYER",
  "roomId": "room-id-123",
  "userId": "user-123",
  "playerId": "player-id-456"
}
```

## Server → Client Mesajları

### 1. Room Updated

Oda durumu güncellendiğinde:

```json
{
  "type": "ROOM_UPDATED",
  "room": {
    "id": "room-id-123",
    "name": "Test Room",
    "status": "DRAFTING",
    "formation": "4-3-3",
    "maxParticipants": 8,
    "pickOrder": ["user-123", "user-456"],
    "currentPickIndex": 0,
    "createdAt": "2024-01-01T12:00:00",
    "participants": [
      {
        "id": "participant-id-1",
        "roomId": "room-id-123",
        "userId": "user-123",
        "displayName": "Test User 1",
        "selectedPlayerIds": [],
        "rosterSizeLimit": 11,
        "createdAt": "2024-01-01T12:00:00"
      }
    ]
  }
}
```

### 2. Pick Made

Bir oyuncu seçildiğinde:

```json
{
  "type": "PICK_MADE",
  "roomId": "room-id-123",
  "userId": "user-123",
  "playerId": "player-id-456",
  "pickNo": 1,
  "currentPickIndex": 1,
  "nextUserId": "user-456"
}
```

### 3. Error

Hata oluştuğunda:

```json
{
  "type": "ERROR",
  "reason": "NOT_YOUR_TURN",
  "message": "It's not your turn"
}
```

Hata Sebepleri:
- `ROOM_NOT_FOUND` - Oda bulunamadı
- `INVALID_STATUS` - Geçersiz oda durumu
- `NOT_YOUR_TURN` - Sıra sizde değil
- `PLAYER_ALREADY_PICKED` - Oyuncu zaten seçilmiş
- `ROSTER_LIMIT_REACHED` - Roster limiti doldu
- `CONCURRENT_MODIFICATION` - Eşzamanlı güncelleme çakışması
- `DRAFT_COMPLETE` - Draft tamamlandı
- `PICK_FAILED` - Pick işlemi başarısız

### 4. Timer Tick

Zamanlayıcı güncellendiğinde (her saniye):

```json
{
  "type": "TIMER_TICK",
  "roomId": "room-id-123",
  "remainingSeconds": 25,
  "currentUserId": "user-123"
}
```

## Test Senaryosu

1. REST API ile oda oluştur: `POST /api/rooms`
2. REST API ile odaya katıl: `POST /api/rooms/{roomId}/join` (birden fazla kullanıcı için tekrarla)
3. REST API ile draft'i başlat: `POST /api/rooms/{roomId}/start`
4. WebSocket'e bağlan: `ws://localhost:8080/ws`
5. Subscribe mesajı gönder
6. Pick mesajı gönder (sıra sizdeyken)
7. `PICK_MADE` ve `TIMER_TICK` mesajlarını dinle

## WebSocket Test Araçları

- [WebSocket King Client](https://chrome.google.com/webstore/detail/websocket-king-client/cbcbkhdmedgianpaifchdaddpnmgnknn)
- [Simple WebSocket Client](https://chrome.google.com/webstore/detail/simple-websocket-client/pfdhoblngboilpfeibedpjmobpeatfoa)
- [Postman](https://www.postman.com/) (WebSocket desteği var)

## Örnek JavaScript Client

```javascript
const ws = new WebSocket('ws://localhost:8080/ws');

ws.onopen = () => {
  console.log('Connected');
  
  // Subscribe to room
  ws.send(JSON.stringify({
    type: 'SUBSCRIBE_ROOM',
    roomId: 'room-id-123',
    userId: 'user-123'
  }));
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('Received:', message);
  
  if (message.type === 'ROOM_UPDATED') {
    console.log('Room updated:', message.room);
  } else if (message.type === 'PICK_MADE') {
    console.log('Pick made:', message);
  } else if (message.type === 'ERROR') {
    console.error('Error:', message);
  } else if (message.type === 'TIMER_TICK') {
    console.log('Timer:', message.remainingSeconds);
  }
};

ws.onerror = (error) => {
  console.error('WebSocket error:', error);
};

ws.onclose = () => {
  console.log('Disconnected');
};

// Pick a player
function pickPlayer(playerId) {
  ws.send(JSON.stringify({
    type: 'PICK_PLAYER',
    roomId: 'room-id-123',
    userId: 'user-123',
    playerId: playerId
  }));
}
```

