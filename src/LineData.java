import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

class LineData {
    private Line line;
    private Circle startCircle;
    private Circle endCircle;
    private Label slopeLabel;
    private Rectangle midPoint;
   

    public LineData(Line line, Circle startCircle, Circle endCircle, Label slopeLabel, Rectangle midPoint) {
        this.line = line;
        this.startCircle = startCircle;
        this.endCircle = endCircle;
        this.slopeLabel = slopeLabel;
        this.midPoint = midPoint;
    }

    public Line getLine() {
        return line;
    }

    public Circle getStartCircle() {
        return startCircle;
    }

    public Circle getEndCircle() {
        return endCircle;
    }

    public Label getSlopeLabel() {
        return slopeLabel;
    }

    public Rectangle getMidPoint() {
        return midPoint;
    }

   
}