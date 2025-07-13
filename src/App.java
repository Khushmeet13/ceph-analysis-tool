import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
 

public class App extends Application {
	private Pane rootPane = new Pane();
	private BorderPane borderPane = new BorderPane();
	private Image image;
	private ImageView imageView;
	private Scene scene;
	private ArrayList<Circle> circleList;
	private ArrayList<Label> labelList;
	private ReadJSONFile reader; 
	private JSONArray jsonData;
	private DrawLine drawLine;
	private DrawTriangle drawTriangle;
	private ToggleButton lineButton;
    private ToggleButton triangleButton;
    private ToggleButton updateButton;
	private ToggleButton importButton;
	private ToggleButton saveButton;
	private ToggleButton undoButton;
	private ToggleButton redoButton;
	private ToggleButton loadButton;
	private boolean isDrawingLine = false;
	private boolean updateLineFlag = false;
	private boolean updateTriangleFlag = false;
	private DrawPoint drawPoint;
	private ToggleButton zoomInButton;
    private ToggleButton zoomOutButton;
	private ToggleGroup toggleGroup;
	private Circle magnifier;
    private ImageView magnifiedImageView;
    private double magnifierRadius = 50;
    private boolean zoomEnabled = false;
    private Rectangle2D originalViewport;
	private Pane magnifiedPane = new Pane();
	private Circle startCircle;
	private Circle endCircle;
	private double originalScaleX;
    private double originalScaleY;
    private double dragStartX;
    private double dragStartY;


