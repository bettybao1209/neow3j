package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import java.io.IOException;
import java.util.List;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PermissionManifestTest {

    private static final String CONTRACT_HASH_1 = "0f46dc4287b70117ce8354924b5cb3a47215ad93";
    private static final String GROUP_PUBKEY_1 =
            "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
    private static final String CONTRACT_METHOD_1 = "method1";
    private static final String CONTRACT_METHOD_2 = "method2";
    private static final String CONTRACT_HASH_2 = "d6c712eb53b1a130f59fd4e5864bdac27458a509";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void withPermissionsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(PermissionManifestTestContract.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(2));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
        assertThat(permissions.get(1).getContract(), is(CONTRACT_HASH_2));
        assertThat(permissions.get(1).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(1).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withPermissionsAnnotationSingleContractHash() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(PermissionManifestTestContractWithSingleAnnotationContractHash.class
                        .getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(2));
        assertThat(permissions.get(0).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(0).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withPermissionsAnnotationSingleGroupPubKey() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(PermissionManifestTestContractWithSingleAnnotationGroupPubKey.class
                        .getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is(GROUP_PUBKEY_1));
        assertThat(permissions.get(0).getMethods(), hasSize(2));
        assertThat(permissions.get(0).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(0).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withPermissionsAnnotationWrapper() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(
                        PermissionManifestTestContractWithPermissionsAnnotation.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(2));
        assertThat(permissions.get(0).getContract(), is(CONTRACT_HASH_1));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
        assertThat(permissions.get(1).getContract(), is(CONTRACT_HASH_2));
        assertThat(permissions.get(1).getMethods().get(0), is(CONTRACT_METHOD_1));
        assertThat(permissions.get(1).getMethods().get(1), is(CONTRACT_METHOD_2));
    }

    @Test
    public void withoutPermissionsAnnotation() throws IOException {
        CompilationUnit unit = new Compiler()
                .compileClass(PermissionManifestTestContractWithoutAnnotation.class.getName());
        List<ContractPermission> permissions = unit.getManifest().getPermissions();
        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getContract(), is("*"));
        assertThat(permissions.get(0).getMethods(), hasSize(1));
        assertThat(permissions.get(0).getMethods().get(0), is("*"));
    }

    @Test
    public void withPermissionsAnnotationButNotValidContract() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "Invalid contract hash or public key:", "invalidContractHashOrPubKey")));
        new Compiler()
                .compileClass(
                        PermissionManifestTestContractWithNotValidContractHashNorGroupKey.class
                                .getName());
    }

    @Permission(contract = CONTRACT_HASH_1)
    @Permission(contract = CONTRACT_HASH_2,
            methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContract {

        public static void main() {
        }

    }

    @Permission(contract = CONTRACT_HASH_1,
            methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContractWithSingleAnnotationContractHash {

        public static void main() {
        }

    }

    @Permission(contract = GROUP_PUBKEY_1,
            methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContractWithSingleAnnotationGroupPubKey {

        public static void main() {
        }

    }

    @Permissions({
            @Permission(contract = CONTRACT_HASH_1),
            @Permission(contract = CONTRACT_HASH_2,
                    methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    })
    static class PermissionManifestTestContractWithPermissionsAnnotation {

        public static void main() {
        }

    }

    static class PermissionManifestTestContractWithoutAnnotation {

        public static void main() {
        }

    }

    @Permission(contract = CONTRACT_HASH_1)
    @Permission(contract = "invalidContractHashOrPubKey",
            methods = {CONTRACT_METHOD_1, CONTRACT_METHOD_2})
    static class PermissionManifestTestContractWithNotValidContractHashNorGroupKey {

        public static void main() {
        }

    }


}
