package de.interaapps.punyshort.helper;

import de.interaapps.punyshort.Punyshort;
import de.interaapps.punyshort.model.database.AccessToken;
import de.interaapps.punyshort.model.database.User;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.exception.ORMQueryException;
import org.javawebstack.orm.query.Query;
import org.javawebstack.orm.wrapper.builder.SQLQueryString;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelper {

    public static <T extends Model> int sum(Query<T> query, String field) {
        return numOp(query, "sum("+field+")");
    }

    public static <T extends Model> int max(Query<T> query, String field) {
        return numOp(query, "max("+field+")");
    }

    private static <T extends Model> int numOp(Query<T> query, String select) {
        SQLQueryString qs = Repo.get(User.class).getConnection().builder().buildQuery(query.select(select));

        try {
            ResultSet rs = Repo.get(User.class).getConnection().read(qs.getQuery(), qs.getParameters().toArray());
            int c = 0;
            if (rs.next()) {
                c = rs.getInt(1);
            }

            Repo.get(User.class).getConnection().close(rs);
            return c;
        } catch (SQLException var4) {
            throw new ORMQueryException(var4);
        }
    }
}
