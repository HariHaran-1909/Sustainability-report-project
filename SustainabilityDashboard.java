import java.io.*;
import java.util.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class SustainabilityDashboard {
    private static final double[] LIMITS = {120, 2.0, 5, 40, 10, 100, 0, 2.0};
    private static final String[] LABELS = {"Energy", "Water", "Waste", "Mobility", "Food", "Air", "Trees", "Land"};
    private static final String[] UNITS = {"kWh", "KL", "Kg", "Km", "Kg", "AQI", "", "Ha"};

    // ⚠️ replace with your valid key
    private static final String OPENAI_API_KEY = "sk-proj-xxxxxxxx";

    public static void main(String[] args) throws Exception {
        List<String[]>[] datasets = new List[8];
        String[] files = {"Energy.csv","Water.csv","Waste.csv","Mobility.csv","Food.csv","Air.csv","Biodiversity.csv","Land.csv"};
        
        for (int i = 0; i < files.length; i++) datasets[i] = readCSV("Data/" + files[i]);

        List<String> dates = new ArrayList<>();
        List<List<Double>> allData = new ArrayList<>();
        for (int i = 0; i < 6; i++) allData.add(new ArrayList<>());
        List<Integer> treeData = new ArrayList<>();
        List<Double> landData = new ArrayList<>();

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Sustainability Report</title>");
        html.append("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");
        html.append("<style>body{font-family:Arial;margin:20px;} table{border-collapse:collapse;width:100%;margin-bottom:40px;} ");
        html.append("th,td{border:1px solid #888;padding:6px;text-align:center;} th{background:#eee;} ");
        html.append(".warn{color:red;font-weight:bold;} .chart-container{width:45%;display:inline-block;margin:10px;}</style></head><body>");
        html.append("<h2>🌍 Sustainability Dashboard</h2>");
        html.append("<table><tr><th>Date</th>");
        for (String label : LABELS) html.append("<th>").append(label).append(" (").append(UNITS[Arrays.asList(LABELS).indexOf(label)]).append(")</th>");
        html.append("<th>Warnings + AI + ML Advice</th></tr>");

        for (int i = 0; i < datasets[0].size(); i++) {
            String date = datasets[0].get(i)[0];
            dates.add(date);

            double[] values = new double[8];
            for (int j = 0; j < 6; j++) {
                values[j] = Double.parseDouble(datasets[j].get(i)[1]);
                allData.get(j).add(values[j]);
            }
            values[6] = Integer.parseInt(datasets[6].get(i)[1]); treeData.add((int) values[6]);
            values[7] = Double.parseDouble(datasets[7].get(i)[1]); landData.add(values[7]);

            StringBuilder warn = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                if (values[j] > LIMITS[j]) {
                    String emoji = switch(j){
                        case 0 -> "⚡"; case 1 -> "💧"; case 2 -> "🗑"; case 3 -> "🚗";
                        case 4 -> "🍽"; case 5 -> "🌫"; case 6 -> "🌳"; case 7 -> "🏞"; default -> "";
                    };
                    warn.append(emoji).append(" ").append(LABELS[j]).append(" > limit<br>")
                        .append("AI: ").append(aiAdvice(LABELS[j] + " value: " + values[j])).append("<br>");
                }
            }

            // ML tips (for whole row)
            String ml = mlTips(values);
            warn.append("<b>ML Tips:</b> ").append(ml).append("<br>");

            html.append("<tr><td>").append(date).append("</td>");
            for (double v : values) html.append("<td>").append(v).append("</td>");
            html.append("<td class='warn'>").append(warn).append("</td></tr>");
        }

        html.append("</table><h3>📊 Data Visualizations</h3>");
        for (String label : LABELS) if (!label.equals("Trees") && !label.equals("Land")) 
            html.append("<div class='chart-container'><canvas id='").append(label).append("Chart'></canvas></div>");

        html.append("<script>");
        html.append("const labels = ").append(dates.toString()).append(";\n");
        for (int i = 0; i < allData.size(); i++)
            html.append("const ").append(LABELS[i].toLowerCase()).append(" = ").append(allData.get(i).toString()).append(";\n");

        html.append("function createChart(id,label,data,color){new Chart(document.getElementById(id),{type:'line',data:{labels:labels,datasets:[{label:label,data:data,fill:false,borderColor:color,tension:0.2}]},options:{responsive:true,maintainAspectRatio:false,height:200}});}\n");

        String[] colors = {"red","blue","green","orange","purple","brown"};
        for (int i = 0; i < 6; i++) 
            html.append("createChart('").append(LABELS[i]).append("Chart','").append(LABELS[i]).append("',").append(LABELS[i].toLowerCase()).append(",'").append(colors[i]).append("');\n");

        html.append("</script></body></html>");

        try (FileWriter fw = new FileWriter("report.html")) { fw.write(html.toString()); }
        System.out.println("✅ Dashboard generated: report.html");
    }

    private static List<String[]> readCSV(String filename) throws Exception {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) data.add(line.split(","));
        }
        return data;
    }

    // OpenAI advice
    private static String aiAdvice(String issue) {
        try {
            String prompt = "Give a short sustainability tip for: " + issue;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString("{" +
                            "\"model\":\"gpt-4o-mini\"," +
                            "\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt + "\"}]" +
                            "}"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
        } catch (Exception e) { return "(AI advice unavailable)"; }
    }

    // ML Flask tips
    private static String mlTips(double[] values) {
        try {
            String[] labels = {"Energy","Water","Waste","Mobility","Food","Air","Trees","Land"};
            StringBuilder json = new StringBuilder("{");
            for (int i = 0; i < values.length; i++) {
                json.append("\"").append(labels[i]).append("\":").append(values[i]);
                if (i < values.length - 1) json.append(",");
            }
            json.append("}");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return "(ML tips unavailable)";
        }
    }
}








