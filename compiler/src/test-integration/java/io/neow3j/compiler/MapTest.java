package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Map;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class MapTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(MapTests.class.getName());
    }

    @Test
    public void putAndGetFromMap() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello"), string("world"));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("world"));
    }

    @Test
    public void getMapValues() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello"), string("world"),
                string("olleh"), string("dlrow"));
        List<StackItem> items =
                response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(items.get(0).asByteString().getAsString(), is("world"));
        assertThat(items.get(1).asByteString().getAsString(), is("dlrow"));
    }

    @Test
    public void getMapKeys() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello"), string("world"),
                string("olleh"), string("dlrow"));
        List<StackItem> items =
                response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(items.get(0).asByteString().getAsString(), is("hello"));
        assertThat(items.get(1).asByteString().getAsString(), is("olleh"));
    }

    @Test
    public void mapContainsKey() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello"), string("world"),
                string("olleh"), string("dlrow"));
        assertThat(response.getInvocationResult().getStack().get(0).asBoolean().getValue(),
                is(true));
    }

    @Test
    public void removeFromMap() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(string("hello"), string("world"),
                string("olleh"), string("dlrow"));
        List<StackItem> items =
                response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(items.get(0).asByteString().getAsString(), is("olleh"));
    }

    static class MapTests {

        public static String putAndGetFromMap(String s1, String s2) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            return m.get(s1);
        }

        public static String[] getMapValues(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            return m.values();
        }

        public static String[] getMapKeys(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            return m.keys();
        }

        public static boolean mapContainsKey(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            return m.containsKey(s1);
        }

        public static String[] removeFromMap(String s1, String s2, String s3, String s4) {
            Map<String, String> m = new Map<>();
            m.put(s1, s2);
            m.put(s3, s4);
            m.remove(s1);
            return m.keys();
        }

    }

}
