package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.ByteArrayTest.ByteArrays;
import io.neow3j.devpack.neo.DesignateRole;
import io.neow3j.devpack.neo.Designation;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class DesignationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(DesignationTestContract.class.getName());
    }

    @Test
    public void getName() throws IOException {
        // TODO: Test when preview4 privatenet docker image is ready.
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("Designation"));
    }

    @Test
    public void getHash() throws IOException {
        // TODO: Test when issue #292 was implemented.
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asBuffer().getValue(),
                is(Numeric.hexStringToByteArray("763afecf3ebba0a67568a2c8be06e8f068c62666")));
    }

    @Test
    public void getDesingates() throws IOException {
        // TODO: Test when preview4 privatenet docker image is ready.
        NeoInvokeFunction response = callInvokeFunction(integer(DesignateRole.STATE_VALIDATOR));
        ArrayStackItem designates = response.getInvocationResult().getStack().get(0).asArray();
        // TODO: What value is to be expected here?
        assertThat(designates.get(0).asBuffer().getValue(), is(new byte[]{}));
    }

    static class DesignationTestContract {

        public static String getName() {
            return Designation.name();
        }

        public static byte[] getHash() {
            return Designation.hash();
        }

        public static byte[][] getDesignates(byte role) {
            return Designation.getDesignatedByRole(role);
        }
    }
}
