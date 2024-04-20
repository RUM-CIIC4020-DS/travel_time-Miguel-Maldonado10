package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import data_structures.ArrayList;
import data_structures.ArrayListStack;
import data_structures.HashSet;
import data_structures.HashTableSC;
import data_structures.LinkedListStack;
import data_structures.LinkedStack;
import data_structures.SimpleHashFunction;
import interfaces.HashFunction;
import interfaces.List;
import interfaces.Map;
import interfaces.Set;
import interfaces.Stack;

/**
 * This class manages all the train stations
 * 
 * @author Miguel Maldonado Maldonado
 */
public class TrainStationManager {
	HashFunction simple = new SimpleHashFunction<>();
	/**
	 * neighborsMap - map of neighbors with it's key is a station's name and the value a list of said city's neighbors
	 * 
	 * shortestDistanceMap - map of the shortest distance with it's key being a station name and it's value is a station with  
	 * 	it's name being a neighbor we have to go through to get to Westside and the distance to Westside
	 * 
	 * stationStack - sorted stack with all the stations ordered in order of the distance
	 * 
	 * stationSet - set with all the stations visited
	 * 
	 * trajectories - map with the key being a station's name and the value being a Stack of the stops we made on our way to "Westside"
	 */ 
	Map<String, List<Station>> neighborsMap = new HashTableSC<String, List<Station>>(1, simple);
	Map<String, Station> shortestDistanceMap = new HashTableSC<String, Station>(1, simple);
	Stack<Station>stationStack = new LinkedListStack<>();
	Set<Station> stationSet = new HashSet<>();
	Map<String, Stack<String>> trajectories = new HashTableSC <String, Stack<String>>(1, simple);


	/**
	 * Reads data from a file to initialize a train station network
 	 * It sets up various data structures to manage station information and calculates the shortest distances between stations
	 *
	 * @param station_file given file with all the data to process
	 */
	public TrainStationManager(String station_file){
		try (BufferedReader stationReader = new BufferedReader(new FileReader("inputFiles/" + station_file))) {
			String s;
			boolean headerRun = true;
			
			while((s = stationReader.readLine()) != null){
				String source = null;
				String destination = null;
				String distance = null;
				int start = 0;
				for(int i = 0; i<s.length()-1; i++){
					if(!Character.isAlphabetic(s.charAt(i))){
						if(s.charAt(i)== ',' || Character.isDigit(s.charAt(i))){
							if(start==0 && source == null){
								source = s.substring(0,i);
								start = i+1;
							}else if(start!=0 && destination == null){
								destination = s.substring(start,i);
								start = i+1;
							}else if(Character.isDigit(s.charAt(i)) && distance == null){
								distance = s.substring(start, s.length());
							}
						}
					}
				}

				if(headerRun){
					source = destination = distance = null;
					headerRun = false;
				}

				if(distance != null){
					List<Station> tempList1 = neighborsMap.get(source);
					List<Station> tempList2 = neighborsMap.get(destination);
					if(tempList1 == null){
						tempList1 = new ArrayList<>();
					}
					if(tempList2 == null){
						tempList2 = new ArrayList<>();
					}
					Station st1 = new Station(destination, Integer.parseInt(distance));
					Station st2 = new Station(source, Integer.parseInt(distance));
					
					tempList1.add(st1);
					tempList2.add(st2);
					
					neighborsMap.put(source, tempList1);
					neighborsMap.put(destination, tempList2);

					
					if(!stationSet.isMember(new Station(source, 0))){
						stationSet.add(new Station(source, 0));
						sortStack(new Station(source, 0), stationStack);
					}
					if(!stationSet.isMember(new Station(destination, Integer.parseInt(distance)))){
						stationSet.add(new Station(destination, Integer.parseInt(distance)));
						sortStack(new Station(destination, Integer.parseInt(distance)), stationStack);
					}
				}
			}
			for(String lambda : neighborsMap.getKeys()){
				shortestDistanceMap.put(lambda, new Station("Westside", Integer.MAX_VALUE));
			}
			findShortestDistance();

			

		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error reading files: " + e.getMessage());
		}
		//GUI ourGUI = new GUI();

		String[] options = new String[neighborsMap.getKeys().size()-1];
		

		for(int i = 1; i < neighborsMap.getKeys().size(); i++){
			options[i-1]=neighborsMap.getKeys().get(i);
		}
		Map<String, Integer> departureMap = new HashTableSC<>(neighborsMap.getKeys().size() - 1, simple);
		departureMap.put("Bugapest", 575);
		departureMap.put("Dubay", 630);
		departureMap.put("Berlint", 1225);
		departureMap.put("Mosbull", 1080);
		departureMap.put("Cayro", 400);
		departureMap.put("Bostin", 625);
		departureMap.put("Los Angelos", 750);
		departureMap.put("Dome", 810);
		departureMap.put("Takyo", 935);
		departureMap.put("Unstabul", 1005);
		departureMap.put("Chicargo", 445);
		departureMap.put("Loondun", 840);

		Map<String, Integer> arrivalMap = new HashTableSC<>(neighborsMap.getKeys().size() - 1, simple);

		for(String n : departureMap.getKeys()){
			double temp = getTravelTimes().get(n) + departureMap.get(n);
			arrivalMap.put(n,(int)temp);
		}



		/** 
		FOR TA/Professor BENEFIT: Comment GUI below for grading purposes :)
		*/



		int Selection = JOptionPane.showOptionDialog(null, 
		GUImessage(departureMap, arrivalMap) + "\nWhere are you headed?", 
		"Welcome to Westside!", 
		0, 
		JOptionPane.INFORMATION_MESSAGE, 
		null, 
		options, 
		0);

		if(Selection >= 0){
			String selected = neighborsMap.getKeys().get(Selection + 1);
			JOptionPane.showMessageDialog(null, "You are headed to " + selected + "\n Estimated time of Arrival: " + 
			toHour(arrivalMap.get(selected)) + "\n Here's your route: \n" +  traceRoute(selected));
		}



		/* 
		 * Comment GUI above for grading purposes
		 */


		 
	}

