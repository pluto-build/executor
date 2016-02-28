package build.pluto.executor.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
	private Map<String, Target> targets;
	private List<File> builderSource;
	private File builderTarget;
	private List<Dependency> dependencies;
	
	public void setTargets(List<Target> targets) {
		this.targets = new HashMap<>();
		for (Target t : targets)
			this.targets.put(t.getName(), t);
	}
	
	/*
	 * Needed for YAML-parsing Java Bean property targets.
	 */
	public List<Target> getTargets() {
		return new ArrayList<>(targets.values());
	}
	
	public Map<String, Target> getTargetsMap() {
		return this.targets;
	}

	public Target getTarget(String buildTarget) {
		return targets.get(buildTarget);
	}

	public List<File> getBuilderSource() {
		return builderSource;
	}

	public void setBuilderSource(List<File> builderSource) {
		this.builderSource = builderSource;
	}

	public File getBuilderTarget() {
		return builderTarget;
	}

	public void setBuilderTarget(File builderTarget) {
		this.builderTarget = builderTarget;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
}
