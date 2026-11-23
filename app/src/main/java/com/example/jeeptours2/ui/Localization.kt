package com.example.jeeptours2.ui

object Strings {
    fun get(key: String, isEnglish: Boolean): String {
        return if (isEnglish) {
            when (key) {
                "route_direct" -> "Direct Climb / Fast Track"
                "route_water" -> "Route via Water Bodies / Lakes / Waterfalls"
                "route_ridge" -> "Panoramic Route via Ridge & Viewpoints"
                "schedule_header" -> "Schedule & Route Thread"
                "hours_short" -> "hrs"
                "generate_btn" -> "🏔 Generate Route"
                else -> key
            }
        } else {
            when (key) {
                "route_direct" -> "Прямий підйом / Швидкий трек"
                "route_water" -> "Маршрут через водойми / Озера / Водоспади"
                "route_ridge" -> "Панорамний маршрут через хребет та оглядові місця"
                "schedule_header" -> "Графік та нитка маршруту"
                "hours_short" -> "год"
                "generate_btn" -> "🏔 Згенерувати Маршрут"
                else -> key
            }
        }
    }
}