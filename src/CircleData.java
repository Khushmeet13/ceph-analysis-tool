public class CircleData {
    private double x;
    private double y;
    private String labelText;
    

    
    public CircleData(double x, double y, String labelText) {
        this.x = x;
        this.y = y;
        this.labelText = labelText;
      
    }

    
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }
}

