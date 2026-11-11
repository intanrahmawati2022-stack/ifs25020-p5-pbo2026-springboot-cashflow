package org.delcom.ifs25020springbootcashflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Smoke test: konteks Spring naik dengan profil test (H2). */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {
    @Test
    void contextLoads() {
    }
}
