import java.io.*;
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
public class IdServer extends UnicastRemoteObject implements LoginRequest, ServerRequest {
    private static int registryPort = 1099;
    // uname: [uuid, ip, receivedTime, realUname, lastChangeDate]
    private static Timer t1;
    private static Timer t2;
    private static HashMap<String, UserData> dict;
    private static HashMap<String, ServerRequest> servers;
    private static boolean isCoordinator = false;

    public IdServer(String s) throws RemoteException {
        super();
        dict = new HashMap<>();
    }

    /**
     * This rmi method is used to create a new user in the system.
     *
     * @param loginName The user login name
     * @param realName The real name of the user
     * @param password The password hash
     * @return Success statement and user information created
     * @throws RemoteException If error occurs
     */
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

    /**
     * This rmi method is used to lookup a particular user.
     *
     * @param loginName The user login name
     * @return A string representation of user data for the requested user
     * @throws RemoteException If error occurs
     */
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

    /**
     * This rmi method is used to lookup a particular user by providing their uuid.
     *
     * @param Uuid The uuid of the requested user
     * @return A string representation of user data for the requested user
     * @throws RemoteException If error occurs
     */
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

    /**
     * This rmi method is used to modify an existing user's login name. *Requires associated login password
     *
     * @param oldLoginName The old login name that the user wishes to modify
     * @param newLoginName The new login name that the user wishes to change to
     * @param password The associated password with the user account login name (Can be null if no password)
     * @return Success statement and user information that was changed
     * @throws RemoteException If error occurs
     */
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

    /**
     * This rmi method is used to delete an existing user's login account.
     *
     * @param loginName The user login name
     * @param password The password associated with the user login name
     * @return Success statement and user login name deleted=
     * @throws RemoteException If error occurs
     */
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

    /**
     * This rmi method is used to get user information from the server data storage.
     *
     * @param type users | uuids | all
     * @return A string representation of the requested data
     * @throws RemoteException If error occurs
     */
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
//            registry.rebind("//localhost:" + registryPort + "/IdServer", obj);
            registry.rebind("/IdServer", obj);
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

        servers = new HashMap<String, ServerRequest>();

        t1 = new Timer();
        t2 = new Timer();
        // New timer scheduled for 5 min with a 5 min delay
        t1.scheduleAtFixedRate(new Task(dict), 5*60*1000, 5*60*1000);
        // New Ping scheduled for every 30 seconds to check if servers are still online
        t2.scheduleAtFixedRate(new Ping(), 0, 30*1000);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // Sleep allows for server status to populate
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println("Server list empty: "+servers.isEmpty());
//        for(String ip: servers.keySet())
//            System.out.println("IP: "+ip+" Online: "+servers.get(ip));

        // Start election now that we have a list of online servers
        // TODO: Move to a method for starting an election
        // TODO: Add to shutdown hook some way of communicating to other servers that election needs to happen
        // TODO: Should only have to perform an election when the coordinator dies
        Random r = new Random();
        int myNum = r.nextInt(10);
        boolean won = false;
        for(String ip: servers.keySet()){
            ServerRequest stub = servers.get(ip);
            boolean keepGoing = true;
            while(keepGoing){
                try {
                    int theirNum = stub.getNum();
                    if(myNum > theirNum) {
                        won = true;
                        keepGoing = false;
                    }
                    else if(myNum < theirNum) {
                        won = false;
                        keepGoing = false;
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }

        if(won)
            isCoordinator = true;

        System.out.println("Am I the coordinator? - " + ((isCoordinator) ? "yes": "no"));
    }

    @Override
    public int getNum() throws RemoteException {
        Random r = new Random();
        return r.nextInt(10);
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
        }
    }

    /**
     * Task that runs on timeout.
     */
    static class Ping extends TimerTask {

        /**
         * The default method to run after timer expiration.
         */
        public void run() {
            System.out.println("Pinging...");

            String[] serverIps = {"172.20.0.2", "172.20.0.3", "172.20.0.4"};
            // Discover server IP addresses
            for (String serverIp : serverIps) {
                try {
                    Registry r = LocateRegistry.getRegistry(serverIp, registryPort);
                    ServerRequest stub = (ServerRequest) r.lookup("/IdServer");
                    IdServer.servers.put(serverIp, stub);
//                    InetAddress ip = InetAddress.getByName(serverIp);
//                    boolean reachable = ip.isReachable(5000);
//                    IdServer.servers.put(serverIp, reachable);
                    System.out.println("IP: "+serverIp+" Online: True");
                } catch (IOException | NotBoundException e) {
                    System.out.println("IP: "+serverIp+" Online: False");
                    IdServer.servers.put(serverIp, null);
                }
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
}
