package com.gattopiccolo.portfell;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.admin.username=test-admin",
		"app.admin.password=test-password"
})
class PortfellApplicationTests {

	@Test
	void contextLoads() {
	}

}
