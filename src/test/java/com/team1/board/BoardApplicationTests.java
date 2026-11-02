package com.team1.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BoardApplicationTests extends IntegrationTestSupport {

    @Autowired
    DataSource dataSource;

    @Test
    void contextLoads_createsMemberTable() throws Exception {
        try (Connection connection = dataSource.getConnection();
             ResultSet tables = connection.getMetaData().getTables(null, null, "member_table", null)) {
            assertThat(tables.next()).isTrue();
        }
    }

    /** REQ-SE-005: Spring Security 기본 로그인이 아니라 index 화면이 뜬다. */
    @Test
    void root_showsIndexNotSecurityLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
        assertThatThrownBy(() -> Class.forName("org.springframework.security.web.SecurityFilterChain"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
