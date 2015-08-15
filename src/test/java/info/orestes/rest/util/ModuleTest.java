package info.orestes.rest.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ModuleTest {

    private final Module module = new Module();

    private interface IObject {}

    @Test
    public final void testBind() {
        IObject instance = new IObject() {};

        assertFalse(module.isBound(IObject.class));
        try {
            module.moduleInstance(IObject.class);
            fail();
        } catch (RuntimeException e) {
        }

        module.bindInstance(IObject.class, instance);

        assertSame(instance, module.moduleInstance(IObject.class));
        assertTrue(module.isBound(IObject.class));
    }

    @Test
    public final void testImplicitBind() {
        Object instance = new Object();

        assertFalse(module.isBound(Object.class));
        Object moduleInstance1 = module.moduleInstance(Object.class);

        assertNotSame(instance, moduleInstance1);
        assertSame(moduleInstance1, module.moduleInstance(Object.class));
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

    @Test(expected = RuntimeException.class)
    public final void testNoneInjectableClass() {
        module.inject(InjectableConstr.class);
    }

    @Test
    public final void testInjectClass() {
        IObject obj = new IObject() {};
        module.bindInstance(IObject.class, obj);

        InjectableConstr test = module.inject(InjectableConstr.class);
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

    public static class IObjectImpl implements IObject {

    }

    @Test
    public void testInjectSameIdentity() throws Exception {
        module.bind(IObject.class, IObjectImpl.class);

        IObject iObject1 = module.moduleInstance(IObject.class);
        IObject iObject2 = module.moduleInstance(IObjectImpl.class);

        assertSame(iObject1, iObject2);
    }

    @Test
    public void testInjectSameInstanceIdentity() throws Exception {
        IObjectImpl instance = new IObjectImpl();
        module.bindInstance(IObject.class, instance);

        IObject iObject1 = module.moduleInstance(IObject.class);
        IObject iObject2 = module.moduleInstance(IObjectImpl.class);

        assertSame(iObject1, iObject2);
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

    @Test
    public final void testGetCurrentInstances() {
        module.bind(IObject.class, IObjectImpl.class);
        module.bind(Object.class, InjectableConstr.class);

        InjectableConstr injectableConstr = module.moduleInstance(InjectableConstr.class);
        assertNotNull(injectableConstr);

        List<?> instances = module.getCurrentInstances(Object.class);
        assertTrue(instances.remove(module));

        assertEquals(2, instances.size());

        assertTrue(instances.remove(injectableConstr));
        assertSame(injectableConstr.getObject(), instances.get(0));
    }

    public static class DefaultConstructorConstr {}

    public static class InjectableConstr {
        private final IObject object;

        @Inject
        public InjectableConstr(IObject object) {
            this.object = object;
        }

        public IObject getObject() {
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
