import java.rmi.RemoteException;
import java.util.UUID;

// TODO: Add Javadocs to all methods

public interface LoginRequest extends java.rmi.Remote {
        String unameLoginRequest(String uname);
        String uuidLoginRequest(String uuid);

        // Create query
        void createLoginName(String uname, String realName, String password) throws RemoteException;

        // Lookup query
        void lookup(String loginName);

        // Reverse lookup query
        void reverseLookup(String Uuid);

        // Modify query
        void modifyLoginName(String oldLoginName, String newLoginName, String password) throws RemoteException;

        // Delete query
        void delete(String loginName, String password) throws RemoteException;

        // Get query
        void get(String type);
}
