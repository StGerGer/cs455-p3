import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class IdServer extends UnicastRemoteObject implements LoginRequest {
    private String name; // Not sure what this is for...
    private static int registryPort = 1099;
    // uname: [uuid, ip, receivedTime, realUname, lastChangeDate]
    private static Timer t;
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
                InetAddress ipv4 = InetAddress.getByName(ip);
            } catch (ServerNotActiveException | UnknownHostException e) {
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

        t = new Timer();
        // New timer scheduled for 5 min
        t.schedule(new Task(), 5*60*1000);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    /**
     * Shutdown timer class.
     */
    static class ShutdownHook extends Thread {
        public void run() {
            //Stuff to do on shutdown
            System.out.println("Shutting down...");
        }
    }

    /**
     * Task that runs on timeout.
     */
    static class Task extends TimerTask {
        public void run() {
            System.out.println("Backing up...");

            // Reset timer
            resetTimer();
        }
    }

    /**
     * Reset the idle timer.
     */
    public static void resetTimer() {
        t.cancel();
        t = new Timer();
        t.schedule(new Task(), 5 * 60 * 1000);
    }

    private static void printUsage() {
        System.out.println("Usage: java IdServer [--numport <port#>] [--verbose]");
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
