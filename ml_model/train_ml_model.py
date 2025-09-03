import pandas as pd
from sklearn.tree import DecisionTreeClassifier
import joblib

files = ["Energy.csv","Water.csv","Waste.csv","Mobility.csv",
         "Food.csv","Air.csv","Biodiversity.csv","Land.csv"]

dfs = []
for f in files:
    df = pd.read_csv("Data/"+f)
    second_col = df.columns[1]   # always pick the second column
    dfs.append(df[second_col])

data = pd.concat(dfs, axis=1)
data.columns = ['Energy','Water','Waste','Mobility','Food','Air','Trees','Land']

limits = [120, 2.0, 5, 40, 10, 100, 0, 2.0]

labels = pd.DataFrame({
    col: (data[col] > limits[i]).astype(int)
    for i, col in enumerate(data.columns)
})

models = {}
for col in data.columns:
    clf = DecisionTreeClassifier()
    clf.fit(data, labels[col])
    models[col] = clf

joblib.dump(models, "sustainability_models.pkl")
print("✅ Models trained and saved.")

