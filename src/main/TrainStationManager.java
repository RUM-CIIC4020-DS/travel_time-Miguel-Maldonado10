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
			
			findShortestDistance();

		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error reading files: " + e.getMessage());
		}
		
	}
	
	private void findShortestDistance() {
		Station Home = new Station("Westside", 0);
		Stack<Station> toVisit = new LinkedStack<>();
		Set<Station> Visited = new HashSet<>();

		toVisit.push(Home);
		shortestDistanceMap.put(Home.getCityName(), Home);

		while(!toVisit.isEmpty()){
			Station currStation = toVisit.pop();
			Visited.add(currStation);

			if(neighborsMap.get(currStation.getCityName()) != null){
                for(Station neighbor : neighborsMap.get(currStation.getCityName())){
                    if(neighborsMap.get(neighbor.getCityName()) != null){
						int distance = 0;
                        int A = Integer.MAX_VALUE;
						int B = Integer.MAX_VALUE;
						int C = neighbor.getDistance();

						for(Station AStation : neighborsMap.get(neighbor.getCityName())){
							if(A>AStation.getDistance()){
								A = AStation.getDistance();
							}
						}

						for(Station BStation : neighborsMap.get(currStation.getCityName())){
							if(B>BStation.getDistance()){
								B = BStation.getDistance();
							}
						}
						distance = A;
						System.out.print("A = " + A + "\n");
						System.out.print("B = " + B + "\n");
						System.out.print("C = " + C + "\n");
						System.out.print("------------------- \n");
						if(A > B + C){
							distance = B+C;
						}

						shortestDistanceMap.put(currStation.getCityName(), new Station(neighbor.getCityName(), distance));

						if(!Visited.isMember(neighbor)){
							toVisit.push(neighbor);
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
		return null;
		// 5 minutes per kilometer
		// 15 min per station
		
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
		throw new UnsupportedOperationException();
	}

}