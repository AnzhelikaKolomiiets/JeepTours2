from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI(title="JeepTours2 API")

class TourRequest(BaseModel):
    duration_hours: int
    difficulty: str
    preferences: str
    language: Optional[str] = "UA"  # Нове поле для мови (за замовчуванням "UA")

class Variant(BaseModel):
    id: str
    title: str
    description: str
    schedule: List[str]
    safety_tip: str

class TourResponse(BaseModel):
    tour_title: str
    recommended_car: str
    variants: List[Variant]

@app.get("/health")
def health_check():
    return {"status": "ok"}

@app.post("/generate_route", response_model=TourResponse)
def generate_route(req: TourRequest):
    if req.duration_hours <= 0:
        raise HTTPException(status_code=422, detail="Duration must be positive")

    lang = (req.language or "UA").upper()
    diff = req.difficulty.upper()

    # Підбір авто залежно від складності
    if diff == "EASY":
        car = "Jeep Compass / Renegade 4x4"
    elif diff == "HARD":
        car = "Modified Jeep Wrangler Rubicon (35' tires)"
    else:
        car = "Jeep Cherokee / Wrangler 4x4"

    if lang == "EN":
        title = f"Jeep Tour '{req.preferences.capitalize() if req.preferences else 'Highlands'}'"
        v1_title = "Option A: Mountain Ridge"
        v1_desc = f"Optimal balance of drive and scenic views for {req.duration_hours}h."
        v1_sched = [
            "00:00 - Meeting and briefing",
            f"01:30 - First stop: {req.preferences}",
            f"03:00 - Main climb (Difficulty: {diff})",
            f"0{req.duration_hours}:00 - Return to base"
        ]
        v1_safety = "Moderate off-road. Follow instructor guidance."

        v2_title = "Option B: Panoramic Route"
        v2_desc = "Alternative route focused on photo spots."
        v2_sched = [
            "00:00 - Departure",
            "02:00 - Panoramic viewpoint",
            f"0{req.duration_hours}:00 - Finish"
        ]
        v2_safety = "Wear comfortable boots and bring water."
    else:
        title = f"Джип-Тур «{req.preferences.capitalize() if req.preferences else 'Гірські вершини'}»"
        v1_title = "Варіант А: Гірський Хребет"
        v1_desc = f"Оптимальний баланс драйву та краєвидів на {req.duration_hours} год."
        v1_sched = [
            "00:00 - Збір, перевірка авто та старт",
            f"01:30 - Перша зупинка: {req.preferences}",
            f"03:00 - Основний підйом (Складність: {diff})",
            f"0{req.duration_hours}:00 - Повернення на базу"
        ]
        v1_safety = "Помірний оффроуд. Дотримуйтесь інструкцій гіда."

        v2_title = "Варіант Б: Панорамний маршрут"
        v2_desc = "Альтернативний маршрут з акцентом на фотозони."
        v2_sched = [
            "00:00 - Інструктаж та виїзд",
            "02:00 - Оглядовий майданчик",
            f"0{req.duration_hours}:00 - Фініш"
        ]
        v2_safety = "Візьміть зручне взуття та воду."

    return TourResponse(
        tour_title=title,
        recommended_car=car,
        variants=[
            Variant(id="variant_1", title=v1_title, description=v1_desc, schedule=v1_sched, safety_tip=v1_safety),
            Variant(id="variant_2", title=v2_title, description=v2_desc, schedule=v2_sched, safety_tip=v2_safety)
        ]
    )