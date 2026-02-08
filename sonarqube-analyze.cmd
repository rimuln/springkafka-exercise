call setup-sonar-env.cmd
gradlew.bat sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%SONAR_TOKEN%