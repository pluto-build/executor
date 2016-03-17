package build.pluto.executor.loaddep;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoadDependencyRegistry {

	private final static Map<String, LoadDependencyFactory> factories = new HashMap<>();
	
	public static synchronized void registerFactory(LoadDependencyFactory factory) {
		factories.put(factory.kind(), factory);
	}
	
	public static LoadDependency get(String kind, Object input, File workingDir) {
		LoadDependencyFactory factory;
		synchronized (LoadDependencyRegistry.class) {
			factory = factories.get(kind);
		}
		
		if (factory == null)
			throw new IllegalArgumentException("Dependency kind '" + kind + "' not supported");
		
		return factory.create(workingDir, input);
	}
}
