package de.interaapps.punyshort.model.database.stats;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("shorten_link_browser_stats")
public class ShortenLinkBrowserStats extends Model {
    @Column
    public int id;

    @Column(size = 8)
    public String linkId;

    @Column
    public int count = 0;

    @Column
    public ShortenLinkClickStats.Browser browser;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;
}
