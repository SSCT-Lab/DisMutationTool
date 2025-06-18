package io.dismute.mutator.network;


/*
 * Upcast network exception
 * 将UnknownHostException, UnknownServiceException
 * ProtocolException, RemoteException, SaslException, SocketException, SSLException
 * SyncFailedException, JMXServerErrorException, JMXProviderException, HttpRetryException
 * 转为IOException
 */

import io.dismute.mutantgen.MutatorType;
import io.dismute.mutator.UpcastExceptionOperator;

public class UNE extends UpcastExceptionOperator {

    public UNE() {
        mutator = MutatorType.UNE;

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

        targetException = "java.io.IOException";
    }

}
