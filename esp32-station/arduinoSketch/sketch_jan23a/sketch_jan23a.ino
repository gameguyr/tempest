#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <DHT.h>
#include <ArduinoJson.h>

// ============== CONFIGURATION ==============
// WiFi credentials
const char* WIFI_SSID = "FailureToConnect";
const char* WIFI_PASSWORD = "willywonka";



// Tempest API endpoint (Railway production)
const char* API_URL = "https://tempest-production-a22b.up.railway.app/api/weather/reading";
// Local API endpoint
// const char* API_URL = "http://192.168.68.115:8080/api/weather/reading";

// Station identifier
const char* STATION_ID = "RussMonsta-House";

// DHT22 sensor configuration
#define DHT_PIN 4        // GPIO pin connected to DHT22 data pin
#define DHT_TYPE DHT22

// Reading interval (milliseconds)
const unsigned long READING_INTERVAL = 60000;  // 1 minute
// ============================================

DHT dht(DHT_PIN, DHT_TYPE);
unsigned long lastReadingTime = 0;

void setup() {
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("\n========================================");
  Serial.println("  Tempest Weather Station - ESP32");
  Serial.println("========================================\n");

  // Initialize DHT sensor
  dht.begin();
  Serial.println("[OK] DHT22 sensor initialized");

  // Connect to WiFi
  connectWiFi();
}

void loop() {
  // Ensure WiFi is connected
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  // Take reading at specified interval
  unsigned long currentTime = millis();
  if (currentTime - lastReadingTime >= READING_INTERVAL || lastReadingTime == 0) {
    lastReadingTime = currentTime;
    takeAndSendReading();
  }

  delay(1000);
}

void connectWiFi() {
  Serial.print("[...] Connecting to WiFi: ");
  Serial.println(WIFI_SSID);
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println();
    Serial.print("[OK] Connected! IP address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println();
    Serial.println("[ERROR] Failed to connect to WiFi");
  }
}

void takeAndSendReading() {
  Serial.println("\n--- Taking Reading ---");
  
  // Read DHT22 sensor
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();  // Celsius
  
  // Check if readings are valid
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("[ERROR] Failed to read from DHT22 sensor!");
    return;
  }
  
  // Print readings to serial
  Serial.print("Temperature: ");
  Serial.print(temperature);
  Serial.println(" °C");
  
  Serial.print("Humidity: ");
  Serial.print(humidity);
  Serial.println(" %");
  
  // Send to API
  sendReading(temperature, humidity);
}

void sendReading(float temperature, float humidity) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[ERROR] WiFi not connected, cannot send reading");
    return;
  }
  
  // Create secure client for HTTPS
  WiFiClientSecure client;
  client.setInsecure();  // Skip certificate verification (for simplicity)
  
  // First test connectivity with ping endpoint
  HTTPClient httpTest;
  String pingUrl = String("https://tempest-production-a22b.up.railway.app/api/weather/ping");
  Serial.print("[...] Testing connection to: ");
  Serial.println(pingUrl);
  
  httpTest.begin(client, pingUrl);
  httpTest.setTimeout(10000);  // 10 second timeout
  int pingResult = httpTest.GET();
  Serial.print("[DEBUG] Ping result: ");
  Serial.println(pingResult);
  if (pingResult > 0) {
    Serial.print("[DEBUG] Ping response: ");
    Serial.println(httpTest.getString());
  }
  httpTest.end();
  
  HTTPClient http;
  http.begin(client, API_URL);
  http.setTimeout(10000);  // 10 second timeout
  http.addHeader("Content-Type", "application/json");
  
  // Build JSON payload
  JsonDocument doc;
  doc["station_id"] = STATION_ID;
  doc["temp"] = temperature;
  doc["humidity"] = humidity;
  // DHT22 doesn't have these sensors, so we omit them or set null
  // The API should handle missing optional fields
  
  String jsonPayload;
  serializeJson(doc, jsonPayload);
  
  Serial.print("[...] Sending to API: ");
  Serial.println(jsonPayload);
  
  // Send POST request
  int httpResponseCode = http.POST(jsonPayload);
  
  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.print("[OK] Response (");
    Serial.print(httpResponseCode);
    Serial.print("): ");
    Serial.println(response);
  } else {
    Serial.print("[ERROR] HTTP Error: ");
    Serial.println(httpResponseCode);
  }
  
  http.end();
}