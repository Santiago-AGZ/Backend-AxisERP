package com.axiserp.auth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
    "spring.jpa.hibernate.ddl-auto=none"
})
@Disabled("Requires PostgreSQL running. Run integration tests manually or with Testcontainers.")
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
