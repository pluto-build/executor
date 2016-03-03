package build.pluto.executor;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.dependency.Origin;
import build.pluto.output.Output;

public class ReflectiveBuilding {

	private ClassLoader loader = ClassLoader.getSystemClassLoader();
	private Set<URL> loaded = new HashSet<>();
	
	public static class Input implements Serializable {
		private static final long serialVersionUID = 6368484213073178045L;
		
		public final String builderClass;
		public final Object builderInput;
		public final Origin classOrigin;
		public final List<File> classPath;
		
		public Input(String builderClass, Object builderInput, Origin classOrigin, List<File> classPath) {
			this.builderClass = builderClass;
			this.builderInput = builderInput;
			this.classOrigin = classOrigin;
			this.classPath = classPath;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private <In extends Serializable, Out extends Output> 
			ExecutableBuilderFactory<In, Out, Builder<In, Out>> 
			loadBuilderFactory(String builderClassName) throws ClassNotFoundException {
		
		Class<Builder<In, Out>> builderClass = (Class<Builder<In, Out>>) loader.loadClass(builderClassName);
		
		// check for field 'factory' in builderClass 
		try {
			Field factoryField = builderClass.getField("factory");
			if (factoryField != null && ExecutableBuilderFactory.class.isAssignableFrom(factoryField.getType()))
				return (ExecutableBuilderFactory<In, Out, Builder<In, Out>>) factoryField.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// ignore
		}

		// check for method 'factory()' in builderClass 
		try {
			Method factoryMethod = builderClass.getMethod("factory");
			if (factoryMethod != null)
				return (ExecutableBuilderFactory<In, Out, Builder<In, Out>>) factoryMethod.invoke(null);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			// ignore
		}

		throw new IllegalArgumentException("Could not load factory for builder " + builderClassName);
	}
	
	private void setupClassLoader(List<File> classPath) throws MalformedURLException {
		List<URL> urls = new ArrayList<>();
		for (File file : classPath) {
			URL url = file.toURI().toURL();
			if (loaded.add(url))
				urls.add(url);
		}
		loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), loader);
	}

	public <In extends Serializable, Out extends Output> Out 
			build(Executor executor, String builderClass, Object builderInput, List<File> classPath) 
			throws Throwable {
		
		setupClassLoader(classPath);
		ExecutableBuilderFactory<In, Out, Builder<In, Out>> factory = loadBuilderFactory(builderClass);
		InputParser<In> parser = factory.inputParser();
		
		In in = parser.parseInput(builderInput);
		return executor.requireBuild(factory, in);
	}
}
