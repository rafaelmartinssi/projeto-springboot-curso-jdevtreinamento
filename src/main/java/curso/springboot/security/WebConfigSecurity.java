package curso.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	@Override //configura as solicitações de acesso por http
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
		.disable() //desativa as configurações padrão de memória.
		.authorizeRequests() //permitir restringir acessos
		.antMatchers(HttpMethod.GET, "/").permitAll() //Qualquer usuario acessa a pagina inicial
		//.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN")
		.anyRequest().authenticated()
		.and().formLogin().permitAll() //permite qualquer usuário
		.loginPage("/login")
		.defaultSuccessUrl("/cadastropessoa")
		.failureUrl("/login?error=true")
		.and().logout().logoutSuccessUrl("/login")  //mapeia URL de logout e invalida usuário autenticado
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
	@Override  //cria autenticação do usuário com banco de dados ou em memória
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(implementacaoUserDetailsService)
		.passwordEncoder(new BCryptPasswordEncoder());
		
		/*auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
		.withUser("rafael")
		.password("$2a$10$Xnjw09p8DffXlccIuqEV.OcmmdBGd71QrqmtLrvTAqiZIqa6rtky6")
		.roles("ADMIN");*/
	}
	
	@Override  //ignora URL especificas
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/materialize/**");
	}

}
