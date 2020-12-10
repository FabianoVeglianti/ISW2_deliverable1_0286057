package data_retriever;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JiraIn {
/** Classe per ottenere la lista dei bug fixati da Jira*/
	private static Logger myLogger = Logger.getLogger("InfoLogging");

	public List<String> getfixedBugs(String projName) throws JSONException, IOException {
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;

		// Support list for fixedbugs
		ArrayList<String> fixedBugs = new ArrayList<>();

		// Get JSON API for closed bugs w/ AV in the project
		myLogger.info("Parsing fixed bug from Jira ...");
		JSONObject json = null;
		JSONArray issues = null;
		FileWriter fw = new FileWriter("./json.txt");
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs
			// >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + projName
					+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
					+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,resolutiondate,versions,created&startAt="
					+ i.toString() + "&maxResults=" + j.toString();

			json = readJsonFromUrl(url);
			
			
			issues = json.getJSONArray("issues");

			total = json.getInt("total");

			for (; i < total && i < j; i++) {
				// Iterate through each bug
				String key = null;

				key = issues.getJSONObject(i % 1000).get("key").toString();
			
				fixedBugs.add(key);	
				
				try {
					
					fw.write(issues.getJSONObject(i % 1000).toString()+"\n");
					
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				
				
			}
			
		} while (i < total);
		fw.flush();
		fw.close();
		
		return fixedBugs;

	}

	private JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String jsonText = readAll(rd);
		JSONObject json = new JSONObject(jsonText);
		
		is.close();
		return json;
	}


	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}