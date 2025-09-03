🌍 Sustainability Report Project

  This project generates a sustainability dashboard using data from industries.
  It checks energy, water, waste, mobility, food, air, trees, and land usage, compares them against limits, and provides AI/ML-based advice.

📂 Project Contents

  Data/ → CSV files with daily values

  ml_model/train_ml_model.py → trains ML models

  ml_model/ml_server.py → Flask server for predictions

  SustainabilityDashboard.java → generates report.html dashboard

  lib/json-20231013.jar → JSON library for Java

🚀 How to Run

  Install Python dependencies

   <> pip install pandas scikit-learn joblib flask


  Train ML models

   <> python ml_model/train_ml_model.py


  Start Flask server

   <> python ml_model/ml_server.py


  Set OpenAI API Key (do not hardcode)

  # Windows PowerShell
  $env:OPENAI_API_KEY="your-key-here"

  # Linux/Mac
  export OPENAI_API_KEY="your-key-here"


  Run Java Dashboard

   <> javac -cp ".;lib/json-20231013.jar" SustainabilityDashboard.java
   <> java  -cp ".;lib/json-20231013.jar" SustainabilityDashboard


  Open report.html in your browser.

✨ Features

  Reads industry data from CSVs

  Detects values exceeding legal limits

  Generates charts & warnings

  Provides ML + AI-based sustainability tips
