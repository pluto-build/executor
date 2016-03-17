package build.pluto.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sugarj.common.FileCommands;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildjava.JavaBulkCompiler;
import build.pluto.buildjava.JavaCompilerInput;
import build.pluto.dependency.Origin;
import build.pluto.executor.config.Config;
import build.pluto.executor.config.Dependency;
import build.pluto.executor.config.Target;
import build.pluto.executor.config.yaml.SimpleYamlObject;
import build.pluto.executor.loaddep.LoadDependency;
import build.pluto.executor.loaddep.LoadDependencyRegistry;
import build.pluto.output.Out;
import build.pluto.output.Output;

import com.cedarsoftware.util.DeepEquals;

public class Executor extends Builder<Executor.Input, Output> {

	public static final String PLUTO_HOME = System.getenv("PLUTO_HOME") != null ? System.getenv("PLUTO_HOME") : System.getProperty("user.home") + "/.pluto";
	
	public static final Class<?>[] DEPENDENCIES = {
		org.sugarj.common.FileCommands.class, // org.sugarj:common
		com.cedarsoftware.util.DeepEquals.class, // com.cedarsoftware:java-util
		build.pluto.builder.BuildManager.class, // build.pluto:pluto
		build.pluto.executor.Executor.class // build.pluto:executor
	};
	
	public static String javaVersion() {
		String version = System.getProperty("java.version");
		int i = version.indexOf('.', version.indexOf('.')+1); // find second '.'
		return version.substring(0, i);
	}
	
	public static BuilderFactory<Input, Output, Executor> factory = BuilderFactoryFactory.of(Executor.class, Input.class);
	
	public static class Input implements Serializable {
		private static final long serialVersionUID = -6190709839488536335L;
		
		public final File plutoConfig;
		public final String buildTarget;
		public final String extraInput;
		
		public Input(File plutoConfig, String buildTarget, String extraInput) {
			this.plutoConfig = plutoConfig;
			this.buildTarget = buildTarget;
			this.extraInput = extraInput;
		}
	}
	
	public Executor(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Execute target " + input.buildTarget;
	}

	@Override
	public File persistentPath(Input input) {
		return new File(PLUTO_HOME, "executor/" + DeepEquals.deepHashCode(input));
	}

	@Override
	protected Output build(Input input) throws Throwable {
		require(input.plutoConfig);
		Yaml yaml = new Yaml(new Constructor(Config.class));
		Config config = (Config) yaml.load(new FileInputStream(input.plutoConfig));
		File workingDir = input.plutoConfig.getParentFile();
		config.makePathsAbsolute(workingDir);
		Target target = config.getTarget(input.buildTarget);
		
		// TODO use extraInput to override config
		
		
		List<File> dependencies = new ArrayList<>();
		for (Class<?> cl : DEPENDENCIES) {
			Path path = FileCommands.getRessourceContainer(cl);
			if (path != null)
				dependencies.add(path.toFile());
			else
				throw new IllegalStateException("Could not find ressource for class " + cl);
		}
		
		if (config.getDependencies() != null)
			for (Dependency dep : config.getDependencies()) {
				LoadDependency loadDep = LoadDependencyRegistry.get(dep.kind, dep.input, workingDir);
				List<File> files = loadDep.loadSimple();
				if (files == null) {
					Origin depOrigin = loadDep.loadComplex();
					Collection<? extends Output> outputs = requireBuild(depOrigin);
					files = loadDep.filesFromOutputs(outputs);
				}
				dependencies.addAll(files);
			}

		if (config.getBuilderSourceDirs() != null) {
			List<File> sourceFiles = new ArrayList<>();
			for (File sourceDir : config.getBuilderSourceDirs())
				sourceFiles.addAll(FileCommands.listFilesRecursive(sourceDir));
			
			JavaCompilerInput javaInput = 
					JavaCompilerInput.Builder()
					.addSourcePaths(config.getBuilderSourceDirs())
					.setTargetDir(config.getBuilderTargetDir())
					.addClassPaths(dependencies)
					.addInputFiles(sourceFiles)
					.setTargetRelease(javaVersion())
					.setSourceRelease(javaVersion())
					.get();
			requireBuild(JavaBulkCompiler.factory, javaInput);
		}
		
		dependencies.add(config.getBuilderTargetDir());
		ReflectiveBuilding reflective = new ReflectiveBuilding();
		Out<String> out = reflective.build(this, target.getName(), workingDir, dependencies, target.getBuilder(), SimpleYamlObject.of(target.getInput()));
		
		return out;
	}

	@Override
    protected 
  //@formatter:off
    <In_ extends Serializable, 
     Out_ extends Output, 
     B_ extends Builder<In_, Out_>, 
     F_ extends BuilderFactory<In_, Out_, B_>, 
     SubIn_ extends In_>
  //@formatter:on
    Out_ requireBuild(F_ factory, SubIn_ input) throws IOException {
      return super.requireBuild(factory, input);
    }
}
