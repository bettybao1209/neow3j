package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.OnNEP11Payment;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OnNEP11PaymentMethodTest {

    private final static String ONNEP11PAYMENT_METHOD_NAME = "onNEP11Payment";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void whenUsingTheOnNEP11PaymentAnnotationTheCorrectMethodNameShouldAppearInTheManifest()
            throws IOException {

        CompilationUnit unit = new Compiler()
                .compileClass(OnNep11PaymentMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods().stream()
                .filter(m -> m.getName().equals(ONNEP11PAYMENT_METHOD_NAME))
                .collect(Collectors.toList());
        assertThat(methods, hasSize(1));
        ContractParameterType[] paramTypes = methods.get(0).getParameters().stream().map(
                ContractParameter::getParamType).toArray(ContractParameterType[]::new);
        assertThat(paramTypes, is(new Object[]{ContractParameterType.HASH160,
                ContractParameterType.INTEGER, ContractParameterType.STRING}));
        assertThat(methods.get(0).getReturnType(), is(ContractParameterType.VOID));

    }

    @Test
    public void OnNep11PaymentMethodIllegalReturnType() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "onPayment", "required to have", Hash160.class.getName(), int.class.getName(),
                String.class.getName(), void.class.getName())));
        new Compiler().compileClass(
                OnNep11PaymentMethodIllegalReturnTypeTestContract.class.getName());
    }

    @Test
    public void OnNep11PaymentMethodIllegalParameters() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "onPayment", "required to have", Hash160.class.getName(), int.class.getName(),
                String.class.getName(), void.class.getName())));
        new Compiler().compileClass(
                OnNep11PaymentMethodIllegalParametersTestContract.class.getName());
    }

    @Test
    public void throwExceptionWhenMultipleMethodsAreUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(
                asList("multiple methods", ONNEP11PAYMENT_METHOD_NAME)));
        new Compiler().compileClass(MultipleOnNep11PaymentMethodsTestContract.class.getName());
    }

    static class OnNep11PaymentMethodTestContract {

        @OnNEP11Payment
        public static void onPayment(Hash160 hash, int i, String data) {
        }
    }

    static class OnNep11PaymentMethodIllegalReturnTypeTestContract {

        @OnNEP11Payment
        public static int onPayment(Hash160 hash, int i, String data) {
            return i;
        }

    }

    static class OnNep11PaymentMethodIllegalParametersTestContract {

        @OnNEP11Payment
        public static void onPayment(Hash160 hash, String s, String data) {
        }

    }

    static class MultipleOnNep11PaymentMethodsTestContract {

        @OnNEP11Payment
        public static void onPayment1(Hash160 hash, int i, String data) {

        }

        @OnNEP11Payment
        public static void onPayment2(Hash160 hash, int i, String data) {
        }

    }
}
