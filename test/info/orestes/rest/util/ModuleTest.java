package info.orestes.rest.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
		
		module.bindInstance(Object.class, instance);
		
		assertSame(instance, module.moduleInstance(Object.class));
		assertTrue(module.isBound(Object.class));
	}
	
	@Test(expected = NullPointerException.class)
	public final void testNull() {
		assertFalse(module.isBound(Object.class));
		
		module.bindInstance(Object.class, null);
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
	public final void testBindSharedClass() {
		assertFalse(module.isBound(Object.class));
		
		module.bind(Object.class, DefaultConstructorConstr.class);
		module.bind(DefaultConstructorConstr.class, DefaultConstructorConstr.class);
		
		Object o1 = module.moduleInstance(Object.class);
		Object o2 = module.moduleInstance(DefaultConstructorConstr.class);
		
		assertSame(o1, o2);
	}
	
	@Test(expected = NullPointerException.class)
	public final void testBindNullClass() {
		assertFalse(module.isBound(Object.class));
		
		module.bind(Object.class, null);
	}
	
	@Test
	public final void testInjectClass() {
		Object obj = new Object();
		module.bindInstance(Object.class, obj);
		
		InjectableConstr test = module.inject(InjectableConstr.class);
		assertTrue(test instanceof InjectableConstr);
		assertSame(obj, test.getObject());
	}
	
	@Test
	public final void testInjectNull() {
		assertFalse(module.isBound(Object.class));
		
		NullableConstructorConstr n = module.inject(NullableConstructorConstr.class);
		assertNull(n.param);
		
		module.bindInstance(Object.class, new Object());
		
		n = module.inject(NullableConstructorConstr.class);
		assertNotNull(n.param);
	}
	
	@Test(expected = RuntimeException.class)
	public final void testInjectParamNotDefined() {
		module.inject(InjectableConstr.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public final void testInjectInvalidClass1() {
		Object obj = new Object();
		module.bindInstance(Object.class, obj);
		
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
	
	public static class NullableConstructorConstr {
		public final Object param;
		
		@Inject
		public NullableConstructorConstr(@Nullable Object obj) {
			param = obj;
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
