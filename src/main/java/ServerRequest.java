import java.rmi.RemoteException;

/**
 * Request interface for all available methods to the client.
 */
public interface ServerRequest extends java.rmi.Remote {
        int getNum() throws RemoteException;
}
