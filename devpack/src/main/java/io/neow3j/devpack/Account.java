package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_CREATESTANDARDACCOUNT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CONTRACT_ISSTANDARD;

/**
 * Offers several account-related methods for use in smart contracts.
 */
public class Account {

    /**
     * Constructs the script hash for the given public key.
     * <p>
     * More precisely, a verification script is produced from the public key and the hash of that
     * script is returned.
     *
     * @param pubKey The public key to get the script hash for.
     * @return the script hash.
     */
    @Syscall(SYSTEM_CONTRACT_CREATESTANDARDACCOUNT)
    public static native Hash160 createStandardAccount(ECPoint pubKey);

    /**
     * Checks if the account with the given script hash is a standard account.
     *
     * @param scriptHash The script hash to check.
     * @return {@code True} if it is a standard account. {@code False}, otherwise.
     */
    @Syscall(SYSTEM_CONTRACT_ISSTANDARD)
    public static native boolean isStandard(Hash160 scriptHash);

}
