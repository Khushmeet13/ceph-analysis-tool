import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class LineSegment {
    private Line line;
    private Circle startCircle;
    private Circle endCircle;

    public LineSegment(Line line, Circle startCircle, Circle endCircle) {
        this.line = line;
        this.startCircle = startCircle;
        this.endCircle = endCircle;
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

    public void setStartCircle(Circle startCircle) {
        this.startCircle = startCircle;
    }

    public void setEndCircle(Circle endCircle) {
        this.endCircle = endCircle;
    }
}
