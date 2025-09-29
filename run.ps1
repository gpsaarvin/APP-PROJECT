param(
    [switch]$Clean,
    [ValidateSet('trace','debug','info','warn','error','off')]
    [string]$LogLevel = 'info'
)

if ($Clean) {
    mvn clean | Write-Host
}

# Build and run using Maven JavaFX plugin, which handles JavaFX modules and classpath.
# Pass SLF4J log level property BEFORE the goal to avoid LifecyclePhaseNotFoundException in PowerShell parsing.
$mvnArgs = @('-q', '-DskipTests', "-Dorg.slf4j.simpleLogger.defaultLogLevel=$LogLevel", 'javafx:run')
mvn @mvnArgs