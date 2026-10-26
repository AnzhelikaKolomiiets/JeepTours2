# Testing & Execution Guide: JeepTours2

## Запуск Backend
1. Перейти в директорію бекенду: `cd backend`
2. Активувати віртуальне середовище: `venv\Scripts\activate`
3. Запустити FastAPI сервер:
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000