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
			if (new File("/home/isak/.desktop").exists()) {
				repoPath = "/home/isak/Documents/Master/projects";
				conflictMessage = "CONFLICT (content): Merge conflict in ";
				cftFolderPath = "/home/isak/Documents/Master/Test/CFT";
			} else {
				repoPath = "/home/isak/Documents/Master/projects";
				conflictMessage = "CONFLICT (content): Merge conflict in ";
				cftFolderPath = "/home/isak/Documents/Master/Test/CFT";
			}
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
