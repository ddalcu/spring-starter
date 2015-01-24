package app.adapters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcAdapter extends WebMvcConfigurerAdapter {

    @Bean
    public ViewInterceptorAdapter viewInterceptor() {
        return new ViewInterceptorAdapter();
    }

    public @Override void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(viewInterceptor());
    }
}
