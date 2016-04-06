package sakai.jp.qrwifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Cipher.DECRYPT_MODE;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new IntentIntegrator(this).initiateScan();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * ;�[����wifi�ݒ��ǉ����܂�
     *
     * @param ssid     �Ώۂ�SSID������
     * @param password �Ώۂ̃p�X���[�h
     * @return �����FnetworkID ���s:-1
     */
    private int addWifiSetting(String ssid, String password) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.preSharedKey = "\"" + password + "\"";
        int networkId = wifiManager.addNetwork(config); // ���s�����ꍇ��-1�ƂȂ�܂�
        wifiManager.saveConfiguration();
        wifiManager.updateNetwork(config);
        return networkId;
    }

    /**
     * �w���wifi�ɐڑ����܂�
     *
     * @param networkId
     * @param targetSSID
     */
    private void connectWifi(int networkId, String targetSSID) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // WiFi�@�\�������̏�ԂŌĂяo������SSID�����̏���null�ƂȂ�̂ŔO�̂��ߗ�O�������s�Ȃ�
        try {
            // ssid�̌������J�n
            wifiManager.startScan();
            for (ScanResult result : wifiManager.getScanResults()) {
                // Android4.2�ȍ~���_�u���N�H�[�e�[�V�������t���Ă���̂ŏ���
                String resultSSID = result.SSID.replace("\"", "");
                if (resultSSID.equals(targetSSID)) {
                    // �ڑ����s��
                    if (networkId != 0) {
                        // ��Ɋ����ڑ���𖳌��ɂ��Ă���ڑ�
                        for (WifiConfiguration c0 : wifiManager.getConfiguredNetworks()) {
                            wifiManager.enableNetwork(c0.networkId, false);
                        }
                        wifiManager.enableNetwork(networkId, true);
                    }
                    break;
                }
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String qrString = scanResult.getContents();
            String[] qrContents = qrString.split("\\[:\\]");
            if (qrContents.length >= 2) {
                String ssId = "";
                String password = "";
                for (int i = 0; i < qrContents.length; i++) {
                    if (i % 2 == 0) {
                        ssId = qrContents[i];
                    } else {
                        password = qrContents[i];
                        int networkId = addWifiSetting(ssId, password);
                        Toast.makeText(this,ssId + getString(R.string.done_setting),Toast.LENGTH_SHORT).show();
                    }
                }
                this.finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
