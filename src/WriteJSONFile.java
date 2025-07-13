import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.geometry.Point2D;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WriteJSONFile {
    public void writeDataToJson(List<PointData> pointDataList) throws IOException {
        JSONArray jsonArray = readExistingData();
    
         int pointCounter = jsonArray.size() + 1;;
	    for (PointData pointData : pointDataList) {
	    	
	    	Point2D point = pointData.getPointData();
            String labelText = "P" + pointCounter + ": (" + (int) point.getX() + " , " + (int) point.getY() + ")";
	        JSONObject jsonObject = new JSONObject();
	        jsonObject.put("x", (int)pointData.getPointData().getX());
	        jsonObject.put("y", (int)pointData.getPointData().getY());
	        jsonObject.put("labelText", labelText);
	        jsonArray.add(jsonObject);
	        
	        pointCounter++;
	    }
	
	    writeJsonFile(jsonArray);
	}

    public JSONArray readExistingData() throws IOException {
        
        JSONArray jsonArray = new JSONArray();

        Path filePath = Paths.get("cephTool/json/TestJson_file.json");
        
        if (Files.exists(filePath)) {
            try (FileReader reader = new FileReader(filePath.toString())) {
                
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(reader);

               
                if (obj instanceof JSONArray) {
                    jsonArray = (JSONArray) obj;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                System.err.println("Error parsing existing JSON data: " + e.getMessage());
            }
        }

        return jsonArray;
    }

    public void writeJsonFile(JSONArray jsonArray) {
        
        Path filePath = Paths.get("cephTool/json/TestJson_file.json");
       
        try (FileWriter fileWriter = new FileWriter(filePath.toString())) {
            fileWriter.write(jsonArray.toJSONString());
            System.out.println("Data successfully written to JSON file.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing JSON data to file: " + e.getMessage());
        }
    }
}

