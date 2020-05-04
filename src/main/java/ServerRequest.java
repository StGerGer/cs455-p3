import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.util.HashMap;

/**
 * Request interface for all available methods to the client.
 */
public interface ServerRequest extends Remote {

    int getPriority() throws RemoteException;

    /**
     * Alert this server that the sending server with provided IP is coordinator.
     */
    void iAmCoordinator(InetAddress ip) throws RemoteException;

    /**
     * Ask this server for its most recently known coordinator IP.
     */

    InetAddress requestCoordinatorIP() throws RemoteException;

    /**
    * Request that this server begin an election.
     */
    void requestElection() throws RemoteException;

    /**
     * This rmi method is used to create a new user in the system.
     *
     * @param loginName The user login name
     * @param realName The real name of the user
     * @param password The password hash
     * @return Success statement and user information created
     * @throws RemoteException If error occurs
     */
    String createLoginName(String loginName, String realName, String password) throws RemoteException;

    /**
     * This rmi method is used to lookup a particular user.
     *
     * @param loginName The user login name
     * @return A string representation of user data for the requested user
     * @throws RemoteException If error occurs
     */
    String lookup(String loginName) throws RemoteException;

    /**
     * This rmi method is used to lookup a particular user by providing their uuid.
     *
     * @param Uuid The uuid of the requested user
     * @return A string representation of user data for the requested user
     * @throws RemoteException If error occurs
     */
    String reverseLookup(String Uuid) throws RemoteException;

    /**
     * This rmi method is used to modify an existing user's login name. *Requires associated login password
     *
     * @param oldLoginName The old login name that the user wishes to modify
     * @param newLoginName The new login name that the user wishes to change to
     * @param password The associated password with the user account login name (Can be null if no password)
     * @return Success statement and user information that was changed
     * @throws RemoteException If error occurs
     */
    String modifyLoginName(String oldLoginName, String newLoginName, String password) throws RemoteException;

    /**
     * This rmi method is used to delete an existing user's login account.
     *
     * @param loginName The user login name
     * @param password The password associated with the user login name
     * @return Success statement and user login name deleted=
     * @throws RemoteException If error occurs
     */
    String delete(String loginName, String password) throws RemoteException;

    /**
     * This rmi method is used to get user information from the server data storage.
     *
     * @param type users | uuids | all
     * @return A string representation of the requested data
     * @throws RemoteException If error occurs
     */
    String get(String type) throws RemoteException;

    void updateMap(HashMap<String, UserData> dict) throws RemoteException;
}
