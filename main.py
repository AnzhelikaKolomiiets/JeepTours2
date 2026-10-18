from fastapi import FastAPI
from pydantic import BaseModel
from typing import List

app = FastAPI()

class TourRequest(BaseModel):
    duration_hours: int
    difficulty: str
    preferences: str

class RouteOption(BaseModel):
    id: str
    name: str
    description: str
    schedule: List[str]

class TourResponse(BaseModel):
    tour_title: str
    estimated_time: str
    recommended_vehicle: str
    routes: List[RouteOption]
    safety_tips: str

@app.post("/api/v1/tours/generate", response_model=TourResponse)
def generate_tour(req: TourRequest):
    return TourResponse(
        tour_title=f"Джип-тур по Карпатах ({req.preferences})",
        estimated_time=f"{req.duration_hours} годин",
        recommended_vehicle="Jeep Wrangler 4x4",
        routes=[
            RouteOption(
                id="route_1",
                name="Варіант А: Експрес Панорама",
                description="Швидкий підйом на оглядовий хребет з зупинкою біля водоспаду.",
                schedule=[
                    "09:00 - Старт та виїзд на підйом",
                    "10:30 - Зупинка біля водоспаду, фотосесія",
                    "12:00 - Оглядовий майданчик на вершині",
                    "13:00 - Повернення на базу"
                ]
            ),
            RouteOption(
                id="route_2",
                name="Варіант Б: Глибокий Оффроуд",
                description="Складніший маршрут через лісові хащі та гірські річки.",
                schedule=[
                    "10:00 - Інструктаж та перетин річки вброд",
                    "11:30 - Подолання лісового підйому (Extreme)",
                    "13:00 - Обід біля високогірного озера",
                    "14:00 - Спуск панорамною стежкою"
                ]
            )
        ],
        safety_tips="Обов'язково пристібайте ремені безпеки та слухайте інструкції гіда."
    )