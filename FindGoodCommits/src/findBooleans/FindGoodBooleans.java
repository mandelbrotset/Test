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
			Commander.print("findingGoodBooleans with " + inIfs + ": commit "
					+ progress + " of " + total);
			progress++;
			ConcurrentHashSet<String> variables = Commander.commitToBooleanVariables
					.get(commit);
			ConcurrentHashSet<String> lines = Commander.commitToDiffPlus
					.get(commit);
			ConcurrentHashSet<String> goodVariables = new ConcurrentHashSet<String>();
			for (String line : lines) {
				if (line.contains("getAsBoolean")) {
					String parameterName = line.split("getAsBoolean(")[1].split(",")[0];
					Commander.goodCommits.add(commit);
					goodVariables.add(parameterName + "|" + line);
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
		if (line.contains("//"))
			result = result.split("//")[0];

		if (line.contains("/*"))
			result = result.split("/*")[0];
		// 椀渀最匀攀
		if (!result
				.matches("[A-Za-z0-9_\\.\\-(){}\\[\\]&|+*/<>\"'!;@=,:?%^#$\\\\ .]+")
				|| result.contains("delete_open_file")) {
			System.out.println(line);
			result = "Signs of fuck";
		}

		return result;
	}

}
