from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI(title="Jeep Tours API")

class TourRequest(BaseModel):
    duration_hours: int
    difficulty: str
    preferences: str
    language: Optional[str] = "UA"

class RouteOption(BaseModel):
    id: str = ""
    name: str
    description: str
    schedule: List[str]

class TourResponse(BaseModel):
    tour_title: str
    estimated_time: str = ""
    recommended_vehicle: str
    routes: Optional[List[RouteOption]] = None
    itinerary: Optional[List[str]] = None
    safety_tips: str

@app.post("/api/v1/tours/generate", response_model=TourResponse)
@app.post("/generate_route", response_model=TourResponse)
def generate_route(req: TourRequest):
    lang = (req.language or "UA").upper()

    if lang == "EN":
        return TourResponse(
            tour_title=f"Jeep Tour '{req.preferences}'",
            estimated_time=f"{req.duration_hours} hours",
            recommended_vehicle="Jeep Wrangler 4x4",
            routes=[
                RouteOption(
                    id="r1",
                    name="Scenic Mountain Trail",
                    description=f"Active tour ({req.difficulty} difficulty level).",
                    schedule=["09:00 - Departure", "12:00 - Waterfall stop", "15:00 - Return"]
                )
            ],
            safety_tips="Always wear seatbelts and follow instructor guidance."
        )
    else:
        return TourResponse(
            tour_title=f"Джип-Тур «{req.preferences}»",
            estimated_time=f"{req.duration_hours} год.",
            recommended_vehicle="Jeep Wrangler 4x4",
            routes=[
                RouteOption(
                    id="r1",
                    name="Мальовничий гірський маршрут",
                    description=f"Активний тур (рівень складності: {req.difficulty}).",
                    schedule=["09:00 - Виїзд", "12:00 - Зупинка біля водоспаду", "15:00 - Повернення"]
                )
            ],
            safety_tips="Завжди пристібайте ремені безпеки та слухайте інструктора."
        )