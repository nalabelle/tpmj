package edu.mit.csail.tpmj.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ByteArrayableMarshaller<T extends ByteArrayable>
{
    public byte[] marshal( T byteArrayable )
    {
        byte[] schedClass = byteArrayable.getClass().getCanonicalName().getBytes();
        //        System.out.println( "[ByteArrayableMarshaller.marshal()] - ("
        //            + schedClass.length + ", " + schedClass + ", "
        //            + byteArrayable.toBytes().length + ")" );

        return ByteArrayUtil.buildBuf( schedClass.length, schedClass,
            byteArrayable.toBytes() );
    }

    @SuppressWarnings("unchecked")
    public T unmarshal( ByteArrayReadWriter brw )
        throws ClassNotFoundException, NoSuchMethodException,
        InstantiationException, InvocationTargetException,
        IllegalAccessException
    {
        byte[] classBytes = brw.readSizedByteArray();
        String className = new String( classBytes );

        //        System.out.println( "[ByteArrayableMarshaller.unmarshal()] - Recreating: "
        //            + className );

        Class<T> schedClass = (Class<T>) Class.forName( className );
        Constructor<T> c = schedClass.getConstructor( (Class[]) null );
        T t = (T) c.newInstance( (Object[]) null );
        brw.readStruct( t );
        return t;
    }

}
