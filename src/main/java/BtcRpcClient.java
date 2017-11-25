import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

import com.neemre.btcdcli4j.core.client.BtcdClient;
import com.neemre.btcdcli4j.core.client.BtcdClientImpl;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * A list of examples demonstrating the use of <i>bitcoind</i>'s wallet RPCs (via the JSON-RPC
 * API) using wrapper https://github.com/priiduneemre/btcd-cli4j.
 */
public class BtcRpcClient {

    // NOTE: do the following before u run
    // 1) get some testnet bitcoins from faucet into default account
    // 2) start bitcoind locally

    public static void main(String[] args) throws Exception {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpProvider = HttpClients.custom().setConnectionManager(cm)
                .build();
        Properties nodeConfig = new Properties();
        InputStream is = new BufferedInputStream(new FileInputStream("src/main/resources/node_config.properties"));
        nodeConfig.load(is);
        is.close();

        BtcdClient client = new BtcdClientImpl(httpProvider, nodeConfig);
        client.listAccounts();

        // setup accounts
        // create account "buyer"
        String buyerAddr = client.getAccountAddress("buyer");
        System.out.println("Buyer's address: " + buyerAddr);
        // create account "seller"
        String sellerAddr = client.getAccountAddress("seller");
        System.out.println("Seller's address: " + sellerAddr);
        // create account "ESCROW"
        client.getAccountAddress("ESCROW");
        // check default account's balance
        BigDecimal defaultBalance = client.getBalance("");
        System.out.println("Default account's balance: " + defaultBalance + " BTC");
        // fund seller from default account ""
        client.move("", "seller", defaultBalance);

        // transact
        // step 1) lock up seller's bitcoins in escrow
        client.move("seller", "ESCROW", defaultBalance);
        // step 2) seller releases bitcoins from escrow after buyer pays
        client.move("ESCROW", "buyer", defaultBalance);

        client.listAccounts();

        // reset all accounts
        client.move("buyer", "", defaultBalance);
    }
}