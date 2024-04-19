package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import data_structures.ArrayList;
import data_structures.ArrayListStack;
import data_structures.HashSet;
import data_structures.HashTableSC;
import data_structures.LinkedListStack;
import data_structures.LinkedStack;
import data_structures.SimpleHashFunction;
import data_structures.SinglyLinkedList;
import interfaces.HashFunction;
import interfaces.List;
import interfaces.Map;
import interfaces.Set;
import interfaces.Stack;

public class TrainStationManager {
	HashFunction simple = new SimpleHashFunction<>();
	Map<String, List<Station>> neighborsMap = new HashTableSC<String, List<Station>>(1, simple);
	Map<String, Station> shortestDistanceMap = new HashTableSC<String, Station>(1, simple);
	List<String>stationNames = new SinglyLinkedList<>();
	Stack<Station>stationStack = new LinkedListStack<>();
	Set<Station> stationSet = new HashSet<>();
	Station Home = new Station(null, 0);
	Map<String, Stack<String>> trajectories = new HashTableSC <String, Stack<String>>(1, simple);

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
		
	}
	
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
	public Map<String, Double> getTravelTimes() {
		Map<String, Double> travelTime = new HashTableSC(1, simple);
		for (String cityName : neighborsMap.getKeys()) {
			Station currentStation = shortestDistanceMap.get(cityName);
			int distance = currentStation.getDistance();
			
			if (currentStation.getCityName().equals("Westside")) {
				travelTime.put(cityName, distance * 2.5);
			} else {
				Stack<String> stopStack = new LinkedListStack<>();
				int stops = calculateStopsToWestside(currentStation, stopStack) - 1;
				trajectories.put(cityName, stopStack);
				double totalTime = distance * 2.5 + stops * 15; // 5 minutes per kilometer, 15 minutes per station
				travelTime.put(cityName, totalTime);
			}
		}
		return travelTime;
	}
	
	private int calculateStopsToWestside(Station currentStation, Stack<String> stops) {

		if(currentStation == null){
			return 0; // para evitar null pointer exception
		}

		stops.push(currentStation.getCityName());
		
		if (currentStation.getCityName().equals("Westside")) {
			return 0; // llego
		} else {
			int max =Integer.MAX_VALUE;
			Station nextStation = new Station(null, max);
			for(Station n : neighborsMap.get(currentStation.getCityName())){
				if(shortestDistanceMap.get(n.getCityName()).getDistance()<max){
					nextStation = n;
					max = shortestDistanceMap.get(n.getCityName()).getDistance();
				}
			} //iteramos por neighborsMap para entonces verificar el shortest distance map
			if(nextStation==null || nextStation.equals(currentStation)){
				return 0;
			} // si no lo encontramos o me da lo mismo a currentStation evitamos que afecte la sumatoria
			return 1 + calculateStopsToWestside(nextStation, stops); // recursion :D
		}
	}
	


	public Map<String, List<Station>> getStations() {
		return neighborsMap;
		
	}


	public void setStations(Map<String, List<Station>> cities) {
		neighborsMap = cities;
	}


	public Map<String, Station> getShortestRoutes() {
		return shortestDistanceMap;
	}


	public void setShortestRoutes(Map<String, Station> shortestRoutes) {
		shortestDistanceMap = shortestRoutes;
	}

	private Stack<String> getTrajectoryStack(String currentStation, Stack<String>stops){

		if(currentStation == null){
			return stops; 
		}

		System.out.print("adding: " + currentStation + "\n");

		stops.push(currentStation);
		
		if (currentStation.equals("Westside")) {
			return stops; // llego
		} else {
			int max =Integer.MAX_VALUE;
			String nextStation = "";
			System.out.print("veryfying: \n");
			for(Station n : neighborsMap.get(currentStation)){
				System.out.print(n.getCityName() + " " + shortestDistanceMap.get(n.getCityName()).getDistance() + "\n");
				if(shortestDistanceMap.get(n.getCityName()).getDistance() < max){
					nextStation = n.getCityName();
					max = shortestDistanceMap.get(n.getCityName()).getDistance();
				}
			} //iteramos por neighborsMap para entonces verificar el shortest distance map
			if(nextStation==null || nextStation.equals(currentStation)){
				return stops;
			} // si no lo encontramos o me da lo mismo a currentStation evitamos que afecte la sumatoria
			System.out.print("passing: " + nextStation + "\n");
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

		System.out.print(result + "\n");
		System.out.print("------------- \n" );

		return result;
	}

}