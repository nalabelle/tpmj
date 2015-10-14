/* 
 * From code made public-domain 
 * by Luis F. G. Sarmenta 
 * (2005)
 */
package edu.mit.csail.tpmj.util.stats;

/**
 * This is a synchronized version of DoubleStat.
 * Warning: only methods in DoubleStat that modify state
 * or use two variables at the same time are synchronized.
 * Simple getting accessor methods are not synchronized.
 * If the implementations of these will be changed in DoubleStat,
 * then these methods should be overridden and synchronized
 * in this class as well.
 */
public class SynchDoubleStat extends DoubleStat
{

    //////////////////
    // Constructors //
    //////////////////

    //////////////////////
    // Accessor methods //
    //////////////////////

    public synchronized double getSampleVar()
    {
        return super.getSampleVar();
    }

    /*
     * Variance of the mean <em>estimator</em>.
     * This is equal to getVar() / getN().
     */
    public synchronized double getMeanVar()
    {
        return super.getMeanVar();
    }

    public synchronized double getSampleStdDev()
    {
        return super.getSampleStdDev();
    }

    /*
     * Stddev of the mean <em>estimator</em> (not the
     * same as the mean of the stddev).
     * Specifically, this is Math.sqrt( getMeanVar() ),
     * not getStdDev() / n.
     */
    public synchronized double getMeanStdDev()
    {
        return super.getMeanStdDev();
    }

    //////////////////////////////////////
    // Interface implementation methods //
    //////////////////////////////////////

    public synchronized void reset()
    {
        super.reset();
    }

    // for adding data numbers directly

    /**
     */
    public synchronized void addSample( double d )
    {
        super.addSample( d );
    }

    /**
     */
    public synchronized double computeTotalVar()
    {
        return super.computeTotalVar();
    }

    // for merging DStats

    /**
     * Meant to be used separately from addSample.
     * That is, a single DStat instance should be used either
     * solely for samples, or for DStat's, not both.
     * Although theoretically, we might be able to mix them
     * if we do all the addSamples and computeVarSamples
     * before adding the DStats.
     */
    public synchronized void addDStat( DoubleStat dStat )
    {
        super.addDStat( dStat );
    }

    //////////////////////
    // Internal methods //
    //////////////////////

    protected synchronized double computeMean()
    {
        return super.computeMean();
    }

    ///////////////////////
    // toString() method //
    ///////////////////////

    /**
     * Returns CSV string with Mean, SampleStdDev, N, MeanStdDev,
     * Min, and Max
     */
    public synchronized String toString()
    {
        return super.toString();
    }

}

