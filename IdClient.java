import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

public class IdClient
{
    public static void main(String[] args) {
	if (args.length < 2) {
	    System.err.println("Usage: java IdClient --server <serverhost> [--numport <port#>] <query>");
	    System.exit(1);
	}

	//TODO: Implement a more robust argparse
	String host = null;
	String query;
	int registryPort = 1099;
	if (args.length == 2) {
	    host = args[0];
	    query = args[1];
	} else {
	    host = args[0];
		registryPort = Integer.parseInt(args[2]);
	    query = args[3];
	}

	try {
	    Registry registry = LocateRegistry.getRegistry(host, registryPort);
	    LoginRequest stub = (LoginRequest) registry.lookup("IdServer");

	    String uname = "purvesta";

	    stub.createLoginName(uname);

		String uuid = stub.unameLoginRequest(uname);
		System.out.println("UUID from unameLoginRequest: "+uuid);
		String reqUname = stub.uuidLoginRequest(uuid);
	    System.out.println("Uname from uuidLoginRequest: "+reqUname);

	    stub.modifyLoginName(uname, "_purvesta");

	    reqUname = stub.uuidLoginRequest(uuid);
	    System.out.println("Uname from uuidLoginRequest: "+reqUname);

	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
    }
}