	Stage window;
	@Override
	public void start(Stage primaryStage) throws Exception {
		
			
		try {
			
		   window = primaryStage; 
           
           //image = new Image("lateralXRay.jpeg");
           imageView = new ImageView();
           //imageView.setFitHeight(500); 
           //imageView.setFitWidth(800);
		   
		   scene = new Scene(borderPane,800,500);
		   imageView.fitWidthProperty().bind(scene.widthProperty());
           imageView.fitHeightProperty().bind(scene.heightProperty());

            window.widthProperty().addListener((obs, oldVal, newVal) -> redrawImage());
            window.heightProperty().addListener((obs, oldVal, newVal) -> redrawImage());
			

		   ToolBar toolBar = new ToolBar();
		   //toolBar.setStyle("-fx-background-color: lightgray;");
		   importButton = new ToggleButton("Import");
           lineButton = new ToggleButton("Line");
           triangleButton = new ToggleButton("Angle");
           updateButton = new ToggleButton("Update");
		   saveButton = new ToggleButton("Save");
		   undoButton = new ToggleButton();
		   redoButton = new ToggleButton();
		   loadButton = new ToggleButton("Load");

		   Image zoomInImage = new Image(getClass().getResourceAsStream("zoom-in-icon.png"));
		   ImageView zoomInImageView = new ImageView(zoomInImage);
			zoomInImageView.setFitWidth(20); 
			zoomInImageView.setFitHeight(20);

           Image zoomOutImage = new Image(getClass().getResourceAsStream("zoom-out-icon.png"));
		   ImageView zoomOutImageView = new ImageView(zoomOutImage);
			zoomOutImageView.setFitWidth(20); 
			zoomOutImageView.setFitHeight(20);

            zoomInButton = new ToggleButton();
            zoomInButton.setGraphic(zoomInImageView);

			zoomOutButton = new ToggleButton();
            zoomOutButton.setGraphic(zoomOutImageView);

			Image undoImage = new Image(getClass().getResourceAsStream("undo-icon1.png"));
			ImageView undoImageView = new ImageView(undoImage);
			undoImageView.setFitHeight(20);
			undoImageView.setFitWidth(20);
			undoButton.setGraphic(undoImageView);

			Image redoImage = new Image(getClass().getResourceAsStream("redo-icon.png"));
			ImageView redoImageView = new ImageView(redoImage);
			redoImageView.setFitHeight(20);
			redoImageView.setFitWidth(20);
			redoButton.setGraphic(redoImageView);

		   toggleGroup = new ToggleGroup();
		   importButton.setToggleGroup(toggleGroup);
		   lineButton.setToggleGroup(toggleGroup);
           triangleButton.setToggleGroup(toggleGroup);
		   updateButton.setToggleGroup(toggleGroup);
		   zoomInButton.setToggleGroup(toggleGroup);
		   zoomOutButton.setToggleGroup(toggleGroup);
		   undoButton.setToggleGroup(toggleGroup);
		   redoButton.setToggleGroup(toggleGroup);
		   saveButton.setToggleGroup(toggleGroup);
		   loadButton.setToggleGroup(toggleGroup);

		   Image lineIcon = new Image(getClass().getResourceAsStream("line-icon.png"));
           ImageView lineIconView = new ImageView(lineIcon);
           lineIconView.setFitWidth(10); 
           lineIconView.setFitHeight(10);
           lineButton.setGraphic(lineIconView);
           
           Image angleIcon = new Image(getClass().getResourceAsStream("angle-icon.png"));
           ImageView angleIconView = new ImageView(angleIcon);
           angleIconView.setFitWidth(15); 
           angleIconView.setFitHeight(15);
           triangleButton.setGraphic(angleIconView);

		    importButton.setCursor(Cursor.HAND);
			lineButton.setCursor(Cursor.HAND);
			triangleButton.setCursor(Cursor.HAND);
			updateButton.setCursor(Cursor.HAND);
			saveButton.setCursor(Cursor.HAND);
			zoomInButton.setCursor(Cursor.HAND);
			zoomOutButton.setCursor(Cursor.HAND);
			undoButton.setCursor(Cursor.HAND);

		    ContextMenu contextMenu = new ContextMenu();
			MenuItem updateLineItem = new MenuItem("Update Line");
			MenuItem updateTriangleItem = new MenuItem("Update Angle");
			contextMenu.getItems().addAll(updateLineItem, updateTriangleItem);

           toolBar.getItems().addAll(importButton, lineButton, triangleButton, updateButton, saveButton, loadButton, zoomInButton, zoomOutButton, undoButton, redoButton);
           borderPane.setTop(toolBar);
           
		   //Initialized the DrawPoint, DrawLine, and DrawTriangle
           drawPoint = new DrawPoint(rootPane, drawLine, drawTriangle);  
		   drawLine = new DrawLine(rootPane, drawPoint, lineButton, true, drawPoint.circleList, drawPoint.labelList);
	        
		   drawTriangle = new DrawTriangle(rootPane, drawPoint, triangleButton, drawPoint.circleList, drawPoint.labelList); 

		   rootPane.getChildren().addAll(imageView);
		   borderPane.setCenter(rootPane); 
		   borderPane.setBottom(updateButton);
		   //scene = new Scene(borderPane,800,500);

		   //rootPane.getChildren().addAll(imageView);
		   //scene = new Scene(rootPane,800,500);
		   importButton.setOnAction(event -> {
			
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Image File");
			fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
			);
			
			File selectedFile = fileChooser.showOpenDialog(window);
			if (selectedFile != null) {
				//WriteJSONFile jsonFileWriter = new WriteJSONFile();
				try {
					
					Image selectedImage = new Image(selectedFile.toURI().toString());
					image = selectedImage;
					// Clear all previous data before refreshing image 
					drawPoint.clearCircles(); 
					drawLine.clearLines();
					drawTriangle.clearAllTriangles();
					
					//reader.clearJSONData();
									
					imageView.setImage(selectedImage);
					//jsonFileWriter.clearExistingData();
					toggleGroup.selectToggle(null);
					//deactivateLine();
				} catch (Exception e) {
					e.printStackTrace();
					
				}
			}
			
		});

		lineButton.setOnAction(event -> {
			
			if (lineButton.isSelected()) {
				activeLine();
				drawTriangle.stopDrawing();
			} else {
				deactivateLine();
			}
		});
		
		triangleButton.setOnAction(event -> {
			
			if (triangleButton.isSelected()) {
				deactivateLine();
				isDrawingLine = false;
				drawTriangle.startDrawing();
			} else {
				drawTriangle.stopDrawing();
			}
		});

		updateButton.setOnAction(event -> {
				
			if (updateButton.isSelected()) {
				double x = updateButton.localToScreen(0, 0).getX();
				double y = updateButton.localToScreen(0, updateButton.getHeight()).getY();
				contextMenu.show(updateButton, x, y);
			} else {
				contextMenu.hide();
			}
		});

		updateLineItem.setOnAction(event -> { 
			deactivateLine();
			drawTriangle.stopDrawing();
			updateLineFlag = true;
			//drawLine.setUpLineEvents();
			System.out.println("Update Line action performed");
			//drawLine.updateLine();
        });

		updateTriangleItem.setOnAction(event -> {
			deactivateLine();
			updateLineFlag = false;
			drawTriangle.stopDrawing();
			drawTriangle.startUpdatingTriangle();
			updateTriangleFlag = true; 
			
			System.out.println("Update angle action performed");
			
		 });
		
		 zoomInButton.setOnAction(e -> {
			if(zoomInButton.isSelected()) {
				handleZoomInButtonAction(e);
			}else{
				magnifier.setVisible(false);
				rootPane.getChildren().remove(magnifiedImageView);
			}
		 }); 
		 zoomOutButton.setOnAction(e -> {
			magnifier.setVisible(false);
			rootPane.getChildren().remove(magnifiedImageView);
		 });

		MouseZoomScrollerAndPanner(imageView);

		undoButton.setOnAction(e -> {
			drawLine.undo();
			drawTriangle.undo();
			drawPoint.undoDelete();
		});
		redoButton.setOnAction(e -> {
			drawLine.redo();
			drawTriangle.redo();
			drawPoint.redoDelete();
		});
			
		saveButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Annotated Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File file = fileChooser.showSaveDialog(window);
            if (file != null) {
                saveSnapshot(file);
            

				FileChooser dataFileChooser = new FileChooser();
				dataFileChooser.setTitle("Save Annotations Data");
				dataFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
				File dataFile = dataFileChooser.showSaveDialog(window);
				if (dataFile != null) {
					saveAnnotationsData(dataFile);
					showAlert("Success", "Annotated image saved successfully.");
				}else {
					// Handle the case where the user canceled the save operation for annotations data
					showAlert("Warning", "Annotations data not saved.");
				}
			} else {
			// Handle the case where the user canceled the save operation for the image
			showAlert("Warning", "Annotated image not saved.");
		}

			
        });

		loadButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Annotated Image");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
			File imageFile = fileChooser.showOpenDialog(window);
			if (imageFile != null) {
				Image loadedImage = new Image(imageFile.toURI().toString());
				imageView.setImage(loadedImage);
			}
		
			FileChooser dataFileChooser = new FileChooser();
			dataFileChooser.setTitle("Open Annotations Data");
			dataFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
			File dataFile = dataFileChooser.showOpenDialog(window);
			if (dataFile != null) {
				loadAnnotationsData(dataFile);
			}
		});
			
			
			window.setTitle("Ceph Analysis Tool");		
			
			primaryStage.setOnCloseRequest(event -> {
				event.consume(); 
				showConfirmationDialog(primaryStage);
			});
			
			scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
			window.setScene(scene);
			window.show();
			reader = new ReadJSONFile(rootPane);
			jsonData = reader.readDataFromJson("cephTool/json/TestJson_file.json");
			reader.processJSONData(jsonData);
			/*rootPane.setOnMouseClicked(e -> {
				if(e.getButton()== MouseButton.PRIMARY) {
					if (!lineButton.isSelected() && !triangleButton.isSelected()) {
							drawPoint.selectCircle(e.getX(),e.getY()); 
					}else {
						if (lineButton.isSelected()) {
							activeLine();
						}
					}
				}else if(e.getButton() == MouseButton.SECONDARY) {
				    double x = e.getX();
			        double y = e.getY();
			        for (Circle c : circleList) {
			            if (c.contains(x, y)) {
			                drawPoint.deleteCircle(c, e.getScreenX(), e.getScreenY());
			                break;
			            }
			        }
				}
			});*/
			
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void saveSnapshot(File file) {
        WritableImage snapshot = rootPane.snapshot(new SnapshotParameters(), null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void handleZoomInButtonAction(ActionEvent event) {	
		deactivateLine();
       
        zoomEnabled = !zoomEnabled;
        if (zoomEnabled) {
        	magnifier = new Circle(magnifierRadius);
            magnifier.setFill(Color.TRANSPARENT);
            magnifier.setStroke(Color.BLACK);
            magnifier.setStrokeWidth(2);
    		magnifier.setVisible(false);

            
            magnifiedImageView = new ImageView();
            magnifiedImageView.setPreserveRatio(true);
            magnifiedImageView.setSmooth(true);
            magnifiedImageView.setCache(true);

          
            rootPane.getChildren().addAll(magnifiedImageView, magnifier);
			originalViewport = imageView.getViewport();
            imageView.setOnMouseMoved(this::handleMouseMoveForZoom);
            magnifier.setVisible(true);
        } /*else {
            
            imageView.setOnMouseMoved(null);
            magnifier.setVisible(false);
			imageView.setViewport(originalViewport);
			
        }*/
    } 
	

	private void MouseZoomScrollerAndPanner(ImageView imageView) {
	    
	    originalScaleX = imageView.getScaleX();
	    originalScaleY = imageView.getScaleY();

	    // Set up scroll event for zooming
	    imageView.setOnScroll((ScrollEvent event) -> {
	        double deltaY = event.getDeltaY();
	        double zoomFactor = 1.05;

	        double oldScaleX = imageView.getScaleX();
	        double oldScaleY = imageView.getScaleY();

	        double newScaleX = oldScaleX;
	        double newScaleY = oldScaleY;

	        if (deltaY < 0) {
	            // Zoom out
	            newScaleX = oldScaleX / zoomFactor;
	            newScaleY = oldScaleY / zoomFactor;
	        } else {
	            // Zoom in
	            newScaleX = oldScaleX * zoomFactor;
	            newScaleY = oldScaleY * zoomFactor;
	        }

	     
	        double maxScale = 3.0; 

	        if (newScaleX < originalScaleX) {
	            newScaleX = originalScaleX;
	            newScaleY = originalScaleY;
	        } else if (newScaleX > maxScale) {
	            newScaleX = maxScale;
	            newScaleY = maxScale;
	        }

	        imageView.setScaleX(newScaleX);
	        imageView.setScaleY(newScaleY);

	     
	        double mouseX = event.getX();
	        double mouseY = event.getY();
	        double pivotX = mouseX * (1 - newScaleX / oldScaleX);
	        double pivotY = mouseY * (1 - newScaleY / oldScaleY);

	        imageView.setTranslateX(imageView.getTranslateX() - pivotX);
	        imageView.setTranslateY(imageView.getTranslateY() - pivotY);
	        
	        adjustImageViewPosition(imageView, rootPane);

	        event.consume();
	    });

	    // Set up mouse drag events for panning
	    imageView.setOnMousePressed(event -> {
	        dragStartX = event.getSceneX() - imageView.getTranslateX();
	        dragStartY = event.getSceneY() - imageView.getTranslateY();
	    });

	    imageView.setOnMouseDragged(event -> {
	        imageView.setTranslateX(event.getSceneX() - dragStartX);
	        imageView.setTranslateY(event.getSceneY() - dragStartY);
	        
	        adjustImageViewPosition(imageView, rootPane);
	    });
	}
	
	private void adjustImageViewPosition(ImageView imageView, Pane rootPane) {
	    Bounds imageViewBounds = imageView.getBoundsInParent();
	    Bounds rootPaneBounds = rootPane.getLayoutBounds();

	    double offsetX = 0;
	    double offsetY = 0;

	    if (imageViewBounds.getMinX() > rootPaneBounds.getMinX()) {
	        offsetX = rootPaneBounds.getMinX() - imageViewBounds.getMinX();
	    } else if (imageViewBounds.getMaxX() < rootPaneBounds.getMaxX()) {
	        offsetX = rootPaneBounds.getMaxX() - imageViewBounds.getMaxX();
	    }

	    if (imageViewBounds.getMinY() > rootPaneBounds.getMinY()) {
	        offsetY = rootPaneBounds.getMinY() - imageViewBounds.getMinY();
	    } else if (imageViewBounds.getMaxY() < rootPaneBounds.getMaxY()) {
	        offsetY = rootPaneBounds.getMaxY() - imageViewBounds.getMaxY();
	    }

	    imageView.setTranslateX(imageView.getTranslateX() + offsetX);
	    imageView.setTranslateY(imageView.getTranslateY() + offsetY);
	}

    
	private void handleMouseMoveForZoom(MouseEvent event) {
		double mouseX = event.getX();
		double mouseY = event.getY();
	
		double imageViewX = imageView.getLayoutX();
		double imageViewY = imageView.getLayoutY();
		double imageViewWidth = imageView.getBoundsInLocal().getWidth();
		double imageViewHeight = imageView.getBoundsInLocal().getHeight();
	
		// Constrain the mouse coordinates within the bounds of the imageView minus the magnifier radius
		double correctedMouseX = Math.max(imageViewX + magnifierRadius, Math.min(imageViewX + imageViewWidth - magnifierRadius, mouseX));
		double correctedMouseY = Math.max(imageViewY + magnifierRadius, Math.min(imageViewY + imageViewHeight - magnifierRadius, mouseY));
	
		magnifier.setCenterX(correctedMouseX);
		magnifier.setCenterY(correctedMouseY);
	
		double scaleX = image.getWidth() / imageViewWidth;
		double scaleY = image.getHeight() / imageViewHeight;
	
		double viewportX = (correctedMouseX - imageViewX) * scaleX - magnifierRadius * scaleX;
		double viewportY = (correctedMouseY - imageViewY) * scaleY - magnifierRadius * scaleY;
	
		viewportX = Math.max(0, Math.min(viewportX, image.getWidth() - 2 * magnifierRadius * scaleX));
		viewportY = Math.max(0, Math.min(viewportY, image.getHeight() - 2 * magnifierRadius * scaleY));
	
		double viewportWidth = 2 * magnifierRadius * scaleX;
		double viewportHeight = 2 * magnifierRadius * scaleY;
	
		if (zoomEnabled) {
			magnifiedImageView.setImage(image);
			magnifiedImageView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
	
			magnifiedImageView.setFitWidth(2 * magnifierRadius);
			magnifiedImageView.setFitHeight(2 * magnifierRadius);
			magnifiedImageView.setLayoutX(correctedMouseX - magnifierRadius);
			magnifiedImageView.setLayoutY(correctedMouseY - magnifierRadius);
	
			Circle clip = new Circle(magnifierRadius);
			clip.setCenterX(magnifierRadius);
			clip.setCenterY(magnifierRadius);
			magnifiedImageView.setClip(clip);
		} else {
			rootPane.getChildren().remove(magnifiedImageView);
		}
	}

	private void activeLine() {
		isDrawingLine = true;
		drawLine = new DrawLine(rootPane,drawPoint, lineButton, true, drawPoint.circleList, drawPoint.labelList); 
	}
	
	private void deactivateLine() {
		isDrawingLine = false;
		drawLine = new DrawLine(rootPane, drawPoint, lineButton, false, drawPoint.circleList, drawPoint.labelList);  
	}

   private void showConfirmationDialog(Stage primaryStage) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText("Do you want to save changes?");
		alert.setContentText("Choose your option.");

		ButtonType saveButton = new ButtonType("Save");
		ButtonType cancelButton = new ButtonType("Cancel");
		ButtonType closeButton = new ButtonType("Close without saving");

		alert.getButtonTypes().setAll(saveButton, cancelButton, closeButton);

		alert.showAndWait().ifPresent(buttonType -> {
			if (buttonType == saveButton) {
				saveJsonToFile();
				primaryStage.close();
			} else if (buttonType == closeButton) {
				clearJsonFile(); 
				primaryStage.close();
			}
   		 });
	}

	private void clearJsonFile() {
       
        JSONArray emptyArray = new JSONArray();
        try (FileWriter fileWriter = new FileWriter("cephTool/json/TestJson_file.json")) {
            fileWriter.write(emptyArray.toJSONString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
		}
	} 

	private void saveJsonToFile(){
		
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Annotated Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File file = fileChooser.showSaveDialog(window);
            if (file != null) {
                saveSnapshot(file);
            

					FileChooser dataFileChooser = new FileChooser();
					dataFileChooser.setTitle("Save Annotations Data");
					dataFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
					File dataFile = dataFileChooser.showSaveDialog(window);
					if (dataFile != null) {
						saveAnnotationsData(dataFile);
						showAlert("Success", "Annotated image saved successfully.");
					}else {
						// Handle the case where the user canceled the save operation for annotations data
						showAlert("Warning", "Annotations data not saved.");
					}
			} else {
				// Handle the case where the user canceled the save operation for the image
				showAlert("Warning", "Annotated image not saved.");
			}
        
	}

	private void redrawImage() {
        imageView.setImage(image);
    }

	private void saveAnnotationsData(File file) {
		List<LineData> lineDataList = drawLine.getLines();
		List<TriangleData> triangleDataList = drawTriangle.getTriangles();
		JSONArray annotationsArray = new JSONArray();

		
		for (LineData lineData : lineDataList) {
			
			Line line = lineData.getLine();
			double startX = line.getStartX();
			double startY = line.getStartY();
			double endX = line.getEndX();
			double endY = line.getEndY();

			double midX = (startX + endX) / 2;
        	double midY = (startY + endY) / 2;
		
			
			JSONObject lineObject = new JSONObject();
			lineObject.put("type", "line");
			lineObject.put("startX", startX);
			lineObject.put("startY", startY);
			lineObject.put("endX", endX);
			lineObject.put("endY", endY);
			
			annotationsArray.add(lineObject);
		
			
			startCircle = lineData.getStartCircle();
			endCircle = lineData.getEndCircle();
		
			
			if (startCircle != null) {
				JSONObject startCircleObject = new JSONObject();
				startCircleObject.put("type", "circle");
				startCircleObject.put("centerX", startCircle.getCenterX());
				startCircleObject.put("centerY", startCircle.getCenterY());
				startCircleObject.put("radius", startCircle.getRadius());

				String circleLabel = getCircleLabel(startCircle);
				if (circleLabel != null && !circleLabel.isEmpty()) {
					startCircleObject.put("label", circleLabel);
				}
				
				annotationsArray.add(startCircleObject);
			}
	
			if (endCircle != null) {
				JSONObject endCircleObject = new JSONObject();
				endCircleObject.put("type", "circle");
				endCircleObject.put("centerX", endCircle.getCenterX());
				endCircleObject.put("centerY", endCircle.getCenterY());
				endCircleObject.put("radius", endCircle.getRadius());

				String circleLabel = getCircleLabel(endCircle);
				if (circleLabel != null && !circleLabel.isEmpty()) {
					endCircleObject.put("label", circleLabel);
				}
				
				annotationsArray.add(endCircleObject);
			}
		
			//Line Slope Label
			Label slopeLabel = lineData.getSlopeLabel();
			if (slopeLabel != null) {
				double labelX = slopeLabel.getLayoutX();
				double labelY = slopeLabel.getLayoutY();
				String labelText = slopeLabel.getText();

				
				JSONObject labelObject = new JSONObject();
				labelObject.put("type", "label");
				labelObject.put("text", labelText);
				labelObject.put("layoutX", labelX);
				labelObject.put("layoutY", labelY);
				
				annotationsArray.add(labelObject);
        	}

			// Line MidPoint
			JSONObject midpointObject = new JSONObject();
			midpointObject.put("type", "rectangle");
			midpointObject.put("centerX", midX);
			midpointObject.put("centerY", midY);
			midpointObject.put("width", lineData.getMidPoint().getWidth());
			midpointObject.put("height", lineData.getMidPoint().getHeight());
			midpointObject.put("color", "black"); // Set the color of the rectangle
	
			annotationsArray.add(midpointObject);
		}
		// Add other shapes (triangles, etc.) in a similar manner
		for (TriangleData triangleData : triangleDataList) {
			Line line1 = triangleData.getLine1();
			Line line2 = triangleData.getLine2();

			JSONObject triangleObject = new JSONObject();
			triangleObject.put("type", "triangle");
			triangleObject.put("line1StartX", line1.getStartX());
			triangleObject.put("line1StartY", line1.getStartY());
			triangleObject.put("line1EndX", line1.getEndX());
			triangleObject.put("line1EndY", line1.getEndY());
			triangleObject.put("line2StartX", line2.getStartX());
			triangleObject.put("line2StartY", line2.getStartY());
			triangleObject.put("line2EndX", line2.getEndX());
			triangleObject.put("line2EndY", line2.getEndY());


			JSONArray angleTextsArray = new JSONArray();
			for (Text angleText : triangleData.getAngleTexts()) {
				JSONObject angleTextObject = new JSONObject();
				angleTextObject.put("text", angleText.getText());
				angleTextObject.put("layoutX", angleText.getLayoutX());
				angleTextObject.put("layoutY", angleText.getLayoutY());
				angleTextsArray.add(angleTextObject);
			}
			triangleObject.put("angleTexts", angleTextsArray);

			JSONArray angleArcsArray = new JSONArray();
			for (Arc angleArc : triangleData.getAngleArcs()) {
				JSONObject angleArcObject = new JSONObject();
				angleArcObject.put("centerX", angleArc.getCenterX());
				angleArcObject.put("centerY", angleArc.getCenterY());
				angleArcObject.put("radiusX", angleArc.getRadiusX());
				angleArcObject.put("radiusY", angleArc.getRadiusY());
				angleArcObject.put("startAngle", angleArc.getStartAngle());
				angleArcObject.put("length", angleArc.getLength());
				angleArcsArray.add(angleArcObject);
			}
			triangleObject.put("angleArcs", angleArcsArray);

			if (triangleData.getCircle1() != null) {
				JSONObject circle1Object = new JSONObject();
				circle1Object.put("type", "circle");
				circle1Object.put("centerX", triangleData.getCircle1().getCenterX());
				circle1Object.put("centerY", triangleData.getCircle1().getCenterY());
				circle1Object.put("radius", triangleData.getCircle1().getRadius());

				String label1 = getCircleLabel(triangleData.getCircle1());
				if (label1 != null && !label1.isEmpty()) {
					circle1Object.put("label", label1);
				}
				triangleObject.put("circle1", circle1Object);
			}

			if (triangleData.getCircle2() != null) {
				JSONObject circle2Object = new JSONObject();
				circle2Object.put("type", "circle");
				circle2Object.put("centerX", triangleData.getCircle2().getCenterX());
				circle2Object.put("centerY", triangleData.getCircle2().getCenterY());
				circle2Object.put("radius", triangleData.getCircle2().getRadius());

				String label2 = getCircleLabel(triangleData.getCircle2());
				if (label2 != null && !label2.isEmpty()) {
					circle2Object.put("label", label2);
				}
				triangleObject.put("circle2", circle2Object);
			}

			if (triangleData.getCircle3() != null) {
				JSONObject circle3Object = new JSONObject();
				circle3Object.put("type", "circle");
				circle3Object.put("centerX", triangleData.getCircle3().getCenterX());
				circle3Object.put("centerY", triangleData.getCircle3().getCenterY());
				circle3Object.put("radius", triangleData.getCircle3().getRadius());

				String label3 = getCircleLabel(triangleData.getCircle3());
				if (label3 != null && !label3.isEmpty()) {
					circle3Object.put("label", label3);
				}
				triangleObject.put("circle3", circle3Object);
			}

			if (triangleData.getLabel1() != null) {
				JSONObject label1Object = new JSONObject();
				label1Object.put("type", "label");
				label1Object.put("text", triangleData.getLabel1().getText());
				label1Object.put("layoutX", triangleData.getLabel1().getLayoutX());
				label1Object.put("layoutY", triangleData.getLabel1().getLayoutY());
				triangleObject.put("label1", label1Object);
			}

			if (triangleData.getLabel2() != null) {
				JSONObject label2Object = new JSONObject();
				label2Object.put("type", "label");
				label2Object.put("text", triangleData.getLabel2().getText());
				label2Object.put("layoutX", triangleData.getLabel2().getLayoutX());
				label2Object.put("layoutY", triangleData.getLabel2().getLayoutY());
				triangleObject.put("label2", label2Object);
			}

			if (triangleData.getLabel3() != null) {
				JSONObject label3Object = new JSONObject();
				label3Object.put("type", "label");
				label3Object.put("text", triangleData.getLabel3().getText());
				label3Object.put("layoutX", triangleData.getLabel3().getLayoutX());
				label3Object.put("layoutY", triangleData.getLabel3().getLayoutY());
				triangleObject.put("label3", label3Object);
			}

        	annotationsArray.add(triangleObject);
		}

		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter.write(annotationsArray.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getCircleLabel(Circle circle) {
		// Construct a label string representing the coordinates
		return "(" + circle.getCenterX() + ", " + circle.getCenterY() + ")";
	}

	private void loadAnnotationsData(File file) {
    JSONParser jsonParser = new JSONParser();

		try {
			String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
			JSONArray annotationsArray = (JSONArray) jsonParser.parse(content);

			for (Object obj : annotationsArray) {
				JSONObject annotationObject = (JSONObject) obj;
				String type = (String) annotationObject.get("type");

				switch (type) {
					case "line":
						Line line = new Line();
						line.setStartX((double) annotationObject.get("startX"));
						line.setStartY((double) annotationObject.get("startY"));
						line.setEndX((double) annotationObject.get("endX"));
						line.setEndY((double) annotationObject.get("endY"));
						line.setStroke(Color.BLUE);
						line.setStrokeWidth(2);
                    	
						drawLine.addLine(line);
						//drawLine.updateLine(line);
						//drawLine.setupMouseHandlers();
						drawLine.drawLine(startCircle, endCircle);
						rootPane.getChildren().add(line);
						break;
					case "circle":
						Circle circle = new Circle();
						circle.setCenterX((double) annotationObject.get("centerX"));
						circle.setCenterY((double) annotationObject.get("centerY"));
						circle.setRadius((double) annotationObject.get("radius"));
						circle.setFill(Color.RED);
						circle.setCursor(Cursor.HAND);
						
						drawLine.addCircle(circle);
						drawPoint.circleDragged(circle);
						rootPane.getChildren().add(circle);

						String labelText = (String) annotationObject.get("label");
						if (labelText != null && !labelText.isEmpty()) {
							Label label = new Label(labelText);
							label.setTextFill(Color.WHITE); 
							label.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
							label.setLayoutX(circle.getCenterX() -15 ); 
							label.setLayoutY(circle.getCenterY() + 10); 
							drawLine.addCircleLabel(label);
							rootPane.getChildren().add(label); 
						}
						break;
					
					// Line Slope Label
					case "label":
						Label label = new Label((String) annotationObject.get("text"));
						label.setLayoutX((double) annotationObject.get("layoutX"));
						label.setLayoutY((double) annotationObject.get("layoutY"));
						label.setTextFill(Color.YELLOW);
						label.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
						
						rootPane.getChildren().add(label);
						break;
					
					case "rectangle":
                    double centerX = (double) annotationObject.get("centerX");
                    double centerY = (double) annotationObject.get("centerY");
                    double width = (double) annotationObject.get("width");
                    double height = (double) annotationObject.get("height");
                    String color = (String) annotationObject.get("color");

                    Rectangle rectangle = new Rectangle(centerX - width / 2, centerY - height / 2, width, height);
                    rectangle.setFill(Color.web(color));
                    rootPane.getChildren().add(rectangle);
                    break;

					case "triangle":
                    Line line1 = new Line();
                    line1.setStartX((double) annotationObject.get("line1StartX"));
                    line1.setStartY((double) annotationObject.get("line1StartY"));
                    line1.setEndX((double) annotationObject.get("line1EndX"));
                    line1.setEndY((double) annotationObject.get("line1EndY"));
                    line1.setStroke(Color.BLUE);
                    line1.setStrokeWidth(2);

                    Line line2 = new Line();
                    line2.setStartX((double) annotationObject.get("line2StartX"));
                    line2.setStartY((double) annotationObject.get("line2StartY"));
                    line2.setEndX((double) annotationObject.get("line2EndX"));
                    line2.setEndY((double) annotationObject.get("line2EndY"));
                    line2.setStroke(Color.BLUE);
                    line2.setStrokeWidth(2);

                    List<Text> angleTexts = new ArrayList<>();
					double textCenterX = (line1.getStartX() + line2.getStartX()) / 2;
					double textCenterY = (line1.getStartY() + line2.getStartY()) / 2;
					
                    JSONArray angleTextsArray = (JSONArray) annotationObject.get("angleTexts");
                    for (Object angleTextObj : angleTextsArray) {
                        JSONObject angleTextObject = (JSONObject) angleTextObj;
                        Text angleText = new Text((String) angleTextObject.get("text"));
                        angleText.setLayoutX(textCenterX);
                        angleText.setLayoutY(textCenterY);
						angleText.setFill(Color.YELLOW);
                        angleTexts.add(angleText);
						System.out.println("angleTextsLIst " + angleTexts);
						rootPane.getChildren().add(angleText);
                    }

                    List<Arc> angleArcs = new ArrayList<>();
                    JSONArray angleArcsArray = (JSONArray) annotationObject.get("angleArcs");
                    for (Object angleArcObj : angleArcsArray) {
                        JSONObject angleArcObject = (JSONObject) angleArcObj;
                        Arc angleArc = new Arc();
                        angleArc.setCenterX((double) angleArcObject.get("centerX"));
                        angleArc.setCenterY((double) angleArcObject.get("centerY"));
                        angleArc.setRadiusX((double) angleArcObject.get("radiusX"));
                        angleArc.setRadiusY((double) angleArcObject.get("radiusY"));
						angleArc.setStartAngle((double) angleArcObject.get("startAngle"));
                        angleArc.setLength((double) angleArcObject.get("length"));
                        angleArc.setType(ArcType.OPEN);
                        angleArc.setFill(null);
                        angleArc.setStroke(Color.YELLOW);
                        angleArcs.add(angleArc);
						rootPane.getChildren().add(angleArc);
                    }

                    Circle circle1 = null;
                    if (annotationObject.containsKey("circle1")) {
                        JSONObject circle1Object = (JSONObject) annotationObject.get("circle1");
                        circle1 = new Circle();
                        circle1.setCenterX((double) circle1Object.get("centerX"));
                        circle1.setCenterY((double) circle1Object.get("centerY"));
                        circle1.setRadius((double) circle1Object.get("radius"));
                        circle1.setFill(Color.RED);
                        circle1.setCursor(Cursor.HAND);
						drawTriangle.addCircle(circle1);
                    }

                    Circle circle2 = null;
                    if (annotationObject.containsKey("circle2")) {
                        JSONObject circle2Object = (JSONObject) annotationObject.get("circle2");
                        circle2 = new Circle();
                        circle2.setCenterX((double) circle2Object.get("centerX"));
                        circle2.setCenterY((double) circle2Object.get("centerY"));
                        circle2.setRadius((double) circle2Object.get("radius"));
                        circle2.setFill(Color.RED);
                        circle2.setCursor(Cursor.HAND);
						drawTriangle.addCircle(circle2);
                    }

                    Circle circle3 = null;
                    if (annotationObject.containsKey("circle3")) {
                        JSONObject circle3Object = (JSONObject) annotationObject.get("circle3");
                        circle3 = new Circle();
                        circle3.setCenterX((double) circle3Object.get("centerX"));
                        circle3.setCenterY((double) circle3Object.get("centerY"));
                        circle3.setRadius((double) circle3Object.get("radius"));
                        circle3.setFill(Color.RED);
                        circle3.setCursor(Cursor.HAND);
						drawTriangle.addCircle(circle3);
                    }

                    Label label1 = null;
                    if (annotationObject.containsKey("label1")) {
                        JSONObject label1Object = (JSONObject) annotationObject.get("label1");
                        label1 = new Label((String) label1Object.get("text"));
                        label1.setLayoutX((double) label1Object.get("layoutX"));
                        label1.setLayoutY((double) label1Object.get("layoutY"));
						label1.setTextFill(Color.WHITE); 
						label1.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
						drawTriangle.addCircleLabel(label1);
                    }

                    Label label2 = null;
                    if (annotationObject.containsKey("label2")) {
                        JSONObject label2Object = (JSONObject) annotationObject.get("label2");
                        label2 = new Label((String) label2Object.get("text"));
                        label2.setLayoutX((double) label2Object.get("layoutX"));
                        label2.setLayoutY((double) label2Object.get("layoutY"));
						label2.setTextFill(Color.WHITE); 
						label2.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
						drawTriangle.addCircleLabel(label2);
                    }

                    Label label3 = null;
                    if (annotationObject.containsKey("label3")) {
                        JSONObject label3Object = (JSONObject) annotationObject.get("label3");
                        label3 = new Label((String) label3Object.get("text"));
                        label3.setLayoutX((double) label3Object.get("layoutX"));
                        label3.setLayoutY((double) label3Object.get("layoutY"));
						label3.setTextFill(Color.WHITE); 
						label3.setStyle("-fx-font-weight: bold; -fx-font-size: small;");
						drawTriangle.addCircleLabel(label3);
                    }

                    TriangleData triangleData = new TriangleData(line1, line2, angleTexts, angleArcs, circle1, circle2, circle3, label1, label2, label3);
                    //drawTriangle.addTriangle(triangleData);
                    rootPane.getChildren().addAll(line1, line2);
                    //rootPane.getChildren().addAll(angleTexts);
                    //rootPane.getChildren().addAll(angleArcs);
                    if (circle1 != null) rootPane.getChildren().add(circle1);
                    if (circle2 != null) rootPane.getChildren().add(circle2);
                    if (circle3 != null) rootPane.getChildren().add(circle3);
                    if (label1 != null) rootPane.getChildren().add(label1);
                    if (label2 != null) rootPane.getChildren().add(label2);
                    if (label3 != null) rootPane.getChildren().add(label3);
                    break;
				}
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
		
	}
}
