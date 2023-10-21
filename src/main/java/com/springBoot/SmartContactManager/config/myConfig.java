package com.springBoot.SmartContactManager.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class myConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/user/**")).hasAnyRole("USER","ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll() 
                        .anyRequest().authenticated()
                        )
                        .formLogin(form -> form.loginPage("/login") // Need to create a controller for this request mapping
                                                // .loginProcessingUrl("/authenticateTheUser") // This request is handled by Spring, No extra coding required
                                                .loginProcessingUrl("/dologin") 
                                                // .defaultSuccessUrl("/")
                                                .defaultSuccessUrl("/user/index")
                                                .permitAll())
                        .logout(logout -> logout
                                    .logoutUrl("/logout")
                                    .permitAll());
                        // .exceptionHandling((exceptionHandling) -> 
                        //                         exceptionHandling.accessDeniedPage("/noAccess"));
        return http.build();
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource){
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        userDetailsManager.setUsersByUsernameQuery("select user.email,user.password,user.enabled from user where user.email=?");
        userDetailsManager.setAuthoritiesByUsernameQuery("select user.email,user.role from user where user.email=?");
        
        return userDetailsManager;
    }

    // @Bean
    // public BCryptPasswordEncoder bCryptPasswordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}

