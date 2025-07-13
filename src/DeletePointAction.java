import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class DeletePointAction extends Action {
    private Pane rootPane;
    private Circle circle;
    private Label circleLabel;
    private DrawPoint drawPoint;

    public DeletePointAction(Pane rootPane, Circle circle, Label circleLabel, DrawPoint drawPoint) {
        this.rootPane = rootPane;
        this.circle = circle;
        this.circleLabel = circleLabel;
        this.drawPoint = drawPoint;
    }

    @Override
    public void undo() {
        rootPane.getChildren().addAll(circle, circleLabel);
        drawPoint.circleList.add(circle);
        drawPoint.labelList.add(circleLabel);
        // You may need to update other data structures or properties in DrawPoint
    }

    @Override
    public void redo() {
        rootPane.getChildren().removeAll(circle, circleLabel);
        drawPoint.circleList.remove(circle);
        drawPoint.labelList.remove(circleLabel);
        // You may need to update other data structures or properties in DrawPoint
    }

    public void execute() {
        rootPane.getChildren().removeAll(circle, circleLabel);
        drawPoint.circleList.remove(circle);
        drawPoint.labelList.remove(circleLabel);
        // You may need to update other data structures or properties in DrawPoint
    }
}
