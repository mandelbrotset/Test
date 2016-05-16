
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class Utils {

	public static ArrayList<String> getAllMergeCommits(String repo) {
		ArrayList<String> mergeCommits = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec("bash scripts/getCommits " + repo);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String commitSHA;
			while((commitSHA = br.readLine()) != null) {
				mergeCommits.add(commitSHA);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return mergeCommits;
	}
	
	public static BufferedReader readScriptOutput(String command, boolean readOutput) throws IOException {
		
		Process p; 
		if(!readOutput) {
			p = Runtime.getRuntime().exec("bash scripts/" + command + " &> /dev/null");
			try {
				p.waitFor(10000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		p = Runtime.getRuntime().exec("bash scripts/" + command + " 2> /dev/null");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		try {
			p.waitFor(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
		//return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}
	
	public static BufferedReader readCommandOutput(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}
	
	public static void checkoutCommit(String repo, String commit) throws IOException {
		readScriptOutput("checkoutCommit " + repo + " " + commit, false);
	}
}
