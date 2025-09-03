import joblib, pandas as pd
from flask import Flask, request, jsonify

app = Flask(__name__)
models = joblib.load("sustainability_models.pkl")

tip_texts = {
    "Energy": "Reduce energy consumption",
    "Water": "Save water",
    "Waste": "Recycle more",
    "Mobility": "Use public transport",
    "Food": "Reduce food waste",
    "Air": "Reduce emissions",
    "Trees": "Plant trees",
    "Land": "Avoid land degradation"
}

@app.route("/predict", methods=["POST"])
def predict():
    data = request.json  # {"Energy":130,"Water":2.5,...}
    df = pd.DataFrame([data])
    tips = {}
    for col, model in models.items():
        if model.predict(df)[0] == 1:
            tips[col] = tip_texts[col]
    return jsonify(tips)

if __name__ == "__main__":
    app.run(port=5000)
