import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class DrawTriangle {
    private Pane rootPane;
    private List<Line> triangleLines = new ArrayList<>();
    private List<Circle> selectedCircles = new ArrayList<>();
    private boolean isDrawing = false;
    private List<Text> angleTexts = new ArrayList<>();
    private Circle circle;
    private DrawPoint drawPoint;
    private Line line;
    private double angleInDegrees;
    private boolean isUpdatingTriangle = false;
    private boolean updateTriangleFlag = false;
    private Circle draggedCircle = null;
    private double angle;
    private double startAngle;
    private List<Point2D> circlePositions = new ArrayList<>(); 
    public ArrayList<Circle> circleList;
	public ArrayList<Label> labelList;
	private List<Arc> angleArcs = new ArrayList<>();
	private Map<Circle, Line> circleLineMap = new HashMap<>();
	private List<Circle> allCircles = new ArrayList<>();
	private ToggleButton triangleButton;
	private Line yellowLine;
	private List<Line> yellowLinesList = new ArrayList<>();
    private Map<Text, Pair<Line, Line>> lineAngleMap = new HashMap<>();
    private Text angleText;
    private Map<Arc, Text> angleArcMap = new HashMap<>();
    private List<Line> allTriangleLines = new ArrayList<>();
    //private Map<Line, Triangle> triangleMap = new HashMap<>();
    private Stack<Action> actionStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();
    private List<TriangleData> triangleDataList = new ArrayList<>();
    
    public DrawTriangle(Pane rootPane, DrawPoint drawPoint, ToggleButton triangleButton, ArrayList<Circle> circleList, ArrayList<Label> labelList) {
        this.rootPane = rootPane;
        this.drawPoint = drawPoint;
        this.triangleButton = triangleButton;
        this.circleList = circleList;
        this.labelList = labelList;
    }
    
    public void clearAllTriangles(){
        for (Iterator<Node> iterator = rootPane.getChildren().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            if (node instanceof Line) {
                iterator.remove();
            }
            if(node instanceof Text) {
            	 iterator.remove();
            }
            if(node instanceof Arc) {
           	 iterator.remove();
           }
        }
        //rootPane.getChildren().removeAll(angleTexts);
        triangleLines.clear();
        clearYellowLines();
    }

    public void startDrawing() {
        isDrawing = true;
        rootPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
            	circle = drawPoint.drawCircle(event.getX(), event.getY());
                selectCircle(event.getX(), event.getY());
                addCircleEvents(circle, circleList, labelList);
            }else if(event.getButton() == MouseButton.SECONDARY) {
			    double x = event.getX();
		        double y = event.getY();
		        boolean circleClicked = false;

		        for (Circle c : circleList) {
		            if (c.contains(x, y)) {
                        drawPoint.deleteCircle(c, event.getScreenX(), event.getScreenY());
                        circleClicked = true;
                        break;
		            }
		        }

                if (!circleClicked) {
                    for (Line line : allTriangleLines) {
                        if (lineContainsPoint(line, x, y)) {
                            deleteTriangle(line, event.getScreenX(), event.getScreenY());
                            return;
                        }
                    }
                }
			}
        });   
        
    }

    public void stopDrawing() {
        isDrawing = false;
        rootPane.setOnMouseClicked(null);
        //clearSelectedCircles();
        //clearTriangle();
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

    public void selectCircle(double x, double y) {
        if (isDrawing) {
            Circle clickedCircle = findCircle(x, y);
            if (clickedCircle != null && !selectedCircles.contains(clickedCircle)) {
                selectedCircles.add(clickedCircle);
                allCircles.add(clickedCircle);
                System.out.println("all circles " + allCircles);
                addCircleEvents(clickedCircle, circleList, labelList);
                if (selectedCircles.size() == 3) {
                    clearTriangle();
                    drawTriangle();
                    angleText();
                    drawArc();
                    clearSelectedCircles();
                    
                    clearYellowLines();
                    rootPane.setOnMouseMoved(null);
                }else {
                    startYellowLine(); 
                }
                
            }
        }
        
        
    } 
    
    private void startYellowLine() {
        if (yellowLinesList.size() < 2) {
            Line yellowLine = new Line();
            rootPane.getChildren().add(yellowLine); 
            yellowLinesList.add(yellowLine); 
            rootPane.setOnMouseMoved(event -> { 
                if (circle != null && triangleButton.isSelected() && selectedCircles.size() < 3) {
                    yellowLine.setStartX(circle.getCenterX());
                    yellowLine.setStartY(circle.getCenterY());
                    yellowLine.setEndX(event.getX());
                    yellowLine.setEndY(event.getY());
                    yellowLine.setStroke(Color.YELLOW);
                    yellowLine.setStrokeWidth(2);
                }
            });
        } else {
            rootPane.getChildren().removeAll(yellowLinesList);
            yellowLinesList.clear();
        }
    }
    
    private void clearYellowLines() {
        rootPane.getChildren().removeIf(node -> node instanceof Line && ((Line) node).getStroke().equals(Color.YELLOW));
        yellowLinesList.clear();
    }
     
    private Circle findCircle(double x, double y) {
        for (Node node : rootPane.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                if (circle.contains(x, y)) {
                    return circle;
                }
            }
        }
        return null;
    } 

    private void bindCirclePosition(Circle circle , Label label, double initialX, double initialY) {
        //circle.centerXProperty().unbind();
        //circle.centerYProperty().unbind();
        
        circle.centerXProperty().bind(rootPane.widthProperty().multiply(initialX / rootPane.getWidth()));
        circle.centerYProperty().bind(rootPane.heightProperty().multiply(initialY / rootPane.getHeight()));

        label.layoutXProperty().bind(circle.centerXProperty().subtract(15));
        label.layoutYProperty().bind(circle.centerYProperty().add(10));
    }
    

    private void drawTriangle() {
    	//circleLineMap.clear();
             
        if (triangleLines.isEmpty()) {
            for (int i = 0; i < 2; i++) {
                line = new Line();
                triangleLines.add(line);
                allTriangleLines.add(line);
                rootPane.getChildren().add(line); 

                //Triangle triangle = new Triangle();
                //triangleMap.put(line, triangle);
            }
        }


        for (int i = 0; i < 2; i++) {
            Line line = triangleLines.get(i);
            Circle circle1 = selectedCircles.get(i);
            Circle circle2 = selectedCircles.get((i + 1) % 3);

            int index1 = circleList.indexOf(circle1);
            int index2 = circleList.indexOf(circle2);
            if (index1 != -1 && index2 != -1) {
                Label label1 = labelList.get(index1);
                Label label2 = labelList.get(index2);
                bindCirclePosition(circle1, label1, circle1.getCenterX(), circle1.getCenterY());
                bindCirclePosition(circle2, label2, circle2.getCenterX(), circle2.getCenterY());
            }

            line.setStartX(circle1.getCenterX());
            line.setStartY(circle1.getCenterY());
            line.setEndX(circle2.getCenterX());
            line.setEndY(circle2.getCenterY());

            line.startXProperty().bind(circle1.centerXProperty());
            line.startYProperty().bind(circle1.centerYProperty());
            line.endXProperty().bind(circle2.centerXProperty());
            line.endYProperty().bind(circle2.centerYProperty());


            line.setStroke(Color.BLUE);
            circleLineMap.put(circle1, line);
            System.out.println("circle line map " +circleLineMap); 
            
        }
        circleDetails();
        
    }

    public void circleDetails() {
        for (int i = 0; i < 2; i++) {
            Line line1 = triangleLines.get(0);
            Line line2 = triangleLines.get(1);

            Circle circle1 = selectedCircles.get(0);
            Circle circle2 = selectedCircles.get(1);
            Circle circle3 = selectedCircles.get(2);

            int index1 = circleList.indexOf(circle1);
            int index2 = circleList.indexOf(circle2);
            int index3 = circleList.indexOf(circle3);
            
                Label label1 = labelList.get(index1);
                Label label2 = labelList.get(index2);
                Label label3 = labelList.get(index3);
            
                triangleDataList.add(new TriangleData(line1, line2, angleTexts, angleArcs, circle1, circle2, circle3, label1, label2, label3));
        }
    }

    public List<TriangleData> getTriangles() {
        return new ArrayList<>(triangleDataList);
    }

    public void addCircle(Circle circle) {
        circleList.add(circle);
    }

    public void addCircleLabel(Label label) {
        labelList.add(label);
    }
    
    private void drawArc() {
        for (int i = 0; i < triangleLines.size(); i++) {
        Circle circle = selectedCircles.get(1);
        double arcCenterX = selectedCircles.get(1).getCenterX();
        double arcCenterY = selectedCircles.get(1).getCenterY();
        
        double radius = 30;
        
        Line line1 = triangleLines.get(0);
        Line line2 = triangleLines.get(1);
        //System.out.println("point 1 ( X :" + selectedCircles.get(0).getCenterX() + ", Y: " + selectedCircles.get(0).getCenterY());
        
        AngleInfo angleInfo = calculateAngleArc(line1, line2);
        startAngle = angleInfo.getStartAngle();
        angle = angleInfo.getAngle();
       
        Arc angleArc = new Arc(arcCenterX, arcCenterY, radius, radius, startAngle, angle);  
        angleArc.setType(ArcType.OPEN);
        angleArc.setStroke(Color.YELLOW);
        angleArc.setStrokeWidth(2);
        angleArc.setFill(null);
        
        angleArc.centerXProperty().bind(circle.centerXProperty());
        angleArc.centerYProperty().bind(circle.centerYProperty());

        updateArc(angleArc, line1, line2);

     
        line1.startXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line1.startYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line1.endXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line1.endYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line2.startXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line2.startYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line2.endXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        line2.endYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
        //String angleArcId = UUID.randomUUID().toString();

        //angleArcMap.put(angleArcId, angleArc);
       
        //angleArcMap.put(angleArc, angleText);
        Text correspondingText = angleTexts.get(i);
        angleArcMap.put(angleArc, correspondingText);
        //System.out.println("Angle Arc Map: " + angleArcMap);
       
        rootPane.getChildren().add(angleArc);
        angleArcs.add(angleArc);
        }
        
    }

    private void updateArc(Arc angleArc, Line line1, Line line2) {
        AngleInfo angleInfo = calculateAngleArc(line1, line2);
        angleArc.setStartAngle(angleInfo.getStartAngle());
        angleArc.setLength(angleInfo.getAngle());
    }      

    private AngleInfo calculateAngleArc(Line line1 , Line line2){
        double slope1 = (line1.getEndY() - line1.getStartY())/(line1.getEndX() - line1.getStartX());
        double slope2 = (line2.getEndY() - line2.getStartY())/(line2.getEndX() - line2.getStartX()) ;
        
        double angleStart = Math.abs(Math.toDegrees(Math.atan(slope1)));
        double angleEnd = Math.abs(Math.toDegrees(Math.atan(slope2)));
       
        System.out.println("Slope 1: " +slope1);
        System.out.println("Slope 2: " +slope2);
        System.out.println("angleStart: " + angleStart);
        System.out.println("angleEnd: " +angleEnd);

        
        if(angleStart > angleEnd){
           angle = angleStart - angleEnd;
           startAngle = angleEnd;
           System.out.println("Angle when angleStart is greater " + angle);
           //System.out.println(isClockwise);
           System.out.println(startAngle);
        }else {
            angle = Math.abs(angleEnd - angleStart);
            startAngle = angleStart;
            //System.out.println(isClockwise);
            System.out.println("Angle when angleEnd is greater " + angle);
            System.out.println(startAngle);
        }
        
        
        if(slope1 > slope2 && line2.getStartX() > line2.getEndX() && line2.getStartY() > line2.getEndY()) {
        	startAngle = angleStart + 2 * (90 - angleStart);
        	System.out.println("under case 1");
        }else if(angleStart < angleEnd && slope1 > slope2 && line1.getStartX() < line1.getEndX() && line1.getStartY() > line1.getEndY()) {
        	startAngle = 180 + angleStart;
        	System.out.println("under case 1.1");
        }else if(slope1 > slope2 && line1.getStartY() > line1.getEndY() && line2.getStartY() < line2.getEndY()) {
        	startAngle = angleStart - (2 * angleStart);
            System.out.println("case 1.2 " + startAngle);
        	System.out.println("under case 1.2");
        }else if(angleStart < angleEnd && slope2 > slope1 && line2.getStartX() > line2.getEndX() && line1.getStartY() < line1.getEndY()) {
        	startAngle = angleStart;
        	System.out.println("under case 1.3");
        }else if(angleStart > angleEnd && slope2 > slope1 && line2.getStartX() > line2.getEndX() && line1.getStartY() < line1.getEndY()) {
        	startAngle = angleStart;
        	System.out.println("under case 1.3.1");
        }else if(angleStart > angleEnd && slope1 > slope2 && line1.getEndY() > line1.getStartY() && line2.getStartY() < line2.getEndY()) {
        	startAngle = angleStart + 2 * (90 - angleStart);
        	System.out.println("under case 1.4");
        }else if(angleStart < angleEnd && slope2 > slope1 && line2.getStartX() > line2.getEndX()) {
        	startAngle = angleEnd + (100 - angleEnd);
        	System.out.println("under case 1.5");
        }else if(angleStart < angleEnd && slope1 > slope2 && line1.getStartY() < line1.getEndY() && line2.getStartY() < line2.getEndY()) {
        	startAngle = angleStart + 2 * (90 - angleStart);
        	System.out.println("under case 1.6");
        }else if(angleStart > angleEnd && slope2 > slope1 && line2.getStartX() > line2.getEndX() && line1.getStartY() > line1.getEndY()) {
        	startAngle = 180 - angleEnd;
            System.out.println("StartAngle: " +startAngle);
        	System.out.println("under case 1.7");
        }/*else if(angleStart < angleEnd && slope1 > slope2 && line2.getStartY() > line2.getEndY() && line1.getStartY() < line1.getEndY()) {
        	startAngle = angleEnd;
            System.out.println("StartAngle: " +startAngle);
        	System.out.println("under case 1.8");
        }*/
        
        if(line2.getStartX() < line2.getEndX() && line1.getEndY() > line1.getStartY()) {
        	angle = angleInDegrees;
        	startAngle = angleEnd;
        	System.out.println("under case 2");
        }else if(line2.getStartX() > line2.getEndX()) {
        	angle = angleInDegrees;
        	System.out.println("under case 2.1");
        }else if(slope1 > slope2 && line2.getStartX() < line2.getEndX() && line2.getStartY() > line2.getEndY() && line1.getStartY() > line1.getEndY()){
            angle = -angleInDegrees;
            startAngle = angleEnd;
            System.out.println("StartAngle: " +startAngle);
        	System.out.println("under case 2.2");
        }
        
        
        if(slope1 < 0 && slope2 > slope1 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()) {
        	startAngle = angleEnd - (2 * angleEnd);
            angle = angleInDegrees;
        	System.out.println("under case 3");
        }/*else if(slope2 > slope1 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()) {
        	startAngle = angleEnd - 2 * (90 - angleEnd);
            System.out.println("under case 3.1");
        	System.out.println("StartAngle: " +startAngle);
        }*/else if(slope2 > slope1 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()) {
        	startAngle = angleEnd - (2 * angleEnd);
            angle = -angleInDegrees;
            System.out.println("under case 3.1");
        	System.out.println("StartAngle: " +startAngle);
        }/*else  if(slope2 > slope1 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()) {
        	startAngle = 180 + angleStart;
            System.out.println("under case 3.2");
        	System.out.println("StartAngle: " +startAngle);
        }*/else if(slope2 < 0 && slope1 > slope2 && line1.getEndY() < line1.getStartY() && line2.getStartY() < line2.getEndY()) {
        	startAngle = 180 + angleEnd;
        	System.out.println("under case 3.3");
        }/*else if(slope2 > slope1 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()) {
        	startAngle = angleEnd - (2 * angleEnd);
            angle = -angleInDegrees;
        	System.out.println("under case 3.4");
        }*/else if(angleStart < angleEnd && slope1 > slope2 && line1.getStartX() > line1.getEndX() && line1.getStartY() > line1.getEndY() && line2.getEndY() > line2.getStartY()) {
        	startAngle = 180 + angleEnd;
        	System.out.println("under case 3.5");
        }else if (slope1 > 0  && slope1 < slope2 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
            startAngle = angleStart + 2 * (90 - angleStart); 
            System.out.println("under case 3.6");
        }else if(slope2 > 0 && slope1 > slope2 && line1.getStartY() < line1.getEndY() && line2.getStartY() < line2.getEndY()) {
        	startAngle = angleEnd - (2 * angleEnd);
        	System.out.println("under case 3.7");
        }/*else if(slope1 > slope2 && line2.getStartX() < line2.getEndX() && line2.getStartY() < line2.getEndY()) {
        	startAngle =angleStart + 2 * (90 - angleStart);
        	System.out.println("under case 3.8");
        }*/
        
       

        /*if(isClockwise){
            startAngle = angleEnd;
            //angle = 360 - angle;
            System.out.println(startAngle);
        }else{
            startAngle = angleEnd;
            System.out.println(startAngle);
        }*/
        return new AngleInfo(startAngle, angle);

    }
    
     
    private void clearSelectedCircles() {
    	storeCirclePositions();
        selectedCircles.clear();
    }
    
    public void clearTriangle() {
        triangleLines.clear();
    }
    

    private void angleText() {
        angleTexts.clear();
   

	    for (int i = 0; i < 2; i++) {
            Line line1 = triangleLines.get(0);
            Line line2 = triangleLines.get(1);
           
	        angle = calculateAngle(triangleLines.get(i), triangleLines.get((i + 1) % 2));
	       
	        //double textCenterX = (triangleLines.get(i).getStartX() + triangleLines.get((i + 1) % 2).getStartX()) / 2;
	        //double textCenterY = (triangleLines.get(i).getStartY() + triangleLines.get((i + 1) % 2).getStartY()) / 2;
	        //double textCenterX = (selectedCircles.get(i).getCenterX() + selectedCircles.get(i +1).getCenterX() + selectedCircles.get(i + 2).getCenterX()) / 3;
	        //double textCenterY = (selectedCircles.get(i).getCenterY() + selectedCircles.get(i +1).getCenterY() + selectedCircles.get(i + 2).getCenterY()) / 3;
	
            DoubleProperty textCenterX = new SimpleDoubleProperty();
            DoubleProperty textCenterY = new SimpleDoubleProperty();

	        Text angleText = new Text();
	        //angleText.setText(String.format("%.2f°", angle));
	        //angleText.setX(textCenterX);
	        //angleText.setY(textCenterY);
	        angleText.setFill(Color.YELLOW);
	        angleText.setStyle("-fx-font-weight:bold;");
	
            updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle);

          
            line1.startXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line1.startYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line1.endXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line1.endYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line2.startXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line2.startYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line2.endXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            line2.endYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));

	        rootPane.getChildren().add(angleText);
	
	       
	        angleTexts.add(angleText);

            lineAngleMap.put(angleText, new Pair<>(line1, line2));
            //System.out.println("line Angle Map " + lineAngleMap);

	    }
    }

    private void updateAngle(Text angleText, DoubleProperty textCenterX, DoubleProperty textCenterY, Line line1, Line line2, double angle) {
        textCenterX.set((line1.getStartX() + line2.getStartX()) / 2);
        textCenterY.set((line1.getStartY() + line2.getStartY()) / 2);
        angleText.setText(String.format("%.2f°", angle));
        angleText.setX(textCenterX.get());
        angleText.setY(textCenterY.get());
    }
     
    private double calculateSlope(Line line) {
        double deltaY = line.getEndY() - line.getStartY();
        double deltaX = line.getEndX() - line.getStartX();
       
        if (deltaX == 0) {
            return Double.POSITIVE_INFINITY; 
        }
        return deltaY / deltaX;
    }
    
    private double calculateAngle(Line line1, Line line2) {
        double slope1 = calculateSlope(line1);
        double slope2 = calculateSlope(line2);

        double abSlope1 = Math.abs(slope1);
        double abSlope2 = Math.abs(slope2);
            
        System.out.println("abSlope1 : " + abSlope1);
        System.out.println("abSlope2 : " + abSlope2);

        if (Double.isInfinite(slope1) && Double.isInfinite(slope2)) {
            return 0; 
        }

        double angleInRadians = Math.atan2(slope2 - slope1 , 1 + slope1 * slope2);
       
        if(slope1 * slope2 > 0){
            angleInDegrees = Math.toDegrees(angleInRadians);

                if(angleInDegrees < 0){
                    angleInDegrees += 180;
                }
         
                angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 1: " + angleInDegrees);
            if(line2.getEndY() > line2.getStartY() && line1.getEndY() > line1.getStartY()){
             
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 1.1: " + angleInDegrees);
            }else if(line1.getEndX() < line1.getStartX() && line2.getEndX() < line2.getStartX()){
              
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 1.2: " + angleInDegrees);
            }else if(line2.getEndY() > line2.getStartY() && line1.getEndY() > line1.getStartY()){
           
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 1.3: " + angleInDegrees);
            }else if(line1.getEndX() > line1.getStartX() && line2.getEndX() > line2.getStartX()){
           
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 1.4: " + angleInDegrees);
            }

            
        }else if(slope1 * slope2 < 0){
            angleInDegrees = Math.toDegrees(angleInRadians);

                if(angleInDegrees < 0){
                    angleInDegrees += 180;
                }
            
            if(slope2 > -0.5 && slope2 < 0 && line1.getEndY() > line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2: " + angleInDegrees);
            }else if(slope1 < 0 && slope2 < 0.5 && slope2 > 0 && line1.getEndY() > line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.1: " + angleInDegrees);
            }else if(line1.getEndY() > line1.getStartY() && line2.getEndY() < line2.getStartY() && slope1 > -1 && slope1 < 0){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.2: " + angleInDegrees);
            }/*else if(line1.getEndY() > line1.getStartY() && line2.getEndY() < line2.getStartY() && slope2 > -0.5 ){
            	angleInDegrees = Math.toDegrees(angleInRadians);

                if(angleInDegrees < 0){
                    angleInDegrees += 180;
                }
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.3: " + angleInDegrees);
            }*/else if(slope2 < -0.5 && line1.getEndX() > line1.getStartX() && line2.getEndX() < line1.getStartX()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.4: " + angleInDegrees);
            }else if(slope1 < -0.5 && slope2 > 1 && line1.getEndY() < line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.4.1: " + angleInDegrees);
            }else if(slope1 > 1 && slope2 < -0.5 && line1.getEndY() < line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.4.2: " + angleInDegrees);
            }else if(slope1 < -0.5 && slope2 > 1 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.4.3: " + angleInDegrees);
            }else if(slope1 > -0.5 && slope1 < 0  && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.5: " + angleInDegrees);
            }else if(slope2 > -0.5 && slope2 < 0 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.5.1: " + angleInDegrees);
            }else if(slope1 < 1 && slope1 > 0 && slope2 > -1 && slope2 < 0 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.5.2: " + angleInDegrees);
            }else if(slope1 < -0.5  && slope2 < 1 && slope2 > 0 && line1.getEndY() < line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.5.3: " + angleInDegrees);
            }else if(slope1 < -1 && slope2 < 1 && slope2 > 0 && line1.getEndY() < line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.10: " + angleInDegrees);
            }else if(slope1 < 1 && slope1 > 0 && slope2 < -1 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.10.1: " + angleInDegrees);
            }else if(slope1 < -1 && slope1 > -2 && slope2 < 1 && slope2 > 0 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.10.2: " + angleInDegrees);
            }else if(slope1 < 1 && slope1 > 0 && slope2 < -1 && line1.getEndY() < line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
                angleInDegrees = Math.max(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.10.3: " + angleInDegrees);
            }else if(line1.getEndY() > line1.getStartY() && line2.getEndY() < line2.getStartY() && slope2 < -0.5){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.6: " + angleInDegrees);
            }else if(line1.getEndX() > line1.getStartX() && line2.getEndX() > line2.getStartX() && slope1 < -1){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.6.1: " + angleInDegrees);
            }else if(line1.getEndX() < line1.getStartX() && line2.getEndX() < line2.getStartX() && slope1 < -1){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.7: " + angleInDegrees);
            }else if( slope2 < -1 && slope1 > 0 && line1.getEndX() < line1.getStartX() && line2.getEndX() < line2.getStartX()){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.7.1: " + angleInDegrees);
            }else if(slope2 > -0.5 && slope2 < 0 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.8: " + angleInDegrees);
            }else if(slope1 > -0.5 && slope1 < 0 && slope2 < 1 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.8.1: " + angleInDegrees);
            }else if(slope1 > -1 && slope1 < 0 && slope2 < 1 && slope2 > 0 && line1.getEndY() < line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.9: " + angleInDegrees);
            }else if(slope2 > -0.5 && slope2 < 0 && line1.getEndY() < line1.getStartY() && line2.getEndY() < line2.getStartY()){
            	
            	angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.9.1: " + angleInDegrees);
            }else if(slope1 < -2 && slope2 < 0.2 && slope2 > 0 && line1.getEndY() > line1.getStartY() && line2.getEndY() >= line2.getStartY()){
                angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.11: " + angleInDegrees);
            }else if(slope1 < -0.5 && slope2 > 0 && line1.getEndY() > line1.getStartY() && line2.getEndY() > line2.getStartY()){
                angleInDegrees = Math.min(angleInDegrees, 180 - angleInDegrees);
                System.out.println("Angle 2.12: " + angleInDegrees);
            }
           
        }
        
        return angleInDegrees;
    }

    private void storeCirclePositions() {
        circlePositions.clear();
        for (Circle circle : selectedCircles) {
            circlePositions.add(new Point2D(circle.getCenterX(), circle.getCenterY()));
        }
    }
    
    public void updateTriangle() {
    	//System.out.println("selected circles " + selectedCircles);
        for (Circle circle : circleLineMap.keySet()) {
            Line line = circleLineMap.get(circle);

            line.startXProperty().unbind();
            line.startYProperty().unbind();
            line.endXProperty().unbind();
            line.endYProperty().unbind();
           
            if (line != null) {
            	System.out.println("updating line " + line);
                line.setStartX(circle.getCenterX());
                line.setStartY(circle.getCenterY());
                //line.setEndX();
                line.startXProperty().bind(circle.centerXProperty());
                line.startYProperty().bind(circle.centerYProperty());
             
                Circle nextCircle = getNextCircle(circle);
                //Line nextLine = circleLineMap.get(nextCircle);
                //if (nextLine != null) {
                	//System.out.println("next line " + nextLine);
                    line.setEndX(nextCircle.getCenterX());
                    line.setEndY(nextCircle.getCenterY());

                    line.endXProperty().bind(nextCircle.centerXProperty());
                    line.endYProperty().bind(nextCircle.centerYProperty());
                //}
            }
        }
        //allCircles.clear();
        updateAngleText();
        
    }
     
    
    private void updateAngleText() {
        for (Map.Entry<Text, Pair<Line, Line>> entry : lineAngleMap.entrySet()) {
            Text angleText = entry.getKey();
            Pair<Line, Line> linePair = entry.getValue();
            Line line1 = linePair.getKey();
            Line line2 = linePair.getValue();
    
            double angle = calculateAngle(line1, line2);
    
            //double textCenterX = (line1.getStartX() + line2.getStartX()) / 2;
            //double textCenterY = (line1.getStartY() + line2.getStartY()) / 2;
    
            DoubleProperty textCenterX = new SimpleDoubleProperty();
            DoubleProperty textCenterY = new SimpleDoubleProperty();

            if (angleText != null) {
                angleText.xProperty().unbind();
                angleText.yProperty().unbind();

                //angleText.setText(String.format("%.2f°", angle));
                //angleText.setX(textCenterX);
                //angleText.setY(textCenterY);
                line1.startXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line1.startYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line1.endXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line1.endYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line2.startXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line2.startYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line2.endXProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
                line2.endYProperty().addListener((obs, oldVal, newVal) -> updateAngle(angleText, textCenterX, textCenterY, line1, line2, angle));
            }
    
            
            //update Angle Arc 
            for (Map.Entry<Arc, Text> arcEntry : angleArcMap.entrySet()) {
                Arc angleArc = arcEntry.getKey();
                angleArc.centerXProperty().unbind();
                angleArc.centerYProperty().unbind();

                Text associatedAngleText = arcEntry.getValue();
                System.out.println("under update angle" +associatedAngleText.equals(angleText));
                if (associatedAngleText.equals(angleText)) {
                    //Circle centerCircle = selectedCircles.get(1);
                    double arcCenterX = line1.getEndX();
                    double arcCenterY = line1.getEndY();
                    double radius = angleArc.getRadiusX();

                    AngleInfo angleInfo = calculateAngleArc(line1, line2);
                    double startAngle = angleInfo.getStartAngle();
                    double arcAngle = angleInfo.getAngle();

                    angleArc.setCenterX(arcCenterX);
                    angleArc.setCenterY(arcCenterY);
                    angleArc.setRadiusX(radius);
                    angleArc.setRadiusY(radius);
                    angleArc.setStartAngle(startAngle);
                    angleArc.setLength(arcAngle);

                    //angleArc.centerXProperty().bind(circle.centerXProperty());
                    //angleArc.centerYProperty().bind(circle.centerYProperty());
                    
                    updateArc(angleArc, line1, line2);
                    
                    line1.startXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line1.startYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line1.endXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line1.endYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line2.startXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line2.startYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line2.endXProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                    line2.endYProperty().addListener((obs, oldVal, newVal) -> updateArc(angleArc, line1, line2));
                }
            }
        }
    }
    
    public void setUpdateTriangleFlag(boolean flag) {
        updateTriangleFlag = flag;
    }

    public void addCircleEvents(Circle circle, ArrayList<Circle> circleList, ArrayList<Label> labelList ) {
        circle.setOnMousePressed(event -> {
            draggedCircle = circle;
            event.consume();
        });

        circle.setOnMouseDragged(event -> {
            if (draggedCircle != null && draggedCircle == circle) {
                draggedCircle.centerXProperty().unbind();
                draggedCircle.centerYProperty().unbind();
              
                draggedCircle.setCenterX(event.getX());
                draggedCircle.setCenterY(event.getY());
                
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
                
                updateTriangle();
                event.consume();
            }
        });

        circle.setOnMouseReleased(event -> {
            draggedCircle = null;
            event.consume();
        });
    }
    
    public void startUpdatingTriangle() {
        isUpdatingTriangle = true;
    }
    
    private Circle getNextCircle(Circle currentCircle) {
        int currentIndex = allCircles.indexOf(currentCircle);
        int nextIndex = (currentIndex + 1) % allCircles.size();
        return allCircles.get(nextIndex);
    }

    public void deleteTriangle(Line line, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            if (allTriangleLines.contains(line)) {
                int index = allTriangleLines.indexOf(line);
                Line line1 = allTriangleLines.get(index);
                Line line2 = allTriangleLines.get((index + 1) % 2);

              
                rootPane.getChildren().remove(line1);
                rootPane.getChildren().remove(line2);
                allTriangleLines.remove(line1);
                allTriangleLines.remove(line2);

                // Remove angle texts 
            List<Text> angleTextsList = new ArrayList<>();
            for (Iterator<Map.Entry<Text, Pair<Line, Line>>> iterator = lineAngleMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Text, Pair<Line, Line>> entry = iterator.next();
                if (entry.getValue().getKey() == line1 || entry.getValue().getKey() == line2 ||
                    entry.getValue().getValue() == line1 || entry.getValue().getValue() == line2) {
                    rootPane.getChildren().remove(entry.getKey());
                    angleTextsList.add(entry.getKey());
                    iterator.remove();
                }
            }

            // Remove angle arcs 
            List<Arc> angleArcsList = new ArrayList<>();
            for (Iterator<Map.Entry<Arc, Text>> iterator = angleArcMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Arc, Text> entry = iterator.next();
                Text associatedText = entry.getValue();
                if (!lineAngleMap.containsKey(associatedText)) {
                    rootPane.getChildren().remove(entry.getKey());
                    angleArcsList.add(entry.getKey());
                    iterator.remove();
                }
            }

            // Remove circles and their labels if not used by other triangles
            removeCircleAndLabelIfNecessary(line1);
            removeCircleAndLabelIfNecessary(line2);
            actionStack.push(new DeleteTriangleAction(rootPane, line1, line2, allTriangleLines, 
                                                      lineAngleMap, angleArcMap,this, angleTextsList, angleArcsList));
            
            }
        });
        contextMenu.getItems().add(deleteItem);
        contextMenu.show(line, x, y);
    }

    private void removeCircleAndLabelIfNecessary(Line line) {
        Circle circle1 = (Circle) line.getProperties().get("circle1");
        Circle circle2 = (Circle) line.getProperties().get("circle2");
        Label label1 = null;
        Label label2 = null;

        if (circle1 != null) {
            for (Label label : labelList) {
                if ((int) label.getLayoutX() == (int) (circle1.getCenterX() - 15) && (int) label.getLayoutY() == (int) (circle1.getCenterY() + 10)) {
                    label1 = label;
                    System.out.println("Label 1 " + label1);
                    break;
                }
            }
            deleteCircleWithoutContextMenu(circle1);
        }

        if (circle2 != null) {
            for (Label label : labelList) {
                if ((int) label.getLayoutX() == (int) circle2.getCenterX() - 15 && (int) label.getLayoutY() == (int) circle2.getCenterY() + 10) {
                    label2 = label;
                    System.out.println("Label 2 " + label2);
                    break;
                }
            }
            deleteCircleWithoutContextMenu(circle2);
        }
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