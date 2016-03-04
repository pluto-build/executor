package build.pluto.executor.test.simple1;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.executor.Executor;
import build.pluto.executor.test.ScopedBuildTest;
import build.pluto.output.Out;
import build.pluto.output.Output;

public class Simple1Test extends ScopedBuildTest {

	@Override
	protected Collection<String> alsoCopyDirs() {
		return Arrays.asList("builder");
	}
	
	@Test
	public void testPrint() throws Throwable {
		File config = getRelativeFile("pluto.yml");
		Output out = BuildManagers.build(new BuildRequest<>(Executor.factory, new Executor.Input(config, "print", null)));
		Assert.assertNotNull(out);
		Assert.assertTrue("builder output should be of type Out, but was " + out, out instanceof Out<?>);
		Out<?> o = (Out<?>) out;
		Assert.assertTrue("builder should output a string, but was " + o, o.val() instanceof String);
		Assert.assertEquals("Please print this text.", o.val());
	}

	@Test
	public void testRead() throws Throwable {
		File config = getRelativeFile("pluto.yml");
		Output out = BuildManagers.build(new BuildRequest<>(Executor.factory, new Executor.Input(config, "read", null)));
		Assert.assertNotNull(out);
		Assert.assertTrue("builder output should be of type Out, but was " + out, out instanceof Out<?>);
		Out<?> o = (Out<?>) out;
		Assert.assertTrue("builder should output a string, but was " + o, o.val() instanceof String);
		Assert.assertEquals("Please read this text.", o.val());
	}

}
