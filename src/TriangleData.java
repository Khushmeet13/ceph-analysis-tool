import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.List;

class TriangleData {
    private Line line1;
    private Line line2;
    private List<Text> angleTexts;
    private List<Arc> angleArcs;
    private Circle circle1;
    private Circle circle2;
    private Circle circle3;
    private Label label1;
    private Label label2;
    private Label label3;

    public TriangleData(Line line1, Line line2, List<Text> angleTexts, List<Arc> angleArcs, Circle circle1, Circle circle2, Circle circle3,
                        Label label1, Label label2, Label label3) {
        this.line1 = line1;
        this.line2 = line2;     
        this.angleTexts = angleTexts;
        this.angleArcs = angleArcs;
        this.circle1 = circle1;
        this.circle2 = circle2;
        this.circle3 = circle3;
        this.label1 = label1;
        this.label2 = label2;
        this.label3 = label3;
    }

    public Line getLine1() {
        return line1;
    }

    public Line getLine2() {
        return line2;
    }


    public List<Text> getAngleTexts() {
        return angleTexts;
    }

    public List<Arc> getAngleArcs() {
        return angleArcs;
    }

    public Circle getCircle1() {
        return circle1;
    }

    public Circle getCircle2() {
        return circle2;
    }

    public Circle getCircle3() {
        return circle3;
    }

    public Label getLabel1() {
        return label1;
    }

    public Label getLabel2() {
        return label2;
    }

    public Label getLabel3() {
        return label3;
    }
}
