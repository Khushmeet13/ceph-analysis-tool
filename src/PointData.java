import javafx.geometry.Point2D;

public class PointData {

	//point data
	public Point2D pointData;

	//label text
	public String pointLabel;
	
	
	public PointData(Point2D point, String pointlabel) {
		this.pointData = point; 
		this.pointLabel = pointlabel;
	}

	public Point2D getPointData() {
		return pointData;
	}

	public void setPointData(Point2D pointData) {
		this.pointData = pointData;
	}

	public String getPointLabel() {
		return pointLabel;
	}

	public void setPointLabel(String pointLabel) {
		this.pointLabel = pointLabel;
	}


}

