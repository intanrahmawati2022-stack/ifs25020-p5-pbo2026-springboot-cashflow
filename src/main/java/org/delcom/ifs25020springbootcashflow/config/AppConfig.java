package org.delcom.ifs25020springbootcashflow.config;

import org.delcom.ifs25020springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25020springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25020springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25020springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25020springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25020springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25020springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25020springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Composition root: seluruh wiring dependency ada di sini. */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final FileStorage fileStorage;

    public AppConfig(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.fileStorage = new FileStorage(uploadDir);
    }

    @Bean
    public FileStorage fileStorage() {
        return fileStorage;
    }

    @Bean
    public SchemaInitializer schemaInitializer(JdbcTemplate jdbc) {
        return new SchemaInitializer(jdbc);
    }

    @Bean
    public IUserRepository userRepository(JdbcTemplate jdbc) {
        return new JdbcUserRepository(jdbc);
    }

    @Bean
    public ITransactionRepository transactionRepository(JdbcTemplate jdbc) {
        return new JdbcTransactionRepository(jdbc);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository users) {
        return new AuthUseCase(users);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository users) {
        return new UserUseCase(users);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactions) {
        return new FinanceUseCase(transactions);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    /** Buat tabel lalu seed admin dari ROOT_USERNAME / ROOT_PASSWORD. */
    @Bean
    public ApplicationRunner startupInitializer(SchemaInitializer schema, AuthUseCase auth,
                                                @Value("${app.root-username:admin}") String rootUser,
                                                @Value("${app.root-password:admin}") String rootPass) {
        return args -> {
            schema.init();
            auth.ensureRootAdmin(rootUser, rootPass);
        };
    }

    /** Layani /uploads/** dari folder UPLOAD_DIR (foto profil + bukti transaksi). */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileStorage.root().toUri().toString());
    }
}
