package org.ozyegin.cs.repository;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class ProduceRepository extends JdbcDaoSupport {
  final String deleteAllPS = "DELETE FROM produce";
  final String deletePS = "DELETE FROM produce WHERE id=?";
  final String createPS = "INSERT INTO produce (company, product_id, capacity) VALUES(?,?,?)";


  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  // this is an issue too
  public Integer produce(String company, int product_id, int capacity)  {
    Objects.requireNonNull(getJdbcTemplate()).update(createPS,
            (ps) -> {
              ps.setString(1, company);
              ps.setInt(2, product_id);
              ps.setInt(3, capacity);
            });
    return null;
  }

  public void delete(int produceId) throws Exception {
    if (Objects.requireNonNull(getJdbcTemplate()).update(deletePS,
            produceId) != 1) {
      throw new Exception("Sample Update is failed!");
    }
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteAllPS);
  }
}
