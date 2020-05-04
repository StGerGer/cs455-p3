import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Server class to track users and serve requests
 *
 * @author Tanner Purves
 * @author Nate St. George
 */
public class IdServer extends UnicastRemoteObject implements ServerRequest {
    // RMI Registry port
    private static int registryPort = 1099;
    // Known server IPs
    private static String[] serverIps = new String[]{"172.20.0.2", "172.20.0.3", "172.20.0.4"};
    // User data storage and server information storage
    private static HashMap<String, UserData> dict;
    private static HashMap<String, ServerRequest> servers;
    // Is this server the coordinator?
    private static boolean isCoordinator = false;
    // This server's IP address
    private static InetAddress localIP;
    // This server's last known coordinator IP
    private static InetAddress lastKnownCoordinator;
    // This server's priority number -- used to determine coordinator
    private static Random r = new Random();
    private static int priority = r.nextInt(100000);
    // Is this server currently running an election? Used to prevent running multiple elections at a time.
    private static boolean runningElection = false;
    // Is this server running in verbose mode?
    private static boolean verbose = false;

    public IdServer(String s) throws RemoteException, UnknownHostException {
        super();
        dict = new HashMap<>();
        localIP = InetAddress.getLocalHost();
        System.out.println("New server started: " + s);
        System.out.println("Address: " + localIP.getHostAddress());
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
                dict.remove(oldLoginName);
            }
            else if(!ud.hasPassword()){
                ud.setLoginName(newLoginName);
                dict.put(newLoginName, ud);
                dict.remove(oldLoginName);
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

        if(!isCoordinator) {
            debugPrint("Passing delete request to coordinator");
            return servers.get(lastKnownCoordinator.getHostAddress()).delete(loginName, password);
        }

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

        if(!isCoordinator) {
            debugPrint("Passing get request to coordinator");
            return servers.get(lastKnownCoordinator.getHostAddress()).get(type);
        }

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

    /**
     * This internal function is used for separating the get operation for requesting
     * all data in the get rmi method.
     *
     * @return A string representation of all user data.
     */
    private String getAll() {
        StringBuilder retVal = new StringBuilder();

        for(String key: dict.keySet())
            retVal.append(dict.get(key).toString()).append("\n");

        return retVal.toString();
    }

    /**
     * This internal function is used for separating the get operation for requesting
     * uuids data in the get rmi method.
     *
     * @return A string representation of uuids user data.
     */
    private String getUUIDS() {
        StringBuilder retVal = new StringBuilder();

        for(String key: dict.keySet())
            retVal.append(dict.get(key).getUUID()).append("\n");

        return retVal.toString();
    }

    /**
     * This internal function is used for separating the get operation for requesting
     * user login name data in the get rmi method.
     *
     * @return A string representation of user login name data.
     */
    private String getUsers() {
        StringBuilder retVal = new StringBuilder();

        for(String key: dict.keySet())
            retVal.append(dict.get(key).getLoginName()).append("\n");

        return retVal.toString();
    }

    /**
     * The entry point method.
     *
     * @param args Command line parameters
     */
    public static void main(String[] args) {

        servers = new HashMap<>();

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

        // Timers to be used for running tasks repeatedly
        Timer t1 = new Timer();
        Timer t2 = new Timer();
        // New timer scheduled for 5 min with a 5 min delay
        t1.scheduleAtFixedRate(new Task(dict), 5*60*1000, 5*60*1000);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // Sleep allows for server status to populate
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            
            debugPrint("Set security manager");
            //Registry registry = LocateRegistry.getRegistry(registryPort);
            
            debugPrint("Got registry");
            IdServer obj = new IdServer("/IdServer");
            System.out.println("Created server");
//            registry.rebind("//localhost:" + registryPort + "/IdServer", obj);
            registry.rebind("/IdServer", obj);
            readFile();
            debugPrint("IdServer bound in registry");
        }
        catch (IOException | ClassNotFoundException e){
            System.out.println("No backup found... starting fresh.");
        }
        catch (Exception e) {
            System.out.println("IdServer err: " + e.getMessage());
            e.printStackTrace();
        }

        // New Ping scheduled for every 30 seconds to check if servers are still online
        t2.scheduleAtFixedRate(new Ping(), 0, 30*1000);

    }

    @Override
    public int getPriority() {
        debugPrint("Priority: " + priority);
        return priority;
    }

    @Override
    public void iAmCoordinator(InetAddress ip) {
        System.out.println("New coordinator: " + ip.getHostAddress());
        lastKnownCoordinator = ip;
    }

    @Override
    public InetAddress requestCoordinatorIP() throws RemoteException {
        return lastKnownCoordinator;
    }

    @Override
    public void requestElection() throws RemoteException {
        if(runningElection) {
            debugPrint("Already running an election.");
        } else {
            runElection();
        }
    }

    /**
     * Shutdown timer class.
     */
    static class ShutdownHook extends Thread {

