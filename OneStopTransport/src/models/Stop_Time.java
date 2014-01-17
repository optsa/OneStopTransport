package models;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Stop_Time {
	private int stop_time_id=-1;
	private int trip_id=-1;
	private String arrival_time;
	private String departure_time;
	private int stop_id=-1;
	private int stop_sequence=-1;
	private String stop_headsign;
	private int pickup_type=-1;
	private int drop_off_type=-1;
	private String shape_dist_traveled;
	
	public String toString(){
		String temp = trip_id + "," + arrival_time + "," + departure_time + "," + stop_id + "," + stop_sequence + "," + 
				stop_headsign + "," + pickup_type + "," + drop_off_type + "," + shape_dist_traveled;
		return temp.replace("null", "").replace("-1", "");
	}
	
	public int getTrip_id() {
		return trip_id;
	}
	public void setTrip_id(int trip_id) {
		this.trip_id = trip_id;
	}
	public String getArrival_time() {
		return arrival_time;
	}
	public void setArrival_time(String arrival_time) {
		this.arrival_time = arrival_time;
	}
	public String getDeparture_time() {
		return departure_time;
	}
	public void setDeparture_time(String departure_time) {
		this.departure_time = departure_time;
	}
	public int getStop_id() {
		return stop_id;
	}
	public void setStop_id(int stop_id) {
		this.stop_id = stop_id;
	}
	public int getStop_sequence() {
		return stop_sequence;
	}
	public void setStop_sequence(int stop_sequence) {
		this.stop_sequence = stop_sequence;
	}
	public String getStop_headsign() {
		return stop_headsign;
	}
	public void setStop_headsign(String stop_headsign) {
		this.stop_headsign = stop_headsign;
	}
	public int getPickup_type() {
		return pickup_type;
	}
	public void setPickup_type(int pickup_type) {
		this.pickup_type = pickup_type;
	}
	public int getDrop_off_type() {
		return drop_off_type;
	}
	public void setDrop_off_type(int drop_off_type) {
		this.drop_off_type = drop_off_type;
	}
	public String getShape_dist_traveled() {
		return shape_dist_traveled;
	}
	public void setShape_dist_traveled(String shape_dist_traveled) {
		this.shape_dist_traveled = shape_dist_traveled;
	}

	public int getStop_time_id() {
		return stop_time_id;
	}

	public void setStop_time_id(int stop_time_id) {
		this.stop_time_id = stop_time_id;
	}
}
