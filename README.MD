# SPRING APP TEMPLATE

Plantilla backend en **Spring Boot 3.5** con JWT, roles/permiso dinámico y gestión de usuarios.

## 📌 Resumen del proyecto

Este proyecto es una API REST segura para autenticación y administración de usuarios, roles y permisos. Está diseñado para funcionar con PostgreSQL y utiliza filtros personalizados para el control de acceso basado en JWT y permisos almacenados en la base de datos.

## 🧩 Tecnologías principales

- Java 21
- Spring Boot 3.5.8
- Spring Data JPA
- Spring Security
- Spring Web
- Spring Validation
- PostgreSQL
- JWT con `io.jsonwebtoken` (JJWT)
- Lombok
- Docker / Docker Compose

## 📁 Estructura importante

- `src/main/java/com/server/app/AppApplication.java` - punto de entrada de Spring Boot.
- `src/main/java/com/server/app/config/` - configuración de seguridad, CORS y JWT.
- `src/main/java/com/server/app/filters/` - filtros de autenticación y autorización dinámica.
- `src/main/java/com/server/app/components/SaveEndpoints.java` - registra las rutas de la app en la tabla `permissions`.
- `src/main/java/com/server/app/controllers/` - controladores REST para auth, usuarios, roles y permisos.
- `src/main/java/com/server/app/services/` - lógica de negocio para usuarios, roles y permisos.
- `src/main/java/com/server/app/entities/` - modelo de datos JPA.
- `src/main/java/com/server/app/repositories/` - acceso a datos con Spring Data JPA.
- `src/main/resources/application.properties` - configuración del entorno.
- `script.sql` - inicialización de roles y asignación de permisos.
- `Dockerfile` / `docker-compose.yml` - despliegue con contenedores.

## 🔒 Diseño de seguridad

### Seguridad general

- `SecurityConfig` habilita seguridad sin estado (`STATELESS`) y desactiva CSRF.
- Todos los endpoints se permiten inicialmente en Spring Security, porque la autorización se realiza en los filtros personalizados.
- Se aplican dos filtros en esta orden:
  1. `JwtAuthenticationFilter` - valida el token y carga el usuario.
  2. `DynamicAuthorizationFilter` - verifica permisos por método y ruta.

### Autenticación JWT

- `JsonWebToken` crea y valida tokens JWT.
- El token guarda el `id` del usuario en sus claims.
- `JwtAuthenticationFilter`:
  - extrae el header `Authorization: Bearer <token>`.
  - valida expiración y contenido.
  - carga el `User` desde la base de datos.
  - construye authorities a partir de permisos: `METHOD:/ruta`.
  - agrega también la autoridad `ROLE_<ROL>`.

### Autorización dinámica

- `DynamicAuthorizationFilter` compara la ruta y el método HTTP contra las autoridades del usuario.
- Si no existe autorización exacta, devuelve `403 Forbidden`.
- Hay rutas públicas y rutas que solo requieren autenticación, definidas en `SecurityRules`.

## 🧠 Registro automático de permisos

`SaveEndpoints` detecta todas las rutas expuestas por Spring al arrancar la aplicación y guarda en la tabla `permissions` los endpoints que no son públicos ni ignorados.

Esto permite registrar automáticamente nuevas rutas en la base de datos sin tener que insertarlas manualmente.

## 🗄️ Modelo de datos

### User

- `id` - entero auto generado.
- `username` - único, no nulo.
- `email` - único, no nulo.
- `password` - cifrado con BCrypt antes de persistir.
- `blocked` - booleano de bloqueo de cuenta.
- `role` - relación `ManyToOne` con `Role`.

### Role

- `id` - entero auto generado.
- `name` - único, no nulo.
- `permissions` - relación `ManyToMany` con `Permission` a través de la tabla intermedia `role_permissions`.

### Permission

- `id` - entero auto generado.
- `path` - ruta HTTP.
- `method` - método HTTP.
- `title` - nombre descriptivo opcional.

## 📜 SQL de inicialización (`script.sql`)

Este archivo realiza:

- Inserción de roles `ADMIN` y `USER`.
- Asignación de todos los permisos a `ADMIN`.
- Asignación de permisos `GET` y el permiso de cambio de contraseña a `USER`.

### Contenido principal

- `INSERT INTO roles (name) VALUES ('ADMIN'), ('USER')`
- `role_permissions` se llena con permisos existentes según método y rol.
- Se agrega manualmente el permiso `id = 13` al rol `USER` para actualización de contraseña.

