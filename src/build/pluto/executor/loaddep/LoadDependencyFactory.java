package build.pluto.executor.loaddep;

import java.io.File;

public interface LoadDependencyFactory {
	public String kind();
	/**
	 * @param input YAML input
	 * @return non-null
	 */
	public LoadDependency create(File workingDir, Object input);
}
