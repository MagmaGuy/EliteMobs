@echo off
@rem ============================================================================
@rem EliteMobs Build Script
@rem Runs: gradle clean shadowJar
@rem Output: testbed/plugins/EliteMobs.jar
@rem ============================================================================

echo Building EliteMobs...
echo.

call "%~dp0gradlew.bat" clean shadowJar %*

if %ERRORLEVEL% equ 0 (
    echo.
    echo ============================================================================
    echo BUILD SUCCESSFUL
    echo Output: testbed/plugins/EliteMobs.jar
    echo ============================================================================
) else (
    echo.
    echo ============================================================================
    echo BUILD FAILED
    echo ============================================================================
)
