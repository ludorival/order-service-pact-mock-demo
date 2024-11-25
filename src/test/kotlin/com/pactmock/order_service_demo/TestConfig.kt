import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun mockedRestTemplate(): RestTemplate {
        return mockk<RestTemplate>()
    }
}