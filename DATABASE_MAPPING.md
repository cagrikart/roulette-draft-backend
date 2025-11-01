# Veritabanı Field Mapping

Bu dosya, MongoDB'deki gerçek field isimleri ile Java model'deki field isimleri arasındaki mapping'i açıklar.

## Player Collection

MongoDB'deki field isimleri ile Java model'deki field isimleri arasındaki mapping:

| MongoDB Field | Java Field | Type | Açıklama |
|--------------|------------|------|----------|
| `_id` | `id` | String | MongoDB ObjectId (Spring Data otomatik dönüştürür) |
| `team` | `teamName` | String | Takım adı |
| `name` | `fullName` | String | Oyuncunun tam adı |
| `position` | `position` | String | Pozisyon |
| `nationality` | `nationality` | String | Milliyet |
| `market_value` | `marketValue` | String | Piyasa değeri (örn: "€250k") |

### Optional Fields

Bu field'lar veritabanında mevcut olmayabilir:

- `teamId`: String (nullable)
- `shirtNumber`: Integer (nullable)

## Field Mapping Kullanımı

Java model'inde `@Field` annotation'ı kullanılarak MongoDB field isimleri map edilir:

```java
@Field("team")
private String teamName;

@Field("name")
private String fullName;

@Field("market_value")
private String marketValue;
```

## Repository Query Methods

Spring Data MongoDB repository method'ları Java field isimlerini kullanır:

```java
// Bu method "teamName" field'ını kullanır ama DB'de "team" field'ına query yapar
List<Player> findByTeamNameIgnoreCase(String teamName);
```

Spring Data MongoDB otomatik olarak `@Field` annotation'ını dikkate alarak doğru MongoDB field'ını kullanır.

## Örnek Veri

```json
{
  "_id": ObjectId("690137b48f9ec9ff61510797"),
  "team": "Alanyaspor",
  "name": "Baran Moğultay",
  "position": "Left-Back",
  "nationality": "Türkiye",
  "market_value": "€250k"
}
```

Bu veri Java model'inde şu şekilde map edilir:

```java
Player {
  id = "690137b48f9ec9ff61510797"
  teamName = "Alanyaspor"
  fullName = "Baran Moğultay"
  position = "Left-Back"
  nationality = "Türkiye"
  marketValue = "€250k"
  teamId = null
  shirtNumber = null
}
```

