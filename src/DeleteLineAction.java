import java.util.ArrayList;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class DeleteLineAction extends Action {
    private Pane rootPane;
    private Line line;
    private Label slopeLabel;
    private Rectangle midPoint;
    private Circle circle1;
    private Circle circle2;
    private Label label1;
    private Label label2;
    private DrawLine drawLine;

    public DeleteLineAction(Pane rootPane, Line line, Label slopeLabel, Rectangle midPoint, Circle circle1, Circle circle2 , Label label1, Label label2, DrawLine drawLine) {
        this.rootPane = rootPane;
        this.line = line;
        this.slopeLabel = slopeLabel;
        this.midPoint = midPoint;
        this.circle1 = circle1;
        this.circle2 = circle2;
        this.label1 = label1;
        this.label2 = label2;
        this.drawLine = drawLine;
    }

    @Override
    public void undo() {
        rootPane.getChildren().add(line);
        drawLine.lineList.add(line);

        if (slopeLabel != null) {
            rootPane.getChildren().add(slopeLabel);
            drawLine.slopeLabelList.add(slopeLabel);
            drawLine.lineLabelMap.put(line, slopeLabel);
        }

        if (midPoint != null) {
            rootPane.getChildren().add(midPoint);
            drawLine.midpointMap.put(line, midPoint);
        }

        if (circle1 != null && !rootPane.getChildren().contains(circle1)) {
            rootPane.getChildren().add(circle1);
            drawLine.circleList.add(circle1);
            drawLine.circleLineMap.computeIfAbsent(circle1, k -> new ArrayList<>()).add(line);

            if (label1 != null && !rootPane.getChildren().contains(label1)) {
                rootPane.getChildren().add(label1);
                drawLine.labelList.add(label1);
                
            }
        
        }

        if (circle2 != null && !rootPane.getChildren().contains(circle2)) {
            rootPane.getChildren().add(circle2);
            drawLine.circleList.add(circle2);
            drawLine.circleLineMap.computeIfAbsent(circle2, k -> new ArrayList<>()).add(line);

            if (label2 != null && !rootPane.getChildren().contains(label2)) {
                rootPane.getChildren().add(label2);
                drawLine.labelList.add(label2);
            }
        }
        System.out.println("CircleList After undo" + drawLine.circleList);
        System.out.println("LabelList After undo" + drawLine.labelList);
        
    }

    public void redo() {
        rootPane.getChildren().remove(line);
        drawLine.lineList.remove(line);

      
        if (slopeLabel != null) {
            rootPane.getChildren().remove(slopeLabel);
            drawLine.slopeLabelList.remove(slopeLabel);
            drawLine.lineLabelMap.remove(line);
        }

      
        if (midPoint != null) {
            rootPane.getChildren().remove(midPoint);
            drawLine.midpointMap.remove(line);
        }

       
        if (circle1 != null) {
            drawLine.deleteCircleWithoutContextMenu(circle1);
        }

        
        if (circle2 != null) {
            drawLine.deleteCircleWithoutContextMenu(circle2);
        }

       
        if (circle1 != null) {
            ArrayList<Line> linesForCircle1 = drawLine.circleLineMap.get(circle1);
            if (linesForCircle1 != null) {
                linesForCircle1.remove(line);
                if (linesForCircle1.isEmpty()) {
                    drawLine.circleLineMap.remove(circle1);
                }
            }
        }

        if (circle2 != null) {
            ArrayList<Line> linesForCircle2 = drawLine.circleLineMap.get(circle2);
            if (linesForCircle2 != null) {
                linesForCircle2.remove(line);
                if (linesForCircle2.isEmpty()) {
                    drawLine.circleLineMap.remove(circle2);
                }
            }
        }
    
    }
}