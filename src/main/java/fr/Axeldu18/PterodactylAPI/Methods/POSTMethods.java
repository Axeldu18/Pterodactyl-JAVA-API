/**
MIT License

Copyright (c) 2017 Axel Vatan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fr.Axeldu18.PterodactylAPI.Methods;

import fr.Axeldu18.PterodactylAPI.PterodactylAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class POSTMethods {

	private PterodactylAPI main;

	public POSTMethods(PterodactylAPI main){
		this.main = main;
	}

	/**
	 * @param email Mail of the new user.
	 * @param username Username of the new user.
	 * @param first_name First name of the new user.
	 * @param last_name Last name of the new user.
	 * @param password Password of the new user OPTIONAL. (Leave blank will be generated by the panel randomly)
	 * @param root_admin Set the root admin role of the new user.
	 * @return if success it return the ID of the new user.
	 */
	public String createNode(String email, String username, String first_name, String last_name, String password, boolean root_admin){
		Validate.notEmpty(email, "The MAIL is required");
		Validate.notEmpty(username, "The USERNAME is required");
		Validate.notEmpty(first_name, "The FIRST_NAME is required");
		Validate.notEmpty(last_name, "The LAST_NAME is required");
		Validate.notNull(root_admin, "The ROOT_ADMIN Boolean is required");
		int admin = (root_admin) ? 1 : 0;
		return call(main.getMainURL() + Methods.USERS_CREATE_USER.getURL(), 
				"email="+email+
				"&username="+username+
				"&name_first="+first_name+
				"&name_last="+last_name+
				"&password="+password+
				"&root_admin="+admin);
	}

	/**
	 * @param name Name of the new server.
	 * @param user_id ID of a user that exists on the system to assign this server to.
	 * @param location_id ID of location in which server should be created.
	 * @param node_id ID of the node to assign this server to. Only required if auto_deploy is false OPTIONAL.
	 * @param allocation_id ID of allocation to use for the server. Only required if auto_deploy is false OPTIONAL.
	 * @param memory Total memory (in MB) to assign to the server.
	 * @param swap Total swap (in MB) to assign to the server.
	 * @param disk Total disk space (in MB) to assign to the server.
	 * @param cpu CPU limit adjustment number.
	 * @param io Block IO adjustment number.
	 * @param service_id ID of the service this server is using.
	 * @param option_id ID of the specific service option this server is using.
	 * @param startup The startup parameters this server is using.
	 * @param auto_deploy Should the server be auto-deployed to a node.
	 * @param pack_id The pack ID to use for this server.
	 * @param custom_container Pass a custom docker image to run this server with.
	 * @return if success it return the ID of the new server.
	 */
	public String createMCServer(String name, int user_id, int location_id, int node_id, int allocation_id, int memory, int swap, int disk, int cpu, int io, int service_id, int option_id, String startup, String jarName, String version, boolean auto_deploy, int pack_id, String custom_container){
		Validate.notEmpty(name, "The NAME is required");
		Validate.notNull(user_id, "The USER_ID is required");
		Validate.notNull(location_id, "The location_id is required");
		Validate.notNull(memory, "The MEMORY is required");
		Validate.notNull(swap, "The SWAP is required");
		Validate.notNull(disk, "The DISK is required");
		Validate.notNull(cpu, "The CPU is required");
		Validate.notNull(io, "The IO is required");
		Validate.notNull(service_id, "The SERVICE_ID is required");
		Validate.notNull(option_id, "The OPTION_ID is required");
		Validate.notNull(startup, "The STARTUP is required");
		Validate.notNull(jarName, "The JARNAME is required");
		Validate.notNull(version, "The VERSION is required");
		int autoDeploy = (auto_deploy) ? 1 : 0;
		JSONObject jsonServerPost = new JSONObject();
		jsonServerPost.put("name",name);
		jsonServerPost.put("user_id",node_id);
		jsonServerPost.put("location_id",location_id);
		jsonServerPost.put("node_id",node_id);
		jsonServerPost.put("allocation_id",allocation_id);
		jsonServerPost.put("memory",memory);
		jsonServerPost.put("swap",swap);
		jsonServerPost.put("disk",disk);
		jsonServerPost.put("cpu",cpu);
		jsonServerPost.put("io",io);
		jsonServerPost.put("service_id",service_id);
		jsonServerPost.put("option_id",option_id);
		jsonServerPost.put("startup",startup);
		jsonServerPost.put("env_SERVER_JARFILE",jarName);
		jsonServerPost.put("env_DL_VERSION",version);
		jsonServerPost.put("auto_deploy",autoDeploy);
		jsonServerPost.put("pack_id",pack_id);
		jsonServerPost.put("custom_container",custom_container);
		return call(main.getMainURL() + Methods.SERVERS_CREATE_SERVER.getURL(), jsonServerPost.toString());
	}

	public String call(String methodURL, String data){
		try {
			URL url = new URL(methodURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			String hmac = main.getPublicKey() + "." + main.hmac(methodURL+data);
			System.out.println("DEBUG CALL: " + methodURL);
			System.out.println("DEBUG CALL2: " + methodURL+data);
			System.out.println("DEBUG CALL3: " + hmac);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "Pterodactyl Java-API");
			connection.setRequestProperty("Authorization", "Bearer " + hmac.replaceAll("\n", ""));
			connection.setRequestProperty("Content-Type","application/json");
			connection.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(data);
			wr.flush();
			wr.close();

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return main.readResponse(connection.getInputStream()).toString();
			} else {
				return main.readResponse(connection.getErrorStream()).toString();
			}
		} catch (Exception e) {
			main.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@AllArgsConstructor
	public enum Methods{

		USERS_CREATE_USER("api/admin/users"), //Create a new user on the system.

		SERVERS_CREATE_SERVER("api/admin/servers"), //Create a new server on the panel and associated daemon.

		NODES_CREATE_NODE("api/admin/nodes"); //Creates a new node on the system.

		private @Getter String URL;
	}
}
