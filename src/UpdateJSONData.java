import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

public class UpdateJSONData {
	private WriteJSONFile writeJSONFile;
	private ReadJSONFile reader;
    private List<PointData> PointDataList = new ArrayList<>(); 

	public UpdateJSONData() {
		
		Pane pane = new Pane();
		reader = new ReadJSONFile(pane );
	}
	public void addNewData(double x, double y, String labelText, String jsonFilePath) throws IOException {
        writeJSONFile = new WriteJSONFile();
        
        JSONArray jsonArray = reader.readDataFromJson(jsonFilePath);

        boolean pointExists = false;
        double threshold = 1.0; 
        for (Object obj : jsonArray) {
        JSONObject jsonObject = (JSONObject) obj;
        double objX = ((Number) jsonObject.get("x")).doubleValue();
		double objY = ((Number) jsonObject.get("y")).doubleValue();
        
        if (Math.abs(objX - x) < threshold && Math.abs(objY - y) < threshold) {
            pointExists = true;
            break;
        }
    }
       
    if (!pointExists) {
    	PointData newData = new PointData(new Point2D(x,y), labelText);
        PointDataList.add(newData);
        writeJSONFile.writeDataToJson(PointDataList);
        System.out.println("New Data Added:");
        System.out.println("x: " + x);
        System.out.println("y: " + y);
        System.out.println("labelText: " + labelText);
    }
    
}
public void updateData(double oldX, double oldY, double newX, double newY, String labelText, String jsonFilePath) throws IOException {
    JSONParser parser = new JSONParser();
    JSONArray jsonArray;
    try (FileReader reader = new FileReader(jsonFilePath)) {
        jsonArray = (JSONArray) parser.parse(reader);
    } catch (ParseException e) {
        e.printStackTrace();
        return;
    }

    boolean found = false;
	    for (int i = 0; i < jsonArray.size(); i++) {
	        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
	        double objX = ((Number) jsonObject.get("x")).doubleValue();
	        double objY = ((Number) jsonObject.get("y")).doubleValue();
	        if (objX == oldX && objY == oldY) {
	          
	            jsonObject.put("x", newX);
	            jsonObject.put("y", newY);
	            jsonObject.put("labelText", labelText);
	            
	            found = true;
	            break;
	        }
	    }

	    if (found) {
	        try (FileWriter writer = new FileWriter(jsonFilePath)) {
	            writer.write(jsonArray.toJSONString());
	        }
	    } 

    
    
}

     public void deleteData(double x, double y, String jsonFilePath) throws IOException {
	        JSONParser parser = new JSONParser();
	        JSONArray jsonArray;
	        try (FileReader reader = new FileReader(jsonFilePath)) {
	            jsonArray = (JSONArray) parser.parse(reader);
	        } catch (ParseException e) {
	            e.printStackTrace();
	            return;
	        }

	        Iterator<Object> iterator = jsonArray.iterator();
	        while (iterator.hasNext()) {
	            Object obj = iterator.next();
	            if (obj instanceof JSONObject) {
	                JSONObject jsonObject = (JSONObject) obj;
	                double objX = ((Number) jsonObject.get("x")).doubleValue();
		            double objY = ((Number) jsonObject.get("y")).doubleValue();
	                if (objX == x && objY == y) {
	                    iterator.remove();
                        
	                    break;
	                }
	            }
	        }

	        try (FileWriter writer = new FileWriter(jsonFilePath)) {
	            writer.write(jsonArray.toJSONString());
	        }

	        System.out.println("Data Deleted:");
	        System.out.println("Point: (" + x + ", " + y + ")");
	    }


}


