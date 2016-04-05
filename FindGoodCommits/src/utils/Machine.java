package utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;

public class Machine {
	
	private String repoPath;
	private String conflictMessage;
	private String cftFolderPath;
	
	private static Machine machine;
	
	private Machine() {
		if(new File("/home/patrik/").exists()) {
			repoPath = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/";
			conflictMessage = "KONFLIKT (innehåll): Sammanslagningskonflikt i ";
			cftFolderPath = "/home/patrik/Documents/Chalmers/5an/MasterThesis/Test/FindGoodParameters/CFT";
		} else if(new File("/home/isak/").exists()) {
			repoPath = "/home/isak/Documents/Master/projects";
			conflictMessage = "CONFLICT (content): Merge conflict in ";
			cftFolderPath = "Börst, will thau please input your shit he";
		}
	}

	public String getRepoPath() {
		return repoPath;
	}

	public String getConflictMessage() {
		return conflictMessage;
	}
	
	
	
	public String getCftFolderPath() {
		return cftFolderPath;
	}

	public static Machine getInstance() {
		if(machine == null)
			machine = new Machine();
		
		return machine;
	}
	
}
