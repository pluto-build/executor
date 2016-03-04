package simple1;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.executor.ExecutableBuilderFactory;
import build.pluto.executor.ExecutableBuilderFactoryFactory;
import build.pluto.executor.InputParser;
import build.pluto.executor.config.yaml.YamlObject;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;

public class Simple1 extends Builder<Simple1.Input, Out<String>> {

	public static ExecutableBuilderFactory<Input, Out<String>, Simple1> factory = ExecutableBuilderFactoryFactory.of(Simple1.class, Input.class, new Parser());

	public Simple1(Input input) {
		super(input);
	}
	
	public static class Input implements Serializable {
		private static final long serialVersionUID = -4492288016711110742L;
		
		public final File target;
		public final String mode; // TODO make enum
		public final String output;
		public final File readFile;
		
		public Input(File target, String mode, String output, File readFile) {
			this.target = target;
			this.mode = mode;
			this.output = output;
			this.readFile = readFile;
		}
	}
	public static class Parser implements InputParser<Input> {
		private static final long serialVersionUID = -4814428580395067253L;

		@Override
		public Input parse(YamlObject input, String target, File workingDir) throws Throwable {
			Map<Object, YamlObject> map = input.asMap();
			String readPath = map.get("file").asString();
			File readFile = null;
			if (readPath != null) {
				readFile = new File(readPath);
				if (!readFile.isAbsolute())
					readFile = new File(workingDir, readPath);
			}
			return new Input(workingDir, target, map.get("output").asString(), readFile);
		}
	}

	@Override
	protected String description(Input input) {
		return "Simple builder 1";
	}

	@Override
	public File persistentPath(Input input) {
		return new File(input.target, "simple1.dep");
	}

	@Override
	protected Out<String> build(Input input) throws Throwable {
		if (input.mode.equals("print")) {
			return OutputPersisted.of(input.output); 
		}
		else if (input.mode.equals("read")) {
			String s = FileCommands.readFileAsString(input.readFile);
			return OutputPersisted.of(s);
		}
		else
			throw new IllegalArgumentException("Unknown mode " + input.mode);
	}
}
