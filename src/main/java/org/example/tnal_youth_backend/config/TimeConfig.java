package org.example.tnal_youth_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Primary;

@Configuration
public class TimeConfig {

    private static final ZoneId CAMBODIA_ZONE =
            ZoneId.of("Asia/Phnom_Penh");

    @Bean
    @Primary
    public Clock utcClock() {
        return Clock.system(ZoneOffset.UTC);
    }

    @Bean("systemClock")
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    /*
     * The EB instance's JVM default zone is UTC, and Jakarta Bean
     * Validation's @Past/@Future/@PastOrPresent/@FutureOrPresent read that
     * default unless told otherwise (a completely separate mechanism from
     * the Clock beans above, which only affect code that explicitly
     * @Autowired a Clock). Every user of this app is in Cambodia
     * (UTC+7), so for roughly the first 7 hours of every Cambodia
     * calendar day, UTC is still "yesterday" -- a date a user picks as
     * today gets rejected by @PastOrPresent as being in the future
     * (this is exactly what broke same-day activity expense entries).
     * Registering our own LocalValidatorFactoryBean overrides Spring
     * Boot's auto-configured one (which otherwise wins by default) so
     * every such annotation across the whole app evaluates "now" in the
     * zone the user actually experiences it in.
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factoryBean =
                new LocalValidatorFactoryBean();

        factoryBean.setConfigurationInitializer(
                configuration -> configuration.clockProvider(
                        () -> Clock.system(CAMBODIA_ZONE)
                )
        );

        return factoryBean;
    }
}
