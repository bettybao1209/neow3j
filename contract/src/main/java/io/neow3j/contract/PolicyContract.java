package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;

import io.neow3j.protocol.Neow3j;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Represents a Policy contract and provides methods to invoke it.
 */
public class PolicyContract extends SmartContract {

    private static final String NAME = "PolicyContract";
    public final static long NEF_CHECKSUM = 1136340263L;

    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NEF_CHECKSUM, NAME);

    private static final String GET_MAX_TRANSACTIONS_PER_BLOCK = "getMaxTransactionsPerBlock";
    private static final String GET_MAX_BLOCK_SIZE = "getMaxBlockSize";
    private static final String GET_MAX_BLOCK_SYSTEM_FEE = "getMaxBlockSystemFee";
    private static final String GET_FEE_PER_BYTE = "getFeePerByte";
    private static final String GET_EXEC_FEE_FACTOR = "getExecFeeFactor";
    private static final String GET_STORAGE_PRICE = "getStoragePrice";
    private static final String IS_BLOCKED = "isBlocked";
    private static final String SET_MAX_BLOCK_SIZE = "setMaxBlockSize";
    private static final String SET_MAX_TX_PER_BLOCK = "setMaxTransactionsPerBlock";
    private static final String SET_MAX_BLOCK_SYSTEM_FEE = "setMaxBlockSystemFee";
    private static final String SET_FEE_PER_BYTE = "setFeePerByte";
    private static final String SET_EXEC_FEE_FACTOR = "setExecFeeFactor";
    private static final String SET_STORAGE_PRICE = "setStoragePrice";
    private static final String BLOCK_ACCOUNT = "blockAccount";
    private static final String UNBLOCK_ACCOUNT = "unblockAccount";

    /**
     * Constructs a new <tt>PolicyContract</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public PolicyContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the maximal amount of transactions allowed per block.
     *
     * @return the maximal amount of transactions allowed per block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Integer getMaxTransactionsPerBlock() throws IOException {
        return callFuncReturningInt(GET_MAX_TRANSACTIONS_PER_BLOCK).intValue();
    }

    /**
     * Returns the maximal size allowed for a block.
     *
     * @return the maximal size allowed for a block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Integer getMaxBlockSize() throws IOException {
        return callFuncReturningInt(GET_MAX_BLOCK_SIZE).intValue();
    }

    /**
     * Returns the maximal summed up system fee allowed for a block.
     *
     * @return the maximal summed up system fee allowed for a block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getMaxBlockSystemFee() throws IOException {
        return callFuncReturningInt(GET_MAX_BLOCK_SYSTEM_FEE);
    }

    /**
     * Gets the system fee per byte.
     *
     * @return the system fee per byte.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getFeePerByte() throws IOException {
        return callFuncReturningInt(GET_FEE_PER_BYTE);
    }

    /**
     * Gets the execution fee factor.
     *
     * @return the execution fee factor.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getExecFeeFactor() throws IOException {
        return callFuncReturningInt(GET_EXEC_FEE_FACTOR);
    }

    /**
     * Gets the GAS price for one byte of smart contract storage.
     *
     * @return the storage price per byte.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getStoragePrice() throws IOException {
        return callFuncReturningInt(GET_STORAGE_PRICE);
    }

    /**
     * Checks whether an account is blocked in the Neo network.
     *
     * @param scriptHash the script hash of the account.
     * @return true if the account is blocked. False, otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isBlocked(ScriptHash scriptHash) throws IOException {
        return callFuncReturningBool(IS_BLOCKED, hash160(scriptHash));
    }

    /**
     * Creates a transaction script to set the maximal size of a block and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param maxBlockSize the maximal size of a block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxBlockSize(Integer maxBlockSize) {
        return invokeFunction(SET_MAX_BLOCK_SIZE, integer(maxBlockSize));
    }

    /**
     * Creates a transaction script to set the maximal amount of transactions per block and
     * initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param maxTxPerBlock the maximal allowed number of transactions per block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxTransactionsPerBlock(Integer maxTxPerBlock) {
        return invokeFunction(SET_MAX_TX_PER_BLOCK, integer(maxTxPerBlock));
    }

    /**
     * Creates a transaction script to set the maximal system fee per block and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param maxBlockSystemFee the maximal system fee per block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setMaxBlockSystemFee(BigInteger maxBlockSystemFee) {
        return invokeFunction(SET_MAX_BLOCK_SYSTEM_FEE,
                integer(maxBlockSystemFee));
    }

    /**
     * Creates a transaction script to set the fee per byte and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param fee the fee per byte.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setFeePerByte(Integer fee) {
        return invokeFunction(SET_FEE_PER_BYTE, integer(fee));
    }

    /**
     * Creates a transaction script to set the execution fee factor and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param fee the execution fee factor.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setExecFeeFactor(Integer fee) {
        return invokeFunction(SET_EXEC_FEE_FACTOR, integer(fee));
    }

    /**
     * Creates a transaction script to set the storage price and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param price the storage price.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder setStoragePrice(Integer price) {
        return invokeFunction(SET_STORAGE_PRICE, integer(price));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param addressToBlock the address of the account to block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(String addressToBlock) {
        return invokeFunction(BLOCK_ACCOUNT, hash160(ScriptHash.fromAddress(addressToBlock)));
    }

    /**
     * Creates a transaction script to block an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param accountToBlock the account to block.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder blockAccount(ScriptHash accountToBlock) {
        return invokeFunction(BLOCK_ACCOUNT, hash160(accountToBlock));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param addressToBlock the address of the account to unblock.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(String addressToBlock) {
        return invokeFunction(UNBLOCK_ACCOUNT, hash160(ScriptHash.fromAddress(addressToBlock)));
    }

    /**
     * Creates a transaction script to unblock an account in the neo-network and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param accountToUnblock the account to unblock.
     * @return a {@link TransactionBuilder}.
     */
    public TransactionBuilder unblockAccount(ScriptHash accountToUnblock) {
        return invokeFunction(UNBLOCK_ACCOUNT, hash160(accountToUnblock));
    }

}
