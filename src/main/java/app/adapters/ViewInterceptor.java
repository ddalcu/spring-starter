package app.adapters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import app.models.User;

public class ViewInterceptor extends HandlerInterceptorAdapter {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(modelAndView != null && !modelAndView.getViewName().contains("error")) {
            //String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = new User();
            user.setRole("ROLE_ADMIN");
            user.setUserName("fakeAdmin");
            user.setEmail("fake@email.com");
            modelAndView.addObject("g_user", user);
        }
    }
}
