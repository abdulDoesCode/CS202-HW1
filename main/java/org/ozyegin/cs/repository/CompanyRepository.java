package org.ozyegin.cs.repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

import org.ozyegin.cs.entity.Company;
import org.ozyegin.cs.entity.Product;
import org.ozyegin.cs.entity.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepository extends JdbcDaoSupport {
    final String getSinglePS = "SELECT * FROM company WHERE name=?";
    final String createPS = "INSERT INTO company (name,zip,country,street,phone) VALUES(?,?,?,?,?)";
    final String deletePS = "DELETE FROM company WHERE name=?";
    final String deleteAllPS = "DELETE FROM company ";


    @Autowired
    public void setDatasource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    private final RowMapper<Company> companyRowMapper = (resultSet, i) -> {
        Company company = new Company();
        company.setName(resultSet.getString("name"));
        company.setZip(resultSet.getInt("zip"));
        company.setCountry(resultSet.getString("country"));
        company.setStreetInfo(resultSet.getString("street"));
        company.setPhoneNumber(resultSet.getString("phone"));
        company.setE_mails(emails(resultSet.getString("name")));
//        company.setCity(resultSet.getString("city"));
        return company;
    };

    public Company findCompany(String name) throws Exception {
        Company company;
        try {
            company = Objects.requireNonNull(getJdbcTemplate()).queryForObject(getSinglePS,
                    new Object[]{name},
                    companyRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new Exception("company not found!");
        }
        return company;
    }

    public List<Company> findByCountry(String country) {
        return Objects.requireNonNull(getJdbcTemplate())
                .query("SELECT * FROM company where country=?", new Object[]{country}, companyRowMapper);
    }


    public String create(Company company) throws Exception {
        if (Objects.requireNonNull(getJdbcTemplate()).queryForList("SELECT * FROM company WHERE zip=?", company.getZip()).isEmpty()) {
            Objects.requireNonNull(getJdbcTemplate()).update("INSERT INTO zip_city (zip,city) VALUES(?,?)",
                    (ps) -> {
                        ps.setInt(1, company.getZip());
                        ps.setString(2, company.getCity());
                    });
        }
        Objects.requireNonNull(getJdbcTemplate()).update(createPS,
                (ps) -> {
                    ps.setString(1, company.getName());
                    ps.setInt(2, company.getZip());
                    ps.setString(3, company.getCountry());
                    ps.setString(4, company.getStreetInfo());
                    ps.setString(5, company.getPhoneNumber());
                });
        creatEmails(company.getName(), company.getE_mails());
        return company.getName();
    }

    public String delete(String name) throws Exception {
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM emails");
        if (Objects.requireNonNull(getJdbcTemplate()).update(deletePS,
                name) != 1) {
            throw new Exception("company Update is failed!");
        }
        return name;
    }

    public void deleteAll() {
        Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM emails");
        Objects.requireNonNull(getJdbcTemplate()).update(deleteAllPS);
    }

    public List<String> emails(String name) {
        return Objects.requireNonNull(getJdbcTemplate()).query("SELECT email FROM emails WHERE name=?",
                new Object[]{name}, (ps, rows) ->
                        ps.getString("email")
        );
    }

    public void creatEmails(String name, List<String> emails) {
        for (String email : emails) {
            Objects.requireNonNull(getJdbcTemplate()).update("INSERT INTO emails (name,email) VALUES (?,?)",
                    preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setString(2, email);
                    });
        }
    }

}
