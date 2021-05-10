package org.ozyegin.cs.repository;

import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

import org.ozyegin.cs.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository extends JdbcDaoSupport {
    final String deleteAllPS = "DELETE FROM product_order";
    final String deletePS = "DELETE FROM product_order WHERE id=?";
    final String createPS = "INSERT INTO product_order (company, product_id, amount,order_date) VALUES(?,?,?,?)";
    private final RowMapper<Integer> intRowMapper = ((resultSet, i) -> resultSet.getInt(1));

    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    public Integer order(String company, int product_id, int amount, Date createdDate) {
        List<Integer> ids = Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT id FROM product_order", intRowMapper);
        Objects.requireNonNull(getJdbcTemplate()).update(createPS,
                (ps) -> {
                    ps.setString(1, company);
                    ps.setInt(2, product_id);
                    ps.setInt(3, amount);
                    ps.setDate(4, (java.sql.Date) createdDate);

                });
        List<Integer> idslater = Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT id FROM product_order", intRowMapper);
        idslater.removeAll(ids);
        return idslater.get(0);
    }

    public void delete(int id) throws Exception {
        if (Objects.requireNonNull(getJdbcTemplate()).update(deletePS,
                id) != 1) {
            throw new Exception("transaction Update is failed!");
        }
    }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update(deleteAllPS);
    }
}
