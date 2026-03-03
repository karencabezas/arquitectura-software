package com.demo.productmanagement.repository;

import com.demo.productmanagement.model.Marca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MarcaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MarcaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Marca> marcaRowMapper() {
        return (rs, rowNum) -> {
            Marca marca = new Marca();
            marca.setId(rs.getLong("id"));
            marca.setNombre(rs.getString("nombre"));
            marca.setPrecio(rs.getString("precio"));
            return marca;
        };
    }

    public List<Marca> findAll() {
        String sql = "SELECT * FROM marcas ORDER BY id";
        return jdbcTemplate.query(sql, marcaRowMapper());
    }

    public Optional<Marca> findById(Long id) {
        String sql = "SELECT * FROM marcas WHERE id = ?";
        try {
            Marca marca = jdbcTemplate.queryForObject(sql, new Object[]{id}, marcaRowMapper());
            return Optional.ofNullable(marca);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Marca save(Marca marca) {
        String sql = "INSERT INTO marcas (nombre, precio) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, marca.getNombre());
            ps.setString(2, marca.getPrecio());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            marca.setId(((Number) keys.get("id")).longValue());
        }
        return marca;
    }

    public boolean update(Marca marca) {
        String sql = "UPDATE marcas SET nombre = ?, precio = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                marca.getNombre(),
                marca.getPrecio(),
                marca.getId());
        return rowsAffected > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM marcas WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }
}

