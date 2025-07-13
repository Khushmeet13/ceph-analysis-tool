import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Pair;
import java.util.List;
import java.util.Map;

public class DeleteTriangleAction extends Action {
    private Pane rootPane;
    private Line line1, line2;
    private List<Line> allTriangleLines;
    private Map<Text, Pair<Line, Line>> lineAngleMap;
    private Map<Arc, Text> angleArcMap;
    private Circle circle1, circle2;
    private Label label1, label2;
    private DrawTriangle drawTriangle;
    private List<Text> angleTexts;
    private List<Arc> angleArcs;

    public DeleteTriangleAction(Pane rootPane, Line line1, Line line2, List<Line> allTriangleLines, 
                                Map<Text, Pair<Line, Line>> lineAngleMap, Map<Arc, Text> angleArcMap,
                                DrawTriangle drawTriangle,  List<Text> angleTexts, List<Arc> angleArcs) {
        this.rootPane = rootPane;
        this.line1 = line1;
        this.line2 = line2;
        this.allTriangleLines = allTriangleLines;
        this.lineAngleMap = lineAngleMap;
        this.angleArcMap = angleArcMap;
        this.drawTriangle = drawTriangle;
        this.angleTexts = angleTexts;
        this.angleArcs = angleArcs;
    }

    @Override
    public void undo() {
        rootPane.getChildren().addAll(line1, line2);
        allTriangleLines.add(line1);
        allTriangleLines.add(line2);

        // Add back angle texts
        for (Text angleText : angleTexts) {
            if (!rootPane.getChildren().contains(angleText)) {
                rootPane.getChildren().add(angleText);
            }
        }

        // Add back angle arcs
        for (Arc angleArc : angleArcs) {
            if (!rootPane.getChildren().contains(angleArc)) {
                rootPane.getChildren().add(angleArc);
            }
        }
        // Add back angle texts
        /*for (Map.Entry<Text, Pair<Line, Line>> entry : lineAngleMap.entrySet()) {
            if ((entry.getValue().getKey() == line1 || entry.getValue().getKey() == line2 ||
                 entry.getValue().getValue() == line1 || entry.getValue().getValue() == line2) &&
                !rootPane.getChildren().contains(entry.getKey())) {
                rootPane.getChildren().add(entry.getKey());
            }
        }

        // Add back angle arcs
        for (Map.Entry<Arc, Text> entry : angleArcMap.entrySet()) {
            Text associatedText = entry.getValue();
            if (!rootPane.getChildren().contains(entry.getKey()) &&
                lineAngleMap.containsKey(associatedText)) {
                rootPane.getChildren().add(entry.getKey());
            }
        }*/

        // Add circles and their labels back if they were removed
        if (circle1 != null && !rootPane.getChildren().contains(circle1)) {
            rootPane.getChildren().add(circle1);
            drawTriangle.circleList.add(circle1);
            if (label1 != null) {
                rootPane.getChildren().add(label1);
                drawTriangle.labelList.add(label1);
            }
        }

        if (circle2 != null && !rootPane.getChildren().contains(circle2)) {
            rootPane.getChildren().add(circle2);
            drawTriangle.circleList.add(circle2);
            if (label2 != null) {
                rootPane.getChildren().add(label2);
                drawTriangle.labelList.add(label2);
            }
        }
    }

    @Override
    public void redo() {
        rootPane.getChildren().removeAll(line1, line2);
        allTriangleLines.remove(line1);
        allTriangleLines.remove(line2);

        // Remove angle texts
        /*for (Map.Entry<Text, Pair<Line, Line>> entry : lineAngleMap.entrySet()) {
            if (entry.getValue().getKey() == line1 || entry.getValue().getKey() == line2 ||
                entry.getValue().getValue() == line1 || entry.getValue().getValue() == line2) {
                rootPane.getChildren().remove(entry.getKey());
            }
        }

        // Remove angle arcs
        for (Map.Entry<Arc, Text> entry : angleArcMap.entrySet()) {
            Text associatedText = entry.getValue();
            if (!lineAngleMap.containsKey(associatedText)) {
                rootPane.getChildren().remove(entry.getKey());
            }
        }*/

        for (Text angleText : angleTexts) {
            rootPane.getChildren().remove(angleText);
        }

        // Remove angle arcs
        for (Arc angleArc : angleArcs) {
            rootPane.getChildren().remove(angleArc);
        }

        // Remove circles and their labels if not used by other triangles
        if (circle1 != null) {
            drawTriangle.deleteCircleWithoutContextMenu(circle1);
        }

        if (circle2 != null) {
            drawTriangle.deleteCircleWithoutContextMenu(circle2);
        }
    }
}