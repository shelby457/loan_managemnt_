package com.example.loan_management_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Management System REST API")
                        .description("Comprehensive, enterprise-grade RESTful API for borrower underwriting, loan origination, EMI amortization, and repayment ledger management.")
                        .version("v2.0.0")
                        .contact(new Contact()
                                .name("Loan Management Team")
                                .email("support@loanmgmt.io"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
