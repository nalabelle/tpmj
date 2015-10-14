/* 
 * From code made public-domain 
 * by Luis F. G. Sarmenta 
 * (1996-2005)
 */
package edu.mit.csail.tpmj.util.stats;

public class Stopwatch implements java.io.Serializable // implements bayanihan.Serializable
{
    protected boolean running = false;
    protected long starttime = 0;
    protected long stoptime = 0;
    protected long basetime = 0; // used for pausing

    /**
     * Returns true if the Timer has been started,
     * and is not currently stopped or paused.
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * Starts a stopped Timer, or restarts a paused Timer
     * (sets starttime).
     */
    public void start()
    {
        if ( !running )
        {
            starttime = System.currentTimeMillis();
            running = true;
        }
    }

    /**
     * Stops a running timer sets stoptime.
     */
    public void stop()
    {
        if ( running )
        {
            stoptime = System.currentTimeMillis();
            running = false;
        }
    }

    /**
     * Stops timer, and remembers running time so far;
     * to restart, call start; getTime() will
     * return time since last reset(), not including
     * paused periods.
     */
    public void pause()
    {
        if ( running )
        {
            this.stop();
            basetime = this.getTime();
            // NOTE: Need to move starttime again so 
            // if you call getTime() on a paused Stopwatch
            // before starting it again, it
            // doesn't double count basetime
            this.starttime = this.stoptime;
        }
    }

    /**
     * Marks the present time, and 
     * returns the current running time
     * without stopping the timer.
     */
    public long mark()
    {
        if ( running )
        {
            stoptime = System.currentTimeMillis();
        }
        return this.getMarkedTime();
    }

    /**
     * Returns time elapsed from starttime to the last time
     * the clock was marked or stopped, 
     * not including pauses.
     * 
     * @return
     */
    public long getMarkedTime()
    {
        return (stoptime - starttime) + basetime;
    }

    /**
     * Stops and resets the basetime stored from previous pauses, 
     * so that the next call to start() would start the timer from 0 time.
     */
    public void reset()
    {
        running = false;
        starttime = 0;
        stoptime = 0;
        basetime = 0;
    }

    /**
     * Returns time from start time to the stop time (or the current 
     * time, if the clock is still running), not including pauses.
     */
    public long getTime()
    {
        // If it's running, do NOT use stoptime to read the time

        long curTime = running ? System.currentTimeMillis() : stoptime;
        return (curTime - starttime) + basetime;
    }

    /**
     * Returns the System time when this Timer
     * was last started.
     */
    public long getAbsStart()
    {
        return starttime;
    }

    /**
     * Returns the System time when this Timer was
     * last stopped, marked, or paused.
     */
    public long getAbsStop()
    {
        return stoptime;
    }

    /**
     * Returns total time on Timer before the last pause.
     * This is added to the time different of the
     * next start-stop/mark pair to get the total running time.
     */
    public long getBaseTime()
    {
        return basetime;
    }

    /**
     * Returns the current System time.
     */
    public static long getAbsCurTime()
    {
        return (System.currentTimeMillis());
    }

    // For testing //

    public static void main( String args[] )
    {
        Stopwatch t = new Stopwatch();

        System.out.println( "Testing Timer class ... " );

        t.start();
        System.out.println( "Timer started: " + t.getAbsStart() + " ms" );

        for ( int i = 0; i < 10; i++ )
        {
            Stopwatch t2 = new Stopwatch();

            t.start(); // This should NOT have an effect on the time.
            t2.start();
            System.out.println( "Timer 2 started." );
            for ( int j = 0; j < 100000; j++ )
                t.mark();  // just doing this to cause a delay
            System.out.println( "Timer 1 at: " + t.mark() + " ms" );
            t2.stop();
            System.out.println( "Timer 2 at: " + t2.getTime() + " ms" );
            t2.reset();
            System.out.println( "Timer 2 reset." );
        }
        t.stop();
        System.out.println( "Timer stopped: " + t.getAbsStop() + " ms" );
        System.out.println( "Total Running Time: " + t.getTime() + " ms" );
    }
}

