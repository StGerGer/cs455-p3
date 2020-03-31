import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class IdServer extends UnicastRemoteObject implements LoginRequest {
    private String name;
    private static int registryPort = 1099;

    public IdServer(String s) throws RemoteException {
        super();
        name = s;
    }

    @Override
    public UUID unameLoginRequest(String uname) throws RemoteException {
        // This will not be random. Should be a result of the lookup on the uname
        //TODO: Implement uname lookup
        return UUID.randomUUID();
    }

    @Override
    public String uuidLoginRequest(String UUID) throws RemoteException {
        //TODO: Implement UUID lookup
        return "USERNAME";
    }

    public static void main(String args[]) {
        if (args.length > 0) {
            registryPort = Integer.parseInt(args[0]);
        }

        try {
            // Create and install a security manager
            System.setSecurityManager(new SecurityManager());
            Registry registry = LocateRegistry.getRegistry(registryPort);
            IdServer obj = new IdServer("//IdServer");
            registry.rebind("IdServer", obj);
            System.out.println("IdServer bound in registry");
        } catch (Exception e) {
            System.out.println("IdServer err: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
