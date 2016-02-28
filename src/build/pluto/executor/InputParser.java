package build.pluto.executor;

import java.io.Serializable;

public interface InputParser<In extends Serializable> {
	
	public In parseInput(Object input) throws Throwable;
}
