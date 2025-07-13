import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DrawPoint {
	private Circle circle;
	public ArrayList<Circle> circleList = new ArrayList<Circle>(); 
	private int pointCounter = 1;
	private Label circleLabel;
	public ArrayList<Label> labelList = new ArrayList<Label>();
	private ArrayList<PointData> pointDataArrayList = new ArrayList<PointData>();
	private UpdateJSONData updateJSONData;
	private Pane rootPane = new Pane(); 
	private double originalX, originalY; 
	private boolean isDragging = false;
	private double newX,newY;
	private boolean updateLineFlag = false;
    private boolean updateTriangleFlag = false;
    private DrawLine drawLine;
	private DrawTriangle drawTriangle;
	private double clickedX, clickedY;
	private Circle selectedCircle = null;
	 private Stack<Action> undoStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();
	//private int pointCounter = circleList.size() + 1;
	
	public DrawPoint(Pane rootPane, DrawLine drawLine, DrawTriangle drawTriangle) {
        this.rootPane = rootPane;
		this.drawLine = drawLine;
		this.drawTriangle = drawTriangle;
        setupMouseHandlers();
        updateJSONData = new UpdateJSONData();	
    }

	public void clearCircles() {
        rootPane.getChildren().removeAll(circleList);
        rootPane.getChildren().removeAll(labelList);
        circleList.clear();
        labelList.clear();
    }
	
	private void setupMouseHandlers() {
		rootPane.setOnMouseClicked(e -> {
			if(e.getButton()== MouseButton.PRIMARY) {	
					//selectCircle(e.getX(),e.getY()); 
				
			}else if(e.getButton() == MouseButton.SECONDARY) {
			    double x = e.getX();
		        double y = e.getY();
		        for (Circle c : circleList) {
		            if (c.contains(x, y)) {
		                deleteCircle(c, e.getScreenX(), e.getScreenY());
		                break;
		            }
		        }
			}
		});
		
    } 

	public void selectCircle(double x, double y) {
		clickedX = x;
	    clickedY = y; 
        boolean existingCircleSelected = false;
    	if (selectedCircle == null) {
	        for (Circle c : circleList) {
	            if (c.contains(x, y)) {
	                selectedCircle = c; 
	                existingCircleSelected = true;
	                break;
	            }
	        }
    	}
		
        if (!existingCircleSelected) { 
            drawCircle(x, y);
            
        }
        
    }
	public Circle drawCircle(double x, double y) {
		
		boolean collisionDetected = false;
	    for (Circle existingCircle : circleList) {
	        double distance = Math.sqrt(Math.pow(x - existingCircle.getCenterX(), 2) +
	                                    Math.pow(y - existingCircle.getCenterY(), 2));
	        double radiusSum = 10; 
	        if (distance < radiusSum) {
	            collisionDetected = true;
	            break;
	        }
	    }
	    if (!collisionDetected) {
	    	int pointCounter = circleList.size() + 1;
			circle = new Circle((int)x,(int)y,5, Color.RED);
			circleList.add(circle);
		
			String labelText = "P" + pointCounter + ": (" + (int) x + " , " + (int) y + ")";
			circleLabel = new Label(labelText);
		
			circleLabel.setLayoutX(x - 15);
			circleLabel.setLayoutY(y + 10);
			labelList.add(circleLabel);
			circleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
			circleLabel.setTextFill(Color.WHITE); 
			
			pointDataArrayList.add(new PointData(new Point2D(x, y), new String(labelText)));
			
			for(Circle circle:circleList) {
			circle.setOnMouseEntered(e -> {
		        circle.setCursor(Cursor.HAND);
		        
		    });
			circle.setOnMouseExited(e -> {
		        circle.setCursor(Cursor.DEFAULT);   
		    });
			}
			circleDragged(circle);
			rootPane.getChildren().addAll(circle, circleLabel);
			try {
		        labelText = circleLabel.getText(); 
		        double labelX = circleLabel.getLayoutX();
		        double labelY = circleLabel.getLayoutY();
		        updateJSONData = new UpdateJSONData();
		        String jsonFilePath = "cephTool/json/TestJson_file.json";
		        updateJSONData.addNewData(x, y, labelText, jsonFilePath);
		    } catch (IOException e) { 
		        e.printStackTrace(); 
		    }
			pointCounter++;
			
		}
	    return circle;
	}
	public void circleDragged(Circle circle) {
		
	    circle.setOnMousePressed(e -> {
			
			originalX = circle.getCenterX();
		    originalY = circle.getCenterY();
	        circle.setCursor(Cursor.HAND);
	        circle.toFront(); 
	        circle.setFill(Color.ORANGE);
	        isDragging = false;
			
	        
	    });

	    circle.setOnMouseDragged(e -> {
			updateLineFlag = true;
			isDragging = true;
	        if (isDragging) {
	           newX = e.getX();
	           newY = e.getY();
	           boolean collisionDetected = false;
	            for (Circle otherCircle : circleList) {
	                if (otherCircle != circle && Math.hypot(otherCircle.getCenterX() - newX, otherCircle.getCenterY() - newY) < circle.getRadius() * 2) {
	                   
	                    newX = originalX; 
	                    newY = originalY;
	                    collisionDetected = true;
	                    break;
	                }
	            }
	            if (!collisionDetected) {
	                circle.setCenterX(newX);
	                circle.setCenterY(newY);
	            }
	           //circle.setCenterX(newX);
	           //circle.setCenterY(newY);
	           
	           /*if (updateTriangleFlag) { 
	                    if (isDragging) {
	                        drawTriangle.updateTriangle();
	                    }
	           }*/
	           
	        }      
	                int index = circleList.indexOf(circle);
	                if (index != -1) {
	                    Label label = labelList.get(index);
	                    label.setText("P" + (index + 1) + ": (" + (int) newX + ", " + (int) newY + ")");
	                    label.setLayoutX(newX - 15);
	                    label.setLayoutY(newY + 10);
	                }
	              
	                //drawLine.updateLine();
	                /*if (updateLineFlag) {
	    				System.out.println("in draw point class");
	    				if (isDragging) {
	    					drawLine.updateLine();
	    				}
	    			}*/
	                
	                isDragging = false;       
	        circle.setCursor(Cursor.CLOSED_HAND);
	    });

	    circle.setOnMouseReleased(e -> {
			
	        circle.setCursor(Cursor.DEFAULT);
	        circle.setFill(Color.RED);
	       
			//if(isDragging){
	        try {
	            int oldX = (int)originalX;
	            int oldY = (int)originalY; 
	            int newX = (int)circle.getCenterX();
	            int newY = (int)circle.getCenterY();
	            int index = circleList.indexOf(circle);
	            if (index != -1) {
	                Label label = labelList.get(index);
	                String labelText = label.getText();
	                
	                String jsonFilePath = "cephTool/json/TestJson_file.json";
	                //updateJSONData = new UpdateJSONData(); 
	                updateJSONData.updateData(oldX, oldY, newX, newY, labelText, jsonFilePath);
					System.out.println("Data Updated:");
					System.out.println("Old Coordinates: (" + oldX + ", " + oldY + ")");
					System.out.println("New Coordinates: (" + newX + ", " + newY + ")");
					System.out.println("Label Text: " + labelText);
					//saveDataToJson();
	            } 
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
		
			//}
			
			isDragging = false;
			updateLineFlag = false;
			updateTriangleFlag = false;
			
	    });
	}
	
	public void deleteCircle(Circle circle, double x, double y) {
	    
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
			int index = circleList.indexOf(circle); // Get the index of the circle
			if (index >= 0) {
				Label label = labelList.get(index); // Retrieve the corresponding label
				rootPane.getChildren().removeAll(circle, label); // Remove both circle and label
				circleList.remove(circle); // Remove the circle from the list
				labelList.remove(label);

				DeletePointAction deleteAction = new DeletePointAction(rootPane, circle, label, this);
				deleteAction.execute();
				
				// Add the action to the undo stack
				undoStack.push(deleteAction);
			

                /*try {
                    updateJSONData = new UpdateJSONData();
                    updateJSONData.deleteData(circle.getCenterX(), circle.getCenterY(), "cephTool/json/TestJson_file.json");

                    
                    WriteJSONFile writeJSONFile = new WriteJSONFile();
                    JSONArray jsonArray = writeJSONFile.readExistingData();

                    
                    for (int i = index; i < jsonArray.size(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        int pointCounter = i + 1;
                        String labelText = "P" + pointCounter + jsonObject.get("labelText").toString().substring(jsonObject.get("labelText").toString().indexOf(":"));
                        jsonObject.put("labelText", labelText);
                    }
       
                    writeJSONFile.writeJsonFile(jsonArray);

					for (int i = 0; i < labelList.size(); i++) {
						labelList.get(i).setText("P" + (i + 1)  + labelList.get(i).getText().substring(labelList.get(i).getText().indexOf(":")));
					}
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
				
            }
        });
        contextMenu.getItems().add(deleteItem);
        contextMenu.show(circle, x, y);
    }

	public void undoDelete() {
        if (!undoStack.isEmpty()) {
            Action action = undoStack.pop();
            action.undo();

            // Add the action to the redo stack
            redoStack.push(action);
        }
    }

    // Method to redo the last undone delete action
    public void redoDelete() {
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redo();

            // Add the action back to the undo stack
            undoStack.push(action);
        }
    }
}

