import java.net.InetAddress;
import java.rmi.RemoteException;

/**
 * Request interface for all available methods to the client.
 */
public interface ServerRequest extends java.rmi.Remote {

        int getPriority() throws RemoteException;

        /**
         * Alert this server that the sending server with provided IP is coordinator.
         */
        void iAmCoordinator(InetAddress ip);

        /**
         * Request that this server begin an election.
         */
        void requestElection();
}
