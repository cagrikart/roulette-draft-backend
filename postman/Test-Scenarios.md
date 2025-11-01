# Test Senaryoları

Bu dosya, Roulette Draft Backend API'lerini test etmek için adım adım senaryolar içerir.

## Senaryo 1: Player ve Team API Testleri

### Adım 1: Tüm Oyuncuları Listele
```
GET /api/players
```
**Beklenen Sonuç:** DB'deki tüm oyuncular (collection: roulette-draft)

### Adım 2: Belirli Bir Oyuncuyu Getir
```
GET /api/players/690137b48f9ec9ff61510797
```
**Beklenen Sonuç:** Baran Moğultay oyuncusu:
```json
{
  "id": "690137b48f9ec9ff61510797",
  "team": "Alanyaspor",
  "name": "Baran Moğultay",
  "position": "Left-Back",
  "nationality": "Türkiye",
  "marketValue": "€250k",
  "league": "Turkish Super League"
}
```

### Adım 3: Takımları Listele (Tüm Ligler)
```
GET /api/teams
```
**Beklenen Sonuç:** Tüm unique takımlar

### Adım 4: Turkish Super League Takımlarını Getir
```
GET /api/teams?league=Turkish Super League
```
**Beklenen Sonuç:** Sadece Turkish Super League takımları

### Adım 5: Takım Oyuncularını Getir
```
GET /api/teams/Alanyaspor/players
```
**Beklenen Sonuç:** Alanyaspor takımının oyuncuları

### Adım 6: Oyuncu Ara
```
GET /api/players/search?team=Alanyaspor&position=Left-Back&nationality=Türkiye
```
**Beklenen Sonuç:** Filtrelenmiş oyuncular

## Senaryo 2: Oda Oluşturma ve Draft (2-5 Kişi)

### Adım 1: Oda Oluştur
```json
POST /api/rooms
{
  "name": "Test Draft Room",
  "formation": "4-3-3",
  "maxParticipants": 5
}
```
**Beklenen Sonuç:** 
- Status: `WAITING`
- `roomId` environment değişkenine kaydedilmeli
- maxParticipants: 2-5 arası olmalı

### Adım 2: Kullanıcı 1 Olarak Odaya Katıl
```json
POST /api/rooms/{roomId}/join
{
  "userId": "user-1",
  "displayName": "Player 1",
  "rosterSizeLimit": 11
}
```

### Adım 3: Kullanıcı 2 Olarak Odaya Katıl
Environment'te `userId` ve `displayName` değiştir:
```json
POST /api/rooms/{roomId}/join
{
  "userId": "user-2",
  "displayName": "Player 2",
  "rosterSizeLimit": 11
}
```

### Adım 4: Oda Durumunu Kontrol Et
```
GET /api/rooms/{roomId}
```
**Beklenen Sonuç:** 
- 2 participant
- Status: `WAITING`

### Adım 5: Draft'i Başlat
```
POST /api/rooms/{roomId}/start
```
**Beklenen Sonuç:**
- Status: `DRAFTING`
- `pickOrder` array'i dolu olmalı
- `currentPickIndex`: 0
- En az 2 katılımcı olmalı

### Adım 6: WebSocket ile Pick Yap
WebSocket'e bağlan ve:
```json
{
  "type": "SUBSCRIBE_ROOM",
  "roomId": "{roomId}",
  "userId": "user-1"
}
```

Sıra sizdeyken:
```json
{
  "type": "PICK_PLAYER",
  "roomId": "{roomId}",
  "userId": "user-1",
  "playerId": "690137b48f9ec9ff61510797"
}
```

## Senaryo 3: Hata Durumları

### Test 1: Geçersiz Oyuncu ID
```
GET /api/players/invalid-id
```
**Beklenen Sonuç:** 404 Not Found

### Test 2: Dolu Odaya Katılma (Max 5)
- Odayı 5 kullanıcıyla doldur
- Bir kullanıcı daha katılmaya çalış
**Beklenen Sonuç:** Error - "Room is full. Max participants: 5"

### Test 3: Tek Kişi ile Draft Başlatma
- Sadece 1 kullanıcı katılmış olsun
- Draft başlatmaya çalış
**Beklenen Sonuç:** Error - "Need at least 2 participants to start. Current: 1"

### Test 4: DRAFTING Durumundaki Odaya Katılma
- Draft'i başlat
- Yeni kullanıcı katılmaya çalış
**Beklenen Sonuç:** Error - "Cannot join room in status: DRAFTING"

### Test 5: Geçersiz Max Participants (6)
```json
POST /api/rooms
{
  "name": "Test",
  "maxParticipants": 6
}
```
**Beklenen Sonuç:** Validation Error - "Max participants must be at most 5"

## Senaryo 4: Lig Bazlı Takım Filtreleme

### Test 1: Turkish Super League Takımları
```
GET /api/teams?league=Turkish Super League
```
**Beklenen Sonuç:** Sadece Turkish Super League takımları

### Test 2: Tüm Takımlar
```
GET /api/teams
```
**Beklenen Sonuç:** Tüm liglerden takımlar

## Hızlı Test Komutları

### cURL Örnekleri

#### Turkish Super League Takımlarını Getir
```bash
curl -X GET "http://localhost:8080/api/teams?league=Turkish%20Super%20League"
```

#### Oyuncu Ara
```bash
curl -X GET "http://localhost:8080/api/players/search?team=Alanyaspor&position=Left-Back"
```

#### Oda Oluştur
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Room",
    "formation": "4-3-3",
    "maxParticipants": 4
  }'
```

#### Odaya Katıl
```bash
curl -X POST http://localhost:8080/api/rooms/{roomId}/join \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1",
    "displayName": "Player 1",
    "rosterSizeLimit": 11
  }'
```

#### Draft Başlat
```bash
curl -X POST http://localhost:8080/api/rooms/{roomId}/start
```

## Postman Runner

Collection'ı Postman Runner ile otomatik test edebilirsiniz:

1. Postman'de Collection'a sağ tıkla
2. "Run collection" seç
3. "Iterations" sayısını ayarla
4. Run'a tıkla

Not: WebSocket testleri otomatik çalışmayabilir, manuel test gerekebilir.
