package com.highlands;

/** Run manually with exec-maven-plugin and the test classpath; all DBs here are H2 in-memory. */
public class RegressionChecks {
    public static void main(String[] args) throws Exception {
        QuanLyAuthenticationCheck.main(args);
        KhachHangRegistrationCheck.main(args);
        DoanhThuIntegrationCheck.main(args);
        System.out.println("PASS: existing authentication, registration and revenue regression suites");
    }
}
