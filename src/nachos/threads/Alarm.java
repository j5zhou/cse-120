package nachos.threads;

import java.util.ArrayList;
import java.util.List;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Alarmlock = new Lock();
		SleepList = new ArrayList<SleepThread>();
		
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {

				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		//KThread.currentThread().yield();
		
		
		int index=-1;
		for(int i=0;i<SleepList.size();i++) {
			SleepThread tmp = SleepList.get(i);
			if(Machine.timer().getTime() >= tmp.getTime()) {
				index = i;
				//System.out.println("put thread to ready	");
				tmp.getThread().ready();
				break;
			}
		}
		if(index > -1) {
			//System.out.println("remove from list");
			SleepList.remove(index);
		}
		
		
	}
	

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		
		// for now, cheat just to get something working (busy waiting is bad)
		/*
		long wakeTime = Machine.timer().getTime() + x;
		while (wakeTime > Machine.timer().getTime())
			KThread.yield();
		*/
		
		long wakeTime = Machine.timer().getTime() + x;
		//System.out.printf("Wake time %d \n",wakeTime);
		if(x >0) {
			Alarmlock.acquire();
			boolean currentState = Machine.interrupt().disable();
			SleepThread current_sleep_thread = new SleepThread(wakeTime,KThread.currentThread());
			SleepList.add(current_sleep_thread);
			KThread.currentThread().sleep();
			Machine.interrupt().restore(currentState);
			Alarmlock.release();
		}
		
		
	}

        /**
	 * Cancel any timer set by <i>thread</i>, effectively waking
	 * up the thread immediately (placing it in the scheduler
	 * ready set) and returning true.  If <i>thread</i> has no
	 * timer set, return false.
	 * 
	 * <p>
	 * @param thread the thread whose timer should be cancelled.
	 */
        public boolean cancel(KThread thread) {
		return false;
	}
        
    // Add Alarm testing code to the Alarm class
    
    public static void alarmTest1() {
	int durations[] = {1000, 10*1000, 100*1000};
	long t0, t1;

	for (int d : durations) {
	    t0 = Machine.timer().getTime();
	    ThreadedKernel.alarm.waitUntil (d);
	    t1 = Machine.timer().getTime();
	    System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
	}
    }
    
    //test case for negative and 0
    public static void alarmTest2() {
    	int durations[] = {-89, 0, 3000};
    	long t0, t1;

	    	for (int d : durations) {
	    	    t0 = Machine.timer().getTime();
	    	    ThreadedKernel.alarm.waitUntil (d);
	    	    t1 = Machine.timer().getTime();
	    	    System.out.println ("alarmTest2: waited for " + (t1 - t0) + " ticks");
	    	}
        }

    // Implement more test methods here ...

    // Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
    public static void selfTest() {
	alarmTest1();
	alarmTest2();
	// Invoke your other test methods here ...
    }
    
    private List<SleepThread> SleepList;
    private Lock Alarmlock;
    
    public class SleepThread{
    	private long sleeptime;
    	private KThread target;
    	public SleepThread(long sleeptime, KThread target ) {
    		this.sleeptime = sleeptime;
    		this.target = target;
    	}
    	public long getTime() {
    		return this.sleeptime;
    	}
    	public KThread getThread(){
    		return this.target;
    	}
    	
    }
}
