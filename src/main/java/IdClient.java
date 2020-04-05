import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.commons.cli.*;
import java.util.UUID;

public class IdClient {

	private static String host;
	private static int registryPort = 1099;
	private static String query;

	public static void main(String[] args) {
		try {
			handleArgs(args);
		} catch (ParseException e) {
			System.out.println("Unable to parse input: " + e.getMessage());
			System.exit(1);
		}

		try {
			Registry registry = LocateRegistry.getRegistry(host, registryPort);
			LoginRequest stub = (LoginRequest) registry.lookup("//" + host + ":" + registryPort + "/IdServer");

			String uname = "purvesta";

			stub.createLoginName(uname);

			String uuid = stub.unameLoginRequest(uname);
			System.out.println("UUID from unameLoginRequest: " + uuid);
			String reqUname = stub.uuidLoginRequest(uuid);
			System.out.println("Uname from uuidLoginRequest: " + reqUname);

			stub.modifyLoginName(uname, "_purvesta");

			reqUname = stub.uuidLoginRequest(uuid);
			System.out.println("Uname from uuidLoginRequest: " + reqUname);

		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private static void handleArgs(String[] args) throws ParseException {
		// Define options
		Options options = new Options();
		OptionGroup queries = new OptionGroup();
		options.addOption(new RequiredOption("s", "server", true, "Specify server IP."));
		options.addOption(new Option("n", "numport", true, "Port number to connect to. Default = 1099."));
		// Create option group
		queries.addOption(Option.builder("c")
				.longOpt("create")
				.numberOfArgs(2)
				.optionalArg(true)
				.desc("Create a new login name with provided name, real name (or name found in OS), and a password if provided.")
				.build());
		queries.addOption(new Option("l", "lookup", true, "Find a user with the provided login name."));
		queries.addOption(new Option("r", "reverse-lookup", true, "Look up a user by their UUID."));
		queries.addOption(Option.builder("m")
				.longOpt("modify")
				.numberOfArgs(2)
				.desc("Change a login name.")
				.build());
		queries.addOption(new Option("d", "delete", true, "Delete a login name. If the user has a password, it is required."));
		queries.addOption(new Option("g", "get", true, " (users|uuids|all) Get a list of all users, uuids, or all data."));
		// Add queries to options
		options.addOptionGroup(queries);
		options.addOption(new Option("p", "password", true, "Set a password if needed for the operation."));

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		host = cmd.getOptionValue("s");
		if (cmd.hasOption("n")) registryPort = Integer.parseInt(cmd.getOptionValue("n"));
	}

	/**
	 * Shorthand for creating required options.
	 */
	private static class RequiredOption extends Option {

		public RequiredOption(String opt, String longOpt, boolean hasArg, String description) throws IllegalArgumentException {
			super(opt, longOpt, hasArg, description);
			this.setRequired(true);
		}
	}
}
