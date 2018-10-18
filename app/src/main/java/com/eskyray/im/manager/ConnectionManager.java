package com.eskyray.im.manager;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.address.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.commands.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.privacy.provider.PrivacyProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.sharedgroups.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.jivesoftware.smackx.xhtmlim.provider.XHTMLExtensionProvider;

public class ConnectionManager {
    private static XMPPTCPConnection connection;
    private static String host = "192.168.31.43";
    private static int port = 5222;
    private static String serviceName = "lwb-pc";

    public static AbstractXMPPConnection getConnection() {
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    // 打开连接
    private static void openConnection() {

        XMPPTCPConnectionConfiguration.Builder builder
                = XMPPTCPConnectionConfiguration.builder();

        builder.setHost(host);
        builder.setPort(port);
        builder.setServiceName(serviceName);
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configure();
        connection = new XMPPTCPConnection(builder.build());

        try {
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭连接
    public static void release() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    public static void configure() {

        // Private Data Storage
        ProviderManager.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

        // Time
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
        }

        // Chat State
        ProviderManager.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        ProviderManager.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

        // XHTML
        ProviderManager.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

        // Group Chat Invitations
        ProviderManager.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

        // Service Discovery # Items
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

        // Service Discovery # Info
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

        // Data Forms
        ProviderManager.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

        // MUC User
        ProviderManager.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());

        // MUC Admin
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

        // MUC Owner
        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

        // Delayed Delivery
        ProviderManager.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());

        // Version
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }

        // VCard
        ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // Offline Message Requests
        ProviderManager.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        ProviderManager.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

        // Last Activity
        ProviderManager.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

        // User Search
        ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

        // SharedGroupsInfo
        ProviderManager.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        ProviderManager.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());

        // FileTransfer
        ProviderManager.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());

        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());

        // Privacy
        ProviderManager.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        ProviderManager.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
        ProviderManager.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.MalformedActionError());
        ProviderManager.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadLocaleError());
        ProviderManager.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadPayloadError());
        ProviderManager.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadSessionIDError());
        ProviderManager.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.SessionExpiredError());
    }
}

