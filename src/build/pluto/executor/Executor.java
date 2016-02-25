package build.pluto.executor;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;

import build.pluto.builder.Builder;
import build.pluto.buildjava.JavaBulkCompiler;
import build.pluto.buildjava.JavaCompilerInput;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.Out;

public class Executor extends Builder<File, None> {

	public static final String PLUTO_HOME = System.getenv("PLUTO_HOME") != null ? System.getenv("PLUTO_HOME") : System.getProperty("user.home") + "/.pluto";
	
	public Executor(File input) {
		super(input);
	}

	@Override
	protected String description(File input) {
		return null;
	}

	@Override
	public File persistentPath(File input) {
		return new File(PLUTO_HOME, "executor/" + input.hashCode());
	}

	@Override
	protected None build(File input) throws Throwable {
		String builderSrc = ".";
		String builderTarget = "target/builder";
		
		JavaCompilerInput javaInput = 
				JavaCompilerInput.Builder()
				.addSourcePaths(new File(builderSrc))
				.setTargetDir(new File(builderTarget))
				// TODO class path and class origin
				.get();
		Origin classOrigin = Origin.from(JavaBulkCompiler.factory, javaInput);
		
		ReflectiveBuilder.Input reflInput = new ReflectiveBuilder.Input(
				"foo.Builder2",
				"in=\"somePath/Input.file\", out=\"somePath/Output.file\"",
				classOrigin,
				Collections.singletonList(new File(builderTarget)));
		requireBuild(ReflectiveBuilder.<Serializable, Out<String>>factory(), reflInput);
		
		return null;
	}

}
