package de.interaapps.punyshort.helper;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

public class DNSHelper {
    public static String getTxtRecord(String hostName) {
        java.util.Hashtable<String, String> env = new java.util.Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

        try {
            DirContext dirContext = new javax.naming.directory.InitialDirContext(env);
            Attributes attrs = dirContext.getAttributes(hostName, new String[] { "TXT" });
            Attribute attr = attrs.get("TXT");

            String txtRecord = "";

            if(attr != null) {
                txtRecord = attr.get().toString();
            }

            return txtRecord;
        } catch (javax.naming.NamingException e) {
            return "";
        }
    }
}