> Nota: `script.sql` asume que ya existen permisos registrados en la tabla `permissions`. El componente `SaveEndpoints` los crea automáticamente en el arranque.

## 📦 Endpoints disponibles

### Auth

- `POST /api/auth/login`
  - body: `{ "username": "...", "password": "..." }`
  - devuelve token JWT y datos de usuario.

- `POST /api/auth/signup`
  - body: `{ "username", "name", "surname", "email", "password" }`
  - crea usuario con rol por defecto `USER` y retorna token.

- `GET /api/auth/profile`
  - requiere token.
  - devuelve datos del usuario autenticado.

- `PUT /api/auth/update/profile`
  - requiere token.
  - body: `{ "username", "name", "surname", "email" }`
  - actualiza perfil del usuario actual.

- `PUT /api/auth/update/password`
  - requiere token.
  - body: `{ "oldpassword", "newpassword", "confirmpassword" }`
  - cambia contraseña del usuario autenticado.

### Usuarios

- `POST /api/users`
  - crea un usuario nuevo.
  - body similar a signup más `role` opcional.

- `PUT /api/users/{id}`
  - actualiza usuario existente.
  - puede cambiar `username`, `name`, `surname`, `email`, `blocked`, `role`.

- `GET /api/users`
  - paginación: `page`, `size`, `search`.

- `GET /api/users/{id}`
  - obtiene usuario por ID.

### Roles

- `POST /api/roles`
  - crea rol con permisos.
  - body: `{ "name", "permissions": [{ "id" }, ...] }`

- `PUT /api/roles/{id}`
  - actualiza nombre y permisos de rol.

- `GET /api/roles`
  - paginación: `page`, `size`.

- `GET /api/roles/{id}`
  - obtiene rol por ID.

- `DELETE /api/roles/{id}`
  - elimina rol.

### Permisos

- `GET /api/permissions`
  - lista permisos con paginación.

- `GET /api/permissions/{id}`
  - obtiene permiso por ID.

- `PUT /api/permissions/{id}`
  - actualiza el `title` de un permiso.

## 🧾 DTOs y respuestas

- `LoginDto` - login básico.
- `UpdatePasswordDto` - cambio de contraseña.
- `UserCreateDto` - creación de usuario con validaciones.
- `UserUpdateDto` - actualización parcial de usuario.
- `UpdateProfileDto` - edición de perfil de usuario.
- `RoleDto` - nombre de rol y lista de permisos.
- `PermissionDto` - título descriptivo del permiso.
- `AuthResponse` - token JWT y datos del usuario.
- `Pagination<T>` / `PaginationMeta` - paginación estándar.

## ⚙️ Variables de entorno usadas

