package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForGetBlockCount;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.core.StringContains;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SmartContractTest {

    private static final ScriptHash NEO_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final ScriptHash SOME_SCRIPT_HASH =
            new ScriptHash("969a77db482f74ce27105f760efa139223431394");
    private static final String NEP17_TRANSFER = "transfer";
    private static final String NEP17_BALANCEOF = "balanceOf";
    private static final String NEP17_NAME = "name";
    private static final String NEP17_TOTALSUPPLY = "totalSupply";

    private static final String TEST_CONTRACT_1_NEF = "contracts/test_contract_1.nef";
    private static final String TEST_CONTRACT_1_MANIFEST =
            "contracts/test_contract_1.manifest.json";
    private static final String TEST_CONTRACT_1_SCRIPT_HASH =
            "0xc570e5cd068dd9f8dcdee0e4b201d70aaff61ff9";
    private static final String SCRIPT_NEO_INVOKEFUNCTION_SYMBOL = Numeric.toHexStringNoPrefix(
            new ScriptBuilder().contractCall(NEO_SCRIPT_HASH, "symbol", new ArrayList<>())
                    .toArray());

    private File nefFile;
    private File manifestFile;
    private Account account1;
    private ScriptHash recipient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Neow3j neow;

    @Before
    public void setUp() throws URISyntaxException {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        nefFile = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_NEF).toURI());
        manifestFile = new File(this.getClass().getClassLoader()
                .getResource(TEST_CONTRACT_1_MANIFEST).toURI());

        account1 = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        recipient = new ScriptHash("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void constructSmartContractWithoutScriptHash() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("script hash"));
        new SmartContract(null, neow);
    }

    @Test
    public void constructSmartContractWithoutNeow3j() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(NEO_SCRIPT_HASH, null);
    }

    @Test
    public void constructSmartContract() {
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        assertThat(sc.getScriptHash(), is(NEO_SCRIPT_HASH));
    }

    @Test
    public void constructSmartContractForDeploymentWithoutNeow3j() throws IOException,
            DeserializationException {

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("Neow3j"));
        new SmartContract(nefFile, manifestFile, null);
    }

    @Test
    public void constructSmartContractForDeployment() throws IOException,
            DeserializationException {

        SmartContract c = new SmartContract(nefFile, manifestFile, neow);
        assertThat(c.getScriptHash().toString(),
                is(Numeric.cleanHexPrefix(TEST_CONTRACT_1_SCRIPT_HASH)));
        assertThat(c.getManifest().getName(), is("neowww"));
        assertThat(c.getName(), is("neowww"));
        assertThat(c.getNefFile().getScriptHash().toString(),
                is(Numeric.cleanHexPrefix(TEST_CONTRACT_1_SCRIPT_HASH)));
    }

    @Test
    public void testGetManifest() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");
        SmartContract c = new SmartContract(SOME_SCRIPT_HASH, neow);
        ContractManifest manifest = c.getManifest();
        assertThat(manifest.getName(), is("neow3j"));
    }

    @Test
    public void testGetName() throws IOException {
        setUpWireMockForCall("getcontractstate", "contractstate.json");
        SmartContract c = new SmartContract(SOME_SCRIPT_HASH, neow);
        String name = c.getName();
        assertThat(name, is("neow3j"));
    }

    @Test
    public void constructSmartContractForDeploymentWithTooLongManifest()
            throws IOException, DeserializationException, URISyntaxException {

        File manifest = new File(this.getClass().getClassLoader()
                .getResource("contracts/too_large.manifest.json").toURI());
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("manifest is too long");
        new SmartContract(nefFile, manifest, neow);
    }

    @Test
    public void tryDeployAfterUsingWrongConstructor() throws IOException {
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        expectedException.expect(IllegalStateException.class);
        sc.deploy();
    }

    @Test
    public void invokeWithNullString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("null"));
        new SmartContract(NEO_SCRIPT_HASH, neow).invokeFunction(null);
    }

    @Test
    public void invokeWithEmptyString() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(new StringContains("empty"));
        new SmartContract(NEO_SCRIPT_HASH, neow).invokeFunction("");
    }

    @Test
    public void invokeShouldProduceCorrectScript() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NEO_SCRIPT_HASH, NEP17_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))).toArray();

        Wallet w = Wallet.withAccounts(account1);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        Transaction tx = sc.invokeFunction(NEP17_TRANSFER,
                ContractParameter.hash160(account1.getScriptHash()),
                ContractParameter.hash160(recipient),
                ContractParameter.integer(5))
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
    }

    @Test
    public void callFunctionReturningString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_symbol.json",
                SOME_SCRIPT_HASH.toString(), "symbol");
        SmartContract sc = new SmartContract(SOME_SCRIPT_HASH, neow);
        String name = sc.callFuncReturningString("symbol");
        assertThat(name, is("ant"));
    }

    @Test
    public void callFunctionReturningNonString() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP17_NAME);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.INTEGER.jsonValue()));
        sc.callFuncReturningString(NEP17_NAME);
    }

    @Test
    public void callFunctionReturningInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_totalSupply.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TOTALSUPPLY);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        BigInteger supply = sc.callFuncReturningInt(NEP17_TOTALSUPPLY);
        assertThat(supply, is(BigInteger.valueOf(3000000000000000L)));
    }

    @Test
    public void callFunctionReturningInt_withParameter() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json",
                NEO_SCRIPT_HASH.toString(), NEP17_BALANCEOF);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        BigInteger balance = sc.callFuncReturningInt(NEP17_BALANCEOF,
                ContractParameter.hash160(new ScriptHash("ec2b32ed87e3747e826a0abd7229cb553220fd7a")));
        assertThat(balance, is(BigInteger.valueOf(3)));
    }

    @Test
    public void callFunctionReturningNonInt() throws IOException {
        setUpWireMockForCall("invokefunction", "invokescript_registercandidate.json",
                NEO_SCRIPT_HASH.toString(), NEP17_TOTALSUPPLY);
        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage(new StringContains(StackItemType.BOOLEAN.jsonValue()));
        sc.callFuncReturningInt(NEP17_TOTALSUPPLY);
    }

    @Test
    public void invokingFunctionPerformsCorrectCall() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_3.json",
                NEO_SCRIPT_HASH.toString(), NEP17_BALANCEOF, account1.getScriptHash().toString());

        SmartContract sc = new SmartContract(NEO_SCRIPT_HASH, neow);
        NeoInvokeFunction response = sc.callInvokeFunction(NEP17_BALANCEOF,
                Arrays.asList(ContractParameter.hash160(account1.getScriptHash())));
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(3)));
    }
    @Test
    public void invokingFunctionPerformsCorrectCall_WithoutParameters() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_symbol_neo.json",
                NEO_SCRIPT_HASH.toString(),
                "symbol"
        );

        NeoInvokeFunction i = new SmartContract(NEO_SCRIPT_HASH, neow)
                .callInvokeFunction("symbol");

        assertThat(i.getResult().getStack().get(0).asByteString().getAsString(), Matchers.is("NEO"));
        assertThat(i.getResult().getScript(), is(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callInvokeFunction_missingFunction() throws IOException {
        new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("",
                Arrays.asList(ContractParameter.hash160(account1.getScriptHash())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callInvokeFunctionWithoutParameters_missingFunction() throws IOException {
        new SmartContract(NEO_SCRIPT_HASH, neow).callInvokeFunction("");
    }
}
