package models;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Trip {
	private int route_id=-1;
	private int service_id=-1;
	private int trip_id=-1;
	private String trip_headsign;
	private String trip_short_name;
	private int direction_id=-1;
	private int block_id=-1;
	private int shape_id=-1;
	private int wheelchair_accessible=-1;
	
	public String toString(){
		String temp = route_id + "," + service_id + "," + trip_id + "," + trip_headsign + "," + trip_short_name + "," +
				direction_id + "," + block_id + "," + shape_id + "," + wheelchair_accessible;
		return temp.replace("null", "").replace("-1", "");
	}
	
	public int getRoute_id() {
		return route_id;
	}
	public void setRoute_id(int route_id) {
		this.route_id = route_id;
	}
	public int getService_id() {
		return service_id;
	}
	public void setService_id(int service_id) {
		this.service_id = service_id;
	}
	public int getTrip_id() {
		return trip_id;
	}
	public void setTrip_id(int trip_id) {
		this.trip_id = trip_id;
	}
	public String getTrip_headsign() {
		return trip_headsign;
	}
	public void setTrip_headsign(String trip_headsign) {
		this.trip_headsign = trip_headsign;
	}
	public String getTrip_short_name() {
		return trip_short_name;
	}
	public void setTrip_short_name(String trip_short_name) {
		this.trip_short_name = trip_short_name;
	}
	public int getDirection_id() {
		return direction_id;
	}
	public void setDirection_id(int direction_id) {
		this.direction_id = direction_id;
	}
	public int getBlock_id() {
		return block_id;
	}
	public void setBlock_id(int block_id) {
		this.block_id = block_id;
	}
	public int getShape_id() {
		return shape_id;
	}
	public void setShape_id(int shape_id) {
		this.shape_id = shape_id;
	}
	public int getWheelchair_accessible() {
		return wheelchair_accessible;
	}
	public void setWheelchair_accessible(int wheelchair_accessible) {
		this.wheelchair_accessible = wheelchair_accessible;
	}
}
