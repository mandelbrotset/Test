package findBooleans;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import utils.ConcurrentHashSet;

public class FindVariablesBooleans extends Thread {

	private ConcurrentHashMap<String, ConcurrentHashSet<String>> list;
	private boolean plus;
	
	public FindVariablesBooleans(ConcurrentHashMap<String, ConcurrentHashSet<String>> list, boolean plus) {
		this.list = list;
		this.plus = plus;
	}
	
	private void findVariableBooleans() {
		for (String key : list.keySet()) {
			ConcurrentHashSet<String> diff = list.get(key);

			String variableName;
			ConcurrentHashSet<String> variables = new ConcurrentHashSet<String>();
			for (String line : diff) {
				if (line.contains("boolean ") && line.endsWith(";")) {
					int startIndex = line.indexOf("boolean") + 8;
					String fromBoolean = line.substring(startIndex);
					fromBoolean = fromBoolean.trim();

					int endIndex = -1;
					if (fromBoolean.indexOf(" ") != -1)
						endIndex = fromBoolean.indexOf(" ");
					else if (fromBoolean.indexOf("=") != -1)
						endIndex = fromBoolean.indexOf("=");
					else if (fromBoolean.indexOf(";") != -1)
						endIndex = fromBoolean.indexOf(";");

					variableName = fromBoolean.substring(0, endIndex);
					if (!variableName.contains(",")
							&& !variableName.contains(")")
							&& Commander.isVariable(fromBoolean)) {
						if (variableName.contains("["))
							variableName = variableName.replace("[", "");
						if (variableName.contains("]"))
							variableName = variableName.replace("]", "");
						variables.add(variableName);
					}
				}
				if (plus)
					Commander.commitToBooleanVariables.put(key, variables);
				else {
					HashSet<String> set = Commander.commitToBooleanVariables.get(key);
					if (set != null) {
						for (String name : variables)
							set.remove(name);
					}

				}

			}

		}

	}

	
	@Override
	public void run() {
		findVariableBooleans();
		super.run();
	}
	
}
