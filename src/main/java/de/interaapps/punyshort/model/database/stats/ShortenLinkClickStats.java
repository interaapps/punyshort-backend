package de.interaapps.punyshort.model.database.stats;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;

import java.sql.Timestamp;

@Dates
public class ShortenLinkClickStats extends Model {
    @Column
    public int id;

    @Column(size = 8)
    public String linkId;

    @Column(size = 4)
    public String countryCode;

    @Column
    public OperatingSystem operatingSystem;

    @Column
    public Browser browser;

    @Column
    public String referrer;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public enum OperatingSystem {
        IOS("ios", "iphone", "ipad"),
        ANDROID("android"),
        LINUX("linux", "ubuntu"),
        MAC("mac"),
        WINDOWS("win"),
        OTHER;

        public final String[] searchFor;

        OperatingSystem(String... searchFor) {
            this.searchFor = searchFor;
        }

        public static OperatingSystem find(String userAgent) {
            for (OperatingSystem os : OperatingSystem.values()) {
                for (String s : os.searchFor) {
                    if (userAgent.toLowerCase().contains(s)) {
                        return os;
                    }
                }
            }
            return OTHER;
        }
    }

    public enum Browser {
        MSIE("internet explorer"),
        FIREFOX("firefox"),
        CHROME("chrome"),
        OPERA("opera"),
        MAXTHON("maxthon"),
        KONQUEROR("konqueror"),
        NETSCAPE("netscape"),
        SAFARI("safari"),
        MOBILE("mobile"),
        OTHER;

        public final String[] searchFor;

        Browser(String... searchFor) {
            this.searchFor = searchFor;
        }

        public static Browser find(String userAgent) {
            for (Browser os : Browser.values()) {
                for (String s : os.searchFor) {
                    if (userAgent.toLowerCase().contains(s)) {
                        return os;
                    }
                }
            }
            return OTHER;
        }
    }
}
