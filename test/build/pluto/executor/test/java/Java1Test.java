package build.pluto.executor.test.java;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.executor.Executor;
import build.pluto.executor.test.ScopedBuildTest;

public class Java1Test extends ScopedBuildTest {

	@Override
	protected Collection<String> alsoCopyDirs() {
		return Arrays.asList("src");
	}
	
	@Test
	public void testBuild() throws Throwable {
		File config = getRelativeFile("pluto.yml");
		File binFile = getRelativeFile("bin/foo/Foo.class");
		BuildManagers.build(new BuildRequest<>(Executor.factory, new Executor.Input(config, "build", null)));
		Assert.assertTrue("Bin file was not generated " + binFile, FileCommands.fileExists(binFile));
	}
	
	@Test
	public void testBuildTwice() throws Throwable {
		File config = getRelativeFile("pluto.yml");
		File binFile = getRelativeFile("bin/foo/Foo.class");
		BuildManagers.build(new BuildRequest<>(Executor.factory, new Executor.Input(config, "build", null)));
		Assert.assertTrue("Bin file was not generated " + binFile, FileCommands.fileExists(binFile));
		
		long modified = binFile.lastModified();
		BuildManagers.build(new BuildRequest<>(Executor.factory, new Executor.Input(config, "build", null)));
		Assert.assertEquals("Bin-file was modified by second build (which should be nop)", modified, binFile.lastModified());
	}
}
