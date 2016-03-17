package build.pluto.executor.loaddep;

public interface LoadDependencyFactory {
	public String kind();
	/**
	 * @param input YAML input
	 * @return non-null
	 */
	public LoadDependency create(Object input);
}
