package main;

/**
 * The Station class represents a station with a city name and its distance from another location
 * 
 * @author Miguel Maldonado Maldonado
 */
public class Station {
	private String city;
	private int cityDistance;
	

	/**
     * Constructs a new Station object with the given city name and distance
     *
     * @param name the name of the city
     * @param dist the distance of the city from another location
     */
	public Station(String name, int dist) {
		city = name;
		cityDistance = dist;
	}

	/**
     * Gets the name of the city where the station is located
     *
     * @return String the city name
     */
	
	public String getCityName() {
		return city;
		
	}
	
	/** 
	 * Sets the name of the city where the station is located
	 * 
	 * @param cityName the new city name we're setting
	 */
	public void setCityName(String cityName) {
		city = cityName;
	}
	
	/** 
	 * Gets the distance of the city from another location
	 * 
	 * @return int the city distance 
	 */
	public int getDistance() {
		return cityDistance;
	}

	
	/** 
	 * Sets the distance of the city from another location
	 * 
	 * @param distance the new distance to set
	 */
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
