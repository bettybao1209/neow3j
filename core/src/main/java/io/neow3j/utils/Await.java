package io.neow3j.utils;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances.Nep17Balance;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Utility class used to wait for blockchain related events like the inclusion of a transaction in a
 * block or the deployment of a contract. The maximum time that these utility methods will wait for
 * a specific event is set in {@link Await#MAX_WAIT_TIME}. After that time, the methods will timeout
 * and throw an exception.
 */
public class Await {

    private final static int MAX_WAIT_TIME = 30;

    /**
     * Checks and waits until the token balance of the given address is greater than zero.
     *
     * @param address The account's address.
     * @param token   The script hash of the token to check the balance for.
     * @param neow3j  The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilBalancesIsGreaterThanZero(String address,
            ScriptHash token, Neow3j neow3j) {
        waitUntil(callableGetBalance(address, token, neow3j), Matchers.greaterThan(0L));
    }

    /**
     * Checks and waits until the contract with the given script hash is seen on the blockchain.
     *
     * @param contract The contract's script hash.
     * @param neow3j   The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilContractIsDeployed(ScriptHash contract, Neow3j neow3j) {
        waitUntil(callableGetContractState(contract, neow3j), Matchers.is(true));
    }

    /**
     * Checks and waits until the transaction with the given hash is seen on the blockchain.
     *
     * @param txHash The transaction hash.
     * @param neow3j The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilTransactionIsExecuted(String txHash, Neow3j neow3j) {
        waitUntil(callableGetTxHash(txHash, neow3j), notNullValue());
    }

    /**
     * Checks and waits until the wallet open on the neo-node has a {@code token}balance greater
     * or equal to {@code amount}.
     *
     * @param amount The amount to compare the balance to.
     * @param token The token's script hash.
     * @param neow3j The {@code Neow3j} object to use to connect to a neo-node.
     */
    public static void waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(String amount,
            ScriptHash token, Neow3j neow3j) {

        waitUntil(callableGetBalance(token, neow3j), greaterThanOrEqualTo(new BigDecimal(amount)));
    }

    private static <T> void waitUntil(Callable<T> callable, Matcher<? super T> matcher) {
        await().timeout(MAX_WAIT_TIME, TimeUnit.SECONDS).until(callable, matcher);
    }

    private static Callable<Boolean> callableGetContractState(ScriptHash contractScriptHash,
            Neow3j neow3j) {
        return () -> {
            try {
                NeoGetContractState response =
                        neow3j.getContractState(contractScriptHash.toString()).send();
                if (response.hasError()) {
                    return false;
                }
                return response.getContractState().getHash().equals("0x" +
                        contractScriptHash.toString());
            } catch (IOException e) {
                return false;
            }
        };
    }

    private static Callable<Long> callableGetBalance(String address, ScriptHash tokenScriptHash,
            Neow3j neow3j) {
        return () -> {
            try {
                List<Nep17Balance> balances = neow3j.getNep17Balances(address).send()
                        .getBalances().getBalances();
                return balances.stream()
                        .filter(b -> b.getAssetHash().equals("0x" + tokenScriptHash.toString()))
                        .findFirst()
                        .map(b -> Long.valueOf(b.getAmount()))
                        .orElse(0L);
            } catch (IOException e) {
                return 0L;
            }
        };
    }

    private static Callable<Long> callableGetTxHash(String txHash, Neow3j neow3j) {
        return () -> {
            try {
                NeoGetTransactionHeight tx = neow3j.getTransactionHeight(txHash).send();
                if (tx.hasError()) {
                    return null;
                }
                return tx.getHeight().longValue();
            } catch (IOException e) {
                return null;
            }
        };
    }

    private static Callable<BigDecimal> callableGetBalance(ScriptHash token, Neow3j neow3j) {
        return () -> {
            try {
                NeoGetWalletBalance response = neow3j.getWalletBalance(token.toString()).send();
                String balance = response.getWalletBalance().getBalance();
                return new BigDecimal(balance);
            } catch (IOException e) {
                return BigDecimal.ZERO;
            }
        };
    }
}
