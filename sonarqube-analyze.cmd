call setup-sonar-env.cmd
call gradlew.bat clean build test jacocoTestReport

docker-compose run --rm --no-deps frontend npm run test:coverage

if %ERRORLEVEL% neq 0 (
    echo.
    echo VAROVÁNÍ: Frontend testy selhaly!
    echo (Pokraèuji bez frontend coverage)
    echo.
    
    REM Vytvoøit prázdnou složku coverage aby SonarQube nespadl
    if not exist "ui-client\coverage" mkdir ui-client\coverage
)

gradlew.bat sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%SONAR_TOKEN%