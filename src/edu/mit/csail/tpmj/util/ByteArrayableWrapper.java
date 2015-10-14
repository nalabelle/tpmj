package edu.mit.csail.tpmj.util;


/**
 * The ByteArrayableWrapper class is a utility class for transporting
 * non-Serializable ByteArrayable objects across a Serialized connection. The
 * type of the ByteArrayable object being transported must be stated in the
 * constructor of this class. The getObject() method will automatically cast the
 * stored object back to the correct class.
 * 
 * Classes of objects that are transported using ByteArrayableWrapper must
 * expose an empty constructor.
 * 
 * @author Jonathan Rhodes
 * 
 * @param <T> -
 *            The class of the ByteArrayable object that is being transported.
 *            The class must extend ByteArrayable.
 */
@SuppressWarnings("serial")
public class ByteArrayableWrapper<T extends ByteArrayable>
    implements ByteArrayable, ByteArrayableWrapperInterface<T>
{

    // obj.toBytes()
    private byte[] data;

    // Class instances cannot be initialized from generic information, so we
    // must keep a copy of the object's class information around.
    private Class classType;

    private static final int NO_DATA = -1;

    /**
     * Generic blank constructor typical of ByteArrayable classes.
     */
    public ByteArrayableWrapper()
    {
        // Generic blank constructor
    }

    /**
     * Constructor from the byte array representation of the
     * ByteArrayableTransport object.
     * 
     * @param source -
     *            A byte[] containing the ByteArrayableTransport object.
     * @param offset -
     *            The offset in the array that the ByteArrayableTransport object
     *            starts.
     */
    public ByteArrayableWrapper( byte[] source, int offset )
    {
        this.fromBytes( source, offset );
    }

    /**
     * Main constructor of ByteArrayableTransport. Stores any ByteArrayable so
     * it can be transported as a Serialized object.
     * 
     * @param byteArrayable -
     *            The ByteArrayable object of type T to be transported.
     */
    public ByteArrayableWrapper( T byteArrayable )
    {
        if ( byteArrayable == null )
        {
            data = null;
            classType = null;
        }
        else
        {
            this.data = byteArrayable.toBytes();
            classType = byteArrayable.getClass();
        }
    }

    /**
     * This method returns the object that was stored, cast as the type
     * specified by the generic at construction-time.
     * 
     * @return - The stored ByteArrayable object
     */
    public T getObject()
    {
        // Case: return a null object if that is what we were passed in the first place.
        if ( data == null )
        {
            return null;
        }

        ByteArrayReadWriter brw = new ByteArrayReadWriter( data, 0 );
        try
        {
            ByteArrayable obj = (ByteArrayable) classType.newInstance();
            brw.readStruct( obj );
            classType.cast( obj );
            return (T) classType.cast( obj );

        }
        catch ( IllegalAccessException e )
        {
            e.printStackTrace();
            return null;
        }
        catch ( InstantiationException e )
        {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * @see ByteArrayable
     */
    public void fromBytes( byte[] source, int offset )
    {
        try
        {
            ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
            int strLen = brw.readInt32();

            // Case: a null object was passed to this wrapper.
            if ( strLen == NO_DATA )
            {
                this.data = null;
                this.classType = null;
            }
            else
            {
                String className = new String( brw.readBytes( strLen ) );
                classType = Class.forName( className );
                int dataLen = brw.readInt32();
                this.data = brw.readBytes( dataLen );
            }
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * @see ByteArrayable
     */
    public byte[] toBytes()
    {
        byte[] classBytes = classType.getCanonicalName().getBytes();

        // Case: A null object was passed to this wrapper.
        if ( data == null )
        {
            return ByteArrayUtil.buildBuf( NO_DATA );
        }
        else
        {
            return ByteArrayUtil.buildBuf( classBytes.length, classBytes,
                data.length, data );
        }
    }

}
