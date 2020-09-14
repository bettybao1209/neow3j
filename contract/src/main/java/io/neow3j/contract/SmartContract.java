package io.neow3j.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Numeric;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a smart contract on the Neo blockchain and provides methods to invoke and deploy it.
 */
public class SmartContract {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected ScriptHash scriptHash;
    protected Neow3j neow;
    protected NefFile nefFile;
    protected ContractManifest manifest;

    /**
     * Constructs a {@code SmartContract} representing the smart contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash The smart contract's script hash.
     * @param neow       The {@link Neow3j} instance to use for invocations.
     */
    public SmartContract(ScriptHash scriptHash, Neow3j neow) {
        if (scriptHash == null) {
            throw new IllegalArgumentException("The contract script hash must not be null.");
        }
        if (neow == null) {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.scriptHash = scriptHash;
        this.neow = neow;
    }

    /**
     * Constructs a {@code SmartContract} with a NEF file and a manifest file for deployment with
     * {@link SmartContract#deploy()}.
     *
     * @param neow         The {@link Neow3j} instance to use for deploying and invoking the
     *                     contract.
     * @param nef          The file containing the contract's code in NEF.
     * @param manifestFile The file containing the contract's manifest.
     * @throws IOException              If there is a problem reading the provided files.
     * @throws DeserializationException If the NEF file cannot be deserialized properly.
     */
    public SmartContract(File nef, File manifestFile, Neow3j neow)
            throws IOException, DeserializationException {

        this(NefFile.readFromFile(nef), objectMapper.readValue(new FileInputStream(manifestFile),
                ContractManifest.class), neow);
    }

    public SmartContract(NefFile nefFile, ContractManifest manifest, Neow3j neow)
            throws JsonProcessingException {

        if (neow == null) {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.neow = neow;
        this.nefFile = nefFile;
        this.scriptHash = this.nefFile.getScriptHash();
        this.manifest = manifest;

        if (!this.nefFile.getScriptHash().toString().equals(
                Numeric.cleanHexPrefix(this.manifest.getAbi().getHash()))) {
            throw new IllegalArgumentException("Script hash of given NEF file does not equal the "
                    + "script hash from the given manifest file.");
        }
        byte[] manifestBytes = objectMapper.writeValueAsBytes(this.manifest);
        if (manifestBytes.length > NeoConstants.MAX_MANIFEST_SIZE) {
            throw new IllegalArgumentException("The given contract manifest is too long. Manifest "
                    + "was " + manifestBytes.length + " bytes big, but a max of "
                    + NeoConstants.MAX_MANIFEST_SIZE + " is allowed.");
        }

    }

    /**
     * Initializes an {@link TransactionBuilder} for a function invocation of this contract with the
     * provided function and parameters. The order of the parameters is relevant.
     *
     * @param function           The function to invoke.
     * @param contractParameters The parameters to pass with the invocation.
     * @return An {@link TransactionBuilder} allowing to set further details of the invocation.
     */
    public TransactionBuilder invokeFunction(String function, ContractParameter... contractParameters) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException(
                    "The invocation function must not be null or empty.");
        }
        ScriptBuilder b = new ScriptBuilder()
                .contractCall(scriptHash, function, Arrays.asList(contractParameters));
        byte[] script = b.toArray();

        return new TransactionBuilder(neow).script(script);
    }

    /**
     * Does an {@code invokefunction} call to the given contract function expecting a String as
     * return type.
     *
     * @param function The function to call.
     * @param params   The contract parameters to include in the call.
     * @return the string returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as a
     *                                       String.
     */
    public String callFuncReturningString(String function, ContractParameter... params)
            throws UnexpectedReturnTypeException, IOException {

        StackItem item = callInvokeFunction(function, Arrays.asList(params))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.BYTE_STRING)) {
            return item.asByteString().getAsString();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.BYTE_STRING);
    }

    /**
     * Does an {@code invokefunction} call to the given contract function expecting an integer as
     * return type.
     *
     * @param function The function to call.
     * @param params   The contract parameters to include in the call.
     * @return the integer returned by the contract.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the returned type could not be interpreted as an
     *                                       integer.
     */
    public BigInteger callFuncReturningInt(String function, ContractParameter... params)
            throws IOException, UnexpectedReturnTypeException {

        StackItem item;
        if (params.length == 0) {
            item = callInvokeFunction(function).getInvocationResult().getStack().get(0);
        } else {
            item = callInvokeFunction(function, Arrays.asList(params))
                    .getInvocationResult().getStack().get(0);
        }
        if (item.getType().equals(StackItemType.INTEGER)) {
            return item.asInteger().getValue();
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.INTEGER);
    }

    public NeoInvokeFunction callInvokeFunction(String function, Signer... signers)
            throws IOException {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException("The invocation function must not be null or empty.");
        }
        return neow.invokeFunction(scriptHash.toString(), function, null, signers).send();
    }

    /**
     * Does an {@code invokefunction} call to the given contract function.
     *
     * @param function  The function to call.
     * @param params    The contract parameters to include in the call.
     * @param signers   The list of signers for this contract call.
     * @return The call's response.
     * @throws IOException  if something goes wrong when communicating with the neo-node.
     */
    public NeoInvokeFunction callInvokeFunction(String function, List<ContractParameter> params,
            Signer... signers)
            throws IOException {
        // Remark: The list of signers is required for `invokefunction` calls that will hit a
        // CheckWitness check in the smart contract. We add the signers even if that is not the
        // case because we cannot know if the invoked function needs it or not and it doesn't
        // lead to failures if we add them in any case.
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException("The invocation function must not be null or empty.");
        }
        if (params.isEmpty()) {
            return neow.invokeFunction(scriptHash.toString(), function, null, signers).send();
        }
        return neow.invokeFunction(scriptHash.toString(), function, params, signers).send();
    }

    public ScriptHash getScriptHash() {
        return scriptHash;
    }

    public NefFile getNefFile() {
        return nefFile;
    }

    public ContractManifest getManifest() {
        return manifest;
    }

    /**
     * Initializes an {@link TransactionBuilder} for deploying this contract. Deploys this contract
     * by creating a deployment transaction and sending it to the neo-node
     *
     * @return The Neo node's response.
     * @throws IOException If something goes wrong when communicating with the Neo node.
     */
    public TransactionBuilder deploy() throws IOException {
        if (nefFile == null) {
            throw new IllegalStateException("This smart contract instance was not constructed for"
                    + " deployment. It is missing its NEF file.");
        }
        byte[] script = new ScriptBuilder()
                .pushData(objectMapper.writeValueAsBytes(manifest))
                .pushData(nefFile.getScript())
                .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CREATE)
                .toArray();

        return new TransactionBuilder(neow)
                .script(script);
    }
}
