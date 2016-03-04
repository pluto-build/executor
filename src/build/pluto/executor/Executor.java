package build.pluto.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildjava.JavaBulkCompiler;
import build.pluto.buildjava.JavaCompilerInput;
import build.pluto.executor.config.Config;
import build.pluto.executor.config.Target;
import build.pluto.output.Out;
import build.pluto.output.Output;

public class Executor extends Builder<Executor.Input, Output> {

	public static final String PLUTO_HOME = System.getenv("PLUTO_HOME") != null ? System.getenv("PLUTO_HOME") : System.getProperty("user.home") + "/.pluto";
	
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
		return null;
	}

	@Override
	public File persistentPath(Input input) {
		return new File(PLUTO_HOME, "executor/" + input.hashCode());
	}

	@Override
	protected Output build(Input input) throws Throwable {
		require(input.plutoConfig);
		Yaml yaml = new Yaml(new Constructor(Config.class));
		Config config = (Config) yaml.load(new FileInputStream(input.plutoConfig));
		config.makePathsAbsolute(input.plutoConfig);
		Target target = config.getTarget(input.buildTarget);
		
		// TODO use extraInput to override config
		
		
		List<File> dependencies = new ArrayList<>();
		// TODO feed in dependencies form config

		if (config.getBuilderSource() != null) {
			List<File> sourceFiles = new ArrayList<>();
			for (File sourceDir : config.getBuilderSource())
				sourceFiles.addAll(FileCommands.listFilesRecursive(sourceDir));
			
			JavaCompilerInput javaInput = 
					JavaCompilerInput.Builder()
					.addSourcePaths(config.getBuilderSource())
					.setTargetDir(config.getBuilderTarget())
					.addClassPaths(dependencies)
					.addInputFiles(sourceFiles)
					.get();
			requireBuild(JavaBulkCompiler.factory, javaInput);
		}
		
		dependencies.add(config.getBuilderTarget());
		ReflectiveBuilding reflective = new ReflectiveBuilding();
		Out<String> out = reflective.build(this, target.getBuilder(), target.getInput(), dependencies);
		System.out.println(out.val());
		
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
