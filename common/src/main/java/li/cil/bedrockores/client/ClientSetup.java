/* SPDX-License-Identifier: MIT */

package li.cil.bedrockores.client;

public final class ClientSetup {
    public static void initialize() {
        ClientNetwork.initialize();
    }

    private ClientSetup() {
    }
}
