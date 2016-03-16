package findBooleans;

import java.util.HashSet;

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
			HashSet<String> variables = Commander.commitToBooleanVariables.get(commit);
			HashSet<String> lines = Commander.commitToDiffPlus.get(commit);
			HashSet<String> goodVariables = new HashSet<String>();
			for (String variable : variables) {
				for (String line : lines) {
					if (inIfs) {
						if (line.matches(".*(if).*[ ,(){}.&|=]+" + variable + "[ ,(){}.&|=]+.*")) {
							if (line.contains("//"))
								line = line.split("//")[0];

							if (line.contains("/*"))
								line = line.split("/*")[0];
							synchronized (Commander.goodCommits) {
								Commander.goodCommits.add(commit);
							}
							goodVariables.add(variable + "|" + line);
						}
					} else {
						String lowerLine = line.toLowerCase();
						if (lowerLine.matches(".*(([ ,(){}.]+" + variable.toLowerCase()
								+ "[ ,(){}.]+.*(setting|propert|config).*)|(setting|propert|config).*[ ,(){}.]+"
								+ variable.toLowerCase() + "[ ,(){}.]+).*")) {
							if (line.contains("//"))
								line = line.split("//")[0];

							if (line.contains("/*"))
								line = line.split("/*")[0];

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

}
