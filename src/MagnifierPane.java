import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MagnifierPane extends Pane {
    private ImageView imageView;
    private Circle magnifierFrame;
    private double magnifierRadius;
    private double magnificationFactor;

    public MagnifierPane(double magnificationFactor, double magnifierRadius) {
        this.magnificationFactor = magnificationFactor;
        this.magnifierRadius = magnifierRadius;
        this.imageView = new ImageView();
        this.magnifierFrame = new Circle(magnifierRadius);
        this.magnifierFrame.setFill(Color.TRANSPARENT);
        this.magnifierFrame.setStroke(Color.BLACK);
        this.getChildren().addAll(imageView, magnifierFrame);
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

    public void setMagnificationFactor(double factor) {
        this.magnificationFactor = factor;
    }

    public void setMagnifierRadius(double radius) {
        this.magnifierRadius = radius;
        magnifierFrame.setRadius(radius);
    }

    public void updateMagnifier(double mouseX, double mouseY) {
        // Calculate magnifier frame position based on mouse position
        double frameX = mouseX - magnifierRadius;
        double frameY = mouseY - magnifierRadius;

        // Ensure that the magnifier frame stays within the bounds of the image
        frameX = Math.max(0, Math.min(frameX, imageView.getImage().getWidth() - 2 * magnifierRadius));
        frameY = Math.max(0, Math.min(frameY, imageView.getImage().getHeight() - 2 * magnifierRadius));

        // Update the position of the magnifier frame circle
        magnifierFrame.setCenterX(frameX + magnifierRadius);
        magnifierFrame.setCenterY(frameY + magnifierRadius);

        // Calculate the viewport for the magnified image
        double magnifiedWidth = magnifierRadius * 2 * magnificationFactor;
        double magnifiedHeight = magnifierRadius * 2 * magnificationFactor;

        // Set the viewport of the ImageView to display the magnified portion of the image
        imageView.setViewport(new Rectangle2D(frameX * magnificationFactor, frameY * magnificationFactor, magnifiedWidth, magnifiedHeight));
    }
}