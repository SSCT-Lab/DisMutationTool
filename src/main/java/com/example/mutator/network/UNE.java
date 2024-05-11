package com.example.mutator.network;

import com.example.mutator.MutatorType;
import com.example.mutator.UpcastExceptionOperator;

/*
 * Upcast network exception
 * 将UnknownHostException, UnknownServiceException
 * ProtocolException, RemoteException, SaslException, SocketException, SSLException
 * SyncFailedException, JMXServerErrorException, JMXProviderException, HttpRetryException
 * 转为IOException
 */

public class UNE extends UpcastExceptionOperator {

    static {
        exceptions.add("java.net.UnknownHostException");
        exceptions.add("java.net.UnknownServiceException");
        exceptions.add("java.net.ProtocolException");
        exceptions.add("java.rmi.RemoteException");
        exceptions.add("javax.security.sasl.SaslException");
        exceptions.add("java.net.SocketException");
        exceptions.add("javax.net.ssl.SSLException");
        exceptions.add("java.io.SyncFailedException");
        exceptions.add("javax.management.remote.JMXServerErrorException");
        exceptions.add("javax.management.JMXProviderException");
        exceptions.add("java.net.HttpRetryException");

        mutator = MutatorType.UNE;

        targetException = "java.io.IOException";
    }

}
