package findBooleans;

import java.util.HashSet;

import utils.ConcurrentHashSet;

public class FindGoodBooleans extends Thread {
	private boolean inIfs;

	public FindGoodBooleans(boolean inIfs) {
		this.inIfs = inIfs;
	}

	@Override
	public void run() {
		findGoodBooleans();
		super.run();
	}

	private void findGoodBooleans() {
		int total = Commander.commitToBooleanVariables.size();
		int progress = 0;
		for (String commit : Commander.commitToBooleanVariables.keySet()) {
			Commander.print("findingGoodBooleans with " + inIfs + ": commit " + progress + " of " + total);
			progress++;
			ConcurrentHashSet<String> variables = Commander.commitToBooleanVariables.get(commit);
			ConcurrentHashSet<String> lines = Commander.commitToDiffPlus.get(commit);
			ConcurrentHashSet<String> goodVariables = new ConcurrentHashSet<String>();
			for (String variable : variables) {
				for (String line : lines) {
					if (inIfs) {
						if (line.matches(".*(if).*[ ,(){}.&|=]+" + variable + "[ ,(){}.&|=]+.*")) {
							line = removeIllegalCharacters(line);
						
							Commander.goodCommits.add(commit);
							goodVariables.add(variable + "|" + line);
						}
					} else {
						String lowerLine = line.toLowerCase();
						if(lowerLine.matches(".*(([ ,(){}.]+" + variable.toLowerCase() + "[ ,(){}.]+.*(setting|propert|config).*)|(setting|propert|config).*[ ,(){}.]+" + variable.toLowerCase() + "[ ,(){}.]+).*")){
							line = removeIllegalCharacters(line);
							
							Commander.goodCommits.add(commit);
							goodVariables.add(variable + "|" + line);
						}
					}

				}
			}
			if (inIfs)
				Commander.commitToIfBoolean.put(commit, goodVariables);
			else
				Commander.commitToSettingBoolean.put(commit, goodVariables);
		}
	}
	
	private String removeIllegalCharacters(String line) {
		String result = line;
		if(line.contains("//"))
			result = result.split("//")[0];
		
		if(line.contains("/*"))
			result = result.split("/*")[0];
		
		if(!line.matches("[A-Za-z0-9_\\.\\-(){}\\[\\]&|+*/<>\"'!;@=,:?%^#$\\t .]+"))
			result = "Signs of fuck";
		
		return result;
	}

}
