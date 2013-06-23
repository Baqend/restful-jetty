package info.orestes.rest.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ModuleTest {
	
	private final Module module = new Module();
	
	@Test
	public final void testBind() {
		Object instance = new Object();
		
		assertFalse(module.isBound(Object.class));
		try {
			module.moduleInstance(Object.class);
			fail();
		} catch (RuntimeException e) {}
		
		module.bind(Object.class, instance);
		
		assertSame(instance, module.moduleInstance(Object.class));
		assertTrue(module.isBound(Object.class));
	}
	
	@Test
	public final void testBindClass() {
		assertFalse(module.isBound(Object.class));
		
		module.bind(Object.class, DefaultConstructorConstr.class);
		assertTrue(module.isBound(Object.class));
		
		Object test = module.moduleInstance(Object.class);
		
		assertTrue(module.isBound(Object.class));
		assertTrue(test instanceof DefaultConstructorConstr);
		assertSame(test, module.moduleInstance(Object.class));
	}
	
	@Test
	public final void testInjectClass() {
		Object obj = new Object();
		module.bind(Object.class, obj);
		
		InjectableConstr test = module.inject(InjectableConstr.class);
		assertTrue(test instanceof InjectableConstr);
		assertSame(obj, test.getObject());
	}
	
	@Test(expected = RuntimeException.class)
	public final void testInjectParamNotDefined() {
		module.inject(InjectableConstr.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public final void testInjectInvalidClass1() {
		Object obj = new Object();
		module.bind(Object.class, obj);
		
		module.inject(NotInjectableConstr.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public final void testInjectInvalidClass2() {
		module.inject(TwoInjectableConstr.class);
	}
	
	@Test(expected = RuntimeException.class)
	public final void testCycleDependency() {
		module.bind(Object.class, InjectableConstr.class);
		module.moduleInstance(InjectableConstr.class);
	}
	
	public static class DefaultConstructorConstr {}
	
	public static class InjectableConstr {
		private final Object object;
		
		@Inject
		public InjectableConstr(Object object) {
			this.object = object;
		}
		
		public Object getObject() {
			return object;
		}
	}
	
	public static class NotInjectableConstr {
		public NotInjectableConstr(Object obj) {}
	}
	
	public static class TwoInjectableConstr {
		@Inject
		public TwoInjectableConstr(Object obj) {}
		
		@Inject
		public TwoInjectableConstr(Object obj1, Object obj2) {}
	}
}
