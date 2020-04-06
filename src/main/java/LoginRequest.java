import java.rmi.RemoteException;
import java.util.UUID;

public interface LoginRequest extends java.rmi.Remote {
        String unameLoginRequest(String uname) throws java.rmi.RemoteException;
        String uuidLoginRequest(String uuid) throws java.rmi.RemoteException;

        // Create query
        void createLoginName(String uname, String realName, String password) throws RemoteException;

        // Lookup query
        void lookup(String loginName) throws RemoteException;

        // Reverse lookup query
        void reverseLookup(String Uuid) throws RemoteException;

        // Modify query
        void modifyLoginName(String oldLoginName, String newLoginName, String password) throws RemoteException;

        // Delete query
        void delete(String loginName, String password) throws RemoteException;

        // Get query
        void get(String type) throws RemoteException;
}
