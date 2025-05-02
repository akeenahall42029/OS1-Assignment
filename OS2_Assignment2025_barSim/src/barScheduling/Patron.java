//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;


import java.util.Random;
import java.util.concurrent.CountDownLatch;

/*class for the patrons at the bar*/

public class Patron extends Thread {
	
	private Random random;// for variation in Patron behaviour
	private CountDownLatch startSignal; //all start at once, actually shared
	private Barman theBarman; //the Barman is actually shared though

	private int ID; //thread ID 
	private int numberOfDrinks;
	private long totalWaitingTime; // calculate the waiting time for all drinks for the patron
	private long [] waitingTimes;
	private long [] orderPlacedTimes; // when each drink was ordered
	private long [] drinkingEndTimes; // when drinking finished
	private long [] serviceStartTimes; // when the barman started making drinks


	private DrinkOrder [] drinksOrder;
	
	Patron( int ID,  CountDownLatch startSignal, Barman aBarman, long seed) {
		this.ID=ID;
		this.startSignal=startSignal;
		this.theBarman=aBarman;
		this.numberOfDrinks=5; // number of drinks is fixed
		// initialize  arrays for calculating waiting times
		this.waitingTimes = new long[numberOfDrinks];
		this.totalWaitingTime=0;
		this.drinkingEndTimes = new long[numberOfDrinks];
		this.serviceStartTimes = new long[numberOfDrinks];
		this.orderPlacedTimes = new long[numberOfDrinks];
		drinksOrder=new DrinkOrder[numberOfDrinks];
		if (seed>0) random = new Random(seed);// for consistent Patron behaviour
		else random = new Random();
	}
	
	
//this is what the threads do	
	public void run() {
		try {
			
			//Do NOT change the block of code below - this is the arrival times
			startSignal.countDown(); //this patron is ready
			startSignal.await(); //wait till everyone is ready
			int arrivalTime = ID*50; //fixed arrival for testing
	        sleep(arrivalTime);// Patrons arrive at staggered  times depending on ID 
			System.out.println("+new thirsty Patron "+ this.ID +" arrived"); //Patron has actually arrived
			//End do not change
			
	        for(int i=0;i<numberOfDrinks;i++) {
	        	//drinksOrder[i]=new DrinkOrder(this.ID); //order a drink (=CPU burst)
	        	drinksOrder[i]=new DrinkOrder(this.ID,i); //fixed drink order (=CPU burst), useful for testing


				System.out.println("Order placed by " + drinksOrder[i].toString()); //output in standard format  - do not change this
				theBarman.placeDrinkOrder(drinksOrder[i]);


				orderPlacedTimes[i]=System.currentTimeMillis(); //record when the patron ordered the drink
				System.out.printf("[%d ms] Patron %d ORDERED drink %d: (prep time: %dms)\n",
						orderPlacedTimes[i], ID, i,
						drinksOrder[i].getExecutionTime());


				System.out.printf("[%d ms] Patron %d WAITING for drink %s\n",
						System.currentTimeMillis(), ID, drinksOrder[i].toString());

				drinksOrder[i].waitForOrder(); //Wait for drink to be ready
				serviceStartTimes[i]=System.currentTimeMillis(); // when the barman began working on the drink order

				System.out.println("Drinking patron " + drinksOrder[i].toString());
				sleep(drinksOrder[i].getImbibingTime()); //drinking drink = "IO"
				drinkingEndTimes[i]=System.currentTimeMillis();

				//calculate the waiting time (time in the ready queue)
				waitingTimes[i] = serviceStartTimes[i] - orderPlacedTimes[i];
				totalWaitingTime += waitingTimes[i];
				System.out.printf("[%d ms] Patron %d  WAIT TIME = %d ms for order drink: %s \n",
						System.currentTimeMillis(), ID, waitingTimes[i], drinksOrder[i].toString()
						);
				System.out.printf("[%d ms] Patron %d STARTED drinking %s \n",
						serviceStartTimes[i], ID, drinksOrder[i].toString());
			}

			System.out.println("Patron "+ this.ID + " completed ");
			System.out.println("*** Total Waiting Time (for all " + numberOfDrinks + " drinks): " + totalWaitingTime+
					" ms");
			
		} catch (InterruptedException e1) {  //do nothing
		}
}
	public long getTotalWaitingTime() {
		return totalWaitingTime;
	}
}
	

