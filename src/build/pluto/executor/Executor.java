package build.pluto.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import build.pluto.output.None;
import build.pluto.output.Out;
import build.pluto.output.Output;

public class Executor extends Builder<Executor.Input, None> {

	public static final String PLUTO_HOME = System.getenv("PLUTO_HOME") != null ? System.getenv("PLUTO_HOME") : System.getProperty("user.home") + "/.pluto";
	
	public static BuilderFactory<Input, None, Executor> factory = BuilderFactoryFactory.of(Executor.class, Input.class);
	
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
	protected None build(Input input) throws Throwable {
		require(input.plutoConfig);
		Yaml yaml = new Yaml(new Constructor(Config.class));
		Config config = (Config) yaml.load(new FileInputStream(input.plutoConfig));
		Target target = config.getTarget(input.buildTarget);
		
		// TODO use extraInput to override config
		
		String sourceFilePath = target.getBuilder().replace('.', '/') + ".java";
		File sourceFile = null;
		if (config.getBuilderSource() != null)
			for (File dir : config.getBuilderSource()) {
				File f = new File(dir, sourceFilePath);
				if (f.exists()) {
					sourceFile = f;
					break;
				}
			}
		
		List<File> dependencies = new ArrayList<>();
		// TODO feed in dependencies form config
		// origin.add(dependency build);

		if (sourceFile != null) {
			JavaCompilerInput javaInput = 
					JavaCompilerInput.Builder()
					.addSourcePaths(config.getBuilderSource())
					.setTargetDir(config.getBuilderTarget())
					.addClassPaths(dependencies)
					.get();
			requireBuild(JavaBulkCompiler.factory, javaInput);
		}
		
		
		dependencies.add(config.getBuilderTarget());
		ReflectiveBuilding reflective = new ReflectiveBuilding();
		Out<String> out = reflective.build(this, target.getBuilder(), target.getInput(), dependencies);
		System.out.println(out.val());
		
		return null;
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

	
	public static void main(String[] args) throws Throwable {
		File plutoConfig = new File("/Users/seba/projects/build/bootstrapp/test/maven/pluto.yml");
		String target = args[0];
		String extraInput = StringCommands.printListSeparated(args, " ").substring(target.length());
		BuildManagers.clean(false, new BuildRequest<>(factory, new Executor.Input(plutoConfig, target, extraInput)));
		
		BuildManagers.build(new BuildRequest<>(factory, new Executor.Input(plutoConfig, target, extraInput)));
	}
}
