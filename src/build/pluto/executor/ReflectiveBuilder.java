package build.pluto.executor;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.output.Output;

public class ReflectiveBuilder<In extends Serializable, Out extends Output> 
	extends Builder<ReflectiveBuilder.Input<In, Out>, Out> {

	public static class Input<In extends Serializable, Out extends Output> implements Serializable {
		private static final long serialVersionUID = 6368484213073178045L;
		public final Class<Builder<In, Out>> builderClass;
		public final In builderInput;
		
		public Input(Class<Builder<In, Out>> builderClass, In builderInput) {
			this.builderClass = builderClass;
			this.builderInput = builderInput;
		}
	}
	
	public ReflectiveBuilder(Input<In, Out> input) {
		super(input);
		loadBuilderFactory(input);
	}

	private BuilderFactory<In, Out, Builder<In,Out>> factory;
	
	@SuppressWarnings("unchecked")
	private void loadBuilderFactory(Input<In, Out> input) {
		// check for field 'factory' in builderClass 
		try {
			Field factoryField = input.builderClass.getField("factory");
			if (factoryField != null && BuilderFactory.class.isAssignableFrom(factoryField.getType()))
				factory = (BuilderFactory<In, Out, Builder<In, Out>>) factoryField.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// ignore
		}
		
		if (factory == null) {
			// try to use the BuilderFactoryFactory
			try {
				factory = BuilderFactoryFactory.of(input.builderClass, (Class<In>) input.builderInput.getClass());
			} catch (IllegalArgumentException e) {
				// ignore
			}
		}
		
		if (factory == null)
			throw new IllegalArgumentException("Could not load factory for builder " + input.builderClass);
	}

	@Override
	protected String description(Input<In, Out> input) {
		return null;
	}

	@Override
	public File persistentPath(Input<In, Out> input) {
		return null;
	}

	@Override
	protected Out build(Input<In, Out> input) throws Throwable {
		return requireBuild(factory, input.builderInput);
	}
}
