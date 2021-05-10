package org.ozyegin.cs.repository;

import java.util.*;
import javax.sql.DataSource;
import javax.validation.constraints.Null;

import org.ozyegin.cs.entity.Company;
import org.ozyegin.cs.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository extends JdbcDaoSupport {
    final int batchSize = 10;
    final String deleteAllPS = "DELETE FROM product";
    final String deletePS = "DELETE FROM product WHERE id=?";
    final String createPS = "INSERT INTO product (name, description, brand_name) VALUES(?,?,?)";
    final String updatePS = "UPDATE product SET name=?, description=?,brand_name=? WHERE id=?";
    final String getPS = "SELECT * FROM product WHERE id IN (:ids)";
    private final RowMapper<Integer> intRowMapper = ((resultSet, i) -> resultSet.getInt(1));

    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    private final RowMapper<Product> productRowMapper = (resultSet, i) -> {
        Product product = new Product();
        product.setName(resultSet.getString("name"));
        product.setId(resultSet.getInt("id"));
        product.setDescription(resultSet.getString("description"));
        product.setBrandName(resultSet.getString("brand_name"));
        return product;
    };

    public Product find(int id) {
        return Objects.requireNonNull(getJdbcTemplate())
                .queryForObject("SELECT * FROM product where id=?", new Object[]{id}, productRowMapper);
    }

    public List<Product> findMultiple(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        } else {
            Map<String, List<Integer>> params = new HashMap<>() {
                {
                    this.put("ids", new ArrayList<>(ids));
                }
            };
            var template = new NamedParameterJdbcTemplate(Objects.requireNonNull(getJdbcTemplate()));
            return template.query(getPS, params, productRowMapper);
        }
    }

    public List<Product> findByBrandName(String brandName) {
        return Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT * FROM product where brand_name=?", new Object[]{brandName}, productRowMapper);
    }

    public List<Integer> create(List<Product> products) {
        List<Integer> ids = Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT id FROM product", intRowMapper);

        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(createPS, products,
                batchSize,
                (ps, product) -> {
                    ps.setString(1, product.getName());
                    ps.setString(2, product.getDescription());
                    ps.setString(3, product.getBrandName());

                });
        List<Integer> idsLater = Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT id FROM product", intRowMapper);
        idsLater.removeAll(ids);

        return idsLater;
    }

    public void update(List<Product> products) {
        Objects.requireNonNull(getJdbcTemplate()).batchUpdate(
                updatePS, products, batchSize,
                (ps, product) -> {
                    ps.setString(1, product.getName());
                    ps.setString(2, product.getDescription());
                    ps.setString(3, product.getBrandName());
                    ps.setInt(4, product.getId());
                }
        );
    }

    public void delete(List<Integer> ids) {
        for (Integer id : ids) {
            Objects.requireNonNull(getJdbcTemplate()).update(deletePS,
                    id);
        }
    }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update(deleteAllPS);
    }
}
