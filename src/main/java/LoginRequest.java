import java.rmi.RemoteException;
import java.util.UUID;

public interface LoginRequest extends java.rmi.Remote {
        String unameLoginRequest(String uname) throws java.rmi.RemoteException;
        String uuidLoginRequest(String uuid) throws java.rmi.RemoteException;
        void createLoginName(String uname) throws RemoteException;
        void modifyLoginName(String oldUname, String newUname) throws RemoteException;
}
