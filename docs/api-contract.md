# API Contract: JeepTours2

Цей документ визначає специфікацію HTTP API між мобільним застосунком Android та бекенд-сервісом FastAPI.

---

## Загальна інформація
- **Base URL (Emulator):** `http://10.0.2.2:8000`
- **Base URL (Local/Host):** `http://localhost:8000` або `http://0.0.0.0:8000`
- **Формат даних:** `application/json`

---

## Endpoints

### 1. Health Check
Перевірка доступності та стану бекенд-сервісу.

- **Method:** `GET`
- **Path:** `/health`
- **Headers:** `Accept: application/json`

#### Response (200 OK)
```json
{
  "status": "ok"
}
2. Generate Route (Основний сценарій)
Приймає вхідні параметри туру від користувача та повертає динамічно згенерований джип-тур.

Method: POST

Path: /generate_route

Content-Type: application/json

Accept: application/json

Request Body
JSON
{
  "duration_hours": 4,
  "difficulty": "MEDIUM",
  "preferences": "водоспади та оглядові майданчики"
}
Опис полів запиту:

duration_hours: integer, обов'язкове (значення > 0)

difficulty: string, обов'язкове ("EASY" | "MEDIUM" | "HARD")

preferences: string, обов'язкове

Response Body (200 OK)
JSON
{
  "tour_title": "Джип-Тур «Водоспади та оглядові майданчики»",
  "recommended_car": "Jeep Cherokee / Wrangler 4x4",
  "variants": [
    {
      "id": "variant_1",
      "title": "Варіант А: Гірський Хребет",
      "description": "Оптимальний баланс драйву та краєвидів на 4 год (водоспади та оглядові майданчики).",
      "schedule": [
        "00:00 - Збір, перевірка авто та старт",
        "01:30 - Перша зупинка: локація (водоспади та оглядові майданчики)",
        "03:00 - Основний підйом (Складність: MEDIUM)",
        "04:00 - Повернення на базу"
      ],
      "safety_tip": "Помірний оффроуд. Дотримуйтесь інструкцій гіда під час підйомів."
    },
    {
      "id": "variant_2",
      "title": "Варіант Б: Панорамний маршрут",
      "description": "Альтернативний маршрут з акцентом на фотозони.",
      "schedule": [
        "00:00 - Інструктаж та виїзд",
        "02:00 - Оглядовий майданчик",
        "04:00 - Фініш"
      ],
      "safety_tip": "Візьміть зручне взуття та воду."
    }
  ]
}
Error Response (422 Unprocessable Entity / 500 Internal Error)
JSON
{
  "detail": "Invalid input format or server error"
}