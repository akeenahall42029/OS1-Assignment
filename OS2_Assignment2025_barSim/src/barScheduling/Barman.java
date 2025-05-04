//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Comparator;
import java.util.concurrent.*;

/*
 Barman Thread class.
 */
public class Barman extends Thread {
	

	private CountDownLatch startSignal;
	private BlockingQueue<DrinkOrder> orderQueue;
	int schedAlg =0;
	int q=10000; //really big if not set, so FCFS
	private int switchTime;
	// Akeena H. 2025:
	// variables for Barman scheduling metrics
	// will record CPU Utilization, response time, and throughput
	private long totalDrinkMakingTime = 0; // total time spent preparing drinks
	private long simStartTime; //start of simulation measured in ms --> throughput
	private long simEndTime; // end of simulation measured in ms, used for throughput calculation
	private int completedOrders = 0; // to calculate throughput calculation
	private double cpu_utilization;

	//variables for turnaround time
	private long firstOrderStartTime = -1;
	private long firstOrderSubmissionTime = -1; // might not need
	private long lastDrinkCompletionTime = -1;
	private final Map<Integer, Long> patronArrivalTimes = new ConcurrentHashMap<>();

	// new implementation for recording when the barman starting working on each drink
	private final Map<Integer,Long> serviceStartTimes = new ConcurrentHashMap<>();

	//Helper function to extract the patronID from DrinkOrder
	private int getPatronID(DrinkOrder order){
		String str = order.toString();
		return Integer.parseInt(str.split(":")[0].trim());
	}
	Barman(  CountDownLatch startSignal,int sAlg) {
		//which scheduling algorithm to use
		this.schedAlg=sAlg;
		if (schedAlg==1) this.orderQueue = new PriorityBlockingQueue<>(5000, Comparator.comparingInt(DrinkOrder::getExecutionTime)); //SJF
		else this.orderQueue = new LinkedBlockingQueue<>(); //FCFS & RR
	    this.startSignal=startSignal;
	}
	
	Barman(  CountDownLatch startSignal,int sAlg,int quantum, int sTime) { //overloading constructor for RR which needs q
		this(startSignal, sAlg);
		q=quantum;
		switchTime=sTime;
	}

	public void placeDrinkOrder(DrinkOrder order) throws InterruptedException {
		int patronID = getPatronID(order);
		// record the time of arrival for the first drink (turnaround time)
		long currentTime = System.currentTimeMillis();
		patronArrivalTimes.put(patronID, System.currentTimeMillis()); // record each patron's drink arrival time

		if(firstOrderSubmissionTime == -1){
			firstOrderSubmissionTime = currentTime;
		}
		orderQueue.put(order);
    }
	
	public void run() {
		int interrupts=0;
		simStartTime = System.currentTimeMillis();
		System.out.println("Barman started @ " + simStartTime + " ms");
		try {
			DrinkOrder currentOrder;
			startSignal.countDown(); //barman ready
			startSignal.await(); //check latch - don't start until told to do so

			if ((schedAlg==0)||(schedAlg==1)) { //FCFS and non-preemptive SJF
				while(true) {
					currentOrder=orderQueue.take();
					long startTime = System.currentTimeMillis(); // record when the barman began making a drink
					// get the id of the current patron
					int currPatronID = getPatronID(currentOrder);
					//retrieve the special key of the current patron's drink to record service time
					int key = Patron.drinkToKeyMap.get(currentOrder);
					serviceStartTimes.putIfAbsent(key,startTime);

					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString());
					sleep(currentOrder.getExecutionTime()); //processing order (="CPU burst")

					//Record the CPU utilization (the % of CPU work --> CPU bursts / total sim time)
					totalDrinkMakingTime+= System.currentTimeMillis()-startTime;
					completedOrders++;

					System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
					currentOrder.orderDone();
					// continuosly update the last drink until the last drink is completed
					lastDrinkCompletionTime = System.currentTimeMillis();
					sleep(switchTime);//cost for switching orders
				}
			}
			else { // RR 
				int burst=0;
				int timeLeft=0;
				System.out.println("---Barman started with q= "+q);

				while(true) {
					System.out.println("---Barman waiting for next order ");
					currentOrder=orderQueue.take();
					long startTime = System.currentTimeMillis();
					int key = Patron.drinkToKeyMap.get(currentOrder);
					serviceStartTimes.putIfAbsent(key,startTime);

					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString() );
					burst=currentOrder.getExecutionTime();
					if(burst<=q) { //within the quantum
						sleep(burst); //processing complete order ="CPU burst"
						totalDrinkMakingTime+= System.currentTimeMillis()-startTime;
						completedOrders++;
						System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
						currentOrder.orderDone();
						// continuosly update the last drink until the last drink is completed
						lastDrinkCompletionTime = System.currentTimeMillis();
					}
					else {
						sleep(q);
						totalDrinkMakingTime+= System.currentTimeMillis()-startTime;
						timeLeft=burst-q;
						System.out.println("--INTERRUPT---preparation of drink for patron "+ currentOrder.toString()+ " time left=" + timeLeft);
						interrupts++;
						currentOrder.setRemainingPreparationTime(timeLeft);
						orderQueue.put(currentOrder); //put back on queue at end
					}
					sleep(switchTime);//switching orders
				}
			}
				
		} catch (InterruptedException e1) {
			simEndTime = System.currentTimeMillis();
			System.out.println("Last drink completion time: " + lastDrinkCompletionTime + " ms");
			// CPU Utilization
			long totalSimTime = simEndTime- simStartTime;
			double totalTimeInSecs = totalSimTime/1000.0;
			cpu_utilization =  Math.round(((double)totalDrinkMakingTime/totalSimTime) * 100);
			System.out.println("---Barman is packing up ");
			System.out.println("---number interrupts="+interrupts);
			System.out.println("*** CPU Utilization: " + cpu_utilization + "%");
			System.out.println("*** Completed orders: " + completedOrders);
		}
	}


	public double getCPUUtilization(){
		return cpu_utilization;
	}

	public int getCompletedOrders(){
		return completedOrders;
	}


	//getter method for serviceStartTimes to be used by the patrons to record response and wait time
	public Map<Integer, Long> getServiceStartTimes(){
		return serviceStartTimes;
	}

	public long getSimStartTime(){
		return simStartTime;
	}

	public long getSimEndTime(){
		return simEndTime;
	}

}


