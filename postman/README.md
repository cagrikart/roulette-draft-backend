# Postman Collections

Bu dizin, Roulette Draft Backend API'lerini test etmek için Postman collection ve environment dosyalarını içerir.

## Dosyalar

- `Roulette-Draft-API.postman_collection.json` - Tüm API endpoint'lerini içeren collection
- `Roulette-Draft-Environment.postman_environment.json` - Local environment değişkenleri
- `WebSocket-Examples.md` - WebSocket mesaj örnekleri ve test senaryoları
- `Test-Scenarios.md` - Detaylı test senaryoları

## Kurulum

1. Postman'i açın
2. **Import** butonuna tıklayın
3. `Roulette-Draft-API.postman_collection.json` dosyasını seçin
4. `Roulette-Draft-Environment.postman_environment.json` dosyasını seçin
5. Environment olarak "Roulette Draft - Local" seçin

## API Endpoints

### Players
- `GET /api/players` - Tüm oyuncuları listele (pagination desteği: ?page=0&size=20)
- `GET /api/players/{id}` - Oyuncu detayını getir
- `GET /api/players/search` - Oyuncu ara (team, position, nationality parametreleri ile)
- `GET /api/players/team/{teamName}` - Takımın oyuncularını getir

### Teams
- `GET /api/teams` - Tüm unique takımları getir
- `GET /api/teams?league=Turkish Super League` - Lig bazlı takımları getir
- `GET /api/teams/{teamName}/players` - Takımın oyuncularını getir

### Rooms (2-5 kişi)
- `POST /api/rooms` - Yeni oda oluştur (maxParticipants: 2-5)
- `POST /api/rooms/{roomId}/join` - Odaya katıl
- `POST /api/rooms/{roomId}/start` - Draft'i başlat (en az 2 kişi gerekli)
- `GET /api/rooms/{roomId}` - Oda durumunu getir

## Kullanım

### Environment Değişkenleri

Collection içindeki istekler şu değişkenleri kullanır:

- `{{baseUrl}}` - Backend URL (default: http://localhost:8080)
- `{{roomId}}` - Oda ID'si (Create Room isteğinden sonra otomatik set edilir)
- `{{userId}}` - Kullanıcı ID'si (default: user-123)
- `{{displayName}}` - Görünen isim (default: Test User)
- `{{playerId}}` - Oyuncu ID'si (default: 690137b48f9ec9ff61510797)
- `{{teamName}}` - Takım adı (default: Alanyaspor)
- `{{leagueName}}` - Lig adı (default: Turkish Super League)

### Test Senaryosu

#### 1. Player ve Team Testleri
1. **Get All Players** - Tüm oyuncuları listele
2. **Get Player by ID** - Belirli bir oyuncuyu getir
3. **Get All Teams** - Tüm takımları listele
4. **Get Teams by League** - Turkish Super League takımlarını getir
5. **Get Players by Team Name** - Takım oyuncularını getir

#### 2. Room ve Draft Testleri
1. **Create Room** - Yeni bir oda oluştur (maxParticipants: 2-5)
2. **Join Room** - Odaya katıl (2-5 kullanıcı için farklı userId ile tekrarla)
3. **Get Room by ID** - Oda durumunu kontrol et
4. **Start Draft** - Draft'i başlat (en az 2 kişi olmalı)
5. **Get Room by ID** - Draft durumunu kontrol et

### WebSocket Testi

REST API ile oda oluşturduktan ve draft'i başlattıktan sonra, WebSocket üzerinden pick işlemleri yapabilirsiniz. Detaylı örnekler için `WebSocket-Examples.md` dosyasına bakın.

## Örnek Request/Response

### Get Teams by League
**Request:**
```
GET /api/teams?league=Turkish Super League
```

**Response:**
```json
[
  {
    "id": "Alanyaspor",
    "name": "Alanyaspor",
    "league": "Turkish Super League"
  },
  {
    "id": "Galatasaray",
    "name": "Galatasaray",
    "league": "Turkish Super League"
  }
]
```

### Get Player by ID
**Request:**
```
GET /api/players/690137b48f9ec9ff61510797
```

**Response:**
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

## Notlar

- Backend'in çalışıyor olduğundan emin olun (http://localhost:8080)
- MongoDB'nin çalışıyor olduğundan emin olun
- Create Room isteği `roomId` değişkenini otomatik olarak set eder
- Birden fazla kullanıcı ile test etmek için environment'te `userId` ve `displayName` değerlerini değiştirin
- Oda limiti: 2-5 kişi arası
- Draft başlatmak için en az 2 kişi gerekli
