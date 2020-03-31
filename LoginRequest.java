import java.util.UUID;

public interface LoginRequest extends java.rmi.Remote {
        UUID unameLoginRequest(String uname) throws java.rmi.RemoteException;
        String uuidLoginRequest(String UUID) throws java.rmi.RemoteException;
}
