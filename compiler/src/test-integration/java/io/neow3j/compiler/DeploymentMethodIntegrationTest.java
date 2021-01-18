package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Execution;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DeploymentMethodIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(DeploymentMethodIntegrationTestContract.class.getName());
    }

    @Test
    @Ignore("Test fails because the neo-node has a bug. Try again as soon as test container is up to" +
            "date with the fixed neo-node version.")
    public void callVerifyWithContractOwner() throws Throwable {
        List<Execution> executions = neow3j.getApplicationLog(deployTxHash).send()
                .getApplicationLog().getExecutions();

        assertThat(executions.get(0).getNotifications().get(0).getEventName(), is("onDeploy"));
        assertThat(
                executions.get(0).getNotifications().get(0).getState().asByteString().getAsString(),
                is("Deployed contract."));

        // Deploy event generated by ManagementContract
        assertThat(executions.get(0).getNotifications().get(1).getEventName(), is("Deploy"));
        String message = executions.get(0).getNotifications().get(1).getState().asArray().get(0)
                .asByteString().getAsString();
        assertThat(message, is(contract.getScriptHash()));
    }

    @DisplayName("Example Contract")
    @ManifestExtra(key = "author", value = "AxLabs")
    static class DeploymentMethodIntegrationTestContract {

        static Event1Arg<String> onDeploy;

        static Event1Arg<String> onUpdate;

        @OnDeployment
        public static void deploy(boolean update) {
            if (update) {
                onUpdate.notify("Updated contract.");
            }
            onDeploy.notify("Deployed contract.");
        }
    }
}