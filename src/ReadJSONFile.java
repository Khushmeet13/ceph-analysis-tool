import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Cursor;

public class ReadJSONFile {

	private Pane rootPane; 
	private JSONParser parser;
	private JSONArray jsonArray;
	public ReadJSONFile(Pane rootPane) {
		this.rootPane = rootPane;
	}
	public JSONArray readDataFromJson(String filePath) { 
		System.out.println("Reading JSON data from file: " + filePath);
        parser = new JSONParser();
        jsonArray = new JSONArray(); 

        try {
        	FileReader reader = new FileReader(filePath);
            Object obj = parser.parse(reader);
            jsonArray = (JSONArray) obj;
            System.out.println("JSON data loaded successfully.");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading JSON data: " + e.getMessage());
        }

        return jsonArray;  
    }
   
	public void processJSONData(JSONArray jsonData) {
		
        for (Object obj : jsonData) {
            JSONObject jsonObject = (JSONObject) obj;
            double x = ((Number) jsonObject.get("x")).doubleValue();
            double y = ((Number) jsonObject.get("y")).doubleValue();
            String labelText = (String) jsonObject.get("labelText");
            
            System.out.println("x: " + x);
            System.out.println("y: " + y);
            System.out.println("labelText: " + labelText);
            
            
            drawCircle(x, y, labelText); 
        }
    }
	private void drawCircle(double x, double y, String labelText) {
        Circle circle = new Circle(x, y, 5, Color.RED);
        Label label = new Label(labelText);
        
        label.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
        label.setTextFill(Color.BLACK);

        label.layoutXProperty().bind(circle.centerXProperty().subtract(label.widthProperty().divide(2)));
        label.layoutYProperty().bind(circle.centerYProperty().add(10));

        circle.setOnMouseEntered(e -> circle.setCursor(Cursor.HAND));
        circle.setOnMouseExited(e -> circle.setCursor(Cursor.DEFAULT));

        rootPane.getChildren().addAll(circle, label);
        
    }

}
