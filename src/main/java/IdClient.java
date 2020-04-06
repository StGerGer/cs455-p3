import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.UUID;

public class IdClient {

	private static String host;
	private static int registryPort = 1099;
	private static String query;

	public static void main(String[] args) {
		CommandLine input = handleArgs(args);

		try {
			Registry registry = LocateRegistry.getRegistry(host, registryPort);
			LoginRequest stub = (LoginRequest) registry.lookup("//" + host + ":" + registryPort + "/IdServer");

			// Handle all query types ---------------------------------
			// Create
			if(input.hasOption("create")) {
				String password = null;
				if(input.hasOption("password")) password = input.getOptionValue("password");
				String[] createArgs = input.getOptionValues("create");
				if(createArgs.length < 2) {
					queryCreate(stub, createArgs[0], null, password);
				} else {
					queryCreate(stub, createArgs[0], createArgs[1], password);
				}
			}
			// Lookup
			else if(input.hasOption("lookup")) {
				queryLookup(stub, input.getOptionValue("lookup"));
			}
			// Reverse lookup
			else if(input.hasOption("reverse-lookup")) {
				queryReverseLookup(stub, input.getOptionValue("reverse-lookup"));
			}
			// Modify
			if(input.hasOption("modify")) {
				String password = null;
				if(input.hasOption("password")) password = input.getOptionValue("password");
				String[] modifyArgs = input.getOptionValues("create");
				queryModify(stub, modifyArgs[0], modifyArgs[1], password);
			}
			// Delete
			if(input.hasOption("delete")) {
				String password = null;
				if(input.hasOption("password")) password = input.getOptionValue("password");
				queryDelete(stub, input.getOptionValue("delete"), password);
			}
			// Get
			if(input.hasOption("get")) {
				String option = input.getOptionValue("get");
				if(!(option.equals("users") || option.equals("uuids") || option.equals("all"))) {
					System.out.println("get expects one of: users, uuids, all");
					System.exit(1);
				}
				queryGet(stub, option);
			}
		} catch(RemoteException | NotBoundException e) {
			System.out.println("Unable to complete request: " + e.getMessage());
		}
	}

	/**
	 * Send a create query to the server.
	 * @param stub LoginRequest object returned by the registry lookup
	 * @param loginName Login name to add to the database
	 * @param realName Real name, if applicable
	 * @param password User's password, if applicable
	 * @throws RemoteException
	 */
	private static void queryCreate(LoginRequest stub, String loginName, String realName, String password) throws RemoteException {
		stub.createLoginName(loginName, realName, password);
	}

	/**
	 * Send a lookup query to the server.
	 * @param stub LoginRequest object returned by the registry lookup
	 * @param loginName Login name to find in the database
	 * @throws RemoteException
	 */
	private static void queryLookup(LoginRequest stub, String loginName) throws RemoteException {
		stub.lookup(loginName);
	}

	/**
	 * Send a reverse lookup query to the server.
	 * @param stub LoginRequest object returned by the registry lookup
	 * @param uuid UUID to reverse lookup
	 * @throws RemoteException
	 */
	private static void queryReverseLookup(LoginRequest stub, String uuid) throws RemoteException {
		stub.reverseLookup(uuid);
	}

	/**
	 * Send a modify query to the server.
	 * @param stub LoginRequest object returned by the registry lookup
	 * @param oldName Login name to replace
	 * @param newName New login name for the given user
	 * @param password User's password, if applicable
	 * @throws RemoteException
	 */
	private static void queryModify(LoginRequest stub, String oldName, String newName, String password) throws RemoteException {
		stub.modifyLoginName(oldName, newName, password);
	}

	/**
	 * Send a delete query to the server.
	 * @param stub LoginRequest object returned by the registry lookup
	 * @param loginName Name of user to delete
	 * @param password User's password, if applicable
	 * @throws RemoteException
	 */
	private static void queryDelete(LoginRequest stub, String loginName, String password) throws RemoteException {
		stub.delete(loginName, password);
	}

	/**
	 * Send a get query to the server.
	 * @param stub LoginRequest object returned by the registry lookup
	 * @param type
	 * @throws RemoteException
	 */
	private static void queryGet(LoginRequest stub, String type) throws RemoteException {
		stub.get(type);
	}

	private static CommandLine handleArgs(String[] args) {
		Options options = buildOptions();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch(ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("IdClient", options);
			System.exit(1);
		}

		host = cmd.getOptionValue("s");
		if (cmd.hasOption("n")) registryPort = Integer.parseInt(cmd.getOptionValue("n"));

		return cmd;
	}

	/**
	 * Build the set of options used by the argument parser.
	 * @return Options object with command line arguments.
	 */
	private static Options buildOptions() {
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
				.desc("Change a login name. Provide a password if user has one.")
				.build());
		queries.addOption(new Option("d", "delete", true, "Delete a login name. If the user has a password, it is required."));
		queries.addOption(new Option("g", "get", true, "(users|uuids|all) Get a list of all users, uuids, or all data."));
		// Add queries to options
		options.addOptionGroup(queries);
		options.addOption(new Option("p", "password", true, "Set a password if needed for the operation."));

		return options;
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
