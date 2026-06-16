@echo off
REM ==========================================
REM Run auth-service locally
REM Copy this file and set your credentials
REM ==========================================
set JWT_SECRET=CHANGE_ME
set SPRING_DATASOURCE_PASSWORD=CHANGE_ME
set SPRING_DATASOURCE_URL=jdbc:postgresql://CHANGE_ME/neondb?sslmode=require
set SPRING_DATASOURCE_USERNAME=CHANGE_ME
set SUPABASE_URL=CHANGE_ME
set SUPABASE_ANON_KEY=CHANGE_ME
set SUPABASE_SERVICE_ROLE_KEY=CHANGE_ME
set SUPABASE_JWT_ISSUER=%SUPABASE_URL%/auth/v1
set INTERNAL_API_KEY=CHANGE_ME

call .\mvnw.cmd spring-boot:run
pause