	private String toHour(int t){
		int hour = t/60;
		int minute = t%60;
		boolean isAM = true;

		while(hour>12){
			hour-=12;
			isAM = !isAM;
		}

		if(isAM){
			return hour + ":" + minute + "am";
		}

		return hour + ":" + minute + "pm";
	}

	private String GUImessage(Map<String, Integer> Departure, Map<String, Integer> Arrival){
		String message = "Station   |   Departure   |   Arrival \n";
		for(String n : Departure.getKeys()){
			message += n + "   |   " + toHour(Departure.get(n)) + "   |   " + toHour(Arrival.get(n)) + "\n";
		}
		System.out.print(message);
		return message;
	}

	/**
	 * Uses a shortest path algorithm to find the shortest distance from a starting 
	 * station ("Westside") to all other stations in the network.
	 */
	private void findShortestDistance() {
		Station home = new Station("Westside", 0);
		Stack<String> toVisit = new LinkedStack<>();
		Set<String> visited = new HashSet<>();
	
		toVisit.push(home.getCityName());
		shortestDistanceMap.put(home.getCityName(), home);
	
		while (!toVisit.isEmpty()) {
			//System.out.print("Started while loop \n");
			String currentCityName = toVisit.pop();
			visited.add(currentCityName);
			Station currentStation = shortestDistanceMap.get(currentCityName);
	
			if (neighborsMap.containsKey(currentCityName)) {
				//System.out.print("Past first if \n");
				for (Station neighbor : neighborsMap.get(currentCityName)) {
					int distanceToNeighbor = currentStation.getDistance() + neighbor.getDistance();
					if (distanceToNeighbor < shortestDistanceMap.get(neighbor.getCityName()).getDistance()) {
						Station temp = new Station(neighbor.getCityName(), distanceToNeighbor);
						shortestDistanceMap.put(temp.getCityName(), temp);
						if (!visited.isMember(temp.getCityName())) {
							toVisit.push(temp.getCityName());
						}
					}
				}
			}
		}
	}

	
	/** 
	 * Inserts a station into a sorted stack based on its distance 
	 * Used to maintain stations in sorted order for certain operations
	 * 
	 * @param station station we're adding to the stack
	 * @param stackToSort the stack to sort
	 */
	public void sortStack(Station station, Stack<Station> stackToSort) {
		if(stackToSort.isEmpty()){
			stackToSort.push(station);
		}else{
			Stack<Station> temp = new ArrayListStack<>(stackToSort.size());
			while(!stackToSort.isEmpty() && station.getDistance()>stackToSort.top().getDistance()){
				temp.push(stackToSort.pop());
			}
			stackToSort.push(station);
			while(!temp.isEmpty()){
				stackToSort.push(temp.pop());
			}
		}
	}
	