        /**
         * Default method to run on shutdown or interrupt.
         */
        public void run() {
            //Stuff to do on shutdown
            System.out.println("Shutting down...");
            try {
                Task.writeToFile(dict);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Select a new coordinator if this is the coordinator
            while(isCoordinator && servers.values().size() > 0) {
                System.out.println("Selecting new coordinator...");
                requestElectionFromFirstServer();
            }
        }
    }

    /**
     * Task that runs on timeout.
     */
    static class Ping extends TimerTask {

        private static int lastServerCount = 0;

        /**
         * The default method to run after timer expiration.
         */
        public void run() {
            int serverCount = 0;
            System.out.println("Pinging...");


            // Discover server IP addresses
            for (String serverIp : serverIps) {
                try {
                    Registry r = LocateRegistry.getRegistry(serverIp, registryPort);
                    ServerRequest stub = (ServerRequest) r.lookup("/IdServer");
                    IdServer.servers.put(serverIp, stub);
                    debugPrint("IP: "+serverIp+" Online: True");
                    serverCount++;
                } catch (IOException | NotBoundException e) {
                    debugPrint("IP: "+serverIp+" Online: False");
                    IdServer.servers.put(serverIp, null);
                }
            }
            // Request a new election from the first online server if the number of servers online has changed
            if(serverCount != lastServerCount) {
                lastServerCount = serverCount;
                requestElectionFromFirstServer();
            }

        }
    }

    /**
     * Task that runs on timeout.
     */
    static class Task extends TimerTask {
        private HashMap<String, UserData> dict;

        /**
         * Constructor.
         *
         * @param dict The data map
         */
        public Task(HashMap<String, UserData> dict){
            this.dict = dict;
        }

        /**
         * The default method to run after timer expiration.
         */
        public void run() {
            System.out.println("Backing up...");

            try {
                writeToFile(this.dict);
            }
            catch(IOException e){
                e.printStackTrace();
            }

        }

        /**
         * Method to assist in writing data map object to file.
         *
         * @param dict The data map
         * @throws IOException If error occurs
         */
        public static void writeToFile(HashMap<String, UserData> dict) throws IOException {
            File f = new File("server.backup");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(dict);
            oos.flush();
            oos.close();
        }
    }

    /**
     * This method reads in the backup file if one exists.
     *
     * @throws IOException If I/O error occurs
     * @throws ClassNotFoundException If UserData class cannot be found
     */
    public static void readFile() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("server.backup"));
        dict = (HashMap<String, UserData>) ois.readObject();
    }

    /**
     * Reset the idle timer.
     */
//    public static void resetTimer(Timer t) {
//        t.
//        t.cancel();
//        t = new Timer();
//        t.schedule(new Task(dict), 5 * 60 * 1000);
//    }

    private static void requestElectionFromFirstServer() {
        boolean successfulElection = false;
        int i = 0;
        while(i < servers.size() && !successfulElection) {
            try {
                servers.get(serverIps[i]).requestElection();
                successfulElection = true;
            } catch (RemoteException e) {
                e.printStackTrace();
                successfulElection = false;
            }
            i++;
        }
    }

    private void runElection() throws RemoteException {
        runningElection = true;
        debugPrint("I have started a new election!");
        debugPrint("My priority: " + priority);
        debugPrint("Known servers: " + servers.values().size());
        if(servers.values().size() == 1) {
            System.out.println("No other servers known. Making self coordinator.");
            isCoordinator = true;
        } else {
            boolean higherPriorityExists = false;
            int i = 0;
            while(i < servers.values().size() && !higherPriorityExists) {
                String ip = (String) servers.keySet().toArray()[i];
                if (!ip.equals(localIP.getHostAddress())) {
                    debugPrint(localIP.getHostAddress() + ": Comparing to " + ip + "...");
                    ServerRequest stub = servers.get(ip);
                    if(stub != null) {
                        int remotePriority = stub.getPriority();
                        if(priority < remotePriority) {
                            debugPrint("My priority is lower than " + ip);
                            higherPriorityExists = true;
                            stub.requestElection();
                        } else {
                            debugPrint("My priority is higher than " + ip);
                        }
                    }
                }
                i++;
            }
            isCoordinator = !higherPriorityExists;
        }

        debugPrint("Am I the coordinator? - " + ((isCoordinator) ? "Yes": "No"));
        if(isCoordinator) {
            for(ServerRequest stub: servers.values()) {
                if(stub != null) {
                    stub.iAmCoordinator(localIP);
                }
            }
        }
        runningElection = false;
    }

    /**
     * Class usage statement.
     */
    private static void printUsage() {
        System.out.println("Usage: java IdServer [--numport <port#>] [--verbose]");
    }

    /**
     * Find a user given their UUID.
     *
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

    private static void debugPrint(String in) {
        if(verbose) System.out.println("DEBUG: " + in);
    }
}
