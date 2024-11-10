package de.interaapps.punyshort.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Filterable;
import org.javawebstack.orm.annotation.Table;

@Table("shorten_link_tags")
public class ShortenLinkTag  extends Model {
    @Column
    private int id;


    @Column(size = 8)
    @Filterable
    public String linkId;

    @Column
    @Filterable
    public String tag;
}
