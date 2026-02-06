# ESP32 Weather Station Configuration

## WiFi Setup

Before uploading the Arduino sketches to your ESP32, you need to configure your WiFi credentials:

### For RussMonsta Station (sketch_jan23a)
1. Open `arduinoSketch/sketch_jan23a/sketch_jan23a.ino`
2. Find lines 11-12 and replace with your WiFi credentials:
   ```cpp
   const char* WIFI_SSID = "YOUR_WIFI_SSID";
   const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
   ```

### For Fractal Station (fractal)
1. Open `arduinoSketch/fractal/fractal.ino`
2. Find lines 11-12 and replace with your WiFi credentials:
   ```cpp
   const char* WIFI_SSID = "YOUR_WIFI_SSID";
   const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
   ```

## Security Note

**IMPORTANT:** Never commit files containing your actual WiFi credentials to git.

The `.ino` files are tracked by git but contain placeholder values. When you add your real credentials:
1. Make sure not to commit the changes
2. Or add them to `.git/info/exclude` locally

## Alternative: Using a Separate Config File

For better security, consider creating a separate `config.h` file (not tracked by git) that contains your credentials:

```cpp
// config.h (DO NOT COMMIT THIS FILE)
#ifndef CONFIG_H
#define CONFIG_H

const char* WIFI_SSID = "YourActualSSID";
const char* WIFI_PASSWORD = "YourActualPassword";

#endif
```

Then include it in your `.ino` file:
```cpp
#include "config.h"
```

And add `config.h` to `.gitignore`.
