package main;

public class Station {
	private String city;
	private int cityDistance;
	
	public Station(String name, int dist) {
		city = name;
		cityDistance = dist;
	}
	
	public String getCityName() {
		return city;
		
	}
	public void setCityName(String cityName) {
		city = cityName;
	}
	public int getDistance() {
		return cityDistance;
	}
	public void setDistance(int distance) {
		cityDistance = distance;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		return this.getCityName().equals(other.getCityName()) && this.getDistance() == other.getDistance();
	}
	@Override
	public String toString() {
		return "(" + this.getCityName() + ", " + this.getDistance() + ")";
	}

}
