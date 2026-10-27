package com.example.blue_hawk.infrastructure.persistence.boardgame;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;
import java.time.LocalDate;
import java.sql.Date;

@Entity
@Table(name = "board_game")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardgameJpaEntity {
    @Id
    @JdbcTypeCode(Types.CHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Convert(converter = YearDateConverter.class)
    @Column(name = "release_date")
    private int releaseYear;

    @Column(name = "player_number", nullable = false)
    private Integer playerNumber;

    @JdbcTypeCode(Types.INTEGER)
    @Column(name = "gameplay_time")
    private int playTime;

    @Converter
    public static class YearDateConverter implements AttributeConverter<Integer, Date> {
        @Override
        public Date convertToDatabaseColumn(Integer year) {
            if (year == null || year == 0) return Date.valueOf(LocalDate.now());
            return Date.valueOf(LocalDate.of(year, 1, 1));
        }

        @Override
        public Integer convertToEntityAttribute(Date dbData) {
            if (dbData == null) return null;
            return dbData.toLocalDate().getYear();
        }
    }
}