- `SPRING_APPLICATION_NAME`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_SHOW_SQL`
- `SPRING_DATA_WEB_PAGEABLE_ONE_INDEXED_PARAMETERS`
- `SERVER_PORT`
- `CORS_ALLOWED_ORIGINS`
- `SECURITY_JWT_SECRET_KEY`
- `SECURITY_JWT_EXPIRATION_TIME`

## 🚀 Ejecución local

```bash
./mvnw clean package
./mvnw spring-boot:run
```

O con Maven instalado:

```bash
mvn clean package
mvn spring-boot:run
```

## 🐳 Ejecución con Docker

```bash
docker compose build
docker compose up -d
docker compose down -v
```

También puedes construir la imagen directamente:

```bash
docker build -t spring-app .
```

## 🔧 Observaciones del código

- `SecurityConfig` delega la autorización a filtros personalizados.
- `JwtAuthenticationFilter` audita token y carga usuario.
- `DynamicAuthorizationFilter` protege rutas con permisos por método y patrón.
- `SaveEndpoints` registra rutas en la tabla `permissions` para mantener control dinámico.
- `User` cifra el password automáticamente con BCrypt en `@PrePersist`/`@PreUpdate`.

## ✅ Recomendaciones

1. Crea `.env` con tus variables.
2. Inicializa la base de datos y ejecuta `script.sql`.
3. Ejecuta la app y prueba login/signup.
4. Usa `POST /api/roles` para crear roles y asignar permisos.
5. Ajusta `SecurityRules` si agregas rutas públicas o auth-only.

##Claude instructions

## Spring Security Implementation Guide

This guide explains how to configure JWT security and authentication endpoints according to the code in this project. It is assumed that the original application has no existing security or auth endpoints.

Fork the backend and frontend repositories to continue with the activity (read the repo's readme).

Keep in mind that you will have to use the `script.sql` file to register the `ADMIN` and `USER` roles along with their permissions. You must execute the first 3 queries.

Resultado de códigoFile generated: Laboratory_04_N-CAPAZ.md

```sql
INSERT INTO roles (name) VALUES('ADMIN'),('USER') ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER' AND p.method = 'GET'
ON CONFLICT DO NOTHING;
Note:The libraries are already installed. The goal of this activity is for you to create the authentication endpoints and configure JWT and Spring Security. Additionally, it must be functional with the front-end, which you will see later. You have a maximum of 2 days to finish it.1. Security DescriptionThe application uses:Spring Security with SecurityFilterChainJWT Authentication in JwtAuthenticationFilterDynamic access rules in SecurityRulesJWT token signed with a secret key in JsonWebTokenJWT error handling in AdviceControllerThe main configuration is located in src/main/java/com/server/app/config/SecurityConfig.java.2. Steps to perform the activityAdd the spring-boot-starter-security dependency in pom.xml.Create SecurityConfig.java with the filters and the STATELESS policy.Create JsonWebToken.java to sign and read JWTs.Create JwtAuthenticationFilter.java to validate the token on each request.Create SecurityRules.java with public and authenticated routes.Create AuthController.java with /login, /signup, /profile, /update/profile, /update/password.Implement UserService with the methods login, signUp, updateProfile, updatePassword, findById.Define the variables security.jwt.secret-key and security.jwt.expiration-time.Test the endpoints using Postman or curl, and add them to the repository.3. Spring Security Configuration3.1. SecurityConfig DefinitionIn SecurityConfig, the following is defined:CORS enabledCSRF disabledSTATELESS mode to avoid using sessionsAll routes permitted at the Spring Security levelJWT validation before the username/password filterCustom authorization after validating JWTThis is the key behavior:authorizeHttpRequests(auth -> auth.anyRequest().permitAll())The actual authorization is executed in custom filters (JwtAuthenticationFilter and DynamicAuthorizationFilter).3.2. Code and LocationYou can copy these files directly into the corresponding project paths.src/main/java/com/server/app/config/SecurityConfig.javaJavapackage com.server.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.server.app.filters.JwtAuthenticationFilter;
import com.server.app.filters.DynamicAuthorizationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DynamicAuthorizationFilter dynamicAuthorizationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, DynamicAuthorizationFilter dynamicAuthorizationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.dynamicAuthorizationFilter = dynamicAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(dynamicAuthorizationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
src/main/java/com/server/app/config/SecurityRules.javaJavapackage com.server.app.config;

import java.util.Map;
import java.util.Set;

public class SecurityRules {

    public static final Map<String, Set<String>> PUBLIC = Map.of(
            "GET", Set.of("/api/public/info"),
            "POST", Set.of("/api/auth/login","/api/auth/signup")
    );

    public static final Map<String, Set<String>> AUTH_ONLY = Map.of(
            "GET", Set.of("/api/auth/profile"),
            "POST", Set.of("/api/auth/logout")
    );

    public static final Set<String> IGNORED = Set.of("/error");

    public static boolean isPublic(String method, String path) {
        return PUBLIC.containsKey(method) && PUBLIC.get(method).contains(path);
    }

    public static boolean isAuthOnly(String method, String path) {
        return AUTH_ONLY.containsKey(method) && AUTH_ONLY.get(method).contains(path);
    }

    public static boolean isIgnored(String path) {
        return IGNORED.contains(path);
    }

    public static boolean requiresAuth(String method, String path) {
        return !isPublic(method, path) && !isIgnored(path);
    }
}
src/main/java/com/server/app/config/JsonWebToken.javaJavapackage com.server.app.config;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.server.app.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JsonWebToken {
    @Value("${security.jwt.expiration-time}")
    private int tokenTime;

    @Value("${security.jwt.secret-key}")
    private String tokenSecret;

    private SecretKey getTokenKey() {
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(User user) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", user.getId());
        return Jwts.builder()
                .claims(json)
                .signWith(getTokenKey())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenTime))
                .compact();
    }

    public Claims extracClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getTokenKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return null;
        }
    }

    public Integer extractIdUser(String token) {
        Claims claims = extracClaims(token);
        if (claims == null) {
            return null;
        }
        return claims.get("id", Integer.class);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extracClaims(token);
        return claims == null || claims.getExpiration().before(new Date());
    }
}
src/main/java/com/server/app/filters/JwtAuthenticationFilter.javaJavapackage com.server.app.filters;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import com.server.app.config.SecurityRules;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.app.config.JsonWebToken;
import com.server.app.dto.response.ExceptionResponse;
import com.server.app.entities.User;
import com.server.app.services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JsonWebToken jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(@Lazy JsonWebToken jwtUtil,
                                   UserService userService) {

        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String method = request.getMethod();
        String path = request.getRequestURI();

        return SecurityRules.isPublic(method, path)
                || SecurityRules.isIgnored(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Bearer token required");
            return;
        }

        final String token = authHeader.substring(7);

        try {

            if (jwtUtil.isTokenExpired(token)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            }

            // 📦 Claims
            Claims claims = jwtUtil.extracClaims(token);

            if (claims == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token data");
                return;
            }

            Integer userId = jwtUtil.extractIdUser(token);

            if (userId == null) {
                sendErrorResponse(response,HttpServletResponse.SC_UNAUTHORIZED,"Token data invalid");
                return;
            }

            User user = userService.findById(userId);

            if (user == null) {
                sendErrorResponse(response,HttpServletResponse.SC_UNAUTHORIZED,"Your account has been deleted");
                return;
            }

            if (user.isBlocked()) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Your account has been blocked");
                return;
            }

            if (!user.getRole().getActive()) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Your account role is not active");
                return;
            }

            Set<GrantedAuthority> authorities =
                    user.getRole()
                            .getPermissions()
                            .stream()
                            .map(permission ->
                                    new SimpleGrantedAuthority(permission.getMethod() + ":" + permission.getPath())
                            )
                            .collect(Collectors.toSet());

            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {

            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired" );

        } catch (JwtException e) {

            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {

        if (response.isCommitted())  return;

        response.setStatus(status);
        response.setContentType("application/json");

        ExceptionResponse error = new ExceptionResponse(status, message);

        String json = new ObjectMapper().writeValueAsString(error);

        response.getWriter().write(json);
    }
}
src/main/java/com/server/app/filters/DynamicAuthorizationFilter.javaJavapackage com.server.app.filters;