	/** 
	 *  Calculates travel times from each station to "Westside" based on distance and stops
	 * 
	 * @return Map<String, Double> map of station names and their respective travel times
	 */
	public Map<String, Double> getTravelTimes() {
		Map<String, Double> travelTime = new HashTableSC(1, simple);
		for (String cityName : neighborsMap.getKeys()) {
			Station currentStation = shortestDistanceMap.get(cityName);
			int distance = currentStation.getDistance();
			
			if (currentStation.getCityName().equals("Westside")) {
				travelTime.put(cityName, distance * 2.5);
			} else {
				Stack<String> stopStack = new LinkedListStack<>();
				int stops = calculateStopsToWestside(currentStation) - 1;
				trajectories.put(cityName, stopStack);
				double totalTime = distance * 2.5 + stops * 15; // 5 minutes per kilometer, 15 minutes per station
				travelTime.put(cityName, totalTime);
			}
		}
		return travelTime;
	}
	
	
	/** 
	 * Recursive method to calculate the number of stops from a station to "Westside" along the shortest path
	 * 
	 * @param currentStation the current station we're recursing over
	 * @return int how many stops we need to make it to "Westside"
	 */
	private int calculateStopsToWestside(Station currentStation) {

		if(currentStation == null){
			return 0; // to avoid null pointer exception
		}
		
		if (currentStation.getCityName().equals("Westside")) {
			return 0; // we made it :D
		} else {
			int max =Integer.MAX_VALUE;
			Station nextStation = new Station(null, max);
			for(Station n : neighborsMap.get(currentStation.getCityName())){
				int distance = shortestDistanceMap.get(n.getCityName()).getDistance() + n.getDistance();
				//System.out.print(n.getCityName() + " " + distance + "\n");
				if(distance < max){
					nextStation = n;
					max = distance;
				}
			} //we iterate over neighborsMap to check the shortest distance map

			if(nextStation==null || nextStation.equals(currentStation)){
				return 0;
			} 

			// we avoid stackOverflow in case the for loop fails to set a new different station to recurse over
			return 1 + calculateStopsToWestside(nextStation); // recursion :D
		}
	}
	

	/**
	 * Retrieves neighborMap that contains all the stations and their neighboring stations
	 * 
 	 * @return Map<String, List<Station>> map containing all stations and their neighboring stations
 	 */
	public Map<String, List<Station>> getStations() {
		return neighborsMap;
		
	}

	/**
	 * Sets the neighbors map using a given map of stations and their neighboring stations
	 * 
	 * @param cities map of stations and their neighbors to set
	 */
	public void setStations(Map<String, List<Station>> cities) {
		neighborsMap = cities;
	}

	/**
	 * Sets the shortest routes map using a given map of stations and their shortest routes
	 * 
	 * @return map of stations and their shortest routes
	 */
	public Map<String, Station> getShortestRoutes() {
		return shortestDistanceMap;
	}

	/**
	 * Sets the shortest routes map using a given map of stations and their shortest routes
	 * 
	 * @param shortestRoutes given map of stations and their shortest routes to set
	 */
	public void setShortestRoutes(Map<String, Station> shortestRoutes) {
		shortestDistanceMap = shortestRoutes;
	}

	/**
	 * Calculates the trajectory stack for tracing the route from a given station to "Westside" using recursion
	 * 
	 * @param currentStation the iterating station
	 * @param stops the stack of station names on our way to "Westside"
	 * @return Stack<String> stack of stops
	 */
	private Stack<String> getTrajectoryStack(String currentStation, Stack<String>stops){

		if(currentStation == null){
			return stops; 
		}

		//System.out.print("adding: " + currentStation + "\n");

		stops.push(currentStation);
		
		if (currentStation.equals("Westside")) {
			return stops; // llego
		} else {
			int max =Integer.MAX_VALUE;
			String nextStation = "";
			//System.out.print("veryfying: \n");
			for(Station n : neighborsMap.get(currentStation)){
				int distance = shortestDistanceMap.get(n.getCityName()).getDistance() + n.getDistance();
				//System.out.print(n.getCityName() + " " + distance + "\n");
				if(distance < max){
					nextStation = n.getCityName();
					max = distance;
				}
			} //iteramos por neighborsMap para entonces verificar el shortest distance map
			if(nextStation==null || nextStation.equals(currentStation)){
				return stops;
			} // si no lo encontramos o me da lo mismo a currentStation evitamos que afecte la sumatoria
			//System.out.print("passing: " + nextStation + "\n");
			return getTrajectoryStack(nextStation, stops); // recursion :D
		}
	}
	
	/**
	 * BONUS EXERCISE THIS IS OPTIONAL
	 * Returns the path to the station given. 
	 * The format is as follows: Westside->stationA->.....stationZ->stationName
	 * Each station is connected by an arrow and the trace ends at the station given.
	 * 
	 * @param stationName - Name of the station whose route we want to trace
	 * @return (String) String representation of the path taken to reach stationName.
	 */
	public String traceRoute(String stationName) {
		// Remove if you implement the method, otherwise LEAVE ALONE
		//throw new UnsupportedOperationException();
		Stack<String> route = getTrajectoryStack(stationName, new LinkedListStack<String>());

		if(route == null || route.isEmpty()){
			return null;
		}

		String result = route.pop();

		while(!route.isEmpty()){
			result += "->" + route.pop();
		}

		//System.out.print(result + "\n");
		//System.out.print("------------- \n" );

		return result;
	}

}