import java.rmi.RemoteException;
import java.util.UUID;

// TODO: Add Javadocs to all methods

public interface LoginRequest extends java.rmi.Remote {

        // Create query
        String createLoginName(String uname, String realName, String password) throws RemoteException;

        // Lookup query
        String lookup(String loginName) throws RemoteException;

        // Reverse lookup query
        String reverseLookup(String Uuid) throws RemoteException;

        // Modify query
        String modifyLoginName(String oldLoginName, String newLoginName, String password) throws RemoteException;

        // Delete query
        String delete(String loginName, String password) throws RemoteException;

        // Get query
        String get(String type) throws RemoteException;
}
