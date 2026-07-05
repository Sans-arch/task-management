package com.github.sansarch.task_management.domain.group.model;

import com.github.sansarch.task_management.domain.group.exception.InvalidGroupStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Group")
class GroupTest {

    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2025, Month.JANUARY, 1, 10, 0, 0);

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create group with correct fields")
        void shouldCreateGroupWithCorrectFields() {
            Group group = Group.create("Engineering");

            assertThat(group.getName()).isEqualTo("Engineering");
            assertThat(group.getId()).isNotNull();
            assertThat(group.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        @DisplayName("should reconstitute with all provided fields")
        void shouldReconstituteWithAllProvidedFields() {
            GroupId id = GroupId.generate();

            Group group = Group.reconstitute(id, "Engineering", FIXED_DATETIME);

            assertThat(group.getId()).isEqualTo(id);
            assertThat(group.getName()).isEqualTo("Engineering");
            assertThat(group.getCreatedAt()).isEqualTo(FIXED_DATETIME);
        }
    }

    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("should throw when name is null")
        void shouldThrowWhenNameIsNull() {
            assertThatThrownBy(() -> Group.create(null))
                    .isInstanceOf(InvalidGroupStateException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("should throw when name is blank")
        void shouldThrowWhenNameIsBlank() {
            assertThatThrownBy(() -> Group.create("   "))
                    .isInstanceOf(InvalidGroupStateException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("should throw when name exceeds 255 characters")
        void shouldThrowWhenNameExceeds255Characters() {
            String longName = "a".repeat(256);

            assertThatThrownBy(() -> Group.create(longName))
                    .isInstanceOf(InvalidGroupStateException.class)
                    .hasMessageContaining("255");
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class Equality {

        @Test
        @DisplayName("should be equal when same id")
        void shouldBeEqualWhenSameId() {
            GroupId id = GroupId.generate();
            Group g1 = Group.reconstitute(id, "A", FIXED_DATETIME);
            Group g2 = Group.reconstitute(id, "B", FIXED_DATETIME);

            assertThat(g1).isEqualTo(g2);
            assertThat(g1.hashCode()).isEqualTo(g2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when different id")
        void shouldNotBeEqualWhenDifferentId() {
            Group g1 = Group.reconstitute(GroupId.generate(), "A", FIXED_DATETIME);
            Group g2 = Group.reconstitute(GroupId.generate(), "A", FIXED_DATETIME);

            assertThat(g1).isNotEqualTo(g2);
        }
    }
}