import java.io.IOException;

import com.server.app.config.SecurityRules;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.app.dto.response.ExceptionResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if (SecurityRules.isIgnored(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityRules.isPublic(method, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityRules.isAuthOnly(method, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            if (!isAuthorized(authentication, method, path)) {

                sendError(response, HttpServletResponse.SC_FORBIDDEN,"Access denied: you do not have permissions for this route: " + path);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthorized(Authentication authentication, String method, String path) {

        return authentication.getAuthorities().stream().anyMatch(a -> {

            String authority = a.getAuthority();

            String[] parts = authority.split(":", 2);

            if (parts.length != 2) return false;

            String authMethod = parts[0];
            String authPath = parts[1];

            return method.equalsIgnoreCase(authMethod) && pathMatcher.match(authPath, path);
        });
    }

    private void sendError(HttpServletResponse response, int status,String message) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        ExceptionResponse error = new ExceptionResponse(status, message);

        String payload = objectMapper.writeValueAsString(error);

        response.getWriter().write(payload);
    }
}
3.3. Required PropertiesIn src/main/resources/application.properties, the following must be defined:Propertiessecurity.jwt.secret-key=<BASE64_SECRET_KEY>
security.jwt.expiration-time=<TIME_IN_MILLISECONDS>
If you use a .env file, define the same variables there and load them into the app during startup. For example:Fragmento de códigoSECURITY_JWT_SECRET_KEY=Zm9vYmFyYmF6cXV4c2VjcmV0a2V5MTIzNDU=
SECURITY_JWT_EXPIRATION_TIME=3600000
In application.properties, you can also reference these environment variables:Propertiessecurity.jwt.secret-key=${SECURITY_JWT_SECRET_KEY}
security.jwt.expiration-time=${SECURITY_JWT_EXPIRATION_TIME}
The secret-key must be a secure base64 string.4. Access Rules DefinitionSecurityRules defines three sets:PUBLIC: Public routes that do not require a token.AUTH_ONLY: Routes that only require being authenticated.IGNORED: Routes that Spring ignores (/error).Examples from the code:Public:POST /api/auth/loginPOST /api/auth/signupRequires Auth:GET /api/auth/profilePOST /api/auth/logoutIf a route is not in PUBLIC nor in IGNORED, the JWT filter automatically protects it.4.1. Automatic Endpoint RegistrationWhen starting the application, the SaveEndpoints component iterates through all registered controllers and creates new permissions in the database for endpoints that do not yet exist.The code is located in src/main/java/com/server/app/components/SaveEndpoints.java:Javapackage com.server.app.components;

import com.server.app.config.SecurityRules;
import com.server.app.services.PermissionService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SaveEndpoints implements ApplicationListener<ApplicationReadyEvent> {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PermissionService permissionService;

    public SaveEndpoints(RequestMappingHandlerMapping handlerMapping, PermissionService permissionService) {
        this.handlerMapping = handlerMapping;
        this.permissionService = permissionService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        handlerMapping.getHandlerMethods()
                .forEach((info, method) -> {

                    Set<String> paths = getPaths(info);
                    Set<org.springframework.web.bind.annotation.RequestMethod> methods =
                            info.getMethodsCondition().getMethods();

                    if (methods.isEmpty()) {
                        methods = Set.of(org.springframework.web.bind.annotation.RequestMethod.GET);
                    }

                    for (String path : paths) {
                        for (var httpMethod : methods) {
                            processEndpoint(path, httpMethod.name());
                        }
                    }
                });
    }

    private Set<String> getPaths(RequestMappingInfo info) {

        if (info.getPathPatternsCondition() != null) {
            return info.getPathPatternsCondition()
                    .getPatterns()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }

        else if (info.getPatternsCondition() != null) {
            return info.getPatternsCondition()
                    .getPatterns();
        }

        return Set.of();
    }

    private void processEndpoint(String path, String method) {

        if (SecurityRules.isIgnored(path)) {
            return;
        }

        if (SecurityRules.isPublic(method, path)) {
            return;
        }

        if (SecurityRules.isAuthOnly(method, path)) {
            return;
        }

        permissionService.createIfNotExists(path, method);
    }
}
This component uses PermissionService.createIfNotExists(path, method) to save each new endpoint into the permissions table.5. JWT Operation5.1. Token GenerationJsonWebToken.createToken(User user) builds a JWT with:claim id: the user IDissuedAtexpirationHMAC signature with the secret key5.2. Token ValidationJwtAuthenticationFilter performs the following:Ignores public or ignored routes.Requires the header Authorization: Bearer <token>.Verifies if the token is expired.Extracts the claims and the user ID.Searches for the user in the database (UserService.findById).Validates that the account is not blocked.Creates the security context with the user's authorities:METHOD:PATH for each permissionROLE_<roleName>If anything fails, it returns a JSON with a 401 or 500 status.6. Create the requested authentication endpoints with the response format exactly as specifiedAll endpoints should be created under /api/auth.Session initiation (login or signup) is not allowed if the role is inactive.6.1. POST /api/auth/loginRequest body:JSON{
  "username": "usuario123",
  "password": "MiPassw0rd!"
}
Validations:username cannot be emptypassword cannot be emptyResponse 200:JSON{
  "token": "<jwt-token>",
  "data": {
    "id": 1,
    "username": "usuario123",
    "name": "Nombre",
    "surname": "Apellido",
    "email": "correo@dominio.com",
    "role": { ... }
  }
}
Typical Errors:401 Unauthorized if no Bearer token is sent or token is invalid401 Unauthorized if the token expired6.2. POST /api/auth/signupRequest body:JSON{
  "username": "usuario123",
  "name": "Nombre",
  "surname": "Apellido",
  "email": "correo@dominio.com",
  "password": "MiPassw0rd!"
}
Validations:username 3-20 charactersname and surname 2-50 charactersemail must be validpassword minimum 8 characters and must include an uppercase letter, lowercase letter, number, and special characterdefault role is ADMINResponse 200:JSON{
  "token": "<jwt-token>",
  "data": {
    "id": 12,
    "username": "usuario123",
    "name": "Nombre",
    "surname": "Apellido",
    "email": "correo@dominio.com",
    "role": {
      "id": 1,
      "name": "ADMIN",
      "permissions": [
        {
          "id": 5,
          "path": "/api/auth/si/sirve/esto",
          "method": "GET"
        }
      ]
    }
  }
}
6.3. GET /api/auth/profileRequires:Header Authorization: Bearer <token>Response 200:JSON{
  "id": 12,
  "username": "usuario123",
  "name": "Nombre",
  "surname": "Apellido",
  "email": "correo@dominio.com",
  "role": {
    "id": 2,
    "name": "USER",
    "permissions": [
      {
        "id": 5,
        "path": "/api/auth/profile",
        "method": "GET"
      }
    ]
  }
}
6.4. PUT /api/auth/update/profileRequires: Authorization header.Request body:JSON{
  "username": "nuevoUsuario",
  "name": "Nuevo Nombre",
  "surname": "Nuevo Apellido",
  "email": "nuevo@dominio.com"
}
Response 200:JSON{
  "token": "<jwt-token>",
  "data": {
    "id": 12,
    "username": "nuevoUsuario",
    "name": "Nuevo Nombre",
    "surname": "Nuevo Apellido",
    "email": "nuevo@dominio.com",
    "role": {
      "id": 2,
      "name": "USER",
      "permissions": [
        {
          "id": 5,
          "path": "/api/endpont/goku",
          "method": "GET"
        }
      ]
    }
  }
}
6.5. PUT /api/auth/update/passwordRequires: Authorization header.Request body:JSON{
  "oldpassword": "MiPassw0rd!",
  "newpassword": "Nuev0Passw0rd#",
  "confirmpassword": "Nuev0Passw0rd#"
}
Response 200:JSON{
  "id": 12,
  "username": "usuario123",
  "name": "Nombre",
  "surname": "Apellido",
  "email": "correo@dominio.com",
  "role": {
    "id": 2,
    "name": "USER",
    "permissions": [
      {
        "id": 5,
        "path": "/api/endpoint-xd",
        "method": "GET"
      }
    ]
  }
}
The goal is also to ensure that the password change works correctly in the integration with the front-end.7. Token Error ResponsesWhen the JWT filter rejects the request, it responds with a JSON containing an ExceptionResponse:JSON{
  "status": 401,
  "message": "Bearer token required"
}
Or similar messages:Token expiredInvalid token dataToken data invalidYour account has been deletedYour account has been blocked7.1. Error Model FormatThe system unifies errors using a simple model with two fields:status: numerical HTTP codemessage: description of the errorExpected error model:JSON{
  "status": 401,
  "message": "Bearer token required"
}
Or for validations:JSON{
  "status": 400,
  "message": "username: The username cannot be empty"
}
The exception handler must always return this JSON model in the body, with the corresponding HTTP status.Paginated Response (Applicable to all GETs)All listing queries (GET) must follow this response contract:JSON{
  "data": [
    {
      "id": 101,
      "monto": 500.0,
      "fecha": "2026-06-10T14:30:00",
      "descripcion": "Pago de cuota mensual",
      "cuenta": { "id": 1, "alias": "Cuenta Principal" },
      "categoria": { "id": 5, "nombre": "Créditos", "tipo": "Egreso" }
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 10,
    "pageCount": 5,
    "total": 50
  }
}
8. Minimum FilesIf you are starting from scratch, the essential files are:SecurityConfig.javaJsonWebToken.javaJwtAuthenticationFilter.javaSecurityRules.javaAuthController.javaAdviceController.javaapplication.propertiesWith this, the app will have functional JWT security and basic authentication services.9. Tasks (Theme: Advanced Financial Management)For the development of functionality, choose one of the following financial modules. Each requires permission management, a valid JWT, use of transactions (@Transactional), and paginated response formatting (Pagination<T>).You must select one of the three activities and fulfill what is requested.9.1. Bank Accounts and Transactions Management (Multi-currency)Entities:Cuenta (Account): (id, alias, moneda, saldo_base, tipo [Ahorro/Corriente], usuario_id).Categoria (Category): (id, nombre, tipo [Ingreso/Egreso], categoria_padre_id [Auto-referencia]).Movimiento (Transaction): (id, monto, moneda_original, tasa_cambio, fecha, descripcion, cuenta_id, categoria_id).MethodEndpointDescriptionGET/api/finanzas/cuentasLists all the authenticated user's accounts.POST/api/finanzas/cuentasCreates a new account (alias, currency, type).GET/api/finanzas/movimientosPaginated list of transactions with date filters.POST/api/finanzas/transferenciasPerforms a transaction between accounts with funds validation.GET/api/finanzas/categoriasLists categories to organize transactions."""




