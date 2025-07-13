import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class DrawLine {
	private Circle startCircle;
    private Line yellowLine;
    private Label slopeLabel;
    private Pane rootPane;
	private Circle circle1;
	private Circle circle2;
	private DrawPoint drawPoint;
	private boolean selectingCircleForLine;
	public ArrayList<Circle> circleList;
	public ArrayList<Label> labelList;
	private ToggleButton lineButton;
	public ArrayList<Line> lineList = new ArrayList<>();
	public ArrayList<Label> slopeLabelList = new ArrayList<>();
	public HashMap<Circle, ArrayList<Line>> circleLineMap = new HashMap<>();
	public HashMap<Line, Label> lineLabelMap = new HashMap<>();
    public HashMap<Line, Rectangle> midpointMap = new HashMap<>();
    private Line currentLine;
    private double deltaX, deltaY;
    private Rectangle midPoint;
    private ArrayList<Circle> circlesForLine = new ArrayList<>();
    private Stack<Action> actionStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();
    private List<LineData> lineDataList = new ArrayList<>();

    public DrawLine(Pane rootPane, DrawPoint drawPoint, ToggleButton lineButton, boolean selectingCircleForLine, ArrayList<Circle> circleList, ArrayList<Label> labelList) {
		this.rootPane = rootPane;
		this.drawPoint = drawPoint;
		this.lineButton = lineButton;
		this.circleList = circleList;
        this.labelList = labelList; 
		this.selectingCircleForLine = selectingCircleForLine;
		setupMouseHandlers();	 
	}
    
    public void clearLines() {
    	rootPane.getChildren().removeAll(lineList);
    	rootPane.getChildren().removeAll(slopeLabelList);
    	lineList.clear();
    	slopeLabelList.clear();
    	circleLineMap.clear(); 
        lineLabelMap.clear();
        for (Iterator<Node> iterator = rootPane.getChildren().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            if (node instanceof Label) {
                iterator.remove();
            }
            if (node instanceof Rectangle) {
                iterator.remove();
            }
        }
    }
    
    public void onCircleDragged(MouseEvent e, Circle draggedCircle, Circle otherCircle, ArrayList<Circle> circleList, ArrayList<Label> labelList ) {
        draggedCircle.centerXProperty().unbind();
        draggedCircle.centerYProperty().unbind();
        
        draggedCircle.setCenterX(e.getX());
        draggedCircle.setCenterY(e.getY());
        
        int index = circleList.indexOf(draggedCircle);
        if (index != -1) {
            Label label = labelList.get(index);
            label.setText("P" + (index + 1) + ": (" + (int) draggedCircle.getCenterX() + ", " + (int) draggedCircle.getCenterY() + ")");
            label.layoutXProperty().unbind();
            label.layoutYProperty().unbind();
            label.setLayoutX(draggedCircle.getCenterX() - 15);
            label.setLayoutY(draggedCircle.getCenterY() + 10);
            bindCirclePosition(draggedCircle, label, draggedCircle.getCenterX(), draggedCircle.getCenterY());
        }
        
        
       
        ArrayList<Line> linesToUpdate = circleLineMap.get(draggedCircle);
        if (linesToUpdate != null) {
            for (Line line : linesToUpdate) { 
                updateLine(line);
                updateSlopeLabel(line);
            }
        }

        
       
    }
    
    private void setupMouseHandlers() {
        rootPane.setOnMousePressed(e -> {
        	 System.out.println("Root pane clicked at: " + e.getX() + ", " + e.getY());
            if (e.getButton() == MouseButton.PRIMARY && lineButton.isSelected()) {
            	if (selectingCircleForLine ) {
                    startCircle = drawPoint.drawCircle(e.getX(), e.getY());
                    
                    if(startCircle != null) {
                    	circlesForLine.add(startCircle);
                    	if(circlesForLine.size() == 2) {
                    	Circle endCircle = circlesForLine.get(1);
                    	drawLine(circlesForLine.get(0), endCircle);

                    	circlesForLine.clear();
                    	startCircle = null;

	                        if (yellowLine != null) {
	                            rootPane.getChildren().removeAll(yellowLine);
	                            yellowLine = null;
	                        }
                        }
                    }
            	}
                    //rootPane.getChildren().add(startCircle);
            }else if(e.getButton() == MouseButton.SECONDARY) {
			    double x = e.getX();
		        double y = e.getY();
                boolean circleClicked = false;

		        for (Circle c : circleList) {
		            if (c.contains(x, y)) {
                        drawPoint.deleteCircle(c, e.getScreenX(), e.getScreenY());
                        circleClicked = true;
                        break;
		            }
		        }

                if (!circleClicked) {
                    for (Line line : lineList) {
                        if (lineContainsPoint(line, x, y)) {
                            deleteLine(line, e.getScreenX(), e.getScreenY());
                            return;
                        }
                    }
                }
			}
        });

        rootPane.setOnMouseMoved(e -> {
            if (startCircle != null && lineButton.isSelected()) {
            	if (yellowLine != null) {
                    rootPane.getChildren().removeAll(yellowLine);
                }
                yellowLine = new Line(startCircle.getCenterX(), startCircle.getCenterY(), e.getX(), e.getY());
                yellowLine.setStroke(Color.YELLOW);
                yellowLine.setStrokeWidth(2);
                //slopeLabel = calculateSlope(yellowLine);
                rootPane.getChildren().addAll(yellowLine);
            }
        });

    }

    private boolean lineContainsPoint(Line line, double x, double y) {
        double x1 = line.getStartX();
        double y1 = line.getStartY();
        double x2 = line.getEndX();
        double y2 = line.getEndY();
    
        // Calculate the distance from the point to the line
        double distance = Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1) /
                          Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
                          
        // Return true if the distance is within a threshold (e.g., 5 pixels)
        return distance < 5.0;
    }

    public void setUpLineEvents() {
        rootPane.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                  
                    Node node = e.getPickResult().getIntersectedNode();
                    if (node instanceof Line) {
                        currentLine = (Line) node;
                        deltaX = e.getX() - currentLine.getStartX();
                        deltaY = e.getY() - currentLine.getStartY();

                        currentLine.setCursor(Cursor.CLOSED_HAND);
                    }

                }
            
        });

       
        rootPane.setOnMouseDragged(e -> {
            if (currentLine != null) {
                double newX = e.getX() - deltaX;
                double newY = e.getY() - deltaY;
               
                double deltaCircleX = newX - currentLine.getStartX();
                double deltaCircleY = newY - currentLine.getStartY();
             
                Circle circle1 = (Circle) currentLine.getProperties().get("circle1");
                Circle circle2 = (Circle) currentLine.getProperties().get("circle2");
                circle1.setCenterX(circle1.getCenterX() + deltaCircleX);
                circle1.setCenterY(circle1.getCenterY() + deltaCircleY);
                circle2.setCenterX(circle2.getCenterX() + deltaCircleX);
                circle2.setCenterY(circle2.getCenterY() + deltaCircleY);
                
                //currentLine.setStartX(newX);
                //currentLine.setStartY(newY);
                //currentLine.setEndX(currentLine.getEndX() + deltaCircleX);
                //currentLine.setEndY(currentLine.getEndY() + deltaCircleY);

                updateCircleLabel(circle1);
                updateCircleLabel(circle2);

                //updateSlopeLabel(currentLine);
                updateLine(currentLine);
            }
        });

        
        rootPane.setOnMouseReleased(e -> {
            currentLine = null;
        });
    }



    private void updateCircleLabel(Circle circle) {
        int index = circleList.indexOf(circle);
        if (index != -1) {
            Label label = labelList.get(index);
            label.setText("P" + (index + 1) + ": (" + (int) circle.getCenterX() + ", " + (int) circle.getCenterY() + ")");

            label.layoutXProperty().unbind();
            label.layoutYProperty().unbind();

            label.setLayoutX(circle.getCenterX() - 15);
            label.setLayoutY(circle.getCenterY() + 10);
        }
    }

    private void bindCirclePosition(Circle circle , Label label, double initialX, double initialY) {
        //circle.centerXProperty().unbind();
        //circle.centerYProperty().unbind();
        
        circle.centerXProperty().bind(rootPane.widthProperty().multiply(initialX / rootPane.getWidth()));
        circle.centerYProperty().bind(rootPane.heightProperty().multiply(initialY / rootPane.getHeight()));

        label.layoutXProperty().bind(circle.centerXProperty().subtract(15));
        label.layoutYProperty().bind(circle.centerYProperty().add(10));
    }

	
    public void drawLine(Circle startCircle, Circle endCircle) {
		Circle circle1 = startCircle;
        Circle circle2 = endCircle;

		if (circle1 != null && circle2 != null) {

            double midX = (circle1.getCenterX() + circle2.getCenterX()) / 2;
	        double midY = (circle1.getCenterY() + circle2.getCenterY()) / 2;
			
	    	Line line = new Line(startCircle.getCenterX(), startCircle.getCenterY(), endCircle.getCenterX(), endCircle.getCenterY());
	        line.startXProperty().bind(circle1.centerXProperty());
            line.startYProperty().bind(circle1.centerYProperty());
            line.endXProperty().bind(circle2.centerXProperty());
            line.endYProperty().bind(circle2.centerYProperty());

            slopeLabel = calculateSlope(line);
	        line.setStroke(Color.BLUE);
	        line.setStrokeWidth(2);
	        
	        line.getProperties().put("circle1", circle1);
	        line.getProperties().put("circle2", circle2);
	        
	        ArrayList<Line> linesForCircle1 = circleLineMap.getOrDefault(circle1, new ArrayList<>());
	        linesForCircle1.add(line);
	        circleLineMap.put(circle1, linesForCircle1);
	
	        ArrayList<Line> linesForCircle2 = circleLineMap.getOrDefault(circle2, new ArrayList<>());
	        linesForCircle2.add(line);
	        circleLineMap.put(circle2, linesForCircle2);

            double squareSize = 4;
	        midPoint = new Rectangle(midX - squareSize / 2, midY - squareSize / 2, squareSize, squareSize);
	        midPoint.setFill(Color.BLACK);
            //midPoint.xProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartX() + line.getEndX()) / 2, line.startXProperty(), line.endXProperty()));
            //midPoint.yProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartY() + line.getEndY()) / 2, line.startYProperty(), line.endYProperty()));
	        
	        rootPane.getChildren().addAll(line, slopeLabel, midPoint); 
	        lineList.add(line);
	        slopeLabelList.add(slopeLabel);
	        lineLabelMap.put(line, slopeLabel);
            midpointMap.put(line, midPoint);
	        System.out.println("Mid Map" + midpointMap);

            midPoint.xProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartX() + line.getEndX()) / 2,
	                line.startXProperty(), line.endXProperty()));
	        midPoint.yProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartY() + line.getEndY()) / 2,
	                line.startYProperty(), line.endYProperty()));
            
            setupMidPointDrag(midPoint, line);

            lineDataList.add(new LineData(line, startCircle, endCircle, slopeLabel, midPoint)); 
                    
	        System.out.println("Line drawn between Circle 1 at (" + circle1.getCenterX() + ", " + circle1.getCenterY() + ") and Circle 2 at (" + circle2.getCenterX() + ", " + circle2.getCenterY() + ")");
		
	        
	        circle1.setOnMouseDragged(event -> onCircleDragged(event, circle1, circle2, circleList, labelList));
	        circle2.setOnMouseDragged(event -> onCircleDragged(event, circle2, circle1, circleList, labelList));
        
            line.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    deleteLine(line, event.getScreenX(), event.getScreenY());
                }
            });

            int index1 = circleList.indexOf(circle1);
            int index2 = circleList.indexOf(circle2);
            if (index1 != -1 && index2 != -1) {
                Label label1 = labelList.get(index1);
                Label label2 = labelList.get(index2);
                bindCirclePosition(circle1, label1, circle1.getCenterX(), circle1.getCenterY());
                bindCirclePosition(circle2, label2, circle2.getCenterX(), circle2.getCenterY());
            }

            slopeLabel.layoutXProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartX() + line.getEndX()) / 2, line.startXProperty(), line.endXProperty()).subtract(15));
            slopeLabel.layoutYProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartY() + line.getEndY()) / 2, line.startYProperty(), line.endYProperty()).subtract(15));
       
		}else {
			System.err.println("Error: Circle objects are not properly initialized.");
		}
        
	}

    private void setupMidPointDrag(Rectangle midPoint, Line line) {

        final double[] initialMouseX = new double[1];
        final double[] initialMouseY = new double[1];
        final double[] initialCircle1X = new double[1];
        final double[] initialCircle1Y = new double[1];
        final double[] initialCircle2X = new double[1];
        final double[] initialCircle2Y = new double[1];

        midPoint.setOnMouseEntered(e -> {
            midPoint.setCursor(Cursor.HAND);
        });
    
        midPoint.setOnMouseExited(e -> {
            midPoint.setCursor(Cursor.DEFAULT); // Set back to default cursor when exiting
        });

        midPoint.setOnMousePressed(e -> {
            initialMouseX[0] = e.getSceneX();
            initialMouseY[0] = e.getSceneY();

            Circle circle1 = (Circle) line.getProperties().get("circle1");
            Circle circle2 = (Circle) line.getProperties().get("circle2");

            //circle1.centerXProperty().unbind();
            //circle1.centerYProperty().unbind();
            //circle2.centerXProperty().unbind();
            //circle2.centerYProperty().unbind();

            initialCircle1X[0] = circle1.getCenterX();
            initialCircle1Y[0] = circle1.getCenterY();
            initialCircle2X[0] = circle2.getCenterX();
            initialCircle2Y[0] = circle2.getCenterY();
            
            midPoint.setCursor(Cursor.CLOSED_HAND);
        });

        midPoint.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - initialMouseX[0];
            double deltaY = e.getSceneY() - initialMouseY[0];

            Circle circle1 = (Circle) line.getProperties().get("circle1");
            Circle circle2 = (Circle) line.getProperties().get("circle2");

            circle1.centerXProperty().unbind();
            circle1.centerYProperty().unbind();
            circle2.centerXProperty().unbind();
            circle2.centerYProperty().unbind();

            circle1.setCenterX(initialCircle1X[0] + deltaX);
            circle1.setCenterY(initialCircle1Y[0] + deltaY);
            circle2.setCenterX(initialCircle2X[0] + deltaX);
            circle2.setCenterY(initialCircle2Y[0] + deltaY);
            
            midPoint.setCursor(Cursor.CLOSED_HAND);

            updateLine(line);
            updateCircleLabel(circle1);
            updateCircleLabel(circle2);
        });

        midPoint.setOnMouseReleased(e -> {

            Circle circle1 = (Circle) line.getProperties().get("circle1");
            Circle circle2 = (Circle) line.getProperties().get("circle2");

            bindCirclePosition(circle1, labelList.get(circleList.indexOf(circle1)), circle1.getCenterX(), circle1.getCenterY());
            bindCirclePosition(circle2, labelList.get(circleList.indexOf(circle2)), circle2.getCenterX(), circle2.getCenterY());
        });
    }
    
    
    private Label calculateSlope(Line line) {
    	
    	double x1 = line.getStartX();
        double y1 = line.getStartY();
        double x2 = line.getEndX();
        double y2 = line.getEndY();

		
		double slope;
		if (x2 - x1 == 0) {
			//System.err.println("Error: The line is vertical, slope is undefined.");
			slope = Double.NaN; 
		} else {
			slope = (y2 - y1) / (x2 - x1);
		}

		double angle;
		if (x1 < x2) {
			angle = Math.atan2(y2 - y1, x2 - x1);
		} else {
			angle = Math.atan2(y1 - y2, x1 - x2);
		}
		
		DecimalFormat df = new DecimalFormat("#.##");
		double roundedSlope = Math.abs( Double.parseDouble(df.format(slope)));
		
		slopeLabel = new Label("Slope: " + (Double.isNaN(slope) ? "∞" : roundedSlope));
		slopeLabel.setStyle("-fx-font-size: small; -fx-font-color: white; -fx-font-weight:bold;");
		slopeLabel.setTextFill(Color.YELLOW); 
		slopeLabel.setRotate(Math.toDegrees(angle));
		slopeLabel.setLayoutX((x1 + x2) / 2);
		slopeLabel.setLayoutY((y1 + y2) / 2);
		//line.getProperties().put("circle1", circle1);
        //line.getProperties().put("circle2", circle2);
		
		//System.out.println("Slope: " + roundedSlope); 
		//slopeLabels.add(slopeLabel);
		//rootPane.getChildren().addAll(slopeLabel);
        
		return slopeLabel;
    }
    
    public void updateLine(Line line) {	

        line.startXProperty().unbind();
        line.startYProperty().unbind();
        line.endXProperty().unbind();
        line.endYProperty().unbind();

    	circle1 = (Circle) line.getProperties().get("circle1");
    	circle2 = (Circle) line.getProperties().get("circle2");
		
        if (circle1 != null && circle2 != null) {
            line.setStartX(circle1.getCenterX());
            line.setStartY(circle1.getCenterY());
            line.setEndX(circle2.getCenterX());
            line.setEndY(circle2.getCenterY());

            line.startXProperty().bind(circle1.centerXProperty());
            line.startYProperty().bind(circle1.centerYProperty());
            line.endXProperty().bind(circle2.centerXProperty());
            line.endYProperty().bind(circle2.centerYProperty());

            updateSlopeLabel(line);
           
            System.out.println("Line updated between Circle 1 at (" + circle1.getCenterX() + ", " + circle1.getCenterY() + ") and Circle 2 at (" + circle2.getCenterX() + ", " + circle2.getCenterY() + ")");
        } else {
            System.err.println("Error: Circle objects are not properly initialized.");
        }
    }
	
    private void updateSlopeLabel(Line line) {
        System.err.println("update Slope Label");
       
        Label slopeLabel = lineLabelMap.get(line);
        
        if (slopeLabel != null) {
            
            Circle circle1 = (Circle) line.getProperties().get("circle1");
            Circle circle2 = (Circle) line.getProperties().get("circle2");

            if (circle1 != null && circle2 != null) {
                double x1 = line.getStartX();
                double y1 = line.getStartY();
                double x2 = line.getEndX();
                double y2 = line.getEndY();

                double slope;
                if (x2 - x1 == 0) {
                    slope = Double.NaN;
                } else {
                    slope = (y2 - y1) / (x2 - x1);
                }

                DecimalFormat df = new DecimalFormat("#.##");
                double roundedSlope = Math.abs(Double.parseDouble(df.format(slope)));

                double labelX = (circle1.getCenterX() + circle2.getCenterX()) / 2;
                double labelY = (circle1.getCenterY() + circle2.getCenterY()) / 2;

                double angle;
                if (x1 < x2) {
                    angle = Math.atan2(y2 - y1, x2 - x1);
                } else {
                    angle = Math.atan2(y1 - y2, x1 - x2);
                }

                slopeLabel.setText("Slope: " + (Double.isNaN(slope) ? "∞" : roundedSlope));

                slopeLabel.setRotate(Math.toDegrees(angle));
                slopeLabel.layoutXProperty().unbind();
                slopeLabel.layoutYProperty().unbind();

                slopeLabel.setLayoutX(labelX);
                slopeLabel.setLayoutY(labelY);

                slopeLabel.layoutXProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartX() + line.getEndX()) / 2, line.startXProperty(), line.endXProperty()).subtract(10));
                slopeLabel.layoutYProperty().bind(Bindings.createDoubleBinding(() -> (line.getStartY() + line.getEndY()) / 2, line.startYProperty(), line.endYProperty()).subtract(10));
               
            }
        }
    }

    /*public void deleteCircle(Circle circle, double x, double y) {

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            int index = circleList.indexOf(circle);
            if (index >= 0) {
				
                rootPane.getChildren().removeAll(circle, labelList.get(index));
                circleList.remove(circle);
                labelList.remove(index);
            }

            actionStack.push(new DeleteCircleAction(rootPane, circle, associatedLabel, linesToRemove, slopeLabelsToRemove, midPointsToRemove, this));
        });
        contextMenu.getItems().add(deleteItem);
        contextMenu.show(circle, x, y);
        
    }*/

    public void deleteLine(Line line, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {

            rootPane.getChildren().remove(line);
            lineList.remove(line);
    

            Label slopeLabel = lineLabelMap.get(line);
            if (slopeLabel != null) {
                rootPane.getChildren().remove(slopeLabel);
                slopeLabelList.remove(slopeLabel);
            }
    
            Rectangle midPoint = midpointMap.get(line);
            if (midPoint != null) {
                rootPane.getChildren().remove(midPoint);
            }

            lineLabelMap.remove(line);
            midpointMap.remove(line);
    
            // Remove the circles associated with the line
            Circle circle1 = (Circle) line.getProperties().get("circle1");
            Circle circle2 = (Circle) line.getProperties().get("circle2");
            Label label1 = null;
            Label label2 = null;


            List<Line> linesToRemoveCircle1 = circleLineMap.get(circle1);
            List<Line> linesToRemoveCircle2 = circleLineMap.get(circle2);

           
            if (circle1 != null && linesToRemoveCircle1 != null && linesToRemoveCircle1.size() == 1) {
                
                for (Label label : labelList) {
                    
                    if ((int)label.getLayoutX() == (int)(circle1.getCenterX() - 15) && (int)label.getLayoutY() == (int)(circle1.getCenterY() + 10)) {
                        label1 = label;
                        System.out.println("Label 1 " + label1);
                        break;
                    }
                }
                deleteCircleWithoutContextMenu(circle1);
            }

           
            if (circle2 != null && linesToRemoveCircle2 != null && linesToRemoveCircle2.size() == 1) {
                for (Label label : labelList) {
                    if ((int)label.getLayoutX() == (int)circle2.getCenterX() - 15 && (int)label.getLayoutY() == (int)circle2.getCenterY() + 10) {
                        label2 = label;
                        System.out.println("Label 2 " + label2);
                        break;
                    }
                }
                deleteCircleWithoutContextMenu(circle2);
            }
    
           
            if (circle1 != null) {
                ArrayList<Line> linesForCircle1 = circleLineMap.get(circle1);
                if (linesForCircle1 != null) {
                    linesForCircle1.remove(line);
                    if (linesForCircle1.isEmpty()) {
                        circleLineMap.remove(circle1);
                    }
                }
            }
    
            if (circle2 != null) {
                ArrayList<Line> linesForCircle2 = circleLineMap.get(circle2);
                if (linesForCircle2 != null) {
                    linesForCircle2.remove(line);
                    if (linesForCircle2.isEmpty()) {
                        circleLineMap.remove(circle2);
                    }
                }
            }
    
            // Push the delete action to the action stack
            actionStack.push(new DeleteLineAction(rootPane, line, slopeLabel, midPoint, circle1, circle2 , label1, label2, this));
        });
        contextMenu.getItems().add(deleteItem);
        contextMenu.show(line, x, y);
    }

    public List<LineData> getLines() {
        return new ArrayList<>(lineDataList);
    }

    public void addLine(Line line) {
        lineList.add(line);
    }

    public void addCircle(Circle circle) {
        circleList.add(circle);
    }

    public void addCircleLabel(Label label) {
        labelList.add(label);
    }

    public void deleteCircleWithoutContextMenu(Circle circle) {
        int index = circleList.indexOf(circle);
        if (index >= 0) {
            rootPane.getChildren().removeAll(circle, labelList.get(index));
            circleList.remove(circle);
            labelList.remove(index);
        }
    }

    public void undo() {
        if (!actionStack.isEmpty()) {
            Action lastAction = actionStack.pop();
            lastAction.undo();
            redoStack.push(lastAction);
        }
    }

    public void redo(){
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redo();
            actionStack.push(action);
        }
    }

    
}


