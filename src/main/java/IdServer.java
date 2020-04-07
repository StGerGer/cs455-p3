import java.lang.reflect.Array;
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
    private HashMap<String, UserData> dict;

    public IdServer(String s) throws RemoteException {
        super();
        name = s;
        dict = new HashMap<>();
    }

    public String unameLoginRequest(String uname) {
        String retVal = null;
        if(dict.containsKey(uname)){
            retVal = dict.get(uname).getUUID().toString();
        }

        return retVal;
    }

    public String uuidLoginRequest(String uuid) {
        return findUUID(uuid);
    }

    @Override
    public void createLoginName(String loginName, String realName, String password) throws RemoteException {
        System.out.println("Adding " + loginName + " to registry...");
        if(dict.containsKey(loginName)){
            throw new RemoteException("Login name already exists.");
        }
        else {
            String ip = "";
            try {
                ip = getClientHost();
            } catch (ServerNotActiveException e) {
                e.printStackTrace();
            }

            System.out.println(ip);
            dict.put(loginName, new UserData(loginName, realName, password, ip));
        }
    }

    @Override
    public void lookup(String loginName) {
        // TODO: Add lookup logic
    }

    @Override
    public void reverseLookup(String Uuid) {
        // TODO: Add reverse lookup logic
    }

    @Override
    public void modifyLoginName(String oldLoginName, String newLoginName, String password) throws RemoteException {
        if(dict.containsKey(oldLoginName)) {
            // TODO: Check for password
            UserData user = dict.get(oldLoginName);
            user.setLoginName(newLoginName);
            dict.put(newLoginName, user);
        }
        else {
            // Throw exception if modification failed
            throw new RemoteException("Provided username does not exist");
        }
    }

    @Override
    public void delete(String loginName, String password) throws RemoteException {
        if(dict.containsKey(loginName)) {
            if(dict.get(loginName).getPassword().equals(password)) {
                dict.remove(loginName);
            } else {
                throw new RemoteException("Incorrect password");
            }
        } else {
            throw new RemoteException("Provided username does not exist");
        }
    }

    @Override
    public void get(String type) {
        // TODO: Add get logic
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            registryPort = Integer.parseInt(args[0]);
        }

        // Connect to the registry or create a new registry if there isn't one already
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(registryPort);
            registry.list();
        } catch (RemoteException e) {
            try {
                System.out.println("No registry found on given port, creating a new one...");
                registry = LocateRegistry.createRegistry(registryPort);
                System.out.println("Registry created.");
            } catch (RemoteException e2) {
                System.out.println("Unable to find or create registry: " + e2.getMessage());
                System.exit(0);
            }
        }

        try {
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

    /**
     * Find a user given their UUID.
     * @param uuid UUID to search for
     * @return index of connected user in HashMap
     */
    private String findUUID(String uuid) {
        String retVal = null;
        Set<String> keys = dict.keySet();
        for(String key: keys){
            if(dict.get(key).getUUID().toString().equals(uuid)){
                // Once we find the right <k,v> pair, break and return
                retVal = key;
                break;
            }
        }

        return retVal;
    }
}
