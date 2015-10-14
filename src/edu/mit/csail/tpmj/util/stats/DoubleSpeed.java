/* 
 * From code made public-domain 
 * by Luis F. G. Sarmenta 
 * (2005)
 */
package edu.mit.csail.tpmj.util.stats;

/**
 * This class is storing progress and time in
 * one object, which can be used as a return value of
 * DoubleSpeedMeter.  Note that the methods are not
 * synchronized by default, so users need to take
 * of that explicitly if needed. 
 * 
 * @author lfgs
 */
public class DoubleSpeed implements java.io.Serializable
{
    private double progress;
    private long time;

    /**
     * 
     */
    public DoubleSpeed()
    {
    }

    public DoubleSpeed( double progress, long time )
    {
        this.progress = progress;
        this.time = time;
    }

    /**
     * Sets the progress and the time.  Warning: this
     * is not thread-safe.
     * 
     * @param progress
     * @param time
     */
    public void set( double progress, long time )
    {
        this.progress = progress;
        this.time = time;
    }

    public double getProgress()
    {
        return this.progress;
    }

    public long getTime()
    {
        return this.time;
    }

    /**
     * Gets speed as progress/time.  Warning: this is not
     * thread-safe.
     * 
     * @return
     */
    public double getSpeed()
    {
        return this.progress / this.time;
    }

    public static String to2DecPlaces( double d )
    {
        // get integer part
        long l = (long) d;
        long l2 = (long) (Math.round( (d - l) * 100 ));
        return l + "." + l2;
    }
}