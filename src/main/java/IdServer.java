import java.io.*;
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
    private static HashMap<String, UserData> dict;

    public IdServer(String s) throws RemoteException {
        super();
        name = s;
        dict = new HashMap<>();
    }

    @Override
    public String createLoginName(String loginName, String realName, String password) throws RemoteException {
        System.out.println("Adding " + loginName + " to registry...");
        String retVal = "";
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
            UserData ud = new UserData(loginName, realName, password, ip);
            dict.put(loginName, ud);

            retVal += "Successfully created user: "+ud.getLoginName()+"\nUUID: "+ud.getUUID();
        }

        return retVal;
    }

    @Override
    public String lookup(String loginName) throws RemoteException{
        String retVal = "";
        if(dict.containsKey(loginName)){
            UserData ud = dict.get(loginName);
            ud.updateLastRequestDate();

            retVal += ud.toString();
        }
        else{
            throw new RemoteException("Login name does not exist.");
        }

        return retVal;
    }

    @Override
    public String reverseLookup(String Uuid) throws RemoteException{
        String loginName = findUUID(Uuid);
        String retVal = "";

        if(loginName != null){
            retVal = lookup(loginName);
        }
        else{
            throw new RemoteException("UUID does not exist.");
        }

        return retVal;
    }

    @Override
    public String modifyLoginName(String oldLoginName, String newLoginName, String password) throws RemoteException {
        String retVal = "";

        if(dict.containsKey(oldLoginName)) {
            UserData ud = dict.get(oldLoginName);
            if(ud.hasPassword() && (ud.getPassword().equals(password))){
                ud.setLoginName(newLoginName);
                dict.put(newLoginName, ud);
            }
            else if(!ud.hasPassword()){
                ud.setLoginName(newLoginName);
                dict.put(newLoginName, ud);
            }
            else{
                throw new RemoteException("Incorrect password.");
            }
            retVal += "Successfully updated login name "+oldLoginName+" -> "+newLoginName;
        }
        else {
            // Throw exception if modification failed
            throw new RemoteException("Login name does not exist.");
        }

        return retVal;
    }

    @Override
    public String delete(String loginName, String password) throws RemoteException {
        String retVal = "";

        if(dict.containsKey(loginName)) {
            UserData ud = dict.get(loginName);
            if(ud.hasPassword() && ud.getPassword().equals(password)) {
                dict.remove(loginName);
            } else if(!ud.hasPassword()) {
                dict.remove(loginName);
            }
            else {
                throw new RemoteException("Incorrect password.");
            }
            retVal += "Successfully deleted user: "+loginName;
        } else {
            throw new RemoteException("Provided username does not exist");
        }

        return retVal;
    }

    @Override
    public String get(String type) throws RemoteException {
        String retVal = "";

        switch (type.toLowerCase()) {
            case "users":
                retVal = getUsers();
                break;
            case "uuids":
                retVal = getUUIDS();
                break;
            case "all":
                retVal = getAll();
        }

        return retVal;
    }

    private String getAll() {
        StringBuilder retVal = new StringBuilder();

        for(String key: dict.keySet())
            retVal.append(dict.get(key).toString()).append("\n");

        return retVal.toString();
    }

    private String getUUIDS() {
        StringBuilder retVal = new StringBuilder();

        for(String key: dict.keySet())
            retVal.append(dict.get(key).getUUID()).append("\n");

        return retVal.toString();
    }

    private String getUsers() {
        StringBuilder retVal = new StringBuilder();

        for(String key: dict.keySet())
            retVal.append(dict.get(key).getLoginName()).append("\n");

        return retVal.toString();
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
                if(verbose)
                    System.out.println("No registry found on given port, creating a new one...");
                registry = LocateRegistry.createRegistry(registryPort);
                if(verbose)
                    System.out.println("Registry created.");
            } catch (RemoteException e2) {
                System.out.println("Unable to find or create registry: " + e2.getMessage());
                System.exit(0);
            }
        }

        try {
            // There is probably a lot better way to print messages verbosely, but oh well...

            // Create and install a security manager
            System.setSecurityManager(new SecurityManager());
            if(verbose)
                System.out.println("Set security manager");
            //Registry registry = LocateRegistry.getRegistry(registryPort);
            if(verbose)
                System.out.println("Got registry");
            IdServer obj = new IdServer("//IdServer");
            if(verbose)
                System.out.println("Created server");
            registry.rebind("//localhost:" + registryPort + "/IdServer", obj);
            readFile();
            System.out.println("IdServer bound in registry");
        }
        catch (IOException | ClassNotFoundException e){
            if(verbose)
                System.out.println("No backup found... starting fresh.");
        }
        catch (Exception e) {
            System.out.println("IdServer err: " + e.getMessage());
            e.printStackTrace();
        }

        t = new Timer();
        // New timer scheduled for 5 min
        t.schedule(new Task(dict), 5*60*1000);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    /**
     * Shutdown timer class.
     */
    static class ShutdownHook extends Thread {
        public void run() {
            //Stuff to do on shutdown
            System.out.println("Shutting down...");
            try {
                Task.writeToFile(dict);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Task that runs on timeout.
     */
    static class Task extends TimerTask {
        private HashMap<String, UserData> dict;

        public Task(HashMap<String, UserData> dict){
            this.dict = dict;
        }
        public void run() {
            System.out.println("Backing up...");

            try {
                writeToFile(this.dict);
            }
            catch(IOException e){
                e.printStackTrace();
            }

            // Reset timer
            resetTimer();
        }

        public static void writeToFile(HashMap<String, UserData> dict) throws IOException {
            File f = new File("server.backup");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(dict);
            oos.flush();
            oos.close();
        }
    }

    public static void readFile() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("server.backup"));
        dict = (HashMap<String, UserData>) ois.readObject();
    }

    /**
     * Reset the idle timer.
     */
    public static void resetTimer() {
        t.cancel();
        t = new Timer();
        t.schedule(new Task(dict), 5 * 60 * 1000);
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
