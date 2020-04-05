import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;

public class IdServer extends UnicastRemoteObject implements LoginRequest {
    private String name; // Not sure what this is for...
    private static int registryPort = 1099;
    // uname: [uuid, ip, receivedTime, realUname, lastChangeDate]
    private HashMap<String, String[]> dict;

    public IdServer(String s) throws RemoteException {
        super();
        name = s;
        dict = new HashMap<String, String[]>();
    }

    @Override
    public String unameLoginRequest(String uname) throws RemoteException {
        String retVal = null;
        if(dict.containsKey(uname)){
            retVal = dict.get(uname)[0];
        }

        return retVal;
    }

    @Override
    public String uuidLoginRequest(String uuid) throws RemoteException {
        String retVal = null;
        retVal = getKeyFromValue(uuid);

        return retVal;
    }

    private String getKeyFromValue(String uuid) {
        String retVal = null;
        Set<String> keys = dict.keySet();
        for(String key: keys){
            if(dict.get(key)[0].equals(uuid)){
                // Once we find the right <k,v> pair, break and return
                retVal = key;
                break;
            }
        }

        return retVal;
    }

    @Override
    public void createLoginName(String uname) throws RemoteException {
        if(dict.containsKey(uname)){
            throw new RemoteException("Uname already exists.");
        }
        else{
            // Could potentially create a duplicate right?
            UUID uuid = UUID.randomUUID();
            String ip = "";
            String client = "";
            Date d = new Date();

            try {
                ip = getClientHost();
                InetAddress ipv4 = InetAddress.getByName(ip);
                client = InetAddress.getByAddress(ipv4.getAddress()).getHostName();
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }

            String[] arr = {uuid.toString(), ip, d.toString(), client, d.toString()};
            dict.put(uname, arr);
        }
    }

    @Override
    public void modifyLoginName(String oldUname, String newUname) throws RemoteException {
        if(dict.containsKey(oldUname)) {
            String[] arr = dict.get(oldUname);
            dict.remove(oldUname);
            dict.put(newUname, arr);
        }
        else{
            // Throw expception if modification failed
            throw new RemoteException("Old uname does not exist.");
        }
    }

    public static void main(String[] args) {
        boolean verbose = false;

        // Arg parse stuff
        if (args.length > 0) {
            switch(args.length){
                case 1:
                    if(!(args[0].equals("--verbose"))){
                        printUsage();
                        System.exit(1);
                    }
                    verbose = true;
                    break;
                case 2:
                    if(!(args[0].equals("--numport"))) {
                        printUsage();
                        System.exit(1);
                    }
                    registryPort = Integer.parseInt(args[1]);
                    break;
                case 3:
                    if(!(args[0].equals("--numport")) || !(args[2].equals("--verbose"))) {
                        printUsage();
                        System.exit(1);
                    }
                    registryPort = Integer.parseInt(args[1]);
                    verbose = true;
                    break;
            }
        }

        // Connect to the registry or create a new registry if there isn't one already
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(registryPort);
            registry.list();
        } catch (RemoteException e) {
            try {
                System.out.println("No registry found on given port, creating a new one");
                registry = LocateRegistry.createRegistry(registryPort);
            } catch (RemoteException e2) {
                System.out.println("Unable to find or create registry: " + e2.getMessage());
                System.exit(0);
            }
        }

        try {
            System.out.println("Made it to the try");
            // Create and install a security manager
            System.setSecurityManager(new SecurityManager());
            System.out.println("Set security manager");
            //Registry registry = LocateRegistry.getRegistry(registryPort);
            System.out.println("Got registry");
            IdServer obj = new IdServer("//IdServer");
            System.out.println("Created server");
            registry.rebind("//localhost:" + registryPort + "/IdServer", obj);
            System.out.println("IdServer bound in registry");
        } catch (Exception e) {
            System.out.println("IdServer err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java IdServer [--numport <port#>] [--verbose]");
    }
}

