# Roulette Draft Backend

Spring Boot 3.3 + Java 17 + MongoDB + WebSocket backend projesi.

## Gereksinimler

- Java 17
- Maven 3.6+
- Docker ve Docker Compose (MongoDB için)

## Kurulum

1. MongoDB'yi başlat:
```bash
docker-compose up -d
```

2. Projeyi derle:
```bash
mvn clean install
```

3. Uygulamayı çalıştır:
```bash
mvn spring-boot:run
```

API dokümantasyonu: http://localhost:8080/swagger-ui.html

## API Endpoints

### Players
- `GET /api/players` - Tüm oyuncuları listele
- `GET /api/players/team/{teamName}` - Takımın oyuncularını getir
- `GET /api/players/{id}` - Oyuncu detayını getir

### Rooms (2-5 kişi)
- `POST /api/rooms` - Yeni oda oluştur (maxParticipants: 2-5)
- `POST /api/rooms/{roomId}/join` - Odaya katıl
- `POST /api/rooms/{roomId}/start` - Draft'i başlat (en az 2 kişi gerekli)
- `GET /api/rooms/{roomId}` - Oda durumunu getir

## WebSocket

Endpoint: `ws://localhost:8080/ws`

### Mesaj Tipleri

**Client → Server:**
- `SUBSCRIBE_ROOM`: `{type:"SUBSCRIBE_ROOM", roomId:"...", userId:"..."}`
- `PICK_PLAYER`: `{type:"PICK_PLAYER", roomId:"...", userId:"...", playerId:"..."}`

**Server → Client:**
- `ROOM_UPDATED`: Oda durumu güncellendi
- `PICK_MADE`: Oyuncu seçildi
- `ERROR`: Hata oluştu
- `TIMER_TICK`: Zamanlayıcı güncellemesi (30 saniye)

## Oda Kuralları

- **Minimum katılımcı**: 2 kişi
- **Maksimum katılımcı**: 5 kişi
- **Draft başlatma**: En az 2 kişi gerekli
- **Zamanlayıcı**: Her pick için 30 saniye

## Veritabanı

MongoDB bağlantısı: `mongodb://localhost:27017/roulette_draft`

### Collections

- `roulette-draft` - Oyuncular
- `rooms` - Odalar
- `room_participants` - Oda katılımcıları
- `draft_picks` - Draft seçimleri

### Player Entity Yapısı

```java
{
  id: String (ObjectId)
  team: String
  name: String
  position: String
  nationality: String
  market_value: String (örn: "€250k")
}
```
