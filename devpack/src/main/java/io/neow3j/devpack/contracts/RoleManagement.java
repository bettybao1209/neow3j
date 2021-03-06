package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.annotations.ContractHash;

/**
 * Represents the native contract that deals with the assignment of nodes to certain roles. A node
 * can have the roles defined in {@link Role}, e.g. be an oracle node or a validator node. This
 * contract provides the functionality to check the nodes assigned a particular role.
 */
@ContractHash("0x597b1471bbce497b7809e2c8f10db67050008b02")
public class RoleManagement extends ContractInterface {

    /**
     * Gets the nodes with the given {@link Role}.
     *
     * @param role The {@link Role} to get the nodes for.
     * @param index The block index at which to get the designated nodes for.
     * @return the public keys of the nodes with the given role.
     */
    public static native ECPoint[] getDesignatedByRole(byte role, int index);

    /**
     * Designates the nodes with the given public keys to the {@link Role};
     *
     * @param role       The role of the designated nodes.
     * @param publicKeys The node's public keys.
     */
    public static native void designateAsRole(byte role, ECPoint[] publicKeys);

}
