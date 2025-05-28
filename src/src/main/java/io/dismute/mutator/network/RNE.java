package io.dismute.mutator.network;


import io.dismute.mutantgen.MutatorType;
import io.dismute.mutator.DiscardExceptionOperator;

public class RNE extends DiscardExceptionOperator {
    public RNE(){
        mutator = MutatorType.RNE;

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
