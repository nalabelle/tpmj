package edu.mit.csail.tpmj.util;

import java.io.Serializable;

@SuppressWarnings("serial")
public interface ByteArrayableWrapperInterface<T extends ByteArrayable>
    extends Serializable
{
    public T getObject();
}
