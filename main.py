from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI()

# 1. Pydantic-моделі (структура залишається абсолютно такою ж, як в Android)
class TourRequest(BaseModel):
    durationHours: int = 4
    difficulty: str = "MEDIUM"
    preferences: str = ""
    language: str = "UA"

class RouteOption(BaseModel):
    name: str
    description: str
    schedule: List[str]

class TourResponse(BaseModel):
    tourTitle: str
    recommendedVehicle: str
    routes: List[RouteOption]
    safetyTips: str

@app.get("/")
def read_root():
    return {"status": "ok", "message": "FastAPI Server is running!"}

# 2. Обробник запиту з двома сумісними роутами
@app.post("/generate_route", response_model=TourResponse)
@app.post("/generate-tour", response_model=TourResponse)
def generate_route(request: TourRequest):
    is_en = (request.language or "UA").upper() == "EN"
    diff = (request.difficulty or "MEDIUM").upper()
    
    # 🚗 Динамічна вибір авто відповідно до складності та тривалості
    if diff == "EASY":
        vehicle = "Suzuki Jimny 4x4" if is_en else "Сузукі Джимні 4x4"
    elif diff == "HARD" or request.durationHours > 6:
        vehicle = "Nissan Patrol Heavy Modified 4x4" if is_en else "Ніссан Патрол (Підготовлений 4x4)"
    else:
        vehicle = "Jeep Wrangler Rubicon 4x4" if is_en else "Джип Вранглер Рубікон 4x4"

    # Формуємо відповідь залежно від обраної мови
    if is_en:
        return TourResponse(
            tourTitle=f"Jeep Tour '{request.preferences}'",
            recommendedVehicle=vehicle,
            routes=[
                RouteOption(
                    name="Scenic Mountain Trail",
                    description=f"Active tour ({request.durationHours}h, level: {request.difficulty}).",
                    schedule=["09:00 - Departure", "12:00 - Rest & viewpoints", "15:00 - Return"]
                )
            ],
            safetyTips="Always fasten your seatbelts and listen to the instructor."
        )
    else:
        return TourResponse(
            tourTitle=f"Джип-Тур «{request.preferences}»",
            recommendedVehicle=vehicle,
            routes=[
                RouteOption(
                    name="Мальовничий гірський маршрут",
                    description=f"Активний тур ({request.durationHours} год, рівень: {request.difficulty}).",
                    schedule=["09:00 - Виїзд", "12:00 - Відпочинок та оглядові майданчики", "15:00 - Повернення"]
                )
            ],
            safetyTips="Завжди пристібайте ремені безпеки та слухайте інструктора."
        )