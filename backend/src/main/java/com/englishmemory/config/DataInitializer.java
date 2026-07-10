package com.englishmemory.config;

import com.englishmemory.entity.User;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByEmailAndActiveTrue("dev@englishmemory.ai")) {
            User user = User.builder()
                    .name("Dev User")
                    .email("dev@englishmemory.ai")
                    .cefrLevel(CefrLevel.B1)
                    .timezone("America/Sao_Paulo")
                    .streakDays(0)
                    .build();
            userRepository.save(user);
            log.info("Usuário padrão de desenvolvimento criado: id=1, email=dev@englishmemory.ai");
        }
    }
}
