package app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("=============ADDING INTERCEPTOR================");
        registry.addInterceptor(new ApplicationInterceptor());
    }
    
    class ApplicationInterceptor extends HandlerInterceptorAdapter {
        
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            if(modelAndView != null) {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                modelAndView.addObject("global_username", username);
            }
        }

    }
}
